package org.db.hrsp.api.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown by the controllers and returns appropriate HTTP responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI DEFAULT_TYPE = URI.create("https://example.com/problem");

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(final ApiException ex,
                                   final HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setType(ex.getType() == null ? DEFAULT_TYPE : ex.getType());
        pd.setTitle(ex.getTitle());
        pd.setInstance(URI.create(request.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(final MethodArgumentNotValidException ex,
                                          final HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setType(URI.create("https://example.com/problem/validation-error"));
        pd.setTitle("Validation error");
        pd.setInstance(URI.create(request.getRequestURI()));

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage));
        pd.setProperty("errors", errors);
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleOther(final Exception ex,
                                     final HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        pd.setType(URI.create("https://example.com/problem/unexpected-error"));
        pd.setTitle("Internal error");
        log.warn(ex.getMessage(), ex);
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }


}
