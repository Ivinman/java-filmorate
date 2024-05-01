package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    @EqualsAndHashCode.Exclude
    private Integer id;

    private final String email;
    private final String login;

    @EqualsAndHashCode.Exclude
    private String name;

    private final String birthday;

    private Set<Integer> friendsId = new HashSet<>();

    public void addFriend(Integer userId) {
        friendsId.add(userId);
    }
}
