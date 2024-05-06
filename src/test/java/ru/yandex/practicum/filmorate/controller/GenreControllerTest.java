package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreControllerTest {
    private final JdbcTemplate jdbcTemplate;
    private GenreController genreController;

    @BeforeEach
    void createController() {
        GenreStorage genreStorage = new GenreStorage(jdbcTemplate);
        GenreService genreService = new GenreService(genreStorage);
        genreController = new GenreController(genreService);
    }

    private Genre getGenre(Integer id) {
        SqlRowSet genreDb = jdbcTemplate.queryForRowSet("select * from genres where genre_id = ?", id);
        if (genreDb.next()) {
            return new Genre(genreDb.getInt("genre_id"), genreDb.getString("genre_name"));
        }
        return null;
    }

    @Test
    void getGenre() {
        assertEquals("Комедия", getGenre(1).getName());
        assertEquals("Драма", getGenre(2).getName());
        assertEquals("Мультфильм", getGenre(3).getName());
        assertEquals("Триллер", getGenre(4).getName());
        assertEquals("Документальный", getGenre(5).getName());
        assertEquals("Боевик", getGenre(6).getName());
    }

    @Test
    void getAll() {
        assertEquals(6, genreController.getAll().size());
    }
}