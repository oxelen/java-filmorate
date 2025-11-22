package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LikesRepository extends BaseDbStorage<Long> {
    private static final String ALL_LIKES_BY_FILM_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String ALL_LIKED_BY_USER_QUERY = "SELECT film_id FROM likes WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    public LikesRepository(JdbcTemplate jdbc,
                           @Qualifier("likesRowMapper") RowMapper<Long> mapper) {
        super(jdbc, mapper);
    }

    public List<Long> findAllLikesByFilmId(Long filmId) {
        return findMany(ALL_LIKES_BY_FILM_QUERY, filmId);
    }

    public List<Long> findAllLikedByUserId(Long userId) {
        return jdbc.queryForList(ALL_LIKED_BY_USER_QUERY, Long.class, userId);
    }

    public void create(Long filmId, Long userId) {
        insert(INSERT_QUERY, filmId, userId);
    }

    public void delete(Long filmId, Long userId) {
        delete(DELETE_QUERY, filmId, userId);
    }
}
