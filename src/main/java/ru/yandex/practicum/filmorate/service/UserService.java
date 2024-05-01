package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbFriend;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;

    private final UserDbFriend userDbFriend;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate,
                       UserDbFriend userDbFriend) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.userDbFriend = userDbFriend;
    }

    public User addUser(User user) throws Exception {
        if (!validation(user)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        SqlRowSet userFromDb = jdbcTemplate.queryForRowSet("select * from users");
        while (userFromDb.next()) {
            if (userFromDb.getString("email").equals(user.getEmail()) ||
                    userFromDb.getString("login").equals(user.getLogin())) {
                log.info("Добавление через POST-запрос уже имеющегося объекта");
                throw new AlreadyExistException("Данный пользователь уже добавлен");
            }
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
        List<User> usersFromDb = new ArrayList<>();
        SqlRowSet addUser = jdbcTemplate.queryForRowSet("select * from users");
        while (addUser.next()) {
            usersFromDb.add(userFromDbBuild(addUser));
        }
        return usersFromDb;
    }

    public void addFriend(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);
        userDbFriend.addFriend(userId, requestedUserId);
    }


    public String deleteFromFriends(Integer userId, Integer requestedUserId) {
        throwException(userId, requestedUserId);
        return userDbFriend.deleteFromFriends(userId, requestedUserId);
    }

    public List<User> getUserFriends(Integer userId) {
        if (!jdbcTemplate.queryForRowSet("select user_id from users where user_id = ?", userId).next()){
            log.info("Пользователь с данным id не найден");
            throw new UserNotFoundException("Пользователь с данным id не найден");
        }
        return userDbFriend.getUserFriends(userId);
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
        if (!jdbcTemplate.queryForRowSet("select user_id from users where user_id = ?", userId).next()
                || !jdbcTemplate.queryForRowSet("select user_id from users where user_id = ?", requestedUserId).next()) {
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

    private User userFromDbBuild(SqlRowSet sqlRowSet) {
        User returnedUser = new User(sqlRowSet.getString("email"),
                sqlRowSet.getString("login"),
                sqlRowSet.getString("birthday"));
        returnedUser.setId(sqlRowSet.getInt("user_id"));
        returnedUser.setName(sqlRowSet.getString("name"));

        SqlRowSet usersFriendsList = jdbcTemplate.queryForRowSet("select * from friends " +
                "inner join users on users.user_id = friends.request_sender_id where request_is_accept = true " +
                "and (request_sender_id = ? or request_recivier_id = ?)", returnedUser.getId(), returnedUser.getId());
        while (usersFriendsList.next()) {
            if (usersFriendsList.getInt("request_recivier_id") == returnedUser.getId()) {
                returnedUser.addFriend(usersFriendsList.getInt("request_sender_id"));
            }
            if (usersFriendsList.getInt("request_sender_id") == returnedUser.getId()) {
                returnedUser.addFriend(usersFriendsList.getInt("request_recivier_id"));
            }
        }
        return returnedUser;
    }
}
