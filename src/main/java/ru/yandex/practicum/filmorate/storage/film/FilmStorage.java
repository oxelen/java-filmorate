package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm);

    Collection<Film> findAll();

    Optional<Film> findById(Long id);

    boolean containsFilm(Long id);

    List<Film> getFilmsByDirectorSortedByYear(Long directorId);

    List<Film> getFilmsByDirectorSortedByLikes(Long directorId);

    List<Film> getRecommendationFilms(Long userId);

    List<Film> getMostPopularFilms(int count, Integer genreId, Integer year);

    boolean deleteById(Long id);

    List<Film> findByTitle(String query);

    List<Film> findByDirector(String query);

    List<Film> findByTitleOrDirector(String query);

}
