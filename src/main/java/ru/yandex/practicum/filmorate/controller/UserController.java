package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    @PostMapping
    public User addUser(@RequestBody User user) throws Exception {
        if (!validation(user)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        if (users.containsValue(user)) {
            log.info("Добавление через POST-запрос уже имеющегося объекта");
            throw new AlreadyExistException("Данный пользователь уже добавлен");
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        id++;
        user.setId(id);
        users.put(id, user);
        log.info("Новый пользователь добавлен в общий список");
        return user;
    }

    @PutMapping
    public User addOrUpdateUser(@RequestBody User user) throws Exception {
        if (!validation(user)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        if (user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getId() == 0) {
            id++;
            user.setId(id);
            log.info("Новый пользователь добавлен в общий список");
            users.put(user.getId(), user);
            return user;
        } else {
            if (users.containsKey(user.getId())) {
                log.info("Был обновлён пользователь с почтой: {}", user.getEmail());
                users.put(user.getId(), user);
                return user;
            } else {
                throw new ValidationException("Попытка обновления предварительно не добавленного объекта");
            }
        }
    }

    @GetMapping
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    private boolean validation(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return user.getEmail() != null
                && !user.getEmail().isBlank()
                && !user.getEmail().isEmpty()
                && user.getEmail().contains("@")
                && !user.getLogin().isBlank()
                && !user.getLogin().isEmpty()
                && !user.getLogin().contains(" ")
                && !LocalDate.parse(user.getBirthday(), formatter).isAfter(LocalDate.now());
    }
}
