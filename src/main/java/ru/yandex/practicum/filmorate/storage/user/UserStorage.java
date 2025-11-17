package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User newUser);

    Collection<User> findAll();

    Optional<User> findById(Long id);

    boolean containsUser(Long id);

    boolean deleteById(Long id);
}
