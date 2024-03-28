package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
}
