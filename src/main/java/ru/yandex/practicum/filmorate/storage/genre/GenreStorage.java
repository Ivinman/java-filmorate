package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

@Component
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Genre getGenre(Integer id) {
        SqlRowSet genre = jdbcTemplate.queryForRowSet("select * from genres where genre_id = ?", id);
        if (genre.next()) {
            return new Genre(genre.getInt("genre_id"), genre.getString("genre_name"));
        }
        return null;
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
