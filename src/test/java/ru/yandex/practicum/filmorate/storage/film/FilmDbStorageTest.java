package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void addFilm() {
        Film film = new Film("name","description", "1997-12-12", 200);
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = Set.of(new Genre(1, "Комедия"));
        film.setMpa(mpa);
        film.setGenres(genres);
        FilmDbStorage filmDbStorage = new FilmDbStorage(jdbcTemplate);

        Film filmDb = filmDbStorage.addFilm(film);
        film.setId(1);

        assertThat(filmDb)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(filmDb);
    }
}