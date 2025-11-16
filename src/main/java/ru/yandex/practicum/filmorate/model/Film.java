package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final Set<Genre> genres = new HashSet<>();
    private final List<Director> directors = new ArrayList<>();
}
