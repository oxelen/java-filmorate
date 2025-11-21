package ru.yandex.practicum.filmorate.service.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Event.*;

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

    public static Event createEvent(Long userId, EventType type, EventOperation operation, Long entityId) {
        if (userId == null) throw new ConditionsNotMetException("userId не может быть null");
        if (type == null) throw new ConditionsNotMetException("eventType не может быть null");
        if (operation == null) throw new ConditionsNotMetException("operation не может быть null");
        if (entityId == null) throw new ConditionsNotMetException("entityId не может быть null");

        return Event.builder()
                .userId(userId)
                .eventType(type)
                .operation(operation)
                .entityId(entityId)
                .build();
    }
}
