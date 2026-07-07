package com.interview.master.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested employee resource cannot be found.
 * Maps to HTTP 404 Not Found.
 *
 * @ResponseStatus on the exception class sets the default HTTP status if the
 * exception bubbles up without being caught by a @ControllerAdvice handler.
 * When GlobalExceptionHandler catches it explicitly, the ResponseEntity
 * status from the handler method takes precedence over this annotation.
 *
 * Interview Q: Does @ResponseStatus on the exception class override the handler?
 *   No. @ExceptionHandler method's ResponseEntity status always wins.
 *   @ResponseStatus on exception class is a fallback used by
 *   ResponseStatusExceptionResolver when no explicit handler exists.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends BusinessException {

    public EmployeeNotFoundException(String message) {
        super(message, "EMPLOYEE_NOT_FOUND");
    }

    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id, "EMPLOYEE_NOT_FOUND");
    }
}
