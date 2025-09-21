package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Period;

import static ru.yandex.practicum.filmorate.storage.film.FilmValidator.*;
import static ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage.DESCRIPTION_MAX_SIZE;
import static ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage.FILM_BIRTHDAY;

public class FilmControllerTests {
    private Film film;

    @BeforeEach
    public void setUp() {
        film = new Film();
    }

    @Test
    public void nullNameValidationTest() {
        Assertions.assertThrows(ValidationException.class,
                () -> validateName(film),
                "Null имя должно генерировать исключение ValidationException");
    }

    @Test
    public void emptyNameValidationTest() {
        film.setName("");

        Assertions.assertThrows(ValidationException.class,
                () -> validateName(film),
                "Пустое имя должно генерировать исключение ValidationException");
    }

    @Test
    public void correctNameValidationTest() {
        film.setName("name");

        Assertions.assertDoesNotThrow(() -> validateName(film),
                "Валидное имя не должно генерировать исключение");
    }

    @Test
    public void nullDescriptionValidationTest() {
        Assertions.assertDoesNotThrow(() -> validateDescription(film),
                "Null описание не должно генерировать исключение");
    }

    @Test
    public void emptyDescriptionValidationTest() {
        film.setDescription("");

        Assertions.assertDoesNotThrow(() -> validateDescription(film),
                "Пустое описание не должно генерировать исключение");
    }

    @Test
    public void descriptionSizeMaxSizeValidationTest() {
        film.setDescription(getString(DESCRIPTION_MAX_SIZE));

        Assertions.assertDoesNotThrow(() -> validateDescription(film),
                "Описание длиной "
                        + DESCRIPTION_MAX_SIZE
                        + " символов не должно генерировать исключение");
    }

    @Test
    public void descriptionSizeMoreThanMaxSizeValidationTest() {
        film.setDescription(getString(DESCRIPTION_MAX_SIZE + 1));

        Assertions.assertThrows(ValidationException.class,
                () -> validateDescription(film),
                "Описание длиной больше "
                        + DESCRIPTION_MAX_SIZE
                        + " символов должно генерировать исключение ValidationException");
    }

    @Test
    public void releaseDateIsBeforeTargetDateValidationTest() {
        film.setReleaseDate(FILM_BIRTHDAY.minus(Period.ofDays(1)));
        Assertions.assertThrows(ValidationException.class,
                () -> validateReleaseDate(film),
                "При дате релиза раньше "
                        + FILM_BIRTHDAY
                        + " должно генерироваться исключение ValidationException");
    }

    @Test
    public void releaseDateEqualsTargetDateValidationException() {
        film.setReleaseDate(FILM_BIRTHDAY);
        Assertions.assertDoesNotThrow(() -> validateReleaseDate(film),
                "Дата релиза равная "
                        + FILM_BIRTHDAY
                        + " не должна генерировать исключение");
    }

    @Test
    public void releaseDateIsAfterTargetDateValidationTest() {
        film.setReleaseDate(FILM_BIRTHDAY.plus(Period.ofDays(1)));
        Assertions.assertDoesNotThrow(() -> validateReleaseDate(film),
                "Дата релиза после "
                        + FILM_BIRTHDAY
                        + " не должна генерировать исключение");
    }

    @Test
    public void negativeDurationValidationTest() {
        film.setDuration(-1L);
        Assertions.assertThrows(ValidationException.class,
                () -> validateDuration(film),
                "Отрицательная продолжительность должна генерировать исключение ValidationException");
    }

    @Test
    public void zeroDurationValidationTest() {
        film.setDuration(0L);
        Assertions.assertThrows(ValidationException.class,
                () -> validateDuration(film),
                "Нулевая продолжительность должна генерировать исключение ValidationException");
    }

    @Test
    public void positiveDurationValidationTest() {
        film.setDuration(1L);
        Assertions.assertDoesNotThrow(() -> validateDuration(film),
                "Положительная продолжительность не должна генерировать исключения");
    }

    @Test
    public void validateFilmTest() {
        film.setName("name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100L);

        Assertions.assertDoesNotThrow(() -> validateFilm(film),
                "Фильм с валидными полями не должен генерировать исключение");
    }

    private String getString(int size) {
        return "1".repeat(size);
    }
}
