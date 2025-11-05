package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Map;

import static ru.yandex.practicum.filmorate.controller.PathVariableValidator.checkIds;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Running POST method: create user");

        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Running PUT method: update user");

        return userService.update(newUser);
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Running GET method: get all users");

        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable("id") Long userId) {
        log.info("Running GET method: find user by id");

        checkIds(userId);
        return userService.findById(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Map<String, Long> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Running PUT method: addFriend");

        checkIds(id, friendId);
        return userService.addFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> findAllFriends(@PathVariable("id") Long userId) {
        log.info("Running GET method: find all friends (id = {})", userId);

        checkIds(userId);
        return userService.findAllFriends(userId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Map<String, Long> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Running DELETE method: delete friend");

        checkIds(id, friendId);
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Starting GET method: find CommonFriends");

        checkIds(id, otherId);
        return userService.findCommonFriends(id, otherId);
    }
}
