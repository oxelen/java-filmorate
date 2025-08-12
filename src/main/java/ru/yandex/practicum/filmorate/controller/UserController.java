package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Running POST method: create user");
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

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Running PUT method: update user");
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

    @GetMapping
    public Collection<User> findAll() {
        log.info("Running GET method: get all users");
        return users.values();
    }

    public static void validateUser(User user) {
        validateEmail(user);
        validateLogin(user);
        validateName(user);
        validateBirthday(user);
    }

    public static void validateEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Empty email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Email \"{}\" does not contains symbol \"@\"", user.getEmail());
            throw new ValidationException("Email должен содержать символ \"@\"");
        }
        log.trace("Email is valid");
    }

    public static void validateLogin(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Empty login");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Login contains spaces");
            throw new ValidationException("Логин не может содержать пробелы");
        }
        log.trace("Login is valid");
    }

    public static void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.trace("Empty name. Name equals login");
            user.setName(user.getLogin());
        }
        log.debug("User set name: {}", user.getName());
    }

    public static void validateBirthday(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Birthday is after LocalDate.now");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.trace("Birthday is valid");
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
