package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User addUser(@RequestBody User user) throws Exception {
        return userService.addUser(user);
    }

    @PutMapping
    public User addOrUpdateUser(@RequestBody User user) throws Exception {
        return userService.addOrUpdateUser(user);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable (required = false) Integer id,
                          @PathVariable (required = false) Integer friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public String deleteFromFriends(@PathVariable (required = false) Integer id,
                                    @PathVariable (required = false) Integer friendId) {
        return userService.deleteFromFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable (required = false) Integer id) {
        return userService.getUserFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable (required = false) Integer id,
                                       @PathVariable (required = false) Integer otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
