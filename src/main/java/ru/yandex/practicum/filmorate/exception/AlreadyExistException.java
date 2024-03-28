package ru.yandex.practicum.filmorate.exception;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(final String message) {
        super(message);
    }
}
