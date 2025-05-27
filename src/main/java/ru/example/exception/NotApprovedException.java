package ru.example.exception;

public class NotApprovedException extends RuntimeException {
    public NotApprovedException(String message) {
        super(message);
    }
}
