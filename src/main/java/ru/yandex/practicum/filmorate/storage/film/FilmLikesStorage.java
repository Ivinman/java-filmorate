package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

@Component
public class FilmLikesStorage {
    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    public FilmLikesStorage(@Qualifier("filmDbStorage") FilmStorage filmStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean checkLike(Integer filmId, Integer userId) {
        return jdbcTemplate.queryForRowSet("select * from film_likes " +
                "where user_id = ? and film_id = ?", userId, filmId).next();
    }

    public Film addLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("insert into film_likes (film_id, user_id) " +
                "values (?, ?)", filmId, userId);
        SqlRowSet filmInDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        filmInDb.next();
        jdbcTemplate.update("update films set likes = ? where film_id = ?",
                (filmInDb.getInt("likes") + 1), filmId);
        return filmStorage.getFilm(filmId);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("delete from film_likes where film_id = ?", filmId);
        SqlRowSet filmInDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        filmInDb.next();
        jdbcTemplate.update("update films set likes = ? where film_id = ?",
                (filmInDb.getInt("likes") - 1), filmId);
        return filmStorage.getFilm(filmId);
    }
}
