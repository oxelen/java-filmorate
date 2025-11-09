package ru.yandex.practicum.filmorate.storage.dal.mapper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Qualifier("likesRowMapper")
public class LikesRowMapper implements RowMapper<Long> {
    @Override
    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (long) rs.getInt("user_id");
    }
}
