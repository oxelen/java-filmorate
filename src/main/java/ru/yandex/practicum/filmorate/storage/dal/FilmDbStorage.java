package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Optional;

@Repository("filmDbStorage")
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
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM films WHERE id = ?";

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
    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
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

    public boolean deleteById(Long filmId) {
        return delete(DELETE_BY_ID_QUERY, filmId);
    }
}
