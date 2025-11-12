package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Optional;

@Repository("reviewsDbStorage")
@Slf4j
public class ReviewsDbStorage extends BaseDbStorage<Review> implements ReviewStorage {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    public static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id, useful " +
            "VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, " +
            "user_id = ?, film_id = ?, useful = ? WHERE id = ?";
    public static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE id = ?";
    public static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";

    public ReviewsDbStorage (JdbcTemplate jdbc,
                             RowMapper<Review> mapper,
                             UserDbStorage userDbStorage,
                             FilmDbStorage filmDbStorage) {
        super(jdbc, mapper);
        this.userDbStorage = userDbStorage;
        this.filmDbStorage = filmDbStorage;
    }

    @Override
    public Review create(Review review) {
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();

        checkUserAndFilmId(userId, filmId);

        Long id = insert(INSERT_QUERY,
                review.getContent(),
                review.isPositive(),
                userId,
                filmId,
                review.getUseful());

        review.setId(id);

        return review;
    }

    @Override
    public Review update(Review newReview) {
        Long reviewId = newReview.getId();
        Long userId = newReview.getUserId();
        Long filmId = newReview.getFilmId();

        checkReviewId(reviewId);

        checkUserAndFilmId(userId, filmId);

        update(UPDATE_QUERY,
                newReview.getContent(),
                newReview.isPositive(),
                userId,
                filmId,
                newReview.getUseful(),
                reviewId);

        return newReview;
    }

    public boolean deleteById(Long id) {
        checkReviewId(id);

        return delete(DELETE_QUERY, id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    private void checkUserAndFilmId(Long userId, Long filmId) {
        log.debug("Start check id:  userId = {}, filmId = {}", userId, filmId);

        if (!userDbStorage.containsUser(userId)) {
            log.warn("Not found user. id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        if (!filmDbStorage.containsFilm(filmId)) {
            log.warn("Not found film. id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }

        log.trace("found user and film");
    }

    private boolean containsReview(Long id) {
        return findById(id).isPresent();
    }

    private void checkReviewId(Long reviewId) {
        if (!containsReview(reviewId)) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
    }
}
