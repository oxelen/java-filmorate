package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private final Set<Long> likes = new HashSet<>();
    private final MPA mpa;
    private final Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
    private final List<Director> directors = new ArrayList<>();
}
