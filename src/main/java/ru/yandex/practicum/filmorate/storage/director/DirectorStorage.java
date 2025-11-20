package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Director create(Director director);

    Director update(Director director);

    boolean delete(long id);

    Optional<Director> findById(long id);

    List<Director> findAll();

    List<Long> findAllExistingIds(List<Long> ids);
}
