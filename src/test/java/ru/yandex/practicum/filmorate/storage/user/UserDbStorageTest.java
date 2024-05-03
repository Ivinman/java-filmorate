package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        User newUser = new User("user@email.ru", "vanya123", "1990-01-01");
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);

        User savedUser = userStorage.addUser(newUser);
        SqlRowSet lastAddedUser = jdbcTemplate.queryForRowSet("select * from users order by user_id desc limit 1");
        lastAddedUser.next();
        newUser.setId(lastAddedUser.getInt("user_id"));

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);
    }
}