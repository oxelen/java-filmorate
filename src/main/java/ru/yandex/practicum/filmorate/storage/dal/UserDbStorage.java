package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private final String FIND_ALL_QUERY = "SELECT * FROM users";
    private final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        Long id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        user.setId(id);

        return user;
    }

    @Override
    public User update(User newUser) {
        update(UPDATE_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId());

        return newUser;
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public boolean containsUser(Long id) {
        return findOne(FIND_BY_ID_QUERY, id).isPresent();
    }
}
