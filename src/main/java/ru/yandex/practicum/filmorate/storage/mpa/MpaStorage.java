package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;

@Component
public class MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mpa getMpa(Integer id) {
        SqlRowSet mpaFromDb = jdbcTemplate.queryForRowSet("select * from mpa " +
                "where mpa_id = ?", id);
        if (mpaFromDb.next()) {
            return new Mpa(mpaFromDb.getInt("mpa_id"), mpaFromDb.getString("mpa_name"));
        }
        return null;
    }

    public List<Mpa> getAllMpa() {
        List<Mpa> allMpa = new ArrayList<>();
        SqlRowSet getAll = jdbcTemplate.queryForRowSet("select * from mpa");
        while (getAll.next()) {
            Mpa mpa = new Mpa(getAll.getInt("mpa_id"), getAll.getString("mpa_name"));
            allMpa.add(mpa);
        }
        return allMpa;
    }
}
