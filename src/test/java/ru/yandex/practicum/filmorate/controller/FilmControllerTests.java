package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Period;

public class FilmControllerTests {
    private Film film;

    @BeforeEach
    public void setUp() {
        film = new Film();
    }

    @Test
    public void nullNameValidationTest() {
        Assertions.assertThrows(ValidationException.class,
                () -> FilmController.validateName(film),
                "Null имя должно генерировать исключение ValidationException");
    }

    @Test
    public void emptyNameValidationTest() {
        film.setName("");

        Assertions.assertThrows(ValidationException.class,
                () -> FilmController.validateName(film),
                "Пустое имя должно генерировать исключение ValidationException");
    }

    @Test
    public void correctNameValidationTest() {
        film.setName("name");

        Assertions.assertDoesNotThrow(() -> FilmController.validateName(film),
                "Валидное имя не должно генерировать исключение");
    }

    @Test
    public void nullDescriptionValidationTest() {
        Assertions.assertDoesNotThrow(() -> FilmController.validateDescription(film),
                "Null описание не должно генерировать исключение");
    }

    @Test
    public void emptyDescriptionValidationTest() {
        film.setDescription("");

        Assertions.assertDoesNotThrow(() -> FilmController.validateDescription(film),
                "Пустое описание не должно генерировать исключение");
    }

    @Test
    public void descriptionSizeMaxSizeValidationTest() {
        film.setDescription(getString(FilmController.DESCRIPTION_MAX_SIZE));

        Assertions.assertDoesNotThrow(() -> FilmController.validateDescription(film),
                "Описание длиной "
                        + FilmController.DESCRIPTION_MAX_SIZE
                        + " символов не должно генерировать исключение");
    }

    @Test
    public void descriptionSizeMoreThanMaxSizeValidationTest() {
        film.setDescription(getString(FilmController.DESCRIPTION_MAX_SIZE + 1));

        Assertions.assertThrows(ValidationException.class,
                () -> FilmController.validateDescription(film),
                "Описание длиной больше "
                        + FilmController.DESCRIPTION_MAX_SIZE
                        + " символов должно генерировать исключение ValidationException");
    }

    @Test
    public void releaseDateIsBeforeTargetDateValidationTest() {
        film.setReleaseDate(FilmController.FILM_BIRTHDAY.minus(Period.ofDays(1)));
        Assertions.assertThrows(ValidationException.class,
                () -> FilmController.validateReleaseDate(film),
                "При дате релиза раньше "
                        + FilmController.FILM_BIRTHDAY
                        + " должно генерироваться исключение ValidationException");
    }

    @Test
    public void releaseDateEqualsTargetDateValidationException() {
        film.setReleaseDate(FilmController.FILM_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> FilmController.validateReleaseDate(film),
                "Дата релиза равная "
                        + FilmController.FILM_BIRTHDAY
                        + " не должна генерировать исключение");
    }

    @Test
    public void releaseDateIsAfterTargetDateValidationTest() {
        film.setReleaseDate(FilmController.FILM_BIRTHDAY.plus(Period.ofDays(1)));
        Assertions.assertDoesNotThrow(() -> FilmController.validateReleaseDate(film),
                "Дата релиза после "
                        + FilmController.FILM_BIRTHDAY
                        + " не должна генерировать исключение");
    }

    @Test
    public void negativeDurationValidationTest() {
        film.setDuration(-1L);
        Assertions.assertThrows(ValidationException.class,
                () -> FilmController.validateDuration(film),
                "Отрицательная продолжительность должна генерировать исключение ValidationException");
    }

    @Test
    public void zeroDurationValidationTest() {
        film.setDuration(0L);
        Assertions.assertThrows(ValidationException.class,
                () -> FilmController.validateDuration(film),
                "Нулевая продолжительность должна генерировать исключение ValidationException");
    }

    @Test
    public void positiveDurationValidationTest() {
        film.setDuration(1L);
        Assertions.assertDoesNotThrow(() -> FilmController.validateDuration(film),
                "Положительная продолжительность не должна генерировать исключения");
    }

    @Test
    public void validateFilmTest() {
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100L);

        Assertions.assertDoesNotThrow(() -> FilmController.validateFilm(film),
                "Фильм с валидными полями не должен генерировать исключение");
    }

    private String getString(int size) {
        return "1".repeat(size);
    }
}
