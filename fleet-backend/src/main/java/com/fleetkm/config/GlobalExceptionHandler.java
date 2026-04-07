package com.fleetkm.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import com.fleetkm.exception.BusinessException;
import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API endpoints.
 * Handles various exception types and returns appropriate HTTP responses.
 */
@RestControllerAdvice
public final class GlobalExceptionHandler {

    /** Standard logger for exception handling. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles HttpMessageNotReadableException.
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with error details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex,
            final HttpServletRequest req) {
        LOGGER.warn(
                "Malformed or missing request body for {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Le corps de la requête est manquant ou contient du JSON invalide");
        body.put("path", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles validation exceptions (MethodArgumentNotValidException and
     * BindException).
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with validation error details
     */
    @ExceptionHandler({MethodArgumentNotValidException.class,
            BindException.class})
    public ResponseEntity<?> handleValidationException(
            final Exception ex,
            final HttpServletRequest req) {
        LOGGER.warn(
                "Validation failed for {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "La validation du corps de la requête a échoué");
        body.put("path", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * Handles MissingServletRequestParameterException.
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with parameter error details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(
            final MissingServletRequestParameterException ex,
            final HttpServletRequest req) {
        LOGGER.warn(
                "Missing request parameter for {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        String paramName = "Le paramètre de requête requis est manquant : "
                + ex.getParameterName();
        body.put("error", paramName);
        body.put("path", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * Handles BusinessException for business rule violations.
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with business error details
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(
            final BusinessException ex,
            final HttpServletRequest req) {
        LOGGER.warn(
                "Business rule failure for {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getReason());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Violation de règle métier");
        body.put("reason", ex.getReason());
        if (ex.getField() != null) {
            body.put("field", ex.getField());
        }
        body.put("path", req.getRequestURI());
        body.put("timestamp", OffsetDateTime.now().toString());
        HttpStatusCode status = ex.getStatus();
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Handles access denied exceptions from Spring Security.
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with access denied details
     */
    @ExceptionHandler({AuthorizationDeniedException.class,
            AccessDeniedException.class})
    public ResponseEntity<?> handleAccessDenied(
            final Exception ex,
            final HttpServletRequest req) {
        LOGGER.warn(
                "Access denied for {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Accès refusé");
        body.put("path", req.getRequestURI());
        body.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handles ResponseStatusException thrown by services.
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with the status and reason from the exception
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(
            final ResponseStatusException ex,
            final HttpServletRequest req) {
        LOGGER.warn("ResponseStatusException for {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getReason());
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getReason());
        body.put("path", req.getRequestURI());
        body.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /**
     * Handles all unhandled exceptions.
     *
     * @param ex the exception (must not be null)
     * @param req the HTTP request (must not be null)
     * @return response entity with generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(
            final Exception ex,
            final HttpServletRequest req) {
        LOGGER.error("Unhandled exception for {} {}",
                req.getMethod(),
                req.getRequestURI(),
                ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Erreur interne du serveur");
        body.put("path", req.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
