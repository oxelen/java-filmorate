package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final MPAsRepository mpasRepository;

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
    private static final String FIND_FILMS_BY_DIRECTOR_BY_YEAR = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.id = fd.film_id
            WHERE fd.director_id = ?
            ORDER BY f.release_date
            """;
    private static final String FIND_FILMS_BY_DIRECTOR_BY_LIKES = """
            SELECT f.*, COUNT(l.user_id) AS likes_count
            FROM films f
            JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN likes l ON f.id = l.film_id
            WHERE fd.director_id = ?
            GROUP BY f.id
            ORDER BY likes_count DESC;
            """;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, MPAsRepository mpasRepository) {
        super(jdbc, mapper);
        this.mpasRepository = mpasRepository;
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

    private void updGenres(Film film) {
        film.getGenres().stream()
                .map(Genre::getId)
                .forEach(genre_id -> insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre_id));
    }

    public List<Film> getFilmsByDirectorSortedByYear(Long directorId) {
        return jdbc.query(FIND_FILMS_BY_DIRECTOR_BY_YEAR, mapper, directorId);
    }

    public List<Film> getFilmsByDirectorSortedByLikes(Long directorId) {
        return jdbc.query(FIND_FILMS_BY_DIRECTOR_BY_LIKES, mapper, directorId);
    }
}
