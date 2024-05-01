package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
public class GenreDb {
    private final JdbcTemplate jdbcTemplate;

    public GenreDb(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Genre getGenre(Integer id) {
        SqlRowSet genre = jdbcTemplate.queryForRowSet("select * from genres where genre_id = ?", id);
        genre.next();
        return new Genre(genre.getInt("genre_id"), genre.getString("genre_name"));
    }
}
