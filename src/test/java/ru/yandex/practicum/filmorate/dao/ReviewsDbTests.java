package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.ReviewsDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor
public class ReviewsDbTests {
    private final ReviewsDbStorage reviewsDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private Long filmId;
    private Long userId;

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
    }

    @Test
    void testCreateReview() {
        Review review = Review.builder()
                .content("test")
                .isPositive(false)
                .userId(1L)
                .filmId(1L)
    }
}
