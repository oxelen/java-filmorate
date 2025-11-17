package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
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

    @Test
    @DisplayName("Удаление фильма должно очищать лайки и жанры, а также удалить его из базы")
    void deleteFilmById_shouldRemoveFilmItsLikesAndGenres() {
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

        Genre genre = genresRepository.findById(1);

        film.getGenres().add(genre);
        filmStorage.update(film);
        assertThat(genresRepository.findFilmGenres(film.getId())).contains(genre);

        filmService.likeFilm(film.getId(), user.getId());
        assertThat(likesRepository.findAllLikes(film.getId())).contains(user.getId());

        filmService.deleteFilmById(film.getId());

        assertThat(genresRepository.findFilmGenres(film.getId())).doesNotContain(genre);
        assertThat(likesRepository.findAllLikes(film.getId())).doesNotContain(user.getId());
    }
}
