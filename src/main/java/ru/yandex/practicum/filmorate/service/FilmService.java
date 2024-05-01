package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film addFilm(Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        Map<Integer, Film> filmInDb = new HashMap<>();
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films");
        while (filmFromDb.next()) {
            Film returnedFilm = new Film(filmFromDb.getString("name"), filmFromDb.getString("description"),
                    filmFromDb.getString("release_date"), filmFromDb.getInt("duration"));
            returnedFilm.setId(filmFromDb.getInt("film_id"));
            returnedFilm.setLikes(filmFromDb.getInt("likes"));

            SqlRowSet mpaDb = jdbcTemplate.queryForRowSet("select * from mpa where mpa_id = ?", filmFromDb.getInt("mpa"));
            if (mpaDb.next()) {
                Mpa mpa = new Mpa(mpaDb.getInt("mpa_id"), mpaDb.getString("mpa_name"));
                returnedFilm.setMpa(mpa);
            }
            Set<Genre> genres = new LinkedHashSet<>();
            SqlRowSet genresFromDb = jdbcTemplate.queryForRowSet("select * from genres " +
                    "inner join film_genres on film_genres.genre_id = genres.genre_id " +
                    "inner join films on films.film_id = film_genres.film_id where films.film_id = ?" +
                    "order by genre_id asc", filmFromDb.getInt("film_id"));
            while (genresFromDb.next()) {
                Genre genre = new Genre(genresFromDb.getInt("genre_id"), genresFromDb.getString("genre_name"));
                genres.add(genre);
            }
            returnedFilm.setGenres(genres);

            SqlRowSet filmLikesList = jdbcTemplate.queryForRowSet("select * from film_likes " +
                    "inner join films on films.film_id = film_likes.film_id where film_likes.film_id = ?", filmFromDb.getInt("film_id"));
            while (filmLikesList.next()) {
                returnedFilm.addUserId(filmLikesList.getInt("user_id"));
            }
            filmInDb.put(returnedFilm.getId(), returnedFilm);
        }
        if (filmInDb.containsValue(film)) {
            log.info("Добавление через POST запрос уже имеющегося объекта");
            throw new AlreadyExistException("Данный фильм уже добавлен");
        }
        return filmStorage.addFilm(film);
    }

    public Film addOrUpdateFilm(Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        Film testFilm = filmStorage.addOrUpdateFilm(film);
        if (testFilm == null) {
            throw new FilmNotFoundException("Попытка обновления предварительно не добавленного объекта");
        }
        return testFilm;
    }

    public List<Film> getAll() {
        List<Film> filmInDb = new ArrayList<>();
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films");
        while (filmFromDb.next()) {
            filmInDb.add(filmBuild(filmFromDb));
        }
        return filmInDb;
    }

    public Film addLike(Integer filmId, Integer userId) {
        throwException(filmId, userId);
        if (jdbcTemplate.queryForRowSet("select * from film_likes " +
                "where user_id = ? and film_id = ?", userId, filmId).next()) {
            log.info("Данный пользователь уже поставил оценку");
            throw new AlreadyExistException("Данный пользователь уже поставил оценку");
        }
        jdbcTemplate.update("insert into film_likes (film_id, user_id) " +
                "values (?, ?)", filmId, userId);
        SqlRowSet filmInDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        filmInDb.next();
        jdbcTemplate.update("update films set likes = ? where film_id = ?",
                (filmInDb.getInt("likes") + 1), filmId);
        log.info("Лайк добавлен к фильму {}", filmInDb.getString("name"));
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        filmFromDb.next();
        return filmBuild(filmFromDb);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        throwException(filmId, userId);
        jdbcTemplate.update("delete from film_likes where film_id = ?", filmId);
        SqlRowSet filmInDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        filmInDb.next();
        jdbcTemplate.update("update films set likes = ? where film_id = ?",
                (filmInDb.getInt("likes") - 1), filmId);
        log.info("Лайк удален из фильма {}", filmInDb.getString("name"));

        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films " +
                "where film_id = ?", filmId);
        filmFromDb.next();
        return filmBuild(filmFromDb);
    }

    public List<Film> getTopFilms(Integer count) {
        if (count == null || count < 0) {
            log.info("Некорректно введён count");
            throw new IncorrectParameterException("Некорректно указан размер списка");
        }
        List<Film> filmInDb = new ArrayList<>();
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films");
        while (filmFromDb.next()) {
            filmInDb.add(filmBuild(filmFromDb));
        }
        return filmInDb.stream().sorted((p0, p1) ->
                p1.getLikes().compareTo(p0.getLikes())
        ).limit(count).collect(Collectors.toList());
    }

    public Film getFilm(Integer id) {
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films where film_id = ?", id);
        if (filmFromDb.next()) {
            return filmBuild(filmFromDb);
        }
        throw new NotFoundException("Идентификатор не найден");
    }

    public Film getFilmWithGenre(Integer id) {
        SqlRowSet filmFromDb = jdbcTemplate.queryForRowSet("select * from films " +
                "inner join film_genres on films.film_id = film_genres.film_id " +
                "inner join genres on genres.genre_id = film_genres.genre_id " +
                "where genres.genre_id = ?", id);
        if (filmFromDb.next()) {
            return  filmBuild(filmFromDb);
        }
        throw new NotFoundException("Идентификатор не найден");
    }

    private void throwException(Integer filmId, Integer userId) {
        if (filmId == null || userId == null) {
            throw new IncorrectParameterException("Некорректно заданные данные фильма и пользователя");
        }
        if (!jdbcTemplate.queryForRowSet("select film_id from films " +
                "where film_id = ?", filmId).next()) {
            log.info("Данный фильм не найден");
            throw  new FilmNotFoundException("Данный фильм не найден");
        }
    }

    private boolean validation(Film film) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Set<Genre> genres = film.getGenres();
        for (Genre genre : genres) {
            if (genre.getId() > 6) {
                return false;
            }
        }
        return film.getName() != null
                && !film.getName().isEmpty()
                && !film.getName().isBlank()
                && film.getDescription().length() <= 200
                && LocalDate.parse(film.getReleaseDate(), formatter)
                .isAfter(LocalDate.parse("1895-12-28", formatter))
                && film.getDuration() > 0
                && film.getMpa().getId() < 6;
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
}
