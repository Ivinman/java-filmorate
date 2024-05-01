package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class MpaDb {
    private final JdbcTemplate jdbcTemplate;

    public MpaDb(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mpa getMpa(Integer id) {
        SqlRowSet mpaFromDb = jdbcTemplate.queryForRowSet("select * from mpa " +
                "where mpa_id = ?", id);
        mpaFromDb.next();
        return new Mpa(mpaFromDb.getInt("mpa_id"), mpaFromDb.getString("mpa_name"));
    }
}
