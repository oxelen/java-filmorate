package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.storage.review.ReviwValidator.*;


@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewsStorage;

    public ReviewService(@Qualifier("reviewsDbStorage") ReviewStorage reviewsStorage) {
        this.reviewsStorage = reviewsStorage;
    }

    public Review create(Review review) {
        log.trace("Start create in reviewService");

        validateReview(review);
        return reviewsStorage.create(review);
    }

    public Review update(Review newReview) {
        log.trace("Start update in reviewService");

        if (newReview.getReviewId() == null) {
            log.warn("Review id is null");
            throw new ValidationException("Id не может быть null");
        }
        validateReview(newReview);

        return reviewsStorage.update(newReview);
    }

    public boolean delete(Long id) {
        log.trace("Start delete in reviewService");

        return reviewsStorage.deleteById(id);
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

        return reviewsStorage.findAll(filmId, count);
    }

    public Review putLike(Long id, Long userId) {
        log.trace("Start putLike in reviewService");

        return reviewsStorage.putLike(id, userId);
    }

    public Review putDislike(Long id, Long userId) {
        log.trace("Start putDislike in reviewService");

        return reviewsStorage.putDislike(id, userId);
    }

    public boolean deleteLike(Long id, Long userId) {
        log.trace("Start deleteLike in reviewService");

        return reviewsStorage.deleteLike(id, userId);
    }

    public boolean deleteDislike(Long id, Long userId) {
        log.trace("Start deleteDislike in reviewService");

        return reviewsStorage.deleteDislike(id, userId);
    }
}
