package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dal.LikesRepository;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmValidator;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikesRepository likesRepository;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikesRepository likesRepository) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likesRepository = likesRepository;
    }

    public Film create(Film film) {
        FilmValidator.validateFilm(film);

        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        FilmValidator.validateFilm(newFilm);

        return filmStorage.update(newFilm);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public Map<String, Long> likeFilm(Long filmId, Long userId) {
        log.debug("Starting likeFilm. film id = {}, userId = {}", filmId, userId);

        if (!userStorage.containsUser(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

        Set<Long> likes = findById(filmId).getLikes();
        if (likes.contains(userId)) {
            log.warn("User (id = {}) already likes film (id = {})", userId, filmId);
            throw new DuplicatedDataException("Пользователь с id = " + userId + " уже лайкнул фильм с id = " + filmId);
        }

        likes.add(userId);
        log.trace("User (id = {}) like film (id ={})", userId, filmId);

        likesRepository.create(filmId, userId);

        return Map.of("film Id", filmId,
                "userId", userId);
    }

    public Map<String, Long> deleteLike(Long filmId, Long userId) {
        log.debug("Starting deleteLike, filmId = {}, userId = {}", filmId, userId);

        if (!userStorage.containsUser(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

        Set<Long> likes = findById(filmId).getLikes();
        if (!likes.contains(userId)) {
            log.warn("Likes of film (id = {}) does not contains like from user (id = {})", filmId, userId);
            throw new ConditionsNotMetException("В списке лайков фильма с id = " + filmId
                    + " нет пользователя с id = " + userId);
        }

        likes.remove(userId);
        log.trace("User (id = {}) removed from likes of film (id = {})", userId, filmId);

        likesRepository.delete(filmId, userId);

        return Map.of("filmId", filmId,
                "userId", userId);
    }

    public List<Film> findMostPopularFilms(int count) {
        log.debug("Starting findMostPopularFilms");

        return findAll().stream()
                .sorted((o1, o2) -> (o2.getLikes().size() - o1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
    public List<Film> getRecommendationFilms(Long userId) {
        return filmStorage.getRecommendationFilms(userId);
    }
}
