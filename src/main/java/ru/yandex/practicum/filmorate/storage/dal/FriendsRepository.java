package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FriendsRepository extends BaseDbStorage<Long> {
    private final String FIND_ALL_FRIENDS_QUERY = "SELECT second_id FROM friends WHERE first_id = ?";
    private final String INSERT_QUERY = "INSERT INTO friends (first_id, second_id) " +
            "VALUES (?, ?)";
    private final String DELETE_QUERY = "DELETE FROM friends WHERE first_id = ? AND second_id = ?";

    public FriendsRepository(JdbcTemplate jdbc,
                             @Qualifier("friendsRowMapper") RowMapper<Long> mapper) {
        super(jdbc, mapper);
    }

    public List<Long> findAllFriends(Long userId) {
        return findMany(FIND_ALL_FRIENDS_QUERY, userId);
    }

    public void create(Long firstId, Long secId) {
        insert(INSERT_QUERY, firstId, secId);
    }

    public boolean delete(Long firstId, Long secId) {
        return delete(DELETE_QUERY, firstId, secId);
    }
}
