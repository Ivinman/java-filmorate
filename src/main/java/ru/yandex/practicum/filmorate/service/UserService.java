package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserFriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final UserFriendStorage userFriendStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, UserFriendStorage userFriendStorage) {
        this.userStorage = userStorage;
        this.userFriendStorage = userFriendStorage;
    }

    public User addUser(User user) throws Exception {
        if (!validation(user)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        Map<Integer, User> userInDb = userStorage.getUsers();
        if (userInDb.containsValue(user)) {
            log.info("Добавление через POST-запрос уже имеющегося объекта");
            throw new AlreadyExistException("Данный пользователь уже добавлен");
        }
        return userStorage.addUser(user);
    }

    public User addOrUpdateUser(User user) throws Exception {
        if (!validation(user)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        User testUser = userStorage.addOrUpdateUser(user);
        if (testUser == null) {
            throw new UserNotFoundException("Попытка обновления предварительно не добавленного объекта");
        }
        return testUser;
    }

    public List<User> getAll() {
        return new ArrayList<>(userStorage.getUsers().values());
    }

    public void addFriend(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);
        userFriendStorage.addFriend(userId, requestedUserId);
    }


    public String deleteFromFriends(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);
        return userFriendStorage.deleteFromFriends(userId, requestedUserId);
    }

    public List<User> getUserFriends(Integer userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            log.info("Пользователь с данным id не найден");
            throw new UserNotFoundException("Пользователь с данным id не найден");
        }
        return userFriendStorage.getUserFriends(userId);
    }

    public List<User> getCommonFriends(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);
        List<User> commonFriends = new ArrayList<>();
        List<User> userFriends = getUserFriends(userId);
        List<User> requestUserFriends = getUserFriends(requestedUserId);
        for (User user : userFriends) {
            if (requestUserFriends.contains(user)) {
                commonFriends.add(user);
            }
        }
        return commonFriends;
    }

    private void throwException(Integer userId, Integer requestedUserId) {
        if (userId == null || requestedUserId == null) {
            throw new IncorrectParameterException("Некорректно заданные данные пользователей");
        }
        User user = userStorage.getUser(userId);
        User requestedUser = userStorage.getUser(requestedUserId);
        if (user == null || requestedUser == null) {
            log.info("Пользователь с данным id не найден");
            throw new UserNotFoundException("Пользователь с данным id не найден");
        }
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
