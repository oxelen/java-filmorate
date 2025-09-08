package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
   private final FilmStorage filmStorage;

   public FilmController(FilmStorage filmStorage) {
       this.filmStorage = filmStorage;
   }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Running POST method: create film");

        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Running PUT method: update film");

        return filmStorage.update(newFilm);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Running GET method: get all films");
        return filmStorage.findAll();
    }
}
