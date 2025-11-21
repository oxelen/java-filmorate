package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Event.Event;
import ru.yandex.practicum.filmorate.model.Event.EventOperation;
import ru.yandex.practicum.filmorate.model.Event.EventType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.util.ServiceUtils;
import ru.yandex.practicum.filmorate.storage.dal.EventsRepository;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.storage.review.ReviewValidator.validateReview;


@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewsStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventsRepository eventsRepository;

    public ReviewService(@Qualifier("reviewsDbStorage") ReviewStorage reviewsStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         EventsRepository eventsRepository) {
        this.reviewsStorage = reviewsStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventsRepository = eventsRepository;
    }

    public Review create(Review review) {
        log.trace("Start create in reviewService");

        validateReview(review);

        checkUserAndFilmId(review.getUserId(), review.getFilmId());
        Review createdReview = reviewsStorage.create(review);

        Event event = ServiceUtils.createEvent(createdReview.getUserId(), EventType.REVIEW, EventOperation.ADD, createdReview.getReviewId());
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

        return createdReview;
    }

    public Review update(Review newReview) {
        log.trace("Start update in reviewService");

        if (newReview.getReviewId() == null) {
            log.warn("Review id is null");
            throw new ValidationException("Id не может быть null");
        }
        validateReview(newReview);

        checkReviewId(newReview.getReviewId());
        checkUserAndFilmId(newReview.getUserId(), newReview.getFilmId());

        Review updatedReview = reviewsStorage.update(newReview);

        Event event = ServiceUtils.createEvent(updatedReview.getUserId(), EventType.REVIEW, EventOperation.UPDATE, updatedReview.getReviewId());
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

        return updatedReview;
    }

    public boolean delete(Long id) {
        log.trace("Start delete in reviewService");

        Review review = findById(id);

        boolean isDeleted = reviewsStorage.deleteById(id);

        if (!isDeleted) {
            log.error("Unexpected: review {} exists but was not deleted", id);
            throw new InternalServerException("Cannot delete review with id " + id);
        }

        Event event = ServiceUtils.createEvent(review.getUserId(),
                EventType.REVIEW,
                EventOperation.REMOVE,
                review.getReviewId());
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

        return isDeleted;
    }

    public Review findById(Long id) {
        log.trace("Start find in reviewService");

        return reviewsStorage.findById(id).orElseThrow(()
                -> new NotFoundException("Отзывв с id = " + id + " не найден"));
    }

    public Collection<Review> findAll() {
        log.trace("Start findAll in reviewService");

        return reviewsStorage.findAll();
    }

    public Collection<Review> findAll(Long filmId, Integer count) {
        log.trace("Start findAll in reviewService. filmId = {}, count = {}", filmId, count);

        checkFilmId(filmId);

        return reviewsStorage.findAll(filmId, count);
    }

    public Review putLike(Long id, Long userId) {
        log.trace("Start putLike in reviewService");

        Review res = findById(id);
        checkUserId(userId);

        if (reviewsStorage.isUserLikeReview(id, userId)) {
            log.warn("userId = {} already like reviewId = {}", userId, id);

            throw new DuplicatedDataException("Пользователь с id = " + userId
                    + " уже поставил лайк отзыву с id = " + id);
        }

        if (reviewsStorage.isUserDislikeReview(id, userId)) {
            log.debug("userId = {} dislike reviewId = {}. Deleting dislike", userId, id);

            deleteDislike(id, userId);
            res.setUseful(res.getUseful() + 1);
        }

        res.setUseful(res.getUseful() + 1);
        update(res);
        reviewsStorage.putLike(id, userId);

        return res;
    }

    public Review putDislike(Long id, Long userId) {
        log.trace("Start putDislike in reviewService");

        Review res = findById(id);
        checkUserId(userId);

        if (reviewsStorage.isUserDislikeReview(id, userId)) {
            log.warn("userId = {} already dislike reviewId = {}", userId, id);

            throw new DuplicatedDataException("Пользователь с id = " + userId
                    + " уже поставил дизлайк отзыву с id = " + id);
        }

        if (reviewsStorage.isUserLikeReview(id, userId)) {
            log.debug("userId = {} like reviewId = {}. Deleting like", userId, id);

            deleteLike(id, userId);
            res.setUseful(res.getUseful() - 1);
        }

        res.setUseful(res.getUseful() - 1);
        update(res);
        reviewsStorage.putDislike(id, userId);

        return res;
    }

    public boolean deleteLike(Long id, Long userId) {
        log.trace("Start deleteLike in reviewService");

        Review res = findById(id);
        checkUserId(userId);

        if (!reviewsStorage.isUserLikeReview(id, userId)) {
            log.warn("userId = {} not like reviewId = {}", userId, id);

            throw new ConditionsNotMetException("Пользователь с id = " + userId
                    + " не ставил лайк отзыву с id = " + id);
        }

        res.setUseful(res.getUseful() - 1);
        update(res);

        return reviewsStorage.deleteLike(id, userId);
    }

    public boolean deleteDislike(Long id, Long userId) {
        log.trace("Start deleteDislike in reviewService");

        Review res = findById(id);
        checkUserId(userId);

        if (!reviewsStorage.isUserDislikeReview(id, userId)) {
            log.warn("userId = {} not dislike reviewId = {}", userId, id);

            throw new ConditionsNotMetException("Пользователь с id = " + userId
                    + " не ставил дизлайк отзыву с id = " + id);
        }

        res.setUseful(res.getUseful() + 1);
        update(res);

        return reviewsStorage.deleteDislike(id, userId);
    }

    private void checkUserAndFilmId(Long userId, Long filmId) {
        log.debug("Start check id:  userId = {}, filmId = {}", userId, filmId);

        checkUserId(userId);
        checkFilmId(filmId);

        log.trace("found user and film");
    }

    private void checkFilmId(Long filmId) {
        log.trace("Start check filmId");

        if (!filmStorage.containsFilm(filmId)) {
            log.warn("Not found film. id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }

        log.trace("found film");
    }

    private void checkUserId(Long userId) {
        log.trace("Start check userId");

        if (!userStorage.containsUser(userId)) {
            log.warn("Not found user. id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }

        log.trace("found user");
    }

    private void checkReviewId(Long reviewId) {
        log.trace("Start check reviewId");

        if (!reviewsStorage.containsReview(reviewId)) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
    }
}
