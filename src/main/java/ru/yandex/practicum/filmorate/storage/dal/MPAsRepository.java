package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

@Repository
public class MPAsRepository extends BaseDbStorage<MPA> {
    private final String FIND_MPA_QUERY = "SELECT * FROM MPAs WHERE id = ?";
    private final String FIND_ID_QUERY = "SELECT id FROM MPAs WHERE name = ?";
    private final String FIND_ALL_QUERY = "SELECT * FROM MPAs";

    public MPAsRepository(JdbcTemplate jdbc, RowMapper<MPA> mapper) {
        super(jdbc, mapper);
    }

    public Optional<MPA> findMPA(Long id) {
        return findOne(FIND_MPA_QUERY, id);
    }

    public Long findId(MPA mpa) {
        String stringMpa = mpa.getName();
        return jdbc.queryForObject(FIND_ID_QUERY, Long.class, stringMpa);
    }

    public List<MPA> findAll() {
        return findMany(FIND_ALL_QUERY);
    }
}
