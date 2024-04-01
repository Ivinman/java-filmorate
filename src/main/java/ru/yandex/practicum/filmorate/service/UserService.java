package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) throws Exception {
        if (!validation(user)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        if (userStorage.getUsers().containsValue(user)) {
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
        if (userStorage.getUsers().get(userId).getFriendsId().contains(requestedUserId)) {
            log.info("Пользователь {} уже находится в списке друзей",
                    userStorage.getUsers().get(requestedUserId).getName());
            throw new AlreadyExistException("Данный пользователь уже находится в списке друзей");
        }

        userStorage.getUsers().get(userId).addFriend(requestedUserId);
        userStorage.getUsers().get(requestedUserId).addFriend(userId);

        log.info("Пользователь {} добавлен в список друзей {}",
                userStorage.getUsers().get(requestedUserId).getName(),
                userStorage.getUsers().get(userId).getName());
    }

    public String deleteFromFriends(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);

        userStorage.getUsers().get(userId).deleteFromFriends(requestedUserId);
        userStorage.getUsers().get(requestedUserId).deleteFromFriends(userId);

        log.info("Пользователь {} удален из списка друзей {}",
                userStorage.getUsers().get(requestedUserId).getName(),
                userStorage.getUsers().get(userId).getName());

        return userStorage.getUsers().get(userId).getName() +
                " удалил из друзей: " +
                userStorage.getUsers().get(requestedUserId).getName();
    }

    public List<User> getUserFriends(Integer userId) {
        if (!userStorage.getUsers().containsKey(userId)) {
            log.info("Пользователь с данным id не найден");
            throw new UserNotFoundException("Пользователь с данным id не найден");
        }
        List<User> userFriends = new ArrayList<>();
        for (User userInStorage : getAll()) {
            for (Integer id : userStorage.getUsers().get(userId).getFriendsId()) {
                if (userInStorage.getId().equals(id)) {
                    userFriends.add(userStorage.getUsers().get(id));
                }
            }
        }
        return userFriends;
    }

    public List<User> getCommonFriends(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);
        List<User> commonFriends = new ArrayList<>();
        for (Integer userFriendId : userStorage.getUsers().get(userId).getFriendsId()) {
            for (Integer requestedUserFriendId : userStorage.getUsers().get(requestedUserId).getFriendsId()) {
                if (userFriendId.equals(requestedUserFriendId)) {
                    commonFriends.add(userStorage.getUsers().get(requestedUserFriendId));
                    break;
                }
            }
        }
        return commonFriends;
    }

    private void throwException(Integer userId, Integer requestedUserId) {
        if (userId == null || requestedUserId == null) {
            throw new IncorrectParameterException("Некорректно заданные данные пользователей");
        }
        if (!userStorage.getUsers().containsKey(userId)
                || !userStorage.getUsers().containsKey(requestedUserId)) {
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
