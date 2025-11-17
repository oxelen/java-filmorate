package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.util.ServiceUtils;
import ru.yandex.practicum.filmorate.storage.dal.FriendsRepository;
import ru.yandex.practicum.filmorate.storage.dal.LikesRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserValidator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendsRepository friendsRepository;
    private final LikesRepository likesRepository;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendsRepository friendsRepository,
                       LikesRepository likesRepository) {
        this.userStorage = userStorage;
        this.friendsRepository = friendsRepository;
        this.likesRepository = likesRepository;
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
        return userStorage.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public Map<String, Long> addFriend(Long firstId, Long secondId) {
        log.debug("Starting addFriend, firstId = {}, secondId = {}", firstId, secondId);

        User user = findById(firstId);
        User friend = findById(secondId);

        addUserToFriendList(user.getId(), friend.getId());


        friendsRepository.create(user.getId(), friend.getId());
        log.info("User with id: {} has been added to friends of user with id: {}", friend.getId(), user.getId());

        return Map.of("firstId", firstId, "secondId", secondId);
    }

    public Map<String, Long> deleteFriend(Long firstId, Long secondId) {
        log.debug("Starting deleteFriend, firstId = {}, secondId = {}", firstId, secondId);

        User user = findById(firstId);
        User friendToRemove = findById(secondId);

        deleteFromFriendList(user.getId(), friendToRemove.getId());

        friendsRepository.delete(user.getId(), friendToRemove.getId());
        log.info("User with id: {} has been removed from friends of user with id: {}",
                friendToRemove.getId(), user.getId());

        return Map.of("firstId", firstId, "secondId", secondId);
    }

    public Collection<User> findAllFriends(Long id) {
        log.debug("Starting findAllFriends, id = {}", id);

        return findById(id).getFriends().stream().map(this::findById).collect(Collectors.toList());
    }

    public Collection<User> findCommonFriends(Long firstId, Long secondId) {
        log.debug("Starting findCommonFriends, firstId = {}, secondId = {}", firstId, secondId);

        Set<Long> firstFriends = findById(firstId).getFriends();
        Set<Long> secondFriends = findById(secondId).getFriends();

        return firstFriends.stream().filter(secondFriends::contains).map(this::findById).collect(Collectors.toList());
    }

    private void addUserToFriendList(Long userId, Long addedUserId) {
        log.debug("Starting addUserToFriendList userId = {}, addedUserId = {}", userId, addedUserId);
        Set<Long> friends = findById(userId).getFriends();

        if (friends.contains(addedUserId)) {
            log.warn("User with id = {} is already friend of User with id = {}", addedUserId, userId);
            throw new DuplicatedDataException("Пользователи с id = " + userId + ", " + addedUserId + " уже друзья");
        }
        friends.add(addedUserId);
        log.info("User with id = {} added to friend list of User with id = {}", addedUserId, userId);
    }

    private void deleteFromFriendList(Long userId, Long deletedUserId) {
        log.debug("Starting deleteFromFriendList, userId = {}, deletedUserId = {}", userId, deletedUserId);
        Set<Long> friends = findById(userId).getFriends();

        if (friends.isEmpty()) {
            log.warn("List friends of User with id = {} is null or empty", userId);
            throw new NoContentException("Список друзей пользователя с id = " + userId + " пуст.");
        }

        if (friends.contains(deletedUserId)) {
            log.info("User with id = {} deleted from list of User with id = {}", deletedUserId, userId);
            friends.remove(deletedUserId);
        } else {
            log.warn("User with id = {} not friend of User with id = {}", deletedUserId, userId);
            throw new ConditionsNotMetException("Пользователи с id = " + userId + ", " + deletedUserId + " не друзья");
        }
    }

    @Transactional
    public void deleteUserById(Long userId) {
        log.debug("Starting deleteUserById, userId = {}", userId);
        User user = findById(userId);

        ServiceUtils.safeDelete(() -> friendsRepository.deleteAllByUserId(user.getId()),
                "Failed to delete friend relations for user with id = ", userId);
        log.debug("Deleted friend relations for userId = {}", userId);

        ServiceUtils.safeDelete(() -> likesRepository.deleteAllByUserId(user.getId()),
                "Failed to delete likes for user with id = ", userId);
        log.debug("Deleted likes for userId = {}", userId);


        if (!userStorage.deleteById(user.getId())) {
            log.error("Failed to remove user with id = {}", userId);
            throw new InternalServerException("Не удалось удалить пользователя с id = " + userId);
        }

        log.info("User with id = {} has been successfully deleted", userId);
    }
}