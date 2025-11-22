package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface FilmDirectorStorage {

    void addDirectorsToFilm(long filmId, List<Director> directors);

    void deleteDirectorsFromFilm(long filmId);

    List<Director> getDirectorsByFilmId(long filmId);

    List<Long> getFilmsByDirector(long directorId);

    void replaceDirectorsForFilm(long filmId, List<Director> directors);
}