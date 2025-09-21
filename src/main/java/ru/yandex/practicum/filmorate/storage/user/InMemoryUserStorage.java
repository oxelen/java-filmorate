package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.storage.user.UserValidator.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
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

        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.warn("User id is null");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        log.debug("Starting update user, id = {}", newUser.getId());

        if (containsUser(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            updateUserFields(oldUser, newUser);

            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Starting findAll");
        return users.values();
    }

    @Override
    public User findById(Long id) {
        log.debug("Starting find user, id = {}", id);
        if (containsUser(id)) {
            return users.get(id);
        }
        throw new NotFoundException("Пользователь с id = " + id + " не найден");
    }

    @Override
    public boolean containsUser(Long id) {
        log.debug("Starting contains user, id = {}", id);
        if (users.containsKey(id)) {
            log.debug("Found user with id = {}", id);
            return true;
        }
        log.warn("Not found user with id = {}", id);
        return false;
    }

    private void updateUserFields(User oldUser, User newUser) {
        log.trace("Starting update User fields, id = {}", newUser.getId());
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
        log.trace("Starting checkEmail, email = {}", user.getEmail());
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
        log.trace("Starting getNextId");
        long currentId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentId;
    }
}
