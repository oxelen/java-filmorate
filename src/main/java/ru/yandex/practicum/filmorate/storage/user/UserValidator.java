package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {
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
}
