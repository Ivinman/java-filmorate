package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

    Film addFilm(Film film) throws Exception;

    Film addOrUpdateFilm(Film film) throws Exception;
}
