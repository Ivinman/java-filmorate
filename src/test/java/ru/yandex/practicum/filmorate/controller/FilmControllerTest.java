package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerTest {
    private FilmController filmController;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createController() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        //jdbcTemplate = new JdbcTemplate();
        FilmService filmService = new FilmService(filmStorage, userStorage, jdbcTemplate);
        filmController = new FilmController(filmService);
    }

    private ValidationException getThrown(Film film) {
        return assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
    }

    @Test
    void addFilm() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        filmController.addFilm(film);

        assertEquals(film, filmController.getAll().get(0));

        Film film1 = new Film("", "description","2000-01-01", 200, "genre","PG-13");
        Film film2 = new Film(null, "description", "2000-01-01", 200, "genre","PG-13");
        Film film3 = new Film("name", "description", "1800-01-01", 200, "genre","PG-13");
        Film film4 = new Film("name", "description", "2020-01-01", -200, "genre","PG-13");

        assertEquals("Ошибка валидации", getThrown(film1).getMessage());
        assertEquals("Ошибка валидации", getThrown(film2).getMessage());
        assertEquals("Ошибка валидации", getThrown(film3).getMessage());
        assertEquals("Ошибка валидации", getThrown(film4).getMessage());

        Film film5 = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        AlreadyExistException thrown = assertThrows(AlreadyExistException.class,
                () -> filmController.addFilm(film5));

        assertEquals("Данный фильм уже добавлен", thrown.getMessage());
    }

    @Test
    void addOrUpdateFilm() throws Exception {
        Film film1 = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        filmController.addOrUpdateFilm(film1);

        assertTrue(filmController.getAll().contains(film1));
        assertEquals(1, filmController.getAll().size());

        Film film2 = new Film("name","New description", "1997-12-12", 200, "genre","PG-13");
        film2.setId(1);
        filmController.addOrUpdateFilm(film2);

        assertEquals(1, filmController.getAll().size());
        assertTrue(filmController.getAll().contains(film2));
        assertEquals(film2.getDescription(), filmController.getAll().get(0).getDescription());
    }

    @Test
    void getAll() throws Exception {
        Film film1 = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        Film film2 = new Film("name2","description2", "1998-12-12", 200, "genre","PG-13");
        Film film3 = new Film("name2","New description", "1997-12-12", 200, "genre","PG-13");
        film3.setId(2);
        filmController.addFilm(film1);
        filmController.addOrUpdateFilm(film2);
        filmController.addOrUpdateFilm(film3);

        assertEquals(2, filmController.getAll().size());
    }

    @Test
    void addLike() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        Film film1 = new Film("New name","New description", "1998-12-12", 200, "genre","PG-13");
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
                () -> filmController.addLike(film1.getId(), user.getId()));
        assertEquals("Данный фильм не найден", filmNotFoundException.getMessage());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> filmController.addLike(film.getId(), user1.getId()));
        assertEquals("Пользователь не найден", userNotFoundException.getMessage());

        filmController.addLike(film.getId(), user.getId());
        //assertEquals(1, filmStorage.getFilms().get(film.getId()).getLikes());
        //assertTrue(filmStorage.getFilms().get(film.getId()).getUsersIdLikes().contains(user.getId()));

        AlreadyExistException alreadyExistException = assertThrows(AlreadyExistException.class,
                () -> filmController.addLike(film.getId(), user.getId()));
        assertEquals("Данный пользователь уже поставил оценку", alreadyExistException.getMessage());
    }

    @Test
    void deleteLike() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        User user = new User("email@", "login", "1997-12-12");
        filmController.addFilm(film);
        userStorage.addUser(user);
        filmController.addLike(film.getId(), user.getId());
        filmController.deleteLike(film.getId(), user.getId());
        //assertEquals(0, filmStorage.getFilms().get(film.getId()).getLikes());
        //assertFalse(filmStorage.getFilms().get(film.getId()).getUsersIdLikes().contains(user.getId()));
    }

    @Test
    void getTopFilms() throws Exception {
        Film film = new Film("name","description", "1997-12-12", 200, "genre","PG-13");
        Film film1 = new Film("New name","New description", "1998-12-12", 200, "genre","PG-13");
        User user = new User("email@", "login", "1997-12-12");
        User user1 = new User("Newemail@", "Newlogin", "1998-12-12");

        filmController.addFilm(film);
        filmController.addFilm(film1);
        userStorage.addUser(user);
        userStorage.addUser(user1);
        filmController.addLike(film.getId(), user.getId());
        filmController.addLike(film.getId(), user1.getId());
        filmController.addLike(film1.getId(), user1.getId());

        IncorrectParameterException nullParam = assertThrows(IncorrectParameterException.class,
                () -> filmController.getTopFilms(null));
        assertEquals("Некорректно указан размер списка", nullParam.getMessage());
        IncorrectParameterException incorrectParam = assertThrows(IncorrectParameterException.class,
                () -> filmController.getTopFilms(-1));
        assertEquals("Некорректно указан размер списка", incorrectParam.getMessage());

        List<Film> topFilms = filmController.getTopFilms(2);
        assertEquals(2, topFilms.size());
        assertEquals(topFilms.get(0), film);
    }
}