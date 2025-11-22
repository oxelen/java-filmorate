package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Event.Event;
import ru.yandex.practicum.filmorate.model.Event.EventOperation;
import ru.yandex.practicum.filmorate.model.Event.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.util.ServiceUtils;
import ru.yandex.practicum.filmorate.storage.dal.EventsRepository;
import ru.yandex.practicum.filmorate.storage.dal.FriendsRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserValidator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.of;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendsRepository friendsRepository;
    private final FilmService filmService;
    private final EventsRepository eventsRepository;


    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            FriendsRepository friendsRepository,
            FilmService filmService,
            EventsRepository eventsRepository) {
        this.userStorage = userStorage;
        this.friendsRepository = friendsRepository;
        this.filmService = filmService;
        this.eventsRepository = eventsRepository;
    }

    public User create(User user) {
        UserValidator.validateUser(user);
        return userStorage.create(user);
    }

    public User update(User newUser) {
        UserValidator.validateUser(newUser);
        return userStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не нашелся"));
    }

    public Map<String, Long> addFriend(Long firstId, Long secondId) {
        log.debug("Starting addFriend, firstId = {}, secondId = {}", firstId, secondId);
        if (!userStorage.containsUser(firstId)) {
            throw new NotFoundException("Пользователь с id = " + firstId + " не найден.");
        }
        if (!userStorage.containsUser(secondId)) {
            throw new NotFoundException("Пользователь с id = " + secondId + " не найден");
        }

        addUserToFriendList(firstId, secondId);
        log.trace("secondId added to friends of firstId");

        friendsRepository.create(firstId, secondId);

        Event event = ServiceUtils.createEvent(firstId, EventType.FRIEND, EventOperation.ADD, secondId);
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

        return of("firstId", firstId, "secondId", secondId);
    }

    public Map<String, Long> deleteFriend(Long firstId, Long secondId) {
        log.debug("Starting deleteFriend, firstId = {}, secondId = {}", firstId, secondId);

        if (!userStorage.containsUser(firstId)) {
            throw new NotFoundException("Пользователь с id = " + firstId + " не найден.");
        }
        if (!userStorage.containsUser(secondId)) {
            throw new NotFoundException("Пользователь с id = " + secondId + " не найден");
        }

        deleteFromFriendList(firstId, secondId);
        log.trace("secondId removed from firstId friends");
        friendsRepository.delete(firstId, secondId);

        Event event = ServiceUtils.createEvent(firstId, EventType.FRIEND, EventOperation.REMOVE, secondId);
        eventsRepository.createEvent(event);
        log.debug("Event created: {}", event);

        return of("firstId", firstId, "secondId", secondId);
    }

    public Collection<User> findAllFriends(Long id) {
        log.debug("Starting findAllFriends, id = {}", id);
        return findById(id).getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> findCommonFriends(Long firstId, Long secondId) {
        log.debug("Starting findCommonFriends, firstId = {}, secondId = {}", firstId, secondId);
        Set<Long> firstFriends = findById(firstId).getFriends();
        Set<Long> secondFriends = findById(secondId).getFriends();

        return firstFriends.stream()
                .filter(secondFriends::contains)
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public boolean isFriends(Long firstId, Long secId) {
        Set<Long> firstQueries = findById(firstId).getFriends();
        Set<Long> secQueries = findById(secId).getFriends();
        return firstQueries.contains(secId) && secQueries.contains(firstId);
    }

    private void addUserToFriendList(Long userId, Long addedUserId) {
        log.debug("Starting addUserToFriendList userId = {}, addedUserId = {}", userId, addedUserId);
        Set<Long> friends = findById(userId).getFriends();

        if (friends.contains(addedUserId)) {
            log.warn("User with id = {} is already friend of User with id = {}", addedUserId, userId);
            throw new DuplicatedDataException("Пользователи с id = " + userId + ", " + addedUserId + " уже друзья");
        }
        log.trace("User with id = {} added to friend list of User with id = {}", addedUserId, userId);
        friends.add(addedUserId);
    }

    private void deleteFromFriendList(Long userId, Long deletedUserId) {
        log.debug("Starting deleteFromFriendList, userId = {}, deletedUserId = {}", userId, deletedUserId);
        Set<Long> friends = findById(userId).getFriends();

        if (friends.isEmpty()) {
            log.warn("List friends of User with id = {} is null or empty", userId);
            throw new NoContentException("Список друзей пользователя с id = " + userId + " пуст.");
        }

        if (friends.contains(deletedUserId)) {
            log.trace("User with id = {} deleted from list of User with id = {}", deletedUserId, userId);
            friends.remove(deletedUserId);
        } else {
            log.warn("User with id = {} not friend of User with id = {}", deletedUserId, userId);
            throw new ConditionsNotMetException("Пользователи с id = " + userId + ", " + deletedUserId + " не друзья");
        }
    }

    public List<Film> getRecommendations(Long userId) {
        log.debug("Starting getRecommendations for user ID: {}", userId);


        // Проверяем существование пользователя
        User user = findById(userId);

        List<Film> recommendations = filmService.getRecommendationFilms(userId);

        log.info("Retrieved {} recommended films for user ID: {}", recommendations.size(), userId);
        return recommendations;
    }

    public List<Event> getUserFeed(Long userId, int count) {
        log.debug("Starting searching userFeed for userId = {}, count = {}", userId, count);
        User user = findById(userId);
        try {
            List<Event> eventsByUser = eventsRepository.findEventsByUser(user.getId(), count);
            log.info("eventsByUser with id = {} has been found", user.getId());
            return eventsByUser;
        } catch (Exception e) {
            throw new InternalServerException("Can't find events for user with id = " + user.getId());
        }
    }

}
