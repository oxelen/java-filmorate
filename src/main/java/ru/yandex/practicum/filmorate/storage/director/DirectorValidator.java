package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorValidator {
    public static void validateDirector(Director director) {
        validateName(director);
    }

    public static void validateName(Director director) {
        if (director.getName() == null || director.getName().isBlank())
            throw new ValidationException("Director name is not valid");
    }
}
