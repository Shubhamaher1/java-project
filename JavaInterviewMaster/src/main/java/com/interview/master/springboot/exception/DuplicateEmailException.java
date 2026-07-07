package com.interview.master.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create or update an employee with an email
 * address that already belongs to another employee.
 * Maps to HTTP 409 Conflict.
 *
 * 409 Conflict: the request could not be completed due to a conflict with
 * the current state of the target resource. The client should resolve the
 * conflict and re-submit the request.
 *
 * Usage:
 *   throw new DuplicateEmailException("alice@company.com");
 *   // -> "Employee with email already exists: alice@company.com"
 *
 *   throw DuplicateEmailException.withMessage("Custom conflict message");
 *   // -> "Custom conflict message"
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends BusinessException {

    /**
     * Primary constructor: wraps the given email in a standard message.
     * @param email the duplicate email address
     */
    public DuplicateEmailException(String email) {
        super("Employee with email already exists: " + email, "DUPLICATE_EMAIL");
    }

    /**
     * Static factory for cases where a fully-formed message is preferred.
     * Avoids the ambiguity of two single-String constructors.
     */
    public static DuplicateEmailException withMessage(String message) {
        return new DuplicateEmailException(message) {
            // anonymous subclass just to reuse parent; message passed through
        };
    }
}
