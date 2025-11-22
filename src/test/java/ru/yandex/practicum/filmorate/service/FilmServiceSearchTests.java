package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmServiceSearchTests {

    private final FilmService filmService;

    @Test
    void searchFilms_nullQuery_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms(null, "title"),
                "Null запрос должен генерировать ValidationException");
    }

    @Test
    void searchFilms_emptyQuery_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms("", "title"),
                "Пустой запрос должен генерировать ValidationException");
    }

    @Test
    void searchFilms_blankQuery_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms("   ", "title"),
                "Пробельный запрос должен генерировать ValidationException");
    }

    @Test
    void searchFilms_invalidSearchType_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> filmService.searchFilms("test", "invalid"),
                "Невалидный тип поиска должен генерировать ValidationException");
    }

    @Test
    void searchFilms_byTitle_returnsMatchingFilms() {
        Film film = Film.builder()
                .name("Unique Search Test Film")
                .description("Test description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120L)
                .mpa(MPA.builder().id(1L).name("G").build())
                .build();
        filmService.create(film);

        List<Film> found = filmService.searchFilms("Unique Search", "title");

        assertFalse(found.isEmpty(), "Поиск должен вернуть результаты");
        assertTrue(found.stream().anyMatch(f -> f.getName().contains("Unique Search")));
    }

    @Test
    void searchFilms_noResults_returnsEmptyList() {
        List<Film> found = filmService.searchFilms("xyznonexistent123", "title");

        assertTrue(found.isEmpty(), "Поиск без результатов должен вернуть пустой список");
    }

    @Test
    void searchFilms_caseInsensitiveSearchType() {
        assertDoesNotThrow(() -> filmService.searchFilms("test", "TITLE"),
                "Поиск с типом в верхнем регистре не должен генерировать исключение");
        assertDoesNotThrow(() -> filmService.searchFilms("test", "Title"),
                "Поиск с типом в смешанном регистре не должен генерировать исключение");
    }
}
