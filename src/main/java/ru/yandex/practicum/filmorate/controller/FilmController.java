package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    public static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);
    public static final int DESCRIPTION_MAX_SIZE = 200;

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Running POST method: create film");
        log.debug("Starting create film: {}", film.getName());

        validateFilm(film);
        log.trace("Film is valid");

        film.setId(getNextId());
        log.debug("Film: {}. Set id = {}", film.getName(), film.getId());

        films.put(film.getId(), film);
        log.info("POST method: create film (id = {}) worked successfully", film.getId());

        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Running PUT method: update film");
        if (newFilm.getId() == null) {
            log.warn("Film id is null");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        log.debug("Starting update film: {}, id = {}", newFilm.getName(), newFilm.getId());

        if (films.containsKey(newFilm.getId())) {
            log.trace("Found film with id = {}", newFilm.getId());

            Film oldFilm = films.get(newFilm.getId());
            updateFilmFields(oldFilm, newFilm);
            log.info("PUT method: update film (id = {}) worked successfully", oldFilm.getId());

            return oldFilm;
        }
        log.warn("Not found film with id = {}", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Running GET method: get all films");
        return films.values();
    }

    public static void validateFilm(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
    }

    public static void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Not valid name");
            throw new ValidationException("Имя не может быть пустым");
        }
        log.trace("Name is valid");
    }

    public static void validateDescription(Film film) {
        if (film.getDescription() != null && film.getDescription().length() > DESCRIPTION_MAX_SIZE) {
            log.warn("Not valid description");
            throw new ValidationException("Максимальная длина описания - " + DESCRIPTION_MAX_SIZE + " символов");
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

    private static void updateFilmFields(Film oldFilm, Film newFilm) {
        log.debug("Starting update Film fields, id = {}", newFilm.getId());
        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
            log.trace("Updated name: {}", oldFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            validateDescription(newFilm);
            oldFilm.setDescription(newFilm.getDescription());
            log.trace("Updated description: {}", oldFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            validateReleaseDate(newFilm);
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.trace("Updated Release date: {}", oldFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            validateDuration(newFilm);
            oldFilm.setDuration(newFilm.getDuration());
            log.trace("Updated duration: {}", oldFilm.getDuration());
        }
        log.debug("Film updated. {}", oldFilm.toString());
    }

    private Long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);

        return ++currentMaxId;
    }
}
