package ru.yandex.practicum.filmorate.storage.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event.Event;

import java.util.List;

@Repository
public class EventsRepository extends BaseDbStorage<Event> {

    private static final String INSERT_QUERY = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM events WHERE event_id = ?";
    private static final String DELETE_ALL_EVENTS_BY_USER_QUERY = "DELETE FROM events WHERE user_id = ?";
    private static final String FIND_EVENTS_BY_USER_QUERY =
            "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";

    public EventsRepository(JdbcTemplate jdbc,
                            @Qualifier("eventsRowMapper") RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    public void createEvent(Event event) {
        Long eventId = insert(INSERT_QUERY,
                System.currentTimeMillis(),
                event.getUserId(),
                event.getEventType(),
                event.getOperation(),
                event.getEntityId());

        event.setEventId(eventId);
    }

    public void deleteEventById(Long eventId) {
        delete(DELETE_BY_ID_QUERY, eventId);
    }

    public void deleteAllEventsByUser(Long userId) {
        delete(DELETE_ALL_EVENTS_BY_USER_QUERY, userId);
    }

    public List<Event> findEventsByUser(Long userId, int count) {
        return findMany(FIND_EVENTS_BY_USER_QUERY, userId, count);
    }
}
