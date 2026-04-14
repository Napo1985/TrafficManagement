package com.traffic.web;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.traffic.service.SlugGenerationException;
import com.traffic.service.UrlValidationException;

/**
 * JDK {@link URI} is not null-marked; Spring's {@link ProblemDetail#setType(URI)} expects {@code @NonNull URI}.
 * {@code URI.create("about:blank")} never returns null.
 */
@SuppressWarnings("null")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static URI problemDetailTypeUri() {
        return URI.create("about:blank");
    }

    @ExceptionHandler(UrlValidationException.class)
    public ResponseEntity<ProblemDetail> handleUrlValidation(UrlValidationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, Objects.requireNonNullElse(ex.getMessage(), ""));
        detail.setTitle("Invalid URL");
        detail.setType(problemDetailTypeUri());
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
        detail.setType(problemDetailTypeUri());
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(SlugGenerationException.class)
    public ResponseEntity<ProblemDetail> handleSlugGeneration(SlugGenerationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, Objects.requireNonNullElse(ex.getMessage(), ""));
        detail.setTitle("Slug allocation failed");
        detail.setType(problemDetailTypeUri());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(detail);
    }
}
