package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmLikesStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmLikesStorage filmLikesStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, FilmLikesStorage filmLikesStorage) {
        this.filmStorage = filmStorage;
        this.filmLikesStorage = filmLikesStorage;
    }

    public Film addFilm(Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        Map<Integer, Film> filmInDb = filmStorage.getFilms();
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
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    public Film addLike(Integer filmId, Integer userId) {
        throwException(filmId, userId);
        if (filmLikesStorage.checkLike(filmId, userId)) {
            log.info("Данный пользователь уже поставил оценку");
            throw new AlreadyExistException("Данный пользователь уже поставил оценку");
        }
        log.info("Лайк добавлен к фильму {}", filmStorage.getFilm(filmId).getName());
        return filmLikesStorage.addLike(filmId, userId);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        throwException(filmId, userId);
        log.info("Лайк удален из фильма {}", filmStorage.getFilm(filmId));
        return filmLikesStorage.deleteLike(filmId, userId);
    }

    public List<Film> getTopFilms(Integer count) {
        if (count == null || count < 0) {
            log.info("Некорректно введён count");
            throw new IncorrectParameterException("Некорректно указан размер списка");
        }
        List<Film> filmInDb = new ArrayList<>(filmStorage.getFilms().values());
        return filmInDb.stream().sorted((p0, p1) ->
                p1.getLikes().compareTo(p0.getLikes())
        ).limit(count).collect(Collectors.toList());
    }

    public Film getFilm(Integer id) {
        Film film = filmStorage.getFilm(id);
        if (film == null) {
            throw new NotFoundException("Идентификатор не найден");
        }
        return film;
    }

    private void throwException(Integer filmId, Integer userId) {
        if (filmId == null || userId == null) {
            throw new IncorrectParameterException("Некорректно заданные данные фильма и пользователя");
        }
        if (filmStorage.getFilm(filmId) == null) {
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
}
