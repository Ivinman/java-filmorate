package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    @Getter
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    public Film addFilm(Film film) {
        id++;
        film.setId(id);
        films.put(id, film);
        log.info("Новый фильм добавлен в общий список");
        return film;
    }

    public Film addOrUpdateFilm(Film film) {
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
                return null;
            }
        }
    }
}
