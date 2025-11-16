package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director> implements DirectorStorage {

    private static final String INSERT_DIRECTOR = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";
    private static final String SELECT_BY_ID = "SELECT id, name FROM directors WHERE id = ?";
    private static final String SELECT_ALL = "SELECT id, name FROM directors ORDER BY id";

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director create(Director director) {
        long id = insert(INSERT_DIRECTOR, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        update(UPDATE_DIRECTOR, director.getName(), director.getId());
        return director;
    }

    @Override
    public boolean delete(long id) {
        return delete(DELETE_DIRECTOR, id);
    }

    @Override
    public Optional<Director> findById(long id) {
        return findOne(SELECT_BY_ID, id);
    }

    @Override
    public List<Director> findAll() {
        return findMany(SELECT_ALL);
    }
}
