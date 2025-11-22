package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbTests {
    private final FilmDbStorage filmStorage;

    //Этот тест надо поправить: сделать @AfterEach с удалением всех фильмов из БД
    /*@Test
    void testFindAll() {
        filmStorage.create(Film.builder()
                .name("A")
                .description("Film A")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build());
        filmStorage.create(Film.builder()
                .name("B")
                .description("Film B")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(120L)
                .mpa(MPA.builder().id(2L).name("PG").build())
                .build());

        Collection<Film> films = filmStorage.findAll();
        assertThat(films.size()).isEqualTo(15);
    }*/

    @Test
    void testCreateFilm() {
        Film film = Film.builder()
                .name("test")
                .description("test")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build();

        film.getGenres().add(Genre.builder().id(1L).name("Комедия").build());

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("test");
        assertThat(filmStorage.findById(created.getId())).isNotNull();
    }

    @Test
    void testFindById() {
        Film film = Film.builder()
                .name("test")
                .description("test")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169L)
                .mpa(MPA.builder().id(2L).name("PG").build())
                .build();

        film.getGenres().add(Genre.builder().id(1L).name("Комедия").build());

        Film created = filmStorage.create(film);
        Film found = filmStorage.findById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("test");
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.create(Film.builder()
                .name("Old Title")
                .description("Old Desc")
                .releaseDate(LocalDate.of(1999, 9, 9))
                .duration(90L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build());

        Film updated = Film.builder()
                .id(film.getId())
                .name("New Title")
                .description("Updated Desc")
                .releaseDate(LocalDate.of(2005, 5, 5))
                .duration(110L)
                .mpa(MPA.builder().id(3L).name("R").build())
                .build();

        filmStorage.update(updated);

        Film found = filmStorage.findById(film.getId());
        assertThat(found.getName()).isEqualTo("New Title");
        assertThat(found.getDescription()).isEqualTo("Updated Desc");
    }

    @Test
    void testContainsFilm() {
        Film film = filmStorage.create(Film.builder()
                .name("Exists Film")
                .description("Description")
                .releaseDate(LocalDate.of(2010, 10, 10))
                .duration(100L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build());

        assertThat(filmStorage.containsFilm(film.getId())).isTrue();
        assertThat(filmStorage.containsFilm(9999L)).isFalse();
    }

    @Test
    void testFindByTitle() {
        filmStorage.create(Film.builder()
                .name("Inception")
                .description("A mind-bending thriller")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build());

        List<Film> found = filmStorage.findByTitle("incep");

        assertThat(found).isNotEmpty();
        assertThat(found.getFirst().getName()).containsIgnoringCase("incep");
    }

    @Test
    void testFindByTitleNoResults() {
        List<Film> found = filmStorage.findByTitle("nonexistentfilm12345");

        assertThat(found).isEmpty();
    }

    @Test
    void testFindByTitleOrDirector() {
        filmStorage.create(Film.builder()
                .name("The Matrix")
                .description("A sci-fi classic")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build());

        List<Film> found = filmStorage.findByTitleOrDirector("matrix");

        assertThat(found).isNotEmpty();
        assertThat(found.getFirst().getName()).containsIgnoringCase("matrix");
    }
}
