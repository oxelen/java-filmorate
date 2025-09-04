package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Period;

public class UserControllerTests {
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
    }

    @Test
    public void nullEmailValidationTest() {
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateEmail(user),
                "Null email должен генерировать исключение ValidationException");
    }

    @Test
    public void emptyEmailValidationTest() {
        user.setEmail("");
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateEmail(user),
                "Пустой email должен генерировать исключение ValidationException");
    }

    @Test
    public void emailWithoutAtValidationTest() {
        user.setEmail("example");
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateEmail(user),
                "Email без знака \"@\" должен генерировать исключение ValidationException");
    }

    @Test
    public void correctEmailValidationTest() {
        user.setEmail("example@test");
        Assertions.assertDoesNotThrow(() -> UserController.validateEmail(user),
                "Валидный email не должен генерировать исключение");
    }

    @Test
    public void nullLoginValidationTest() {
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateLogin(user),
                "Null логин должен генерировать исключение ValidationException");
    }

    @Test
    public void emptyLoginValidationTest() {
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateLogin(user),
                "Пустой логин должен генерировать исключение ValidationException");
    }

    @Test
    public void loginWithSpacesValidationTest() {
        user.setLogin("test test");
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateLogin(user),
                "Логин с пробелами должен генерировать исключение ValidationException");
    }

    @Test
    public void correctLoginValidationTest() {
        user.setLogin("test_login");
        Assertions.assertDoesNotThrow(() -> UserController.validateLogin(user),
                "Валидный логин не должен генерировать исключение");
    }

    @Test
    public void nullNameValidationTest() {
        user.setLogin("login");
        UserController.validateName(user);
        Assertions.assertEquals(user.getName(), user.getLogin(),
                "При null имени ему должно присваиваться значение логина");
    }

    @Test
    public void nowBirthdayValidationTest() {
        user.setBirthday(LocalDate.now());
        Assertions.assertDoesNotThrow(() -> UserController.validateBirthday(user),
                "Валидный день рождения не должен генерировать исключение");
    }

    @Test
    public void futureBirthdayValidationTest() {
        user.setBirthday(LocalDate.now().plus(Period.ofDays(1)));
        Assertions.assertThrows(ValidationException.class,
                () -> UserController.validateBirthday(user),
                "День рождения в будущем должен генерировать исключение ValidationException");
    }

    @Test
    public void correctUserValidationTest() {
        user.setEmail("test@test");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Assertions.assertDoesNotThrow(() -> UserController.validateUser(user),
                "Валидный пользователь не должен генерировать исключение");
    }
}
