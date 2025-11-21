package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final MPAsRepository mpasRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    // Конструктор
    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         FilmRowMapper filmRowMapper,
                         MPAsRepository mpasRepository,
                         NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbcTemplate, filmRowMapper);
        this.mpasRepository = mpasRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, MPA_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films " +
            "SET name = ?, " +
            "description = ?, " +
            "release_date = ?, " +
            "duration = ?, " +
            "MPA_id = ? " +
            "WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) " +
            "VALUES (?, ?)";

    @Override
    public Film create(Film film) {
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId().intValue()
        );

        film.setId(id);
        updGenres(film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        update(UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId());
        updGenres(newFilm);
        return newFilm;
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    @Override
    public boolean containsFilm(Long id) {
        return findOne(FIND_BY_ID_QUERY, id).isPresent();
    }

    /**
     * Находит ID наиболее похожего пользователя по совпадению лайков на фильмы.
     * @param userId ID целевого пользователя
     * @return ID похожего пользователя или null, если не найден
     */
    @Transactional(readOnly = true)
    public Long getMostSimilarUser(Long userId) {
        log.debug("Finding most similar user for userId={}", userId);

        String sql = """
        SELECT fl2.user_id
        FROM likes fl1
        JOIN likes fl2 ON fl1.film_id = fl2.film_id
        WHERE fl1.user_id = :userId
          AND fl2.user_id != :userId
        GROUP BY fl2.user_id
        ORDER BY COUNT(*) DESC
        LIMIT 1
        """;

        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);

        try {
            Long similarUserId = namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
            if (similarUserId == null) {
                log.warn("No similar user found for userId={}", userId);
                return null; // Не бросаем исключение, возвращаем null
            }
            log.info("Most similar user found for userId={}: similarUserId={}", userId, similarUserId);
            return similarUserId;
        } catch (EmptyResultDataAccessException e) {
            log.warn("No similar user found for userId={} (EmptyResultDataAccessException)", userId);
            return null;
        } catch (Exception e) {
            log.error("Database error while finding similar user for userId={}: {}", userId, e.getMessage(), e);
            throw new DatabaseException("Ошибка при поиске похожего пользователя для userId=" + userId, e);
        }
    }

    //Метод формирует рекомендации для пользователя на основе лайков наиболее похожего пользователя.
    //Исключает фильмы, которые уже лайкнул целевой пользователь.
    @Transactional(readOnly = true)
    public List<Film> getRecommendationsForUserBasedOnLikesOfSimilarUser(Long userId, Long similarUserId) {
        log.debug("Generating recommendations for userId={} based on similarUserId={}", userId, similarUserId);

        int limitForQuery = 10;

        String sql = """
                WITH similar_user_liked AS (
                    SELECT film_id
                    FROM likes
                    WHERE user_id = :similarUserId
                ),
                user_already_liked AS (
                    SELECT film_id
                    FROM likes
                    WHERE user_id = :userId
                )
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id AS mpa_id,
                    m.name AS mpa_name
                FROM films f
                JOIN MPAs m ON f.MPA_id = m.id
                WHERE f.id IN (SELECT film_id FROM similar_user_liked)
                  AND f.id NOT IN (SELECT film_id FROM user_already_liked)
                ORDER BY f.release_date DESC
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("similarUserId", similarUserId)
                .addValue("limit", limitForQuery);

        try {
            List<Film> recommendations = namedParameterJdbcTemplate.query(sql, params, filmRowMapper);

            log.info("Generated {} recommendations for userId={} based on similarUserId={}",
                    recommendations.size(), userId, similarUserId);
            return recommendations;
        } catch (Exception e) {
            log.error("Database error generating recommendations for userId={}, similarUserId={}: {}",
                    userId, similarUserId, e.getMessage(), e);
            throw new DatabaseException("Ошибка при формировании рекомендаций для userId=" + userId, e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<Film> getRecommendationFilms(Long userId) {
        log.debug("Starting recommendation process for userId={}", userId);

        Long similarUserId = getMostSimilarUser(userId); // Может выбросить NotFoundException или DatabaseException
        return getRecommendationsForUserBasedOnLikesOfSimilarUser(userId, similarUserId); // Может выбросить DatabaseException
    }


    private void updGenres(Film film) {
        if (film.getGenres() == null) return;

        film.getGenres().stream()
                .filter(genre -> genre.getId() != null)
                .distinct()
                .forEach(genre -> insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId()));
    }
}
