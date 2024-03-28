package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
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
        for (User userInStorage : userStorage.getAll()) {
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
}
