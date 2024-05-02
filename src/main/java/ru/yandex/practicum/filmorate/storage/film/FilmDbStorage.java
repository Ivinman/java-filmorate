package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film addFilm(Film film) {
        return addToDB(film);
    }

    public Film addOrUpdateFilm(Film film) {
        if (film.getId() == null) {
            return addToDB(film);
        } else {
            SqlRowSet addFilm = jdbcTemplate.queryForRowSet("select * from films where film_id = ?", film.getId());
            if (addFilm.next()) {
                jdbcTemplate.update("update films set name = ?, description = ?, " +
                                "release_date = ?, duration = ? where film_id = ?",
                        film.getName(), film.getDescription(),
                        film.getReleaseDate(), film.getDuration(),
                        film.getId());
                log.info("Был обновлён фильм под названием: {}", film.getName());
                return filmFromDB(film);
            } else {
                return null;
            }
        }
    }

    public Map<Integer, Film> getFilms() {
        Map<Integer, Film> filmsFromDb = new HashMap<>();
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films");
        while (filmFromDb.next()) {
            Film returnedFilm = filmBuild(filmFromDb);
            filmsFromDb.put(returnedFilm.getId(), returnedFilm);
        }
        return filmsFromDb;
    }

    public Film getFilm(Integer filmId) {
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        if (filmFromDb.next()) {
            return filmBuild(filmFromDb);
        }
        return null;
    }

    private Film addToDB(Film film) {
        jdbcTemplate.update("insert into films (name, description, mpa, release_date, duration, likes) " +
                        "values (?, ?, ?, ?, ?, ?)", film.getName(), film.getDescription(), film.getMpa().getId(),
                film.getReleaseDate(), film.getDuration(), 0);
        SqlRowSet filmDb = jdbcTemplate.queryForRowSet("select film_id from films order by film_id desc limit 1");
        filmDb.next();
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("insert into film_genres (film_id, genre_id) " +
                    "values (?, ?)", filmDb.getInt("film_id"), genre.getId());
        }

    log.info("Фильм {} добавлен в общий список", film.getName());
        return filmFromDB(film);
    }

    private Film filmBuild(SqlRowSet sqlRowSet) {
        Film returnedFilm = new Film(sqlRowSet.getString("name"), sqlRowSet.getString("description"),
                sqlRowSet.getString("release_date"), sqlRowSet.getInt("duration"));
        returnedFilm.setId(sqlRowSet.getInt("film_id"));
        returnedFilm.setLikes(sqlRowSet.getInt("likes"));
        SqlRowSet mpaDb = jdbcTemplate.queryForRowSet("select * from mpa where mpa_id = ?", sqlRowSet.getInt("mpa"));
        if (mpaDb.next()) {
            Mpa mpa = new Mpa(mpaDb.getInt("mpa_id"), mpaDb.getString("mpa_name"));
            returnedFilm.setMpa(mpa);
        }
        Set<Genre> genres = new LinkedHashSet<>();
        SqlRowSet genresFromDb = jdbcTemplate.queryForRowSet("select * from genres " +
                "inner join film_genres on film_genres.genre_id = genres.genre_id " +
                "inner join films on films.film_id = film_genres.film_id where films.film_id = ? " +
                "order by genre_id asc", sqlRowSet.getInt("film_id"));
        while (genresFromDb.next()) {
            Genre genre = new Genre(genresFromDb.getInt("genre_id"), genresFromDb.getString("genre_name"));
            genres.add(genre);
        }
        returnedFilm.setGenres(genres);
        SqlRowSet filmLikesList = jdbcTemplate.queryForRowSet("select * from film_likes " +
                "inner join films on films.film_id = film_likes.film_id where film_likes.film_id = ?", sqlRowSet.getInt("film_id"));
        while (filmLikesList.next()) {
            returnedFilm.addUserId(filmLikesList.getInt("user_id"));
        }
        return returnedFilm;
    }

    private Film filmFromDB(Film film) {
        SqlRowSet filmFromDB = jdbcTemplate.queryForRowSet("select * from films order by film_id desc limit 1");
        filmFromDB.next();
        return filmBuild(filmFromDB);
    }
}
