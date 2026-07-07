package com.interview.master.springboot.dto;

import com.interview.master.springboot.entity.Employee.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * DTO (Data Transfer Object) Pattern - Complete Interview Guide
 * ============================================================
 *
 * Q: What is a DTO?
 *    An object used to transfer data between layers (Controller <-> Service <-> Client).
 *    It carries data but has NO business logic.
 *
 * Q: Why use DTOs instead of exposing JPA Entities directly in REST APIs?
 *
 *    1. SECURITY:
 *       Entities may have sensitive fields (passwords, internal audit columns, version fields).
 *       A DTO exposes only what the client needs to know.
 *       Example: Employee has 'version' (optimistic locking) — clients don't need this.
 *
 *    2. DECOUPLING (API contract vs DB schema):
 *       Your DB schema can change without breaking the API contract.
 *       Example: rename DB column 'emp_name' -> 'first_name'; DTO stays the same.
 *
 *    3. PERFORMANCE (Projection):
 *       Load and transfer only needed fields instead of the full entity graph.
 *       Example: A list view only needs id + name + department, not the full employee.
 *
 *    4. AVOID SERIALIZATION ISSUES:
 *       Bidirectional JPA relationships cause infinite recursion in Jackson JSON serialization.
 *       Employee -> Department -> List<Employee> -> Employee -> ... -> StackOverflowError.
 *       DTOs break this cycle by flattening the structure.
 *
 *    5. DIFFERENT SHAPES PER USE CASE:
 *       Create request: no id, all fields required.
 *       Update request: optional fields.
 *       Response: includes audit timestamps, computed fields.
 *
 *    6. VERSIONING:
 *       You can have EmployeeDTOv1 and EmployeeDTOv2 without changing the Entity.
 *
 * Q: DTO vs Value Object (VO)?
 *    DTO: Only for transferring data; may be mutable; equals/hashCode not critical.
 *    VO : Part of domain model; IMMUTABLE; equals/hashCode based on ALL fields (value equality).
 *    Example VO: Money{amount=100, currency="USD"} — two Money objects with same values are equal.
 *
 * Q: DTO vs Entity?
 *    Entity: Represents a DB row; managed by JPA; has lifecycle states; has relationships.
 *    DTO   : Plain Java object; no JPA annotations; used in API/service layer only.
 *
 * Q: How to map between Entity and DTO?
 *    Option 1: Manual mapping (in service layer or a dedicated mapper class) — most control.
 *    Option 2: MapStruct — compile-time code generation, type-safe, fast.
 *    Option 3: ModelMapper — reflection-based, runtime, flexible but slower.
 *    Recommended: MapStruct for production (zero runtime overhead).
 *
 * ============================================================
 */
public class EmployeeDTO {

    // ============================================================
    // RESPONSE DTO - Used when returning employee data to clients
    // ============================================================
    // Contains all fields that are safe to expose externally.
    // Includes audit timestamps (read-only, set by server).
    // Uses @Value (Lombok) for an IMMUTABLE DTO — preferred for response objects.
    // ============================================================

    /**
     * EmployeeResponse - returned by GET /employees, GET /employees/{id}, etc.
     *
     * @Value makes the class:
     *  - final (cannot be extended)
     *  - all fields private final (immutable)
     *  - generates all-args constructor, getters, equals, hashCode, toString
     *  - no setters (immutability)
     *
     * Immutable DTOs are thread-safe by design and easier to reason about.
     */
    @Value
    @Builder
    public static class EmployeeResponse {
        Long id;
        String firstName;
        String lastName;
        String email;
        Double salary;
        String department;
        Integer age;
        String phone;
        EmployeeStatus status;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        // NOTE: No 'version' field — internal optimistic locking detail not exposed
        // NOTE: No 'skills' collection by default — use a separate endpoint for details
    }

    // ============================================================
    // SUMMARY DTO - Lightweight projection for list views
    // ============================================================
    // When returning a list of 1000 employees, sending full objects wastes bandwidth.
    // This projection only sends the fields needed for a table/list display.
    // ============================================================

