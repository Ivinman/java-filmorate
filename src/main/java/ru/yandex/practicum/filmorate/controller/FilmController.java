package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    @PostMapping
    public Film addFilm(@RequestBody Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        } else if (films.containsValue(film)) {
            log.info("Добавление через POST запрос уже имеющегося объекта");
            throw new AlreadyExistException("Данный фильм уже добавлен");
        } else {
            id++;
            film.setId(id);
            films.put(id, film);
            log.info("Новый фильм добавлен в общий список");
            return film;
        }
    }

    @PutMapping
    public Film addOrUpdateFilm(@RequestBody Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        } else {
            if (film.getId() == 0) {
                id++;
                film.setId(id);
                log.info("Новый фильм добавлен в общий список");
                films.put(id, film);
                return film;
            } else {
                if (films.containsKey(film.getId())) {
                    log.info("Был обновлён фильм под названием: {}", film.getName());
                    films.put(id, film);
                    return film;
                } else {
                    throw new ValidationException("Попытка обновления предварительно не добавленного объекта");
                }
            }
        }
    }

    @GetMapping
    public List<Film> getAll() {
        List<Film> filmsList = new ArrayList<>();
        for (int i = 1; i <= films.size(); i++) {
            for (Film film : films.values()) {
                if (film.getId() == i) {
                    filmsList.add(film);
                }
            }
        }
        return filmsList;
    }

    private boolean validation(Film film) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return film.getName() != null
                && !film.getName().isEmpty()
                && !film.getName().isBlank()
                && film.getDescription().length() <= 200
                && LocalDate.parse(film.getReleaseDate(), formatter)
                    .isAfter(LocalDate.parse("1895-12-28", formatter))
                && film.getDuration() > 0;
    }
}
