package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NoContentException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id);
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

        addUserToFriendList(secondId, firstId);
        log.trace("firstId added to friends of secondId");

        return Map.of("firstId", firstId,
                "secondId", secondId);
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

        deleteFromFriendList(secondId, firstId);
        log.trace("firstId removed from secondId friends");

        return Map.of("firstId", firstId,
                "secondId", secondId);
    }

    public Collection<User> findAllFriends(Long id) {
        log.debug("Starting findAllFriends, id = {}", id);

        return findById(id)
                .getFriends()
                .stream()
                .map(userStorage::findById)
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
}