package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {
    Map<Integer, User> getUsers();

    User addUser(User user) throws Exception;

    User addOrUpdateUser(User user) throws Exception;

    List<User> getAll();
}
