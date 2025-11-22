package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dal.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.director.FilmDirectorStorage;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDirectorDbStorage implements FilmDirectorStorage {
    private static final String INSERT = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_ALL = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String SELECT_BY_FILM =
            "SELECT d.id, d.name " +
            "FROM directors d " +
            "JOIN film_directors fd ON d.id = fd.director_id " +
            "WHERE fd.film_id = ?";
    private static final String SELECT_FILMS_BY_DIRECTOR =
            "SELECT film_id FROM film_directors WHERE director_id = ?";

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper mapper;

    @Override
    public void addDirectorsToFilm(long filmId, List<Director> directors) {
        log.info(directors.toString());
        if (directors == null) return;
        log.info(directors.toString());
        for (Director director : directors) {
            jdbc.update(INSERT, filmId, director.getId());
        }
    }

    @Override
    public void deleteDirectorsFromFilm(long filmId) {
        jdbc.update(DELETE_ALL, filmId);
    }

    @Override
    public List<Director> getDirectorsByFilmId(long filmId) {
        return jdbc.query(SELECT_BY_FILM, mapper, filmId);
    }

    @Override
    public List<Long> getFilmsByDirector(long directorId) {
        return jdbc.queryForList(SELECT_FILMS_BY_DIRECTOR, Long.class, directorId);
    }

    @Override
    public void replaceDirectorsForFilm(long filmId, List<Director> directors) {
        deleteDirectorsFromFilm(filmId);
        addDirectorsToFilm(filmId, directors);
    }
}
