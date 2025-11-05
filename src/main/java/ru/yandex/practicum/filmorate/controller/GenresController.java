package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dal.GenresRepository;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenresController {
    private final GenresRepository rep;

    @GetMapping
    public Collection<Genre> findAll() {
        log.info("Starting GET METHOD find all genres");

        return rep.findAll();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        log.info("Starting GET METHOD find by id");

        return rep.findById(id);
    }
}
