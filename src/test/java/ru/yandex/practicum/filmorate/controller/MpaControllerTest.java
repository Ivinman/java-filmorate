package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaControllerTest {
    private final JdbcTemplate jdbcTemplate;
    private MpaController mpaController;

    @BeforeEach
    void createController() {
        MpaStorage mpaStorage = new MpaStorage(jdbcTemplate);
        MpaService mpaService = new MpaService(mpaStorage);
        mpaController = new MpaController(mpaService);
    }

    private Mpa getMpa(Integer id) {
        SqlRowSet mpa = jdbcTemplate.queryForRowSet("select * from mpa where mpa_id = ?", id);
        if (mpa.next()) {
            return new Mpa(mpa.getInt("mpa_id"), mpa.getString("mpa_name"));
        }
        return null;
    }

    @Test
    void getMpa() {
        assertEquals("G", getMpa(1).getName());
        assertEquals("PG", getMpa(2).getName());
        assertEquals("PG-13", getMpa(3).getName());
        assertEquals("R", getMpa(4).getName());
        assertEquals("NC-17", getMpa(5).getName());
    }

    @Test
    void getAllMpa() {
        assertEquals(5, mpaController.getAllMpa().size());
    }
}