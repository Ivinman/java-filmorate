package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;


    private User createUser() {
        return new User("email@", "login", "1997-12-12");
    }

    @BeforeEach
    void createController() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
    }

    private ValidationException getThrown(User user) {
        return assertThrows(ValidationException.class,
                () -> userController.addUser(user));
    }

    @Test
    void addUser() throws Exception {
        User user = createUser();
        System.out.println(user.getId());
        userController.addUser(user);
        System.out.println(user.getId());


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
        userController.addOrUpdateUser(user2);
        userController.addOrUpdateUser(user3);

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
                () -> userController.addFriend(1, 2));
        assertEquals("Пользователь с данным id не найден", userNotFoundException.getMessage());

        userController.addUser(user);
        userController.addUser(user1);
        userController.addFriend(user.getId(), user1.getId());

        assertEquals(1, user.getFriendsId().size());
        assertTrue(user.getFriendsId().contains(user1.getId()));
        assertTrue(user1.getFriendsId().contains(user.getId()));

        AlreadyExistException alreadyExistException = assertThrows(AlreadyExistException.class,
                () -> userController.addFriend(user.getId(), user1.getId()));
        assertEquals("Данный пользователь уже находится в списке друзей", alreadyExistException.getMessage());
    }

    @Test
    void deleteFriend() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");

        userController.addUser(user);
        userController.addUser(user1);
        userController.addFriend(user.getId(), user1.getId());
        userController.deleteFromFriends(user.getId(), user1.getId());

        assertEquals(0, user.getFriendsId().size());
        assertEquals(0, user1.getFriendsId().size());
    }

    @Test
    void getUserFriends() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");
        User user2 = new User("Anotheremail@", "Anotherlogin", "1999-12-12");

        userController.addUser(user);
        userController.addUser(user1);
        userController.addUser(user2);
        userController.addFriend(user.getId(), user1.getId());
        userController.addFriend(user.getId(), user2.getId());

        List<User> userFriends = userController.getUserFriends(user.getId());
        assertEquals(2, userFriends.size());
        assertTrue(userFriends.contains(user1));
        assertTrue(userFriends.contains(user2));
    }

    @Test
    void getCommonFriends() throws Exception {
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");
        User user2 = new User("Anotheremail@", "Anotherlogin", "1999-12-12");
        User user3 = new User("Thirdemail@", "Thirdlogin", "2000-12-12");

        userController.addUser(user);
        userController.addUser(user1);
        userController.addUser(user2);
        userController.addUser(user3);
        userController.addFriend(user.getId(), user1.getId());
        userController.addFriend(user.getId(), user2.getId());
        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

        List<User> commonFriends = userController.getCommonFriends(user.getId(), user1.getId());
        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(user2));
    }
}