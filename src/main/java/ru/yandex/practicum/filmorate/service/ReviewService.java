package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dal.ReviewsDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
public class ReviewService {
    private final ReviewStorage reviewsStorage;

    public ReviewService(@Qualifier("reviewsDbStorage") ReviewStorage reviewsStorage) {
        this.reviewsStorage = reviewsStorage;
    }

    public Review create(Review review) {
        return reviewsStorage.create(review);
    }

    public Review update(Review newReview) {
        if (newReview.getId() == null) {
            throw new ValidationException("Id не может быть null");
        }

        return reviewsStorage.update(newReview);
    }

    public boolean delete(Long id) {
        return reviewsStorage.deleteById(id);
    }

    public Review findById(Long id) {
        return reviewsStorage.findById(id).orElseThrow(()
                -> new NotFoundException("Отзывв с id = " + id + " не найден"));
    }

    public Collection<Review> findAll() {
        return reviewsStorage.findAll();
    }

    public Collection<Review> findAll(Long filmId, Integer count) {
        return reviewsStorage.findAll(filmId, count);
    }

    public Review putLike(Long id, Long userId) {
        return reviewsStorage.putLike(id, userId);
    }
}
