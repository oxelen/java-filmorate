package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Map<String, Long> likeFilm(Long filmId, Long userId) {
        log.debug("Starting likeFilm. film id = {}, userId = {}", filmId, userId);

        if (!userStorage.containsUser(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

        Set<Long> likes = filmStorage.findById(filmId).getLikes();
        if (likes.contains(userId)) {
            log.warn("User (id = {}) already likes film (id = {})", userId, filmId);
            throw new DuplicatedDataException("Пользователь с id = " + userId + " уже лайкнул фильм с id = " + filmId);
        }

        likes.add(userId);
        log.trace("User (id = {}) like film (id ={})", userId, filmId);

        return Map.of("film Id", filmId,
                "userId", userId);
    }

    public Map<String, Long> deleteLike(Long filmId, Long userId) {
        log.debug("Starting deleteLike, filmId = {}, userId = {}", filmId, userId);

        if (!userStorage.containsUser(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

        Set<Long> likes = filmStorage.findById(filmId).getLikes();
        if (!likes.contains(userId)) {
            log.warn("Likes of film (id = {}) does not contains like from user (id = {})", filmId, userId);
            throw new ConditionsNotMetException("В списке лайков фильма с id = " + filmId
                    + " нет пользователя с id = " + userId);
        }

        likes.remove(userId);
        log.trace("User (id = {}) removed from likes of film (id = {})", userId, filmId);

        return Map.of("filmId", filmId,
                "userId", userId);
    }

    public List<Film> findMostPopularFilms(int count) {
        log.debug("Starting findMostPopularFilms");

        return filmStorage.findAll().stream()
                .sorted((o1, o2) -> (o2.getLikes().size() - o1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
