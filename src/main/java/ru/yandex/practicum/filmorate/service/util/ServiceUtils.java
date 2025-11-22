package ru.yandex.practicum.filmorate.service.util;

import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Event.Event;
import ru.yandex.practicum.filmorate.model.Event.EventOperation;
import ru.yandex.practicum.filmorate.model.Event.EventType;

public class ServiceUtils {

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
