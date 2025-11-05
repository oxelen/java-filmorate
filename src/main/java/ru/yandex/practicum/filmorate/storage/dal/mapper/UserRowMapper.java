package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FriendsRepository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class UserRowMapper implements RowMapper<User> {
    private final FriendsRepository friendsRepository;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder()
                .id((long) rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();

        user.getFriends().addAll(friendsRepository.findAllFriends(user.getId()));

        return user;
    }
}
