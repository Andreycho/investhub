package com.example.investhub.exception;

import com.example.investhub.model.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== Domain Exceptions ====================

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException e) {
        log.warn("Insufficient balance: required=${}, available=${}", e.getRequired(), e.getAvailable());

        Map<String, Object> details = new HashMap<>();
        details.put("required", e.getRequired());
        details.put("available", e.getAvailable());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Insufficient balance", e.getMessage(), details);
    }

    @ExceptionHandler(InsufficientHoldingsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientHoldings(InsufficientHoldingsException e) {
        log.warn("Insufficient holdings: asset={}, owned={}, requested={}",
                e.getAssetSymbol(), e.getOwned(), e.getRequested());

        Map<String, Object> details = new HashMap<>();
        details.put("assetSymbol", e.getAssetSymbol());
        details.put("owned", e.getOwned());
        details.put("requested", e.getRequested());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Insufficient holdings", e.getMessage(), details);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: type={}, identifier={}", e.getResourceType(), e.getIdentifier());

        Map<String, Object> details = new HashMap<>();
        details.put("resourceType", e.getResourceType());
        details.put("identifier", e.getIdentifier());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", e.getMessage(), details);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException e) {
        log.warn("Resource already exists: type={}, identifier={}", e.getResourceType(), e.getIdentifier());

        Map<String, Object> details = new HashMap<>();
        details.put("resourceType", e.getResourceType());
        details.put("identifier", e.getIdentifier());

        return buildErrorResponse(HttpStatus.CONFLICT, "Resource already exists", e.getMessage(), details);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());

        Map<String, Object> details = new HashMap<>();
        if (e.getField() != null) {
            details.put("field", e.getField());
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation error", e.getMessage(), details.isEmpty() ? null : details);
    }

    // ==================== Security Exceptions ====================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", e.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        log.warn("Bad credentials: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", "Invalid username or password", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", "You don't have permission to access this resource", null);
    }

    // ==================== Generic Exceptions ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input", e.getMessage(), null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Operation failed", e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred. Please try again later.",
                null
        );
    }

    // ==================== Helper ====================

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, Map<String, Object> details) {
        ErrorResponse body = new ErrorResponse(status.value(), error, message, details);
        return ResponseEntity.status(status).body(body);
    }
}
