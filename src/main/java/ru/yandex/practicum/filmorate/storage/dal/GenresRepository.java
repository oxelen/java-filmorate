package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class GenresRepository extends BaseDbStorage<Genre> {
    private static final String FIND_FILM_GENRES_QUERY = "SELECT * " +
                                                         "FROM genres " +
                                                         "WHERE id IN (SELECT genre_id " +
                                                         "FROM film_genres " +
                                                         "WHERE film_id = ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    public GenresRepository(JdbcTemplate jdbc,
                            @Qualifier("genresRowMapper") RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findFilmGenres(Long filmId) {
        return findMany(FIND_FILM_GENRES_QUERY, filmId.intValue());
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Genre findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id).orElseThrow(() ->
                new NotFoundException("Не удалось найти жанр по айди"));
    }

    public boolean deleteFilmGenresByFilmId(Long filmId) {
        return delete(DELETE_FILM_GENRES_QUERY, filmId);
    }
}
