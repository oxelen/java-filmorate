package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("reviewsDbStorage")
@Slf4j
public class ReviewsDbStorage extends BaseDbStorage<Review> implements ReviewStorage {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    private static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id, useful " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, " +
            "user_id = ?, film_id = ?, useful = ? WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM reviews";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";
    private static final String FIND_USER_LIKE_QUERY = "SELECT user" +
            " FROM reviews_likes" +
            " WHERE review_id = ? AND user_id = ? AND status = true";

    public ReviewsDbStorage(JdbcTemplate jdbc,
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

    @Override
    public boolean deleteById(Long id) {
        checkReviewId(id);

        return delete(DELETE_QUERY, id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Review> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Collection<Review> findAll(Long filmId, Integer count) {
        checkFilmId(filmId);

        return findMany(FIND_BY_FILM_ID_QUERY, filmId, count);
    }

    @Override
    public Review putLike(Long id, Long userId) {
        checkReviewId(id);
        checkUserId(userId);


        return null;
    }

    private boolean isUserEstimateReview(Long id, Long userId) {
        return findOne(FIND_USER_LIKE_QUERY, id, userId).isPresent();
    }

    private void checkUserAndFilmId(Long userId, Long filmId) {
        log.debug("Start check id:  userId = {}, filmId = {}", userId, filmId);

        checkUserId(userId);
        checkFilmId(filmId);

        log.trace("found user and film");
    }

    private void checkFilmId(Long filmId) {
        if (!filmDbStorage.containsFilm(filmId)) {
            log.warn("Not found film. id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }

    private void checkUserId(Long userId) {
        if (!userDbStorage.containsUser(userId)) {
            log.warn("Not found user. id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
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
