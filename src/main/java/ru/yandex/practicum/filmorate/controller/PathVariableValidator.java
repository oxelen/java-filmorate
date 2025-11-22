package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Slf4j
public class PathVariableValidator {
    public static void checkIds(Long... ids) {
        for (Long id : ids) {
            if (id == null) {
                log.warn("User id is null");
                throw new ConditionsNotMetException("Id должен быть указан");
            }
            if (id < 0) {
                log.warn("User id is below 0, id = {}", id);
                throw new NotFoundException("Id должен быть не меньше 0, указанный id = " + id);
            }
        }
    }
}
