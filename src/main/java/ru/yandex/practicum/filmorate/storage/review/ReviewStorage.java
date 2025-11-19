package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review newReview);

    boolean deleteById(Long id);

    Optional<Review> findById(Long id);

    Collection<Review> findAll();

    Collection<Review> findAll(Long filmId, Integer count);

    void putLike(Long id, Long userId);

    void putDislike(Long id, Long userId);

    boolean deleteLike(Long id, Long userId);

    boolean deleteDislike(Long id, Long userId);

    void deleteReviewByFilmConnection(Long filmId);

    void deleteReviewByUserConnection(Long userId);

    boolean containsReview(Long reviewId);

    boolean isUserLikeReview(Long id, Long userId);

    boolean isUserDislikeReview(Long id, Long userId);
}