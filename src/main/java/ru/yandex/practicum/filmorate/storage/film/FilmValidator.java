package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import static ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage.DESCRIPTION_MAX_SIZE;
import static ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage.FILM_BIRTHDAY;

@Slf4j
public class FilmValidator {
    private static final int MAX_MPAS = 5;
    private static final int MAX_GENRES = 6;

    public static void validateFilm(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
        validateMPA(film);
        validateGenres(film);
    }

    public static void validateGenres(Film film) {
        if (!film.getGenres().stream()
                .map(Genre::getId)
                .filter(id -> id > MAX_GENRES)
                .toList()
                .isEmpty()) {
            log.warn("Not valid genre");
            throw new NotFoundException("Неправильный id genre");
        }

        log.trace("Genre is valid");
    }

    public static void validateMPA(Film film) {
        if (film.getMpa().getId() > MAX_MPAS) {
            log.warn("Not valid MPA");
            throw new NotFoundException("Неправильный id MPA");
        }

        log.trace("MPA is valid");
    }

    public static void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Not valid name");
            throw new ValidationException("Имя не может быть пустым");
        }
        log.trace("Name is valid");
    }

    public static void validateDescription(Film film) {
        if (film.getDescription() != null
                && film.getDescription().length() > DESCRIPTION_MAX_SIZE) {
            log.warn("Not valid description");
            throw new ValidationException("Максимальная длина описания - "
                    + DESCRIPTION_MAX_SIZE + " символов");
        }
        log.trace("Description is valid");
    }

    public static void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(FILM_BIRTHDAY)) {
            log.warn("Not valid Release date");
            throw new ValidationException("Дата релиза - не раньше " + FILM_BIRTHDAY);
        }
        log.trace("Release date is valid");
    }

    public static void validateDuration(Film film) {
        if (film.getDuration() <= 0) {
            log.warn("Not valid duration");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.trace("Duration is valid");
    }
}
