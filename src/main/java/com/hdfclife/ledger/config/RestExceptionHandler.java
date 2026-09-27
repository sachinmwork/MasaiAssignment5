package com.hdfclife.ledger.config;

import com.hdfclife.ledger.dto.ErrorResponse;
import com.hdfclife.ledger.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(),
                        error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()));

        return response(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed", fields);
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePolicyNotFound(PolicyNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClaimNotFound(ClaimNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicatePolicyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicatePolicyException ex) {
        return response(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidRequestException ex) {
        return response(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), Map.of());
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status, String error, String message, Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), error, message, fields));
    }
}
