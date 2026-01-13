package com.example.investhub.controller;

import com.example.investhub.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle insufficient balance exception - 400 Bad Request
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException e) {
        log.warn("Insufficient balance: required=${}, available=${}", e.getRequired(), e.getAvailable());
        Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Insufficient balance", e.getMessage());
        body.put("required", e.getRequired());
        body.put("available", e.getAvailable());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle insufficient holdings exception - 400 Bad Request
     */
    @ExceptionHandler(InsufficientHoldingsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientHoldings(InsufficientHoldingsException e) {
        log.warn("Insufficient holdings: asset={}, owned={}, requested={}",
            e.getAssetSymbol(), e.getOwned(), e.getRequested());
        Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Insufficient holdings", e.getMessage());
        body.put("assetSymbol", e.getAssetSymbol());
        body.put("owned", e.getOwned());
        body.put("requested", e.getRequested());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle resource not found exception - 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: type={}, identifier={}", e.getResourceType(), e.getIdentifier());
        Map<String, Object> body = buildErrorBody(HttpStatus.NOT_FOUND, "Resource not found", e.getMessage());
        body.put("resourceType", e.getResourceType());
        body.put("identifier", e.getIdentifier());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handle resource already exists exception - 409 Conflict
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException e) {
        log.warn("Resource already exists: type={}, identifier={}", e.getResourceType(), e.getIdentifier());
        Map<String, Object> body = buildErrorBody(HttpStatus.CONFLICT, "Resource already exists", e.getMessage());
        body.put("resourceType", e.getResourceType());
        body.put("identifier", e.getIdentifier());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Handle validation exception - 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Validation error", e.getMessage());
        if (e.getField() != null) {
            body.put("field", e.getField());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ==================== Security Exceptions ====================

    /**
     * Handle authentication errors - 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed",
            e.getMessage()
        );
    }

    /**
     * Handle bad credentials - 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        log.warn("Bad credentials: {}", e.getMessage());
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid credentials",
            "Invalid username or password"
        );
    }

    /**
     * Handle access denied - 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "Access denied",
            "You don't have permission to access this resource"
        );
    }

    // ==================== Generic Exceptions ====================

    /**
     * Handle illegal argument - 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid input",
            e.getMessage()
        );
    }

    /**
     * Handle generic runtime exceptions - 400 Bad Request
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception: {}", e.getMessage(), e);
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Operation failed",
            e.getMessage()
        );
    }

    /**
     * Handle unexpected exceptions - 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected error occurred. Please try again later."
        );
    }

    // ==================== Helper Methods ====================

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = buildErrorBody(status, error, message);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
