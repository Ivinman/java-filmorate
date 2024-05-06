package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * Film.
 */
@Data
@EqualsAndHashCode
public class Film {
    @EqualsAndHashCode.Exclude
    private Integer id;

    private final String name;
    private final String description;
    private final String releaseDate;
    private final Integer duration;

    private Set<Genre> genres = new LinkedHashSet<>();
    private Mpa mpa;

    private Integer likes = 0;
    private Set<Integer> usersIdLikes = new HashSet<>();

    public void addUserId(Integer id) {
        usersIdLikes.add(id);
    }
}
