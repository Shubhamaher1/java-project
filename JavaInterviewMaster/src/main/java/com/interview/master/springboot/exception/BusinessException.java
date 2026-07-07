package com.interview.master.springboot.exception;

/**
 * Base class for all application business exceptions.
 *
 * Extends RuntimeException (unchecked) so:
 *   - Callers are NOT forced to catch or declare it (unlike checked exceptions)
 *   - Spring @Transactional rolls back automatically on RuntimeException by default
 *   - For checked exceptions you must add: @Transactional(rollbackFor = CheckedException.class)
 *
 * errorCode field: application-specific machine-readable code returned in error response.
 * Useful for clients to handle specific error types programmatically.
 *
 * Interview Q: Checked vs Unchecked exceptions in Spring?
 *   Checked (extends Exception): compiler forces you to handle or declare.
 *     Examples: IOException, SQLException
 *   Unchecked (extends RuntimeException): no compiler enforcement.
 *     Examples: NullPointerException, IllegalArgumentException
 *   Spring @Transactional: rolls back on RuntimeException/Error by default.
 *   To roll back on a checked exception: @Transactional(rollbackFor = MyCheckedException.class)
 *
 * Interview Q: Why use custom exceptions instead of generic ones?
 *   - Meaningful names make code self-documenting
 *   - @ExceptionHandler can target specific types precisely
 *   - Carry domain-specific data (errorCode, context fields)
 *   - Easier to test and log specific scenarios
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
