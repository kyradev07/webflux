package com.reactivespring.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalErrorHandle {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandle.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleRequestBodyException(WebExchangeBindException ex) {
        log.error("Exception occurred while processing request: {}", ex.getMessage(), ex);
        String errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(", "));
        log.error("Errors are: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
}
