package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Event.Event;
import ru.yandex.practicum.filmorate.model.Event.EventOperation;
import ru.yandex.practicum.filmorate.model.Event.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchType;
import ru.yandex.practicum.filmorate.service.util.ServiceUtils;
import ru.yandex.practicum.filmorate.storage.dal.EventsRepository;
import ru.yandex.practicum.filmorate.storage.dal.LikesRepository;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.director.FilmDirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmValidator;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikesRepository likesRepository;
    private final DirectorStorage directorStorage;
    private final FilmDirectorStorage filmDirectorStorage;
    private final EventsRepository eventsRepository;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikesRepository likesRepository, DirectorStorage directorStorage, FilmDirectorStorage filmDirectorStorage, EventsRepository eventsRepository) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likesRepository = likesRepository;
        this.directorStorage = directorStorage;
        this.filmDirectorStorage = filmDirectorStorage;
        this.eventsRepository = eventsRepository;
    }

    public Film create(Film film) {
        FilmValidator.validateFilm(film);

        film = filmStorage.create(film);
        validateDirectorsExist(film);
        filmDirectorStorage.addDirectorsToFilm(film.getId(), film.getDirectors());

        return film;
    }

    public Film update(Film newFilm) {
        FilmValidator.validateFilm(newFilm);

        newFilm = filmStorage.update(newFilm);
        validateDirectorsExist(newFilm);
        filmDirectorStorage.replaceDirectorsForFilm(newFilm.getId(), newFilm.getDirectors());

        return newFilm;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public Map<String, Long> likeFilm(Long filmId, Long userId) {
        log.debug("Starting likeFilm. film id = {}, userId = {}", filmId, userId);

        checkUserInStorage(userId);

        Set<Long> likes = findById(filmId).getLikes();
        if (likes.contains(userId)) {
            log.warn("User (id = {}) already likes film (id = {})", userId, filmId);
            Event event = ServiceUtils.createEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
            eventsRepository.createEvent(event);
            log.debug("Event created: {}", event);
            return Map.of("film Id", filmId,
                    "userId", userId);
        }

        likes.add(userId);


        likesRepository.create(filmId, userId);

        Event event = ServiceUtils.createEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

        log.info("User (id = {}) liked film (id ={})", userId, filmId);
        return Map.of("film Id", filmId,
                "userId", userId);
    }

    public Map<String, Long> deleteLike(Long filmId, Long userId) {
        log.debug("Starting deleteLike, filmId = {}, userId = {}", filmId, userId);

        checkUserInStorage(userId);

        Set<Long> likes = findById(filmId).getLikes();
        if (!likes.contains(userId)) {
            log.warn("Likes of film (id = {}) does not contains like from user (id = {})", filmId, userId);
            throw new ConditionsNotMetException("В списке лайков фильма с id = " + filmId
                    + " нет пользователя с id = " + userId);
        }

        likes.remove(userId);
        log.trace("User (id = {}) removed from likes of film (id = {})", userId, filmId);

        likesRepository.delete(filmId, userId);

        Event event = ServiceUtils.createEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

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

    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        if (directorStorage.findById(directorId).isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + directorId + " не найден");
        }

        switch (sortBy) {
            case "year":
                return filmStorage.getFilmsByDirectorSortedByYear(directorId);
            case "likes":
                return filmStorage.getFilmsByDirectorSortedByLikes(directorId);
            default:
                throw new ValidationException("sortBy должен быть 'year' или 'likes'");
        }
    }

    private void validateDirectorsExist(Film film) {
        List<Director> directors = film.getDirectors();
        if (directors == null || directors.isEmpty()) {
            return;
        }

        List<Long> ids = directors.stream()
                .map(Director::getId)
                .toList();

        List<Long> existing = directorStorage.findAllExistingIds(ids);

        if (existing.size() != ids.size()) {
            List<Long> missing = ids.stream()
                    .filter(id -> !existing.contains(id))
                    .toList();

            throw new NotFoundException("Режиссёры не найдены: " + missing);
        }
    }

    public List<Film> getMostPopularFilms(int count, Integer genreId, Integer year) {
        List<Film> mostPopularFilms = filmStorage.getMostPopularFilms(count, genreId, year);
        if (mostPopularFilms.isEmpty()) {
            log.info("Popular films list is empty for parameters: count={}, genreId={}, year={}", count, genreId, year);
        } else {
            log.info("Most popular films have been successfully found for count: {}, genreId: {}, year: {}",
                    count, genreId, year);
        }
        return mostPopularFilms;
    }

    @Transactional
    public void deleteFilmById(Long filmId) {
        log.debug("Starting deleteFilmById, filmId = {}", filmId);
        Film film = findById(filmId);

        if (!filmStorage.deleteById(film.getId())) {
            log.error("Failed to remove film with id = {}", filmId);
            throw new InternalServerException("Не удалось удалить фильм с id = " + filmId);
        }
        log.info("Film with id = {} has been successfully deleted", filmId);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        checkUserInStorage(userId, friendId);

        return likesRepository.findAllLikedByUserId(userId)
                .stream()
                .filter(likesRepository.findAllLikedByUserId(friendId)::contains)
                .map(this::findById)
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .toList();
    }

    private void checkUserInStorage(Long... userIds) {
        for (Long userId : userIds) {
            if (!userStorage.containsUser(userId)) {
                log.warn("Not found user id = {}", userId);
                throw new NotFoundException("Пользователь с id = " + userId + " не найден");
            }
        }
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Пустой запрос! Проверьте корректность ввода подстроки");
        }

        Set<SearchType> searchBy = parseSearchTypes(by);

        if (searchBy.isEmpty()) {
            throw new ValidationException("Необходимо указать хотя бы один тип поиска");
        }

        if (searchBy.contains(SearchType.TITLE) && searchBy.contains(SearchType.DIRECTOR)) {
            return filmStorage.findByTitleOrDirector(query);
        }

        if (searchBy.contains(SearchType.TITLE)) {
            return filmStorage.findByTitle(query);
        }

        if (searchBy.contains(SearchType.DIRECTOR)) {
            return filmStorage.findByDirector(query);
        }

        throw new InternalServerException("Неожиданное состояние поиска");
    }

    private Set<SearchType> parseSearchTypes(String by) {
        try {
            return Arrays.stream(by.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(SearchType::valueOf)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new ValidationException("Несуществующий тип поиска, доступны: title, director");
        }
    }
}
