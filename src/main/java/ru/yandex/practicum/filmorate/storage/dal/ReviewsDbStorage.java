package ru.yandex.practicum.filmorate.storage.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
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

        Long id = insert(INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful());

        review.setReviewId(id);
        log.info("Review created");

        return review;
    }

    @Override
    public Review update(Review newReview) {
        log.trace("Start update in reviewDb");

        update(UPDATE_QUERY,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getUserId(),
                newReview.getFilmId(),
                newReview.getUseful(),
                newReview.getReviewId());

        log.info("Review updated");

        return newReview;
    }

    @Override
    public boolean deleteById(Long id) {
        log.trace("Start delete in reviewDb");

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

        return findMany(FIND_BY_FILM_ID_QUERY, filmId, count);
    }

    @Override
    public void putLike(Long id, Long userId) {
        log.trace("Start putLike in reviewDb");

        update(INSERT_LIKE_QUERY, id, userId);

        log.info("Like added");
    }

    @Override
    public void putDislike(Long id, Long userId) {
        log.trace("Start putDislike in reviewDb");

        update(INSERT_DISLIKE_QUERY, id, userId);

        log.info("Dislike added");
    }

    @Override
    public boolean deleteLike(Long id, Long userId) {
        log.trace("Start deleteLike in reviewDb");

        return delete(DELETE_LIKE_QUERY, id, userId);
    }

    @Override
    public boolean deleteDislike(Long id, Long userId) {
        log.trace("Start deleteDislike in reviewDb");

        return delete(DELETE_DISLIKE_QUERY, id, userId);
    }

    @Override
    public void deleteReviewByFilmConnection(Long filmId) {
        jdbc.queryForList("SELECT id FROM reviews WHERE film_id = ?", Long.class, filmId)
                .forEach((id) -> {
                    delete("DELETE FROM reviews_ratings WHERE review_id = ?", id);

                    this.deleteById(id);
                });
    }

    @Override
    public void deleteReviewByUserConnection(Long userId) {
        delete("DELETE FROM reviews_ratings WHERE user_id = ?", userId);

        jdbc.queryForList("SELECT id FROM reviews WHERE user_id = ?", Long.class, userId)
                .forEach((id) -> {
                    delete("DELETE FROM reviews_ratings WHERE review_id = ?", id);

                    this.deleteById(id);
                });
    }

    @Override
    public boolean containsReview(Long reviewId) {
        log.trace("Start containsReview in reviewDb");

        return findById(reviewId).isPresent();
    }

    @Override
    public boolean isUserLikeReview(Long id, Long userId) {
        log.trace("Start isUserLikeReview in reviewDb");

        return findCount(FIND_USER_LIKE_QUERY, id, userId) == 1;
    }

    @Override
    public boolean isUserDislikeReview(Long id, Long userId) {
        log.trace("Start isUserDislikeReview in reviewDb");

        return findCount(FIND_USER_DISLIKE_QUERY, id, userId) == 1;
    }
}
