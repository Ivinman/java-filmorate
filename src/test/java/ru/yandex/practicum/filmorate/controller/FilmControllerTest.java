package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void createController() {
        filmController = new FilmController();
    }

    private ValidationException getThrown(Film film) {
        return assertThrows(ValidationException.class,
                () -> filmController.addFilm(film));
    }

    @Test
    void addFilm() throws Exception{
        Film film = new Film("name","description", "1997-12-12", 200);
        filmController.addFilm(film);

        assertEquals(film, filmController.getAll().get(0));

        Film film1 = new Film("", "description","2000-01-01", 200);
        Film film2 = new Film(null, "description", "2000-01-01", 200);
        Film film3 = new Film("name", "description", "1800-01-01", 200);
        Film film4 = new Film("name", "description", "2020-01-01", -200);

        assertEquals("Ошибка валидации", getThrown(film1).getMessage());
        assertEquals("Ошибка валидации", getThrown(film2).getMessage());
        assertEquals("Ошибка валидации", getThrown(film3).getMessage());
        assertEquals("Ошибка валидации", getThrown(film4).getMessage());

        Film film5 = new Film("name","description", "1997-12-12", 200);
        AlreadyExistException thrown = assertThrows(AlreadyExistException.class,
                () -> filmController.addFilm(film5));

        assertEquals("Данный фильм уже добавлен", thrown.getMessage());
    }

    @Test
    void addOrUpdateFilm() throws Exception{
        Film film1 = new Film("name","description", "1997-12-12", 200);
        filmController.addOrUpdateFilm(film1);

        assertTrue(filmController.getAll().contains(film1));
        assertEquals(1, filmController.getAll().size());

        Film film2 = new Film("name","New description", "1997-12-12", 200);
        film2.setId(1);
        filmController.addOrUpdateFilm(film2);

        assertEquals(1, filmController.getAll().size());
        assertTrue(filmController.getAll().contains(film2));
        assertEquals(film2.getDescription(), filmController.getAll().get(0).getDescription());
    }

    @Test
    void getAll() throws Exception{
        Film film1 = new Film("name","description", "1997-12-12", 200);
        Film film2 = new Film("name2","description2", "1998-12-12", 200);
        Film film3 = new Film("name2","New description", "1997-12-12", 200);
        film3.setId(2);
        filmController.addFilm(film1);
        filmController.addOrUpdateFilm(film2);
        filmController.addOrUpdateFilm(film3);

        assertEquals(2, filmController.getAll().size());
    }
}