package ru.yandex.practicum.filmorate.exception;

public class AlreadyExistException extends Exception{
    public AlreadyExistException(final String message) {
        super(message);
    }
}
