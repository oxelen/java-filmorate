package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm);

    Collection<Film> findAll();

    Optional<Film> findById(Long id);

    boolean containsFilm(Long id);

    boolean deleteById(Long id);
}
