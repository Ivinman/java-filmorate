package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre getGenre(Integer id) {
        Genre genre = genreStorage.getGenre(id);
        if (genre == null) {
            throw new NotFoundException("Не найден идентификатоор");
        }
        return genre;
    }

    public List<Genre> getAllGenre() {
        return genreStorage.getAllGenre();
    }
}
