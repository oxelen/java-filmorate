package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dal.MPAsRepository;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MPAController {
    private final MPAsRepository rep;

    @GetMapping
    public Collection<MPA> findAll() {
        log.info("Starting findAll MPA");

        return rep.findAll();
    }

    @GetMapping("/{id}")
    public MPA findById(@PathVariable Long id) {
        log.info("Starting GET METHOD findById");

        return rep.findMPA(id).orElseThrow(() -> new NotFoundException("MPA не найден"));
    }
}
