package com.reactivespring.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleMovieInfoClientException(MoviesInfoClientException exception) {
        log.error("Handler Movie Info Client Exception 4XX: {}", exception.getMessage());
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
    }

    @ExceptionHandler(MoviesInfoServerException.class)
    public ResponseEntity<String> handleMovieInfoClientExceptionServer(MoviesInfoServerException exception) {
        log.error("Handler Movie Info Client Exception 5XX: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}
