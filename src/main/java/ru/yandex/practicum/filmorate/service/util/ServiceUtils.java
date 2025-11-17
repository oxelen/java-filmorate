package ru.yandex.practicum.filmorate.service.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

@Slf4j
public class ServiceUtils {
    public static void safeDelete(Runnable action, String errorMessage, Long userId) {
        try {
            action.run();
        } catch (Exception e) {
            log.error(errorMessage +  userId, e);
            throw new InternalServerException(errorMessage + userId);
        }
    }
}