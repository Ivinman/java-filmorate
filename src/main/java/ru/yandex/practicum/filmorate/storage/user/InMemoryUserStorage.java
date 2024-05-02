package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    @Getter
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        id++;
        user.setId(id);
        users.put(id, user);
        log.info("Новый пользователь добавлен в общий список");
        return user;
    }

    public User addOrUpdateUser(User user) {
        if (user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getId() == null) {
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
                return null;
            }
        }
    }

    public User getUser(Integer id) {
        return users.get(id);
    }
}
