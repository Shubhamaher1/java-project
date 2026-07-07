package com.interview.master.springboot.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================
 * SPRING @ControllerAdvice / @RestControllerAdvice - Complete Interview Guide
 * ============================================================
 *
 * @ControllerAdvice:
 *   - Specialization of @Component; picked up by component scan automatically
 *   - Applied GLOBALLY to ALL @Controller / @RestController classes
 *   - Centralizes exception handling so each controller stays clean
 *   - Also supports @InitBinder (data binding config) and @ModelAttribute (model data)
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   - Every handler method writes directly to the response body (JSON)
 *   - Use this for pure REST APIs (no view rendering needed)
 *   - We use @ControllerAdvice here to show the full explicit form
 *
 * Scoping @ControllerAdvice to specific controllers / packages:
 *   @ControllerAdvice(basePackages = "com.interview.master.springboot.controller")
 *   @ControllerAdvice(assignableTypes = {EmployeeController.class})
 *   @ControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
 *
 * @ExceptionHandler:
 *   - Marks a method as the handler for one or more exception types
 *   - Handle multiple: @ExceptionHandler({ExceptionA.class, ExceptionB.class})
 *   - Spring resolves the MOST SPECIFIC handler (closest in the exception hierarchy)
 *   - Method can receive: the Exception, HttpServletRequest, HttpServletResponse,
 *     Model, Principal, Locale, @RequestHeader params, WebRequest, etc.
 *
 * Exception Handler Resolution Priority:
 *   1. @ExceptionHandler in the SAME @Controller class (local, highest priority)
 *   2. @ExceptionHandler in @ControllerAdvice (global)
 *   3. @ResponseStatus on the exception class itself (fallback)
 *   4. Spring's DefaultHandlerExceptionResolver (handles Spring MVC built-ins)
 *   5. Servlet container /error page (last resort - HTML error page)
 *
 * Interview Q: @ControllerAdvice vs local @ExceptionHandler?
 *   Local: only handles exceptions from that one controller. Good for controller-specific cases.
 *   Global (@ControllerAdvice): handles from ALL controllers. Avoids duplication.
 *
 * Interview Q: What if two @ControllerAdvice beans handle the same exception?
 *   Spring picks based on @Order or Ordered interface. Lower order value = higher priority.
 *   Without @Order, order is undefined (Spring Boot typically processes in declaration order).
 *
 * Interview Q: Can @ExceptionHandler access the original request?
 *   Yes - declare HttpServletRequest as a method parameter; Spring injects it automatically.
 *
 * ErrorResponse DTO:
 *   Standardized error body returned for ALL error responses.
 *   Structure: { timestamp, status, error, message, path, errorCode, validationErrors[] }
 *   - timestamp: when the error occurred (aids debugging / log correlation)
 *   - status: numeric HTTP status (400, 404, 500...)
 *   - error: human-readable status description ("Not Found", "Bad Request")
 *   - message: specific error detail
 *   - path: the request URI that triggered the error
 *   - errorCode: application-specific code for programmatic client handling
 *   - validationErrors: field-level detail for form validation (400 responses only)
 * ============================================================
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================================
    // STANDARDIZED ERROR RESPONSE DTO
    // ==========================================================

    /**
     * ErrorResponse - the JSON body returned for every error.
     * Consistent structure means API clients always know what to parse on failure.
     */
    public static class ErrorResponse {
        private final LocalDateTime timestamp = LocalDateTime.now();
        private int status;
        private String error;
        private String message;
        private String path;
        private String errorCode;
        private List<ValidationError> validationErrors;

        public ErrorResponse(int status, String error, String message, String path) {
            this.status  = status;
            this.error   = error;
            this.message = message;
            this.path    = path;
        }

        /** Nested DTO: one entry per failing field in @Valid validation. */
        public static class ValidationError {
            private final String field;
            private final Object rejectedValue;
            private final String message;

            public ValidationError(String field, Object rejectedValue, String message) {
                this.field         = field;
                this.rejectedValue = rejectedValue;
                this.message       = message;
            }

            public String getField()           { return field; }
            public Object getRejectedValue()   { return rejectedValue; }
            public String getMessage()         { return message; }
        }

        // Getters
        public LocalDateTime getTimestamp()                      { return timestamp; }
        public int getStatus()                                   { return status; }
        public String getError()                                 { return error; }
        public String getMessage()                               { return message; }
        public String getPath()                                  { return path; }
        public String getErrorCode()                             { return errorCode; }
        public List<ValidationError> getValidationErrors()       { return validationErrors; }

        // Setters
        public void setStatus(int status)                        { this.status = status; }
        public void setError(String error)                       { this.error = error; }
        public void setMessage(String message)                   { this.message = message; }
        public void setPath(String path)                         { this.path = path; }
        public void setErrorCode(String errorCode)               { this.errorCode = errorCode; }
        public void setValidationErrors(List<ValidationError> v) { this.validationErrors = v; }
    }

    // ==========================================================
    // 1. EmployeeNotFoundException -> 404 Not Found
    // ==========================================================
    /**
     * Handles domain-level "not found" errors.
     * HttpServletRequest is injected by Spring — gives us the request URI for the error body.
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found",
                ex.getMessage(), request.getRequestURI());
        body.setErrorCode(ex.getErrorCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ==========================================================
    // 2. DuplicateEmailException -> 409 Conflict
    // ==========================================================
    /**
     * 409 Conflict = request cannot complete due to current resource state.
     * Example: POST /employees with an email that already exists.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(), "Conflict",
                ex.getMessage(), request.getRequestURI());
        body.setErrorCode(ex.getErrorCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ==========================================================
    // 3. BusinessException (catch-all for domain exceptions) -> 400
    // ==========================================================
    /**
     * Spring picks the MOST SPECIFIC handler, so EmployeeNotFoundException
     * and DuplicateEmailException (which extend BusinessException) are caught
     * by their own handlers above. This handler only fires for other
     * BusinessException subclasses not explicitly handled.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Business Rule Violation",
                ex.getMessage(), request.getRequestURI());
        body.setErrorCode(ex.getErrorCode());
        return ResponseEntity.badRequest().body(body);
    }

    // ==========================================================
    // 4. ValidationException -> 422 Unprocessable Entity
    // ==========================================================
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity",
                ex.getMessage(), request.getRequestURI());
        body.setErrorCode(ex.getErrorCode());
        return ResponseEntity.unprocessableEntity().body(body);
    }

    // ==========================================================
    // 5. MethodArgumentNotValidException -> 400 Bad Request
    //    Triggered by @Valid / @Validated on @RequestBody
    // ==========================================================
    /**
     * Thrown when @Valid fails on a @RequestBody parameter.
     * Contains a BindingResult with ALL validation errors — not just the first one.
     *
     * BindingResult:
     *   getFieldErrors()  -> List<FieldError>  (field-level: @NotNull, @Size, @Email...)
     *   getGlobalErrors() -> List<ObjectError> (class-level / cross-field constraints)
     *
     * FieldError accessors:
     *   getField()          : "email", "salary"
     *   getRejectedValue()  : the submitted value that failed (null, "", -5)
     *   getDefaultMessage() : "Email must be a valid email address"
     *
     * Interview Q: How do you return ALL validation errors, not just the first?
     *   MethodArgumentNotValidException.getBindingResult().getFieldErrors()
     *   returns a List — iterate it and include all in the response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.ValidationError> errors = new ArrayList<>();

        // Field-level errors (e.g. @Email failed on the "email" field)
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ErrorResponse.ValidationError(
                    fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
        }
        // Object/global errors (cross-field constraints, e.g. @ScriptAssert)
        for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            errors.add(new ErrorResponse.ValidationError(
                    oe.getObjectName(), null, oe.getDefaultMessage()));
        }

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed",
                "Request contains " + errors.size() + " validation error(s)",
                request.getRequestURI());
        body.setErrorCode("VALIDATION_FAILED");
        body.setValidationErrors(errors);
        return ResponseEntity.badRequest().body(body);
    }

    // ==========================================================
    // 6. ConstraintViolationException -> 400 Bad Request
    //    Triggered by @Validated on @PathVariable / @RequestParam
    // ==========================================================
    /**
     * ConstraintViolationException fires when @Validated fails on method
     * parameters directly (not @RequestBody). For example:
     *   GET /api/v1/employees/-1   where @Min(1) is on the id @PathVariable
     *
     * Interview Q: Difference between MethodArgumentNotValidException and ConstraintViolationException?
     *   MethodArgumentNotValidException: @Valid / @Validated on @RequestBody -> BindingResult
     *   ConstraintViolationException:    @Validated on @PathVariable, @RequestParam, method params
     *
     * ConstraintViolation accessors:
     *   getPropertyPath() : "methodName.paramName" (e.g. "getById.id")
     *   getInvalidValue() : the bad value (-1)
     *   getMessage()      : "ID must be a positive number"
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ErrorResponse.ValidationError> errors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    // Strip "methodName." prefix to get just the param name
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new ErrorResponse.ValidationError(field, cv.getInvalidValue(), cv.getMessage());
                })
                .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Constraint Violation",
                "Request parameter validation failed",
                request.getRequestURI());
        body.setErrorCode("CONSTRAINT_VIOLATION");
        body.setValidationErrors(errors);
        return ResponseEntity.badRequest().body(body);
    }

    // ==========================================================
    // 7. HttpMessageNotReadableException -> 400 Bad Request
    //    Malformed / unparseable request body JSON
    // ==========================================================
    /**
     * Thrown when Jackson cannot parse the request body.
     * Examples: missing closing brace, wrong type (string where number expected),
     *           missing @RequestBody entirely.
     *
     * Interview Q: HttpMessageNotReadableException vs MethodArgumentNotValidException?
     *   HttpMessageNotReadable: JSON syntax error — can't even deserialize the object
     *   MethodArgumentNotValid: JSON parsed fine, but field values fail @Valid constraints
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Request body is malformed or unreadable: " + ex.getMostSpecificCause().getMessage(),
                request.getRequestURI());
        body.setErrorCode("MALFORMED_JSON");
        return ResponseEntity.badRequest().body(body);
    }

    // ==========================================================
    // 8. HttpRequestMethodNotSupportedException -> 405
    //    Wrong HTTP method used for an endpoint
    // ==========================================================
    /**
     * Example: PUT /api/v1/employees (no collection-level PUT handler)
     * RFC 7231 requires: 405 responses MUST include an Allow header listing supported methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString() : "unknown";

        ErrorResponse body = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed",
                ex.getMethod() + " not supported here. Supported: " + supported,
                request.getRequestURI());
        body.setErrorCode("METHOD_NOT_ALLOWED");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Allow", supported)
                .body(body);
    }

    // ==========================================================
    // 9. NoHandlerFoundException -> 404
    //    No controller mapping matched the request URL
    // ==========================================================
    /**
     * Requires in application.properties:
     *   spring.mvc.throw-exception-if-no-handler-found=true
     *   spring.web.resources.add-mappings=false
     * (otherwise Spring's ResourceHttpRequestHandler intercepts it first)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found",
                "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
                request.getRequestURI());
        body.setErrorCode("ENDPOINT_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ==========================================================
    // 10. Exception (catch-all) -> 500 Internal Server Error
    // ==========================================================
    /**
     * Last-resort handler for any unhandled exception.
     *
     * IMPORTANT: NEVER expose internal details (stack traces, DB error messages)
     * to the client in production. It leaks implementation details and aids attackers.
     *
     * Correct pattern:
     *   - Log FULL details server-side (exception, stack trace, request context)
     *   - Return GENERIC message to the client
     *
     * Interview Q: Why hide the real error message from clients?
     *   Stack traces reveal: library versions, class names, file paths, DB schema details.
     *   This information helps attackers craft targeted exploits.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // In production: log.error("Unhandled exception [{}] {}", request.getRequestURI(), ex.getMessage(), ex);
        System.err.println("[ERROR] Unhandled exception for: " + request.getRequestURI());
        ex.printStackTrace();

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI());
        body.setErrorCode("INTERNAL_ERROR");
        // DO NOT: body.setMessage(ex.getMessage()) — may expose internals
        return ResponseEntity.internalServerError().body(body);
    }

    // ==========================================================
    // HELPER: alternative flat map format for validation errors
    // ==========================================================
    /**
     * Returns errors as Map<fieldName, message> instead of List<ValidationError>.
     * Simpler format, useful when clients just need field -> message for form display.
     *
     * Example:
     *   { "email": "must be a valid email", "salary": "must be positive" }
     */
    private Map<String, String> toFieldErrorMap(MethodArgumentNotValidException ex) {
        Map<String, String> map = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> map.put(fe.getField(), fe.getDefaultMessage()));
        return map;
    }
}
