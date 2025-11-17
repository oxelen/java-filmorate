package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.film.FilmValidator.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    public static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public static final int DESCRIPTION_MAX_SIZE = 200;

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        log.debug("Starting create film: {}", film.getName());

        validateFilm(film);
        log.trace("Film is valid");

        film.setId(getNextId());
        log.debug("Film: {}. Set id = {}", film.getName(), film.getId());

        films.put(film.getId(), film);
        log.info("POST method: create film (id = {}) worked successfully", film.getId());

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Film id is null");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        log.debug("Starting update film: {}, id = {}", newFilm.getName(), newFilm.getId());

        if (containsFilm(newFilm.getId())) {
            log.trace("Found film with id = {}", newFilm.getId());

            Film oldFilm = films.get(newFilm.getId());
            updateFilmFields(oldFilm, newFilm);
            log.info("PUT method: update film (id = {}) worked successfully", oldFilm.getId());

            return oldFilm;
        }
        log.warn("Not found film with id = {}", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        log.debug("Starting findById, id = {}", id);

        if (!containsFilm(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");

        return Optional.of(films.get(id));
    }

    @Override
    public boolean containsFilm(Long id) {
        log.debug("Starting containsFilm, id = {}", id);
        if (films.containsKey(id)) {
            log.debug("Found film with, id = {}", id);
            return true;
        }
        log.warn("Not found film, id = {}", id);
        return false;
    }

    @Override
    public boolean deleteById(Long id) {
        log.debug("Starting deleteById, id = {}", id);
        return films.remove(id) != null;
    }

    private void updateFilmFields(Film oldFilm, Film newFilm) {
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
