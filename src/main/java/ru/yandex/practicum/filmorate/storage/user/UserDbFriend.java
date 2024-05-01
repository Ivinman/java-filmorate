package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserDbFriend {
    private final JdbcTemplate jdbcTemplate;

    public UserDbFriend(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(Integer userId, Integer requestedUserId) {
        SqlRowSet alreadyFriend = jdbcTemplate.queryForRowSet("select * from friends " +
                "where request_is_accept = true " +
                "and ((request_sender_id = ? and request_recivier_id = ?) " +
                "or (request_sender_id = ? and request_recivier_id = ?))", userId, requestedUserId, requestedUserId, userId);
        if (alreadyFriend.next()) {
            SqlRowSet name = jdbcTemplate.queryForRowSet("select name from users where user_id = ?", requestedUserId);
            name.next();
            log.info("Пользователь {} уже находится в списке друзей", name.getString("name"));
            throw new AlreadyExistException("Данный пользователь уже находится в списке друзей");
        }
        SqlRowSet notFriend = jdbcTemplate.queryForRowSet("select * from friends " +
                "where request_is_accept = false " +
                "and request_sender_id = ? and request_recivier_id = ?", requestedUserId, userId);
        if (notFriend.next()) {
            jdbcTemplate.update("update friends set request_is_accept = true");

            SqlRowSet userRecivName = jdbcTemplate.queryForRowSet("select name from users where user_id = ?",
                    requestedUserId);
            userRecivName.next();
            String userRecivNameStr = userRecivName.getString("name");
            SqlRowSet userSendName = jdbcTemplate.queryForRowSet("select name from users where user_id = ?",
                    userId);
            userSendName.next();
            String userSendNameStr = userSendName.getString("name");

            log.info("Пользователь {} добавлен в список друзей {}", userRecivNameStr, userSendNameStr);
        } else {
            SqlRowSet friendRequest = jdbcTemplate.queryForRowSet("select * from friends " +
                    "where request_is_accept = false " +
                    "and request_sender_id = ? and request_recivier_id = ?", userId, requestedUserId);
            if (!friendRequest.next()) {
                jdbcTemplate.update("insert into friends (request_sender_id, request_recivier_id) " +
                        "values (?, ?)", userId, requestedUserId);
            }
        }
    }

    public String deleteFromFriends(Integer userId, Integer requestedUserId) {

        jdbcTemplate.update("delete from friends where " +
                        "request_sender_id = ? and request_recivier_id = ?",
                userId, requestedUserId);

        SqlRowSet userRecivName = jdbcTemplate.queryForRowSet("select name from users where user_id = ?",
                requestedUserId);
        userRecivName.next();
        String userRecivNameStr = userRecivName.getString("name");
        SqlRowSet userSendName = jdbcTemplate.queryForRowSet("select name from users where user_id = ?",
                userId);
        userSendName.next();
        String userSendNameStr = userSendName.getString("name");

        log.info("Пользователь {} удален из списка друзей {}", userRecivNameStr, userSendNameStr);

        return userSendNameStr + " удалил из друзей: " + userRecivNameStr;
    }

    public List<User> getUserFriends(Integer userId) {
        List<User> userFriends = new ArrayList<>();
        SqlRowSet userFriendsDb = jdbcTemplate.queryForRowSet("select * from friends " +
                "where request_sender_id = ?", userId);
        while (userFriendsDb.next()) {
            SqlRowSet userFromDb = jdbcTemplate.queryForRowSet("select * from users " +
                    "where user_id = ?", userFriendsDb.getInt("request_recivier_id"));
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
            SqlRowSet userFriendRecivList = jdbcTemplate.queryForRowSet("select * from friends " +
                    "where request_recivier_id = ?", returnedUser.getId());
            while (userFriendRecivList.next()) {
                returnedUser.addFriend(userFriendRecivList.getInt("request_sender_id"));
            }
            userFriends.add(returnedUser);
        }
        return userFriends;
    }
}
