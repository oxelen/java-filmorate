package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;
import java.util.Optional;

@Repository("reviewsDbStorage")
@Slf4j
public class ReviewsDbStorage extends BaseDbStorage<Review> implements ReviewStorage {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    private static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, " +
            "user_id = ?, film_id = ?, useful = ? WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT *" +
            " FROM reviews" +
            " WHERE film_id = ?" +
            " ORDER BY useful DESC" +
            " LIMIT ?";
    private static final String FIND_USER_LIKE_QUERY = "SELECT COUNT(*)" +
            " FROM reviews_ratings" +
            " WHERE review_id = ? AND user_id = ? AND status = true";
    private static final String FIND_USER_DISLIKE_QUERY = "SELECT COUNT(*) " +
            "FROM reviews_ratings " +
            "WHERE review_id = ? AND user_id = ? AND status = false";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO reviews_ratings (review_id, user_id, status) " +
            "VALUES (?, ?, true)";
    private static final String INSERT_DISLIKE_QUERY = "INSERT INTO reviews_ratings (review_id, user_id, status) " +
            "VALUES (?, ?, false)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM reviews_ratings" +
            " WHERE review_id = ?" +
            " AND user_id = ?" +
            " AND status = true";
    private static final String DELETE_DISLIKE_QUERY = "DELETE FROM reviews_ratings " +
            "WHERE review_id = ?" +
            " AND user_id = ?" +
            " AND status = false";

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
        log.trace("Start create in reviewDb");

        Long userId = review.getUserId();
        Long filmId = review.getFilmId();

        checkUserAndFilmId(userId, filmId);

        Long id = insert(INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                userId,
                filmId,
                review.getUseful());

        review.setReviewId(id);
        log.info("Review created");

        return review;
    }

    @Override
    public Review update(Review newReview) {
        log.trace("Start update in reviewDb");

        Long reviewId = newReview.getReviewId();
        Long userId = newReview.getUserId();
        Long filmId = newReview.getFilmId();

        checkReviewId(reviewId);

        checkUserAndFilmId(userId, filmId);

        update(UPDATE_QUERY,
                newReview.getContent(),
                newReview.getIsPositive(),
                userId,
                filmId,
                newReview.getUseful(),
                reviewId);

        log.info("Review updated");

        return newReview;
    }

    @Override
    public boolean deleteById(Long id) {
        log.trace("Start delete in reviewDb");
        checkReviewId(id);

        log.info("Review deleted");

        return delete(DELETE_QUERY, id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        log.trace("Start find in reviewDb");

        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Review> findAll() {
        log.trace("Start findAll in reviewDb");

        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Collection<Review> findAll(Long filmId, Integer count) {
        log.trace("Start findAll in reviewDb. filmId = {}, count = {}", filmId, count);

        checkFilmId(filmId);

        return findMany(FIND_BY_FILM_ID_QUERY, filmId, count);
    }

    @Override
    public Review putLike(Long id, Long userId) {
        log.trace("Start putLike in reviewDb");

        Review res = checkReviewId(id);
        checkUserId(userId);

        if (isUserLikeReview(id, userId)) {
            log.warn("userId = {} already like reviewId = {}", userId, id);

            throw new DuplicatedDataException("Пользователь с id = " + userId
                    + " уже поставил лайк отзыву с id = " + id);
        }

        if (isUserDislikeReview(id, userId)) {
            log.debug("userId = {} dislike reviewId = {}. Deleting dislike", userId, id);

            deleteDislike(id, userId);
            res.setUseful(res.getUseful() + 1);
        }

        update(INSERT_LIKE_QUERY, id, userId);

        res.setUseful(res.getUseful() + 1);
        update(res);

        log.info("Like added");

        return res;
    }

    @Override
    public Review putDislike(Long id, Long userId) {
        log.trace("Start putDislike in reviewDb");

        Review res = checkReviewId(id);
        checkUserId(userId);

        if (isUserDislikeReview(id, userId)) {
            log.warn("userId = {} already dislike reviewId = {}", userId, id);

            throw new DuplicatedDataException("Пользователь с id = " + userId
                    + " уже поставил дизлайк отзыву с id = " + id);
        }

        if (isUserLikeReview(id, userId)) {
            log.debug("userId = {} like reviewId = {}. Deleting like", userId, id);

            deleteLike(id, userId);
            res.setUseful(res.getUseful() - 1);
        }

        update(INSERT_DISLIKE_QUERY, id, userId);

        res.setUseful(res.getUseful() - 1);
        update(res);

        log.info("Dislike added");

        return res;
    }

    @Override
    public boolean deleteLike(Long id, Long userId) {
        log.trace("Start deleteLike in reviewDb");

        Review res = checkReviewId(id);
        checkUserId(userId);

        if (!isUserLikeReview(id, userId)) {
            log.warn("userId = {} not like reviewId = {}", userId, id);

            throw new ConditionsNotMetException("Пользователь с id = " + userId
                    + " не ставил лайк отзыву с id = " + id);
        }

        res.setUseful(res.getUseful() - 1);
        update(res);

        log.info("Like deleted");

        return delete(DELETE_LIKE_QUERY, id, userId);
    }

    @Override
    public boolean deleteDislike(Long id, Long userId) {
        log.trace("Start deleteDislike in reviewDb");

        Review res = checkReviewId(id);
        checkUserId(userId);

        if (!isUserDislikeReview(id, userId)) {
            log.warn("userId = {} not dislike reviewId = {}", userId, id);

            throw new ConditionsNotMetException("Пользователь с id = " + userId
                    + " не ставил дизлайк отзыву с id = " + id);
        }

        res.setUseful(res.getUseful() + 1);
        update(res);

        log.info("Dislike deleted");

        return delete(DELETE_DISLIKE_QUERY, id, userId);
    }

    private boolean isUserLikeReview(Long id, Long userId) {
        log.trace("Start isUserLikeReview in reviewDb");

        return findCount(FIND_USER_LIKE_QUERY, id, userId) == 1;
    }

    private boolean isUserDislikeReview(Long id, Long userId) {
        log.trace("Start isUserDislikeReview in reviewDb");

        return findCount(FIND_USER_DISLIKE_QUERY, id, userId) == 1;
    }

    private void checkUserAndFilmId(Long userId, Long filmId) {
        log.debug("Start check id:  userId = {}, filmId = {}", userId, filmId);

        checkUserId(userId);
        checkFilmId(filmId);

        log.trace("found user and film");
    }

    private void checkFilmId(Long filmId) {
        log.trace("Start check filmId");

        if (!filmDbStorage.containsFilm(filmId)) {
            log.warn("Not found film. id = {}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }

        log.trace("found film");
    }

    private void checkUserId(Long userId) {
        log.trace("Start check userId");

        if (!userDbStorage.containsUser(userId)) {
            log.warn("Not found user. id = {}", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }

        log.trace("found user");
    }

    private Review checkReviewId(Long reviewId) {
        log.trace("Start check reviewId");

        return findById(reviewId).orElseThrow(()
                -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
    }
}
