package ru.yandex.practicum.filmorate.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDb;

import java.util.ArrayList;
import java.util.List;

@Service
public class MpaService {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDb mpaDb;

    public MpaService(MpaDb mpaDb, JdbcTemplate jdbcTemplate) {
        this.mpaDb = mpaDb;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mpa getMpa(Integer id) {
        SqlRowSet valid = jdbcTemplate.queryForRowSet("select * from mpa where mpa_id = ?", id);
        if (!valid.next()) {
            throw new NotFoundException("Не найден идентификатоор");
        }
        return mpaDb.getMpa(id);
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
