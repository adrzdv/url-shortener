package ru.example.exception;

public class NotFoundShortUrlException extends RuntimeException {
    public NotFoundShortUrlException(String message) {
        super(message);
    }
}
