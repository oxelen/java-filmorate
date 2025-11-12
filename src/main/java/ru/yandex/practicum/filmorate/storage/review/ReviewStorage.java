package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Optional;

public interface ReviewStorage {
    public Review create(Review review);

    public Review update(Review newReview);

    public boolean deleteById(Long id);

    public Optional<Review> findById(Long id);
}
