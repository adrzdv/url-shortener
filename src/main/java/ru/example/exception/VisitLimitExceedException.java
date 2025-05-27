package ru.example.exception;

public class VisitLimitExceedException extends RuntimeException {
    public VisitLimitExceedException(String message) {
        super(message);
    }
}
