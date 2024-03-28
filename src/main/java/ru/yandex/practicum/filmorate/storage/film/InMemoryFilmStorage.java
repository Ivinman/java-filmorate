package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    @Getter
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    public Film addFilm(Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        if (films.containsValue(film)) {
            log.info("Добавление через POST запрос уже имеющегося объекта");
            throw new AlreadyExistException("Данный фильм уже добавлен");
        }
        id++;
        film.setId(id);
        films.put(id, film);
        log.info("Новый фильм добавлен в общий список");
        return film;
    }

    @PutMapping
    public Film addOrUpdateFilm(@RequestBody Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        if (film.getId() == null) {
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
                throw new FilmNotFoundException("Попытка обновления предварительно не добавленного объекта");
            }
        }
    }

    @GetMapping
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
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
