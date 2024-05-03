package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmLikesStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerTest {
    private FilmController filmController;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createController() {
        userStorage = new UserDbStorage(jdbcTemplate);
        filmStorage = new FilmDbStorage(jdbcTemplate);
        FilmLikesStorage filmLikesStorage = new FilmLikesStorage(filmStorage, jdbcTemplate);
        FilmService filmService = new FilmService(filmStorage, filmLikesStorage);
        filmController = new FilmController(filmService);
    }

    private ValidationException getThrown(Film film) {
        return assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
    }

    private void filmComp(Film film) {
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = Set.of(new Genre(1, "Комедия"));
        film.setMpa(mpa);
        film.setGenres(genres);
    }

    private Integer getFilmId() {
        SqlRowSet filmIdFromDb = jdbcTemplate.queryForRowSet("select film_id from films order by film_id desc limit 1");
        filmIdFromDb.next();
        return filmIdFromDb.getInt("film_id");
    }

    private Integer getUserId() {
        SqlRowSet userIdFromDb = jdbcTemplate.queryForRowSet("select user_id from users order by user_id desc limit 1");
        userIdFromDb.next();
        return userIdFromDb.getInt("user_id");
    }

    @Test
    void addFilm() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200);
        filmComp(film);
        filmController.addFilm(film);

        assertEquals(film, filmController.getAll().get(0));

        Film film1 = new Film("", "description","2000-01-01", 200);
        filmComp(film1);
        Film film2 = new Film(null, "description", "2000-01-01", 200);
        filmComp(film2);
        Film film3 = new Film("name", "description", "1800-01-01", 200);
        filmComp(film3);
        Film film4 = new Film("name", "description", "2020-01-01", -200);
        filmComp(film4);

        assertEquals("Ошибка валидации", getThrown(film1).getMessage());
        assertEquals("Ошибка валидации", getThrown(film2).getMessage());
        assertEquals("Ошибка валидации", getThrown(film3).getMessage());
        assertEquals("Ошибка валидации", getThrown(film4).getMessage());

        Film film5 = new Film("name","description", "1997-12-12", 200);
        filmComp(film5);
        AlreadyExistException thrown = assertThrows(AlreadyExistException.class,
                () -> filmController.addFilm(film5));

        assertEquals("Данный фильм уже добавлен", thrown.getMessage());
    }

    @Test
    void addOrUpdateFilm() throws Exception {
        Film film1 = new Film("name","description", "1997-12-12", 200);
        filmComp(film1);
        filmController.addOrUpdateFilm(film1);

        assertTrue(filmController.getAll().contains(film1));
        assertEquals(1, filmController.getAll().size());

        Film film2 = new Film("name","New description", "1997-12-12", 200);
        filmComp(film2);
        film2.setId(getFilmId());
        filmController.addOrUpdateFilm(film2);

        assertEquals(1, filmController.getAll().size());
        assertTrue(filmController.getAll().contains(film2));
        assertEquals(film2.getDescription(), filmController.getAll().get(0).getDescription());
    }

    @Test
    void getAll() throws Exception {
        Film film1 = new Film("name","description", "1997-12-12", 200);
        filmComp(film1);
        Film film2 = new Film("name2","description2", "1998-12-12", 200);
        filmComp(film2);
        Film film3 = new Film("name2","New description", "1997-12-12", 200);
        filmComp(film3);
        film3.setId(2);
        filmController.addFilm(film1);
        filmController.addOrUpdateFilm(film2);
        filmController.addOrUpdateFilm(film3);

        assertEquals(2, filmController.getAll().size());
    }

    @Test
    void addLike() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200);
        filmComp(film);
        Film film1 = new Film("New name","New description", "1998-12-12", 200);
        filmComp(film1);
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");

        filmController.addFilm(film);
        userStorage.addUser(user);
        film1.setId(2);
        user1.setId(2);

        IncorrectParameterException exception = assertThrows(IncorrectParameterException.class,
                () -> filmController.addLike(null, null));
        assertEquals("Некорректно заданные данные фильма и пользователя", exception.getMessage());

        FilmNotFoundException filmNotFoundException = assertThrows(FilmNotFoundException.class,
                () -> filmController.addLike(film1.getId(), getUserId()));
        assertEquals("Данный фильм не найден", filmNotFoundException.getMessage());

        filmController.addLike(getFilmId(), getUserId());
        assertEquals(1, filmStorage.getFilms().get(getFilmId()).getLikes());
        assertTrue(filmStorage.getFilms().get(getFilmId()).getUsersIdLikes().contains(getUserId()));

        AlreadyExistException alreadyExistException = assertThrows(AlreadyExistException.class,
                () -> filmController.addLike(getFilmId(), getUserId()));
        assertEquals("Данный пользователь уже поставил оценку", alreadyExistException.getMessage());
    }

    @Test
    void deleteLike() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200);
        filmComp(film);
        User user = new User("email@", "login", "1997-12-12");
        filmController.addFilm(film);
        userStorage.addUser(user);

        filmController.addLike(getFilmId(), getUserId());
        filmController.deleteLike(getFilmId(), getUserId());
        assertEquals(0, filmStorage.getFilms().get(getFilmId()).getLikes());
        assertFalse(filmStorage.getFilms().get(getFilmId()).getUsersIdLikes().contains(getUserId()));
    }

    @Test
    void getTopFilms() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200);
        filmComp(film);
        Film film1 = new Film("New name","New description", "1998-12-12", 200);
        filmComp(film1);
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");

        filmController.addFilm(film);
        userStorage.addUser(user);
        filmController.addLike(getFilmId(), getUserId());
        userStorage.addUser(user1);
        filmController.addLike(getFilmId(), getUserId());
        filmController.addFilm(film1);
        filmController.addLike(getFilmId(), getUserId());

        IncorrectParameterException nullParam = assertThrows(IncorrectParameterException.class,
                () -> filmController.getTopFilms(null));
        assertEquals("Некорректно указан размер списка", nullParam.getMessage());
        IncorrectParameterException incorrectParam = assertThrows(IncorrectParameterException.class,
                () -> filmController.getTopFilms(-1));
        assertEquals("Некорректно указан размер списка", incorrectParam.getMessage());

        List<Film> topFilms = filmController.getTopFilms(2);
        assertEquals(2, topFilms.size());
        assertEquals(topFilms.get(1), filmController.getFilm(getFilmId()));
    }

    @Test
    void getFilm() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200);
        filmComp(film);
        filmController.addFilm(film);
        film.setId(1);

        assertEquals(film, filmController.getFilm(getFilmId()));
    }
}