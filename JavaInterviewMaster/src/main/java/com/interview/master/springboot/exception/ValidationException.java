package com.interview.master.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for business-rule validation failures that go beyond
 * Bean Validation (@NotNull, @Size, etc.).
 *
 * Examples:
 *   - Salary increase exceeds 50% policy limit
 *   - Employee cannot be assigned to a department that is closed
 *   - Start date cannot be in the past for new hires
 *
 * Maps to HTTP 422 Unprocessable Entity:
 *   The server understands the request (valid JSON, correct types)
 *   but cannot process it due to semantic/business rule errors.
 *
 * Interview Q: 400 Bad Request vs 422 Unprocessable Entity?
 *   400: request is syntactically wrong (malformed JSON, wrong type)
 *   422: request is syntactically valid but semantically invalid (business rule violated)
 *   In practice, many APIs use 400 for both. 422 is more precise.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String message, String errorCode) {
        super(message, errorCode);
    }
}
