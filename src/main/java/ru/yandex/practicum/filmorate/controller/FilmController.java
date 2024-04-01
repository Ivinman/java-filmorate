package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) throws Exception {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film addOrUpdateFilm(@RequestBody Film film) throws Exception {
        return filmService.addOrUpdateFilm(film);
    }

    @GetMapping
    public List<Film> getAll() {
        return filmService.getAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable (required = false) Integer id,
                        @PathVariable (required = false) Integer userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable (required = false) Integer id,
                           @PathVariable (required = false) Integer userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilms(@RequestParam (defaultValue = "10", required = false) Integer count) {
        return filmService.getTopFilms(count);
    }
}
