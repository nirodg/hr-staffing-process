package org.db.hrsp.api.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

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
    private static final URI VALIDATION_TYPE = URI.create("https://example.com/problem/validation-error");
    private static final URI UNEXPECTED_TYPE = URI.create("https://example.com/problem/unexpected-error");
    private static final URI ACCESS_DENIED_TYPE = URI.create("https://example.com/problem/access-denied");
    private static final URI NOT_FOUND_TYPE = URI.create("https://example.com/problem/not-found");

    /*────────────────────────────  2×× / 3×× domain errors  ───────────────────────────*/

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(ApiException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setType(ex.getType() == null ? DEFAULT_TYPE : ex.getType());
        pd.setTitle(ex.getTitle());
        pd.setInstance(URI.create(req.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /*────────────────────────────  400 – validation  ────────────────────────────────*/

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (l, r) -> l));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setType(VALIDATION_TYPE);
        pd.setTitle("Validation error");
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("errors", errors);
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /* Bean-validation on path/query params */
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleConstraintViolation(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(VALIDATION_TYPE);
        pd.setTitle("Validation error");
        pd.setInstance(URI.create(req.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /* Unparseable JSON, wrong Content-Type, etc. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadableBody(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
        pd.setType(VALIDATION_TYPE);
        pd.setTitle("Invalid payload");
        pd.setInstance(URI.create(req.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /*────────────────────────────  401 / 403  ───────────────────────────────────────*/

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        pd.setType(ACCESS_DENIED_TYPE);
        pd.setTitle("Forbidden");
        pd.setInstance(URI.create(req.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /*────────────────────────────  404  ─────────────────────────────────────────────*/

    @ExceptionHandler({ResponseStatusException.class, ErrorResponseException.class, HttpClientErrorException.NotFound.class})
    public ProblemDetail notFound(RuntimeException ex, HttpServletRequest req) {
        if (resolveStatus(ex) != HttpStatus.NOT_FOUND) {   // let other handlers deal with it
            throw ex;
        }
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(NOT_FOUND_TYPE);
        pd.setTitle("Not found");
        pd.setInstance(URI.create(req.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /*────────────────────────────  409 – optimistic lock  ───────────────────────────*/

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail optimisticLock(OptimisticLockingFailureException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "This record was modified by another user. Please refresh and try again.");
        pd.setType(URI.create("https://example.com/problem/optimistic-lock"));
        pd.setTitle("Conflict");
        pd.setInstance(URI.create(req.getRequestURI()));
        log.warn(ex.getMessage(), ex);
        return pd;
    }

    /*────────────────────────────  fallback 5××  ───────────────────────────────────*/

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleOther(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        pd.setType(UNEXPECTED_TYPE);
        pd.setTitle("Internal error");
        pd.setInstance(URI.create(req.getRequestURI()));
        log.error(ex.getMessage(), ex);
        return pd;
    }

    private static HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.resolve(rse.getStatusCode().value());
        }
        if (ex instanceof ErrorResponseException ere) {
            return HttpStatus.resolve(ere.getStatusCode().value());
        }
        if (ex instanceof HttpClientErrorException http) {
            return HttpStatus.resolve(http.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
