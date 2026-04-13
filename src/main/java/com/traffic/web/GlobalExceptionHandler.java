package com.traffic.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.traffic.service.SlugGenerationException;
import com.traffic.service.UrlValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TYPE_VALIDATION = URI.create("about:blank");

    @ExceptionHandler(UrlValidationException.class)
    public ResponseEntity<ProblemDetail> handleUrlValidation(UrlValidationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid URL");
        detail.setType(TYPE_VALIDATION);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Invalid request");
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setTitle("Validation failed");
        detail.setType(TYPE_VALIDATION);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(SlugGenerationException.class)
    public ResponseEntity<ProblemDetail> handleSlugGeneration(SlugGenerationException ex) {
        ProblemDetail detail =
                ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        detail.setTitle("Slug allocation failed");
        detail.setType(TYPE_VALIDATION);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(detail);
    }
}
