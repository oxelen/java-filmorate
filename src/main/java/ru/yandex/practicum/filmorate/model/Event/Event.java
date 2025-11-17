package ru.yandex.practicum.filmorate.model.Event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private Long eventId;
    private Long timestamp; // время события в миллисекундах
    private Long userId;    // кто совершил действие
    private EventType eventType;
    private EventOperation operation;
    private Long entityId;  // на кого или на что произошло действие

}
