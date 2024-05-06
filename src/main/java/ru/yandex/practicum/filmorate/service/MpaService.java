package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa getMpa(Integer id) {
        Mpa mpa = mpaStorage.getMpa(id);
        if (mpa == null) {
            throw new NotFoundException("Не найден идентификатоор");
        }
        return mpa;
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }
}
