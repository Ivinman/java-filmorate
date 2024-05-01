package ru.yandex.practicum.filmorate.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDb;

import java.util.ArrayList;
import java.util.List;

@Service
public class GenreService {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDb genreDb;

    public GenreService(GenreDb genreDb, JdbcTemplate jdbcTemplate) {
        this.genreDb = genreDb;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Genre getGenre(Integer id) {
        SqlRowSet valid = jdbcTemplate.queryForRowSet("select * from genres where genre_id = ?", id);
        if (!valid.next()) {
            throw new NotFoundException("Не найден идентификатоор");
        }
        return genreDb.getGenre(id);
    }

    public List<Genre> getAllGenre() {
        List<Genre> allGenre = new ArrayList<>();
        SqlRowSet allGenreDb = jdbcTemplate.queryForRowSet("select * from genres");
        while (allGenreDb.next()) {
            allGenre.add(new Genre(allGenreDb.getInt("genre_id"), allGenreDb.getString("genre_name")));
        }
        return allGenre;
    }
}
