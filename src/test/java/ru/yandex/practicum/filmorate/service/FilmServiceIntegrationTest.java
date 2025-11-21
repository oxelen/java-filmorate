package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.dal.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class FilmServiceIntegrationTest {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final LikesRepository likesRepository;
    private final FilmService filmService;
    private final GenresRepository genresRepository;
    private final ReviewService reviewService;
    private final ReviewsDbStorage reviewsDbStorage;

    @Test
    @DisplayName("Удаление фильма должно очищать лайки, жанры и отзывы, а также удалить его из базы")
    void deleteFilmById_shouldRemoveFilmItsLikesReviewsAndGenres() {
        User user = userStorage.create(User.builder()
                .email("test@example.com")
                .login("testUser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        MPA mpa = MPA.builder()
                .id(1L)
                .name("PG-13")
                .build();

        Film film = filmStorage.create(Film.builder()
                .name("Test Film")
                .description("Some film for test")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .mpa(mpa)
                .build());

        Review review = Review.builder()
                .content("Test Review")
                .isPositive(true)
                .userId(user.getId())
                .filmId(film.getId())
                .useful(0)
                .build();

        Genre genre = genresRepository.findById(1);

        film.getGenres().add(genre);
        filmStorage.update(film);
        assertThat(genresRepository.findFilmGenres(film.getId())).contains(genre);

        filmService.likeFilm(film.getId(), user.getId());
        assertThat(likesRepository.findAllLikes(film.getId())).contains(user.getId());

        reviewService.create(review);
        assertThat(reviewsDbStorage.findById(review.getReviewId())).contains(review);

        filmService.deleteFilmById(film.getId());

        assertThat(reviewsDbStorage.findAll()).doesNotContain(review);
        assertThat(genresRepository.findFilmGenres(film.getId())).doesNotContain(genre);
        assertThat(likesRepository.findAllLikes(film.getId())).doesNotContain(user.getId());
    }
}
