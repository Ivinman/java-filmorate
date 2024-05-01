package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return addToDB(user);
    }

    public User addOrUpdateUser(User user) {
        if (user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getId() == null) {
            return addToDB(user);
        } else {
            SqlRowSet addUser = jdbcTemplate.queryForRowSet("select * from users where user_id = ?", user.getId());
            if (addUser.next()) {
                jdbcTemplate.update("update users set email = ?, login = ?," +
                        "name = ?, birthday = ? where user_id = ?", user.getEmail(),
                        user.getLogin(), user.getName(),
                        user.getBirthday(),user.getId());
                log.info("Был обновлён пользователь с почтой: {}", user.getEmail());
                return userFromDB(user);
            } else {
                return null;
            }
        }
    }


    private User addToDB(User user) {
        jdbcTemplate.update("insert into users (email, login, name, birthday) " +
                "values (?, ?, ?, ?)", user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        log.info("Пользователь {} добавлен", user.getName());
        return userFromDB(user);
    }

    private User userFromDB(User user) {
        SqlRowSet userFromDb = jdbcTemplate.queryForRowSet("select * from users where name = ?", user.getName());
        userFromDb.next();
        User returnedUser = new User(userFromDb.getString("email"),
                userFromDb.getString("login"),
                userFromDb.getString("birthday"));
        returnedUser.setId(userFromDb.getInt("user_id"));
        returnedUser.setName(userFromDb.getString("name"));

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
