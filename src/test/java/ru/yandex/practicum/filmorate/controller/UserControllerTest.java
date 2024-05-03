package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserFriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private UserController userController;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createController() {
        UserStorage userStorage = new UserDbStorage(jdbcTemplate);
        UserFriendStorage userFriendStorage = new UserFriendStorage(userStorage, jdbcTemplate);
        UserService userService = new UserService(userStorage, userFriendStorage);
        userController = new UserController(userService);
    }

    private User createUser() {
        return new User("email@", "login", "1997-12-12");
    }

    private ValidationException getThrown(User user) {
        return assertThrows(ValidationException.class,
                () -> userController.addUser(user));
    }

    private Integer getUserId() {
        SqlRowSet userIdFromDb = jdbcTemplate.queryForRowSet("select user_id from users order by user_id desc limit 1");
        userIdFromDb.next();
        return userIdFromDb.getInt("user_id");
    }

    @Test
    void addUser() throws Exception {
        User user = createUser();
        user.setId(1);
        userController.addUser(user);

        assertEquals(user, userController.getAll().get(0));
        assertEquals(user.getLogin(), userController.getAll().get(0).getName());

        User user1 = new User("", "login", "2000-01-01");
        User user2 = new User(null, "login", "2000-01-01");
        User user3 = new User("email@", " ", "2000-01-01");
        User user4 = new User("email@", "login", "2030-01-01");

        assertEquals("Ошибка валидации", getThrown(user1).getMessage());
        assertEquals("Ошибка валидации", getThrown(user2).getMessage());
        assertEquals("Ошибка валидации", getThrown(user3).getMessage());
        assertEquals("Ошибка валидации", getThrown(user4).getMessage());

        User user5 = createUser();
        AlreadyExistException thrown = assertThrows(AlreadyExistException.class,
                () -> userController.addUser(user5));

        assertEquals("Данный пользователь уже добавлен", thrown.getMessage());
    }

    @Test
    void addOrUpdateUser() throws Exception {
        User user1 = createUser();
        user1.setName("name");
        userController.addOrUpdateUser(user1);
        user1.setId(getUserId());

        assertTrue(userController.getAll().contains(user1));
        assertEquals(1, userController.getAll().size());

        user1.setName("New Name");
        userController.addOrUpdateUser(user1);

        assertEquals(1, userController.getAll().size());
        assertTrue(userController.getAll().contains(user1));
        assertEquals(user1.getName(), userController.getAll().get(0).getName());
    }

    @Test
    void getAll() throws Exception {
        User user1 = createUser();
        User user2 = new User("@mail", "lofin", "2000-02-02");
        User user3 = user2;
        user3.setName("New Name");
        userController.addUser(user1);
        user1.setId(getUserId());
        userController.addOrUpdateUser(user2);
        user2.setId(getUserId());
        userController.addOrUpdateUser(user3);
        user3.setId(getUserId());

        assertEquals(2, userController.getAll().size());
    }

    @Test
    void addFriend() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");

        IncorrectParameterException incorrectParameterException = assertThrows(IncorrectParameterException.class,
                () -> userController.addFriend(null, null));
        assertEquals("Некорректно заданные данные пользователей", incorrectParameterException.getMessage());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> userController.addFriend(25, 27));
        assertEquals("Пользователь с данным id не найден", userNotFoundException.getMessage());

        userController.addUser(user);
        user.setId(getUserId());
        userController.addUser(user1);
        user1.setId(getUserId());
        userController.addFriend(user.getId(), user1.getId());

        assertEquals(1, userController.getUserFriends(user.getId()).size());
        assertEquals(0, userController.getUserFriends(user1.getId()).size());
    }

    @Test
    void deleteFriend() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");

        userController.addUser(user);
        user.setId(getUserId());
        userController.addUser(user1);
        user1.setId(getUserId());
        userController.addFriend(user.getId(), user1.getId());
        userController.deleteFromFriends(user.getId(), user1.getId());

        assertEquals(0, userController.getUserFriends(user.getId()).size());
        assertEquals(0, userController.getUserFriends(user1.getId()).size());
    }

    @Test
    void getUserFriends() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");
        User user2 = new User("Anotheremail@", "Anotherlogin", "1999-12-12");

        userController.addUser(user);
        user.setId(getUserId());
        userController.addUser(user1);
        user1.setId(getUserId());
        userController.addUser(user2);
        user2.setId(getUserId());
        userController.addFriend(user.getId(), user1.getId());
        userController.addFriend(user.getId(), user2.getId());

        List<User> userFriends = userController.getUserFriends(user.getId());
        assertEquals(2, userFriends.size());
    }

    @Test
    void getCommonFriends() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");
        User user2 = new User("Anotheremail@", "Anotherlogin", "1999-12-12");
        User user3 = new User("Thirdemail@", "Thirdlogin", "2000-12-12");

        userController.addUser(user);
        user.setId(getUserId());
        userController.addUser(user1);
        user1.setId(getUserId());
        userController.addUser(user2);
        user2.setId(getUserId());
        userController.addUser(user3);
        user3.setId(getUserId());
        userController.addFriend(user.getId(), user1.getId());
        userController.addFriend(user.getId(), user2.getId());
        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

        List<User> commonFriends = userController.getCommonFriends(user.getId(), user1.getId());
        assertEquals(1, commonFriends.size());
    }
}