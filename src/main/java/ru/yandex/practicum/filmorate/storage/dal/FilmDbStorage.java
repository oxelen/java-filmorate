package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Collections;
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

    @Override
    @Transactional(readOnly = true)
    public List<Film> getRecommendationFilms(Long userId) {
        // 1. Валидация входных параметров
        if (userId == null || userId <= 0) {
            log.warn("Invalid userId provided: {}. Returning empty list.", userId);
            return Collections.emptyList();
        }

        int limitForQuery = 10;


        log.info("Starting recommendation process for user ID={} with limit={}", userId, limitForQuery);

        Long similarUserId;

        try {
            // 2. Поиск наиболее похожего пользователя
            String findSimilarUserSql = """
                    SELECT fl2.user_id
                    FROM likes fl1
                    JOIN likes fl2 ON fl1.film_id = fl2.film_id
                    WHERE fl1.user_id = :userId
                      AND fl2.user_id != :userId
                    GROUP BY fl2.user_id
                    ORDER BY COUNT(*) DESC
                    LIMIT 1
                    """;

            MapSqlParameterSource similarUserParams = new MapSqlParameterSource("userId", userId);

            log.debug("Executing query to find similar user for userId={}", userId);
            similarUserId = namedParameterJdbcTemplate.queryForObject(
                    findSimilarUserSql,
                    similarUserParams,
                    Long.class
            );

            if (similarUserId == null) {
                log.info("No similar user found for userId={}. Returning empty list.", userId);
                return Collections.emptyList();
            }

            log.info("Found similar user ID={} for userId={}", similarUserId, userId);

        } catch (EmptyResultDataAccessException e) {
            log.info("No similar user found in database for userId={}. Returning empty list.", userId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while finding similar user for userId={}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }

        // 3. Получение рекомендаций
        String recommendationSql = """
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
                ORDER BY f.release_date DESC;
              
                """;

        MapSqlParameterSource recommendationParams = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("similarUserId", similarUserId)
                .addValue("limit", limitForQuery);


        log.debug("Executing recommendation query for userId={} using similarUserId={} with limit={}",
                userId, similarUserId, limitForQuery);

        try {
            List<Film> recommendations = namedParameterJdbcTemplate.query(
                    recommendationSql,
                    recommendationParams,
                    filmRowMapper
            );

            int resultSize = recommendations.size();
            log.info("Successfully retrieved {} recommendation(s) for userId={} from similarUserId={}",
                    resultSize, userId, similarUserId);

            return recommendations;

        } catch (Exception e) {
            log.error("Error executing recommendation query for userId={}, similarUserId={}: {}",
                    userId, similarUserId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void updGenres(Film film) {
        if (film.getGenres() == null) return;

        film.getGenres().stream()
                .filter(genre -> genre.getId() != null)
                .distinct()
                .forEach(genre -> insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId()));
    }
}
