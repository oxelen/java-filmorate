package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director createDirector(Director director) {
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.update(director);
    }

    public void deleteDirector(long id) {
        if (!directorStorage.delete(id)) {
            throw new NotFoundException("Режиссёр с id = " + id + " не найден");
        }
    }

    public Director getDirectorById(long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
    }

    public List<Director> getAllDirectors() {
        return directorStorage.findAll();
    }
}
