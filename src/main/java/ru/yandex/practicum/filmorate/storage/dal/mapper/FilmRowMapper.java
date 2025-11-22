package ru.yandex.practicum.filmorate.storage.dal.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dal.GenresRepository;
import ru.yandex.practicum.filmorate.storage.dal.LikesRepository;
import ru.yandex.practicum.filmorate.storage.dal.MPAsRepository;
import ru.yandex.practicum.filmorate.storage.director.FilmDirectorStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final LikesRepository likesRepository;
    private final MPAsRepository mpasRepository;
    private final GenresRepository genresRepository;
    private final FilmDirectorStorage filmDirectorStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long filmId = (long) rs.getInt("id");

        Film film = Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration((long) rs.getInt("duration"))
                .mpa(mpasRepository.findMPA(rs.getLong("MPA_id")).orElse(null))
                .build();

        film.getLikes().addAll(likesRepository.findAllLikesByFilmId(filmId));
        film.getGenres().addAll(genresRepository.findFilmGenres(filmId));
        List<Director> directors = filmDirectorStorage.getDirectorsByFilmId(filmId);
        film.getDirectors().addAll(directors != null ? directors : new ArrayList<>());

        return film;
    }
}
