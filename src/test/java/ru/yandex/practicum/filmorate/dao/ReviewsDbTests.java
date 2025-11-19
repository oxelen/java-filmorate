package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.ReviewsDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewsDbTests {
    private final ReviewsDbStorage reviewsDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @BeforeEach
    public void setup() {
        Film film = Film.builder()
                .name("test")
                .description("test")
                .releaseDate(LocalDate.EPOCH)
                .duration(100L)
                .mpa(MPA.builder().id(1L).build())
                .build();
        User user = User.builder()
                .email("test@test.com")
                .login("test")
                .name("test")
                .birthday(LocalDate.EPOCH)
                .build();

        filmDbStorage.create(film);
        userDbStorage.create(user);
    }

    @AfterEach
    public void teardown() {
        reviewsDbStorage.deleteReviewByFilmConnection(1L);
        reviewsDbStorage.deleteReviewByUserConnection(1L);
    }

    @Test
    void testCreateReview() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .build();

        Review created = reviewsDbStorage.create(review);

        assertThat(created).isNotNull();
        assertThat(created.getContent()).isEqualTo("test");
        assertThat(created.getUseful()).isEqualTo(0);

        Optional<Review> found = reviewsDbStorage.findById(created.getReviewId());
        assertThat(found).isPresent();
    }

    @Test
    void testUpdateReview() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .build();

        Review created = reviewsDbStorage.create(review);

        review = Review.builder()
                .reviewId(created.getReviewId())
                .content("upd test")
                .isPositive(true)
                .userId(1L)
                .filmId(1L)
                .useful(10)
                .build();

        Review updated = reviewsDbStorage.update(review);

        assertThat(updated).isNotNull();
        assertThat(updated.getReviewId()).isEqualTo(review.getReviewId());
        assertThat(updated.getContent()).isEqualTo("upd test");
        assertThat(updated.getIsPositive()).isTrue();
        assertThat(updated.getUseful()).isEqualTo(10);

        Optional<Review> found = reviewsDbStorage.findById(updated.getReviewId());
        assertThat(found).isPresent();
    }

    @Test
    void testDeleteReview() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .build();

        Review created = reviewsDbStorage.create(review);

        boolean res = reviewsDbStorage.deleteById(created.getReviewId());

        assertThat(res).isTrue();

        Optional<Review> found = reviewsDbStorage.findById(created.getReviewId());
        assertThat(found).isEmpty();
    }

    @Test
    void testFindById() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .build();

        Review created = reviewsDbStorage.create(review);

        Review found = reviewsDbStorage.findById(created.getReviewId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found).isEqualTo(created);
    }

    @Test
    void testFindAll() {
        List<Review> target = new ArrayList<>();

        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .useful(5)
                .build();

        target.add(reviewsDbStorage.create(review));


        review = Review.builder()
                .content("test2")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .useful(10)
                .build();

        target.add(reviewsDbStorage.create(review));

        List<Review> found = reviewsDbStorage.findAll().stream().toList();

        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(2);
        assertThat(found.getFirst()).isEqualTo(target.get(1));
        assertThat(found.get(1)).isEqualTo(target.get(0));
    }

    @Test
    void testFindAllByFilm() {
        filmDbStorage.create(Film.builder()
                .name("test2")
                .description("test2")
                .releaseDate(LocalDate.EPOCH)
                .duration(100L)
                .mpa(MPA.builder().id(1L).build())
                .build());

        List<Review> target = new ArrayList<>();

        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .useful(5)
                .build();

        target.add(reviewsDbStorage.create(review));


        review = Review.builder()
                .content("test2")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .useful(10)
                .build();

        target.add(reviewsDbStorage.create(review));

        review = Review.builder()
                .content("test2")
                .isPositive(false)
                .userId(1L)
                .filmId(2L)
                .useful(10)
                .build();

        target.add(reviewsDbStorage.create(review));

        List<Review> found = reviewsDbStorage.findAll(1L, 1).stream().toList();

        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(1);
        assertThat(found.getFirst()).isEqualTo(target.get(1));

        List<Review> reviewsNotInResult = List.of(target.getFirst(), target.getLast());
        assertThat(found.containsAll(reviewsNotInResult)).isFalse();

        found = reviewsDbStorage.findAll(1L, 2).stream().toList();

        assertThat(found).isNotNull();
        assertThat(found.size()).isEqualTo(2);
        assertThat(found.getFirst()).isEqualTo(target.get(1));
        assertThat(found.get(1)).isEqualTo(target.get(0));

        reviewsNotInResult = List.of(target.getLast());
        assertThat(found.containsAll(reviewsNotInResult)).isFalse();
    }

    @Test
    void testDeleteReviewByFilmConnection() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .build();

        Review created = reviewsDbStorage.create(review);
        reviewsDbStorage.deleteReviewByFilmConnection(1L);

        Optional<Review> found = reviewsDbStorage.findById(created.getReviewId());

        assertThat(found).isEmpty();
    }

    @Test
    void testDeleteReviewByUserConnection() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
                .build();

        Review created = reviewsDbStorage.create(review);
        reviewsDbStorage.deleteReviewByUserConnection(1L);

        Optional<Review> found = reviewsDbStorage.findById(created.getReviewId());

        assertThat(found).isEmpty();
    }
}