    /**
     * EmployeeSummary - lightweight view for list APIs.
     * GET /employees returns List<EmployeeSummary> (not full EmployeeResponse).
     */
    @Value
    @Builder
    public static class EmployeeSummary {
        Long id;
        String firstName;
        String lastName;
        String department;
        EmployeeStatus status;
        // Only 5 fields instead of 12 — significantly reduces JSON payload size
    }

    // ============================================================
    // CREATE REQUEST DTO - Used for POST /employees
    // ============================================================
    // - No 'id' field (server generates it)
    // - No audit timestamps (server sets them)
    // - Bean Validation annotations enforce business rules
    // - @Data (Lombok) for mutable request objects (setters needed for JSON deserialization)
    //
    // Interview Q: Why use @Data on request DTOs but @Value on response DTOs?
    //   Request DTOs need setters so Jackson can deserialize JSON -> Java object.
    //   Response DTOs are immutable (no setters) because once built, they don't change.
    // ============================================================

    /**
     * EmployeeCreateRequest - request body for POST /employees.
     *
     * @Data generates: getters, setters, toString, equals, hashCode, requiredArgsConstructor.
     * @NoArgsConstructor: needed for Jackson deserialization (Jackson calls no-arg constructor
     *                      first, then sets fields via setters or reflection).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeCreateRequest {

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be 2-100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be 2-100 characters")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.01", message = "Salary must be greater than 0")
        private Double salary;

        @NotBlank(message = "Department is required")
        private String department;

        @Min(value = 18, message = "Employee must be at least 18 years old")
        @Max(value = 100, message = "Age must be realistic (max 100)")
        private Integer age;

        @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Phone must be 10-13 digits")
        private String phone;

        // Status defaults to ACTIVE on creation — not user-supplied
        // (if you want to allow it, add: private EmployeeStatus status;)
    }

    // ============================================================
    // UPDATE REQUEST DTO - Used for PUT /employees/{id}
    // ============================================================
    // All fields are optional (null = keep existing value).
    // No validation annotations that enforce @NotBlank/@NotNull — the field may be absent.
    // Service layer checks which fields are non-null and applies them selectively.
    //
    // Interview Q: PUT vs PATCH?
    //   PUT   : Replace the ENTIRE resource. Missing fields are set to null/default.
    //           Client must send the full object.
    //   PATCH : Partial update. Only send the fields you want to change.
    //           Missing fields are left unchanged.
    //   This UpdateRequest is designed for PATCH semantics (null = unchanged).
    // ============================================================

    /**
     * EmployeeUpdateRequest - request body for PUT/PATCH /employees/{id}.
     * All fields are optional — only non-null fields will be updated.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeUpdateRequest {

        // Validation annotations still apply when the field IS provided
        @Size(min = 2, max = 100, message = "First name must be 2-100 characters")
        private String firstName;

        @Size(min = 2, max = 100, message = "Last name must be 2-100 characters")
        private String lastName;

        @Email(message = "Must be a valid email address")
        private String email;

        @DecimalMin(value = "0.01", message = "Salary must be greater than 0")
        private Double salary;

        private String department;

        @Min(value = 18, message = "Employee must be at least 18 years old")
        @Max(value = 100, message = "Age must be realistic (max 100)")
        private Integer age;

        @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Phone must be 10-13 digits")
        private String phone;

        private EmployeeStatus status;
    }

    // ============================================================
    // API RESPONSE WRAPPER - Generic envelope for all API responses
    // ============================================================
    // Interview Q: Why wrap API responses in a standard envelope?
    //
    //   1. Consistency: every endpoint returns the same shape.
    //      Clients can always access .data, .success, .message.
    //   2. Error handling: .success=false + .message="Not found" instead of 404 body variation.
    //   3. Metadata: add pagination info, request ID, timestamp without changing the data type.
    //   4. Versioning: wrap with version metadata without changing individual DTOs.
    //
    // Using a generic <T> means one class serves all response types:
    //   ApiResponse<EmployeeResponse>           -- single employee
    //   ApiResponse<List<EmployeeResponse>>     -- list of employees
    //   ApiResponse<PagedResponse<EmployeeResponse>> -- paginated employees
    //   ApiResponse<Void>                       -- operations with no return value
    // ============================================================

    /**
     * ApiResponse<T> - standard envelope for all REST API responses.
     *
     * @Value + @Builder: immutable, built via builder pattern.
     *
     * Example JSON:
     * {
     *   "success": true,
     *   "message": "Employee created successfully",
     *   "data": { "id": 1, "firstName": "John", ... },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    @Value
    @Builder
    public static class ApiResponse<T> {
        boolean success;
        String message;
        T data;               // the actual payload (generic)
        LocalDateTime timestamp;

        // ---- Static factory methods (convenience builders) ----

        /**
         * Creates a successful response with data.
         * Usage: ApiResponse.success("Employee found", employeeResponse)
         */
        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        }

        /**
         * Creates a successful response without data (e.g., DELETE operations).
         * Usage: ApiResponse.success("Employee deleted successfully")
         */
        public static <T> ApiResponse<T> success(String message) {
            return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        }

        /**
         * Creates an error response.
         * Usage: ApiResponse.error("Employee not found")
         */
        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    // ============================================================
    // PAGED RESPONSE - Wraps paginated data
    // ============================================================
    // Spring Data's Page<T> is not serialization-friendly for custom API responses.
    // We extract the fields we want to expose and put them in this clean DTO.
    //
    // Interview Q: What is the difference between Page<T> and Slice<T>?
    //   Page<T>  : Extends Slice<T>. Runs a COUNT(*) query to know totalElements/totalPages.
    //              Use when the UI needs to show "Page 3 of 47".
    //   Slice<T> : No COUNT query. Only knows if there is a NEXT page (hasNext()).
    //              More performant. Use for infinite scroll / "load more" UIs.
    // ============================================================

    /**
     * PagedResponse<T> - wraps paginated API results.
     *
     * Example JSON:
     * {
     *   "content": [...],
     *   "pageNumber": 0,
     *   "pageSize": 10,
     *   "totalElements": 150,
     *   "totalPages": 15,
     *   "last": false,
     *   "first": true
     * }
     */
    @Value
    @Builder
    public static class PagedResponse<T> {
        List<T> content;        // the data items for this page
        int pageNumber;         // current page (0-indexed)
        int pageSize;           // items per page
        long totalElements;     // total number of items across ALL pages
        int totalPages;         // total number of pages
        boolean last;           // is this the last page?
        boolean first;          // is this the first page?

        // Interview Q: How do you create a PagedResponse from Spring's Page<T>?
        // In the service/controller:
        //   Page<EmployeeResponse> page = ...;
        //   PagedResponse<EmployeeResponse> response = PagedResponse.<EmployeeResponse>builder()
        //       .content(page.getContent())
        //       .pageNumber(page.getNumber())
        //       .pageSize(page.getSize())
        //       .totalElements(page.getTotalElements())
        //       .totalPages(page.getTotalPages())
        //       .last(page.isLast())
        //       .first(page.isFirst())
        //       .build();
    }

    // ============================================================
    // DEPARTMENT STATS DTO - Example of a custom aggregation result DTO
    // ============================================================
    // Used to return the result of:
    //   SELECT department, COUNT(*), AVG(salary) FROM employees GROUP BY department
    // JPA returns Object[] for such queries — we map those arrays into this DTO.
    // ============================================================

    /**
     * DepartmentStats - aggregated statistics per department.
     */
    @Value
    @Builder
    public static class DepartmentStats {
        String department;
        Long employeeCount;
        Double averageSalary;
        Double minSalary;
        Double maxSalary;
    }
}
