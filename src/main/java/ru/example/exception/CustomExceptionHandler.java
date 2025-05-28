package ru.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(NotFoundShortUrlException.class)
    public ResponseEntity<String> handleNotFoundShortUrlException(NotFoundShortUrlException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(VisitLimitExceedException.class)
    public ResponseEntity<String> handleVisitLimitException(VisitLimitExceedException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Validation failed: " +
                ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(NotApprovedException.class)
    public ResponseEntity<String> handleNotApprovedException(NotApprovedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<String> handleLinkExpiredException(LinkExpiredException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<String> handleCustomRuntimeException(CustomRuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
