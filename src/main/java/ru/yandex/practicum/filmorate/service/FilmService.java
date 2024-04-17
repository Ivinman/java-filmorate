package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) throws Exception {
        if (!validation(film)) {
            log.info("Ошибка валидации");
            throw new ValidationException("Ошибка валидации");
        }
        if (filmStorage.getFilms().containsValue(film)) {
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
        if (filmStorage.getFilms().get(filmId).getUsersIdLikes().contains(userId)) {
            log.info("Данный пользователь уже поставил оценку");
            throw new AlreadyExistException("Данный пользователь уже поставил оценку");
        }
        filmStorage.getFilms().get(filmId).setLikes(filmStorage.getFilms().get(filmId).getLikes() + 1);
        filmStorage.getFilms().get(filmId).addUserId(userId);
        log.info("Лайк добавлен к фильму {}", filmStorage.getFilms().get(filmId).getName());
        return filmStorage.getFilms().get(filmId);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        throwException(filmId, userId);

        filmStorage.getFilms().get(filmId).setLikes(filmStorage.getFilms().get(filmId).getLikes() - 1);
        filmStorage.getFilms().get(filmId).deleteUserId(userId);
        log.info("Лайк удален из фильма {}", filmStorage.getFilms().get(filmId).getName());
        return filmStorage.getFilms().get(filmId);
    }

    public List<Film> getTopFilms(Integer count) {
        if (count == null || count < 0) {
            log.info("Некорректно введён count");
            throw new IncorrectParameterException("Некорректно указан размер списка");
        }
        return filmStorage.getFilms().values().stream().sorted((p0, p1) ->
                p1.getLikes().compareTo(p0.getLikes())
        ).limit(count).collect(Collectors.toList());
    }

    private void throwException(Integer filmId, Integer userId) {
        if (filmId == null || userId == null) {
            throw new IncorrectParameterException("Некорректно заданные данные фильма и пользователя");
        }
        if (!filmStorage.getFilms().containsKey(filmId)) {
            log.info("Данный фильм не найден");
            throw  new FilmNotFoundException("Данный фильм не найден");
        }
        if (!userStorage.getUsers().containsKey(userId)) {
            log.info("Пользователь не найден");
            throw new UserNotFoundException("Пользователь не найден");
        }
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
