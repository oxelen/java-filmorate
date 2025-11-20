package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final MPAsRepository mpasRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;  // ← Добавлено поле

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

    // Конструктор
    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         FilmRowMapper filmRowMapper,
                         MPAsRepository mpasRepository,
                         NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbcTemplate, filmRowMapper);
        this.mpasRepository = mpasRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.filmRowMapper = filmRowMapper;  // ← Сохраняем в поле
    }

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
        return findOne(FIND_BY_ID_QUERY, id).orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    @Override
    public boolean containsFilm(Long id) {
        return findOne(FIND_BY_ID_QUERY, id).isPresent();
    }

    @Override
    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {

        String sqlGetMostPopularFilms = """
                SELECT
                f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.MPA_id,
                COUNT(l.user_id) AS likes_count
                FROM films f
                JOIN likes l ON f.id = l.film_id
                JOIN film_genres fg ON f.id = fg.film_id
                JOIN genres g ON fg.genre_id = g.id
                JOIN MPAs m ON f.MPA_id = m.id
                """;

        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();


        if (genreId != null) {
            conditions.add("g.id = :genreId");
            params.addValue("genreId", genreId);
        }
        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = :year  ");
            params.addValue("year", year);
        }

        if (!conditions.isEmpty()) {
            sqlGetMostPopularFilms += " WHERE " + String.join(" AND ", conditions);
        }

        sqlGetMostPopularFilms += " GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.MPA_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT :count";
        params.addValue("count", count);

        System.out.println("Final SQL: " + sqlGetMostPopularFilms);
        System.out.println("Params: " + params.getValues());

        return namedParameterJdbcTemplate.query(sqlGetMostPopularFilms, params, filmRowMapper);  // Теперь работает!
    }

    private void updGenres(Film film) {
        film.getGenres().stream()
                .map(Genre::getId)
                .forEach(genre_id -> insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre_id));
    }
}
