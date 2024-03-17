package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.Duration;

/**
 * Film.
 */
@Data
@EqualsAndHashCode
public class Film {
    @EqualsAndHashCode.Exclude
    private int id;

    private final String name;
    private final String description;
    private final String releaseDate;
    private final Integer duration;
}
