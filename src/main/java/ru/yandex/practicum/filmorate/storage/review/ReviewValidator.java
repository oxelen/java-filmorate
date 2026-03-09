package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

@Slf4j
public class ReviewValidator {
    public static void validateReview(Review review) {
        validateContent(review);
        validateUserId(review);
        validateIsPositive(review);
        validateFilmId(review);
    }

    public static void validateContent(Review review) {
        log.trace("validateContent");
        if (review.getContent() == null || review.getContent().isEmpty()) {
            log.warn("Not valid content");
            throw new ValidationException("Содержание не может быть пустым");
        }

        log.trace("content is valid");
    }

    public static void validateUserId(Review review) {
        log.trace("validateUserId");
        if (review.getUserId() == null) {
            log.warn("Not valid userId");
            throw new ValidationException("Id пользователя не может быть пустым");
        }

        log.trace("userId is valid");
    }

    public static void validateIsPositive(Review review) {
        log.trace("validateIsPositive");
        if (review.getIsPositive() == null) {
            log.warn("Not valid isPositive");
            throw new ValidationException("Is positive не может быть пустым");
        }

        log.trace("isPositive is valid");
    }

    public static void validateFilmId(Review review) {
        log.trace("validateFilmId");
        if (review.getFilmId() == null) {
            log.warn("Not valid filmId");
            throw new ValidationException("Id фильма не может быть пустым");
        }

        log.trace("filmId is valid");
    }
}
