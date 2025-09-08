package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.storage.user.UserValidator.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage{
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        log.debug("Starting create user: {}", user.getEmail());

        validateUser(user);
        log.trace("User is valid");

        checkEmail(user);

        user.setId(getNextId());
        log.debug("User: {}. Set id = {}", user.getEmail(), user.getId());

        users.put(user.getId(), user);
        log.info("POST method: create user (id = {}) worked successfully", user.getId());

        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.warn("User id is null");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        log.debug("Starting update user, id = {}", newUser.getId());

        if (users.containsKey(newUser.getId())) {
            log.trace("Found user with id = {}", newUser.getId());

            User oldUser = users.get(newUser.getId());
            updateUserFields(oldUser, newUser);
            log.info("PUT method: update user (id = {}) worked successfully", oldUser.getId());

            return oldUser;
        }
        log.warn("Not found user with id = {}", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    private void updateUserFields(User oldUser, User newUser) {
        log.debug("Starting update User fields, id = {}", newUser.getId());
        if (newUser.getEmail() != null && !oldUser.getEmail().equals(newUser.getEmail())) {
            validateEmail(newUser);
            checkEmail(newUser);
            oldUser.setEmail(newUser.getEmail());
            log.trace("Updated email: {}", oldUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            validateLogin(newUser);
            oldUser.setLogin(newUser.getLogin());
            log.trace("Updated login: {}", oldUser.getLogin());
        }
        if (newUser.getName() != null) {
            validateName(newUser);
            oldUser.setName(newUser.getName());
            log.trace("Updated name: {}", oldUser.getName());
        }
        if (newUser.getBirthday() != null) {
            validateBirthday(newUser);
            oldUser.setBirthday(newUser.getBirthday());
            log.trace("Updated birthday: {}", oldUser.getBirthday());
        }
        log.debug("User updated. {}", oldUser.toString());
    }

    private void checkEmail(User user) {
        if (users.values()
                .stream()
                .map(User::getEmail)
                .toList()
                .contains(user.getEmail())) {
            log.warn("Email is duplicated: {}", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }
        log.trace("Email is not duplicated");
    }

    private long getNextId() {
        long currentId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentId;
    }
}
