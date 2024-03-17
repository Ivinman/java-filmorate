package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;


    private User createUser() {
        return new User("email@", "login", "1997-12-12");
    }

    @BeforeEach
    void createController() {
        userController = new UserController();
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
}