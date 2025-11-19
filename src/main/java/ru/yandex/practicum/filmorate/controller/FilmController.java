package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

import static ru.yandex.practicum.filmorate.controller.PathVariableValidator.checkIds;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Running POST method: create film");
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Running PUT method: update film");
        return filmService.update(newFilm);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Running GET method: get all films");

        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Running GET method find film by id");

        checkIds(id);
        return filmService.findById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Map<String, Long> likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Running PUT method likeFilm");

        checkIds(id, userId);
        return filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Map<String, Long> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Running DELETE method: deleteLike");

        checkIds(id, userId);
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> findMostPopularFilms(@RequestParam Optional<Integer> count) {
        log.info("Running GET method find most Popular films");

        final int DEFAULT_COUNT = 10;
        log.trace("Default most popular films count = {}", DEFAULT_COUNT);

        return filmService.findMostPopularFilms(count.orElse(DEFAULT_COUNT));
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        return filmService.searchFilms(query, by);
    }
}
