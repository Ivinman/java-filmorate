package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class User {
    @EqualsAndHashCode.Exclude
    private int id;

    private final String email;
    private final String login;

    @EqualsAndHashCode.Exclude
    private String name;

    private final String birthday;

}
