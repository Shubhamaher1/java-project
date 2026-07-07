package com.interview.master.springboot.controller;

import com.interview.master.springboot.exception.EmployeeNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * ============================================================
 * SPRING MVC / REST CONTROLLER - Complete Interview Guide
 * ============================================================
 *
 * @RestController = @Controller + @ResponseBody
 *   - @Controller         : marks class as Spring MVC controller (returns view name by default)
 *   - @ResponseBody       : serialize return value directly to HTTP response body (JSON/XML)
 *   - Combined effect     : every method returns data (JSON), NOT a view name
 *
 * @RequestMapping - maps HTTP requests to handler methods
 * Shortcut Annotations (introduced in Spring 4.3):
 *   @GetMapping    = @RequestMapping(method = RequestMethod.GET)
 *   @PostMapping   = @RequestMapping(method = RequestMethod.POST)
 *   @PutMapping    = @RequestMapping(method = RequestMethod.PUT)
 *   @PatchMapping  = @RequestMapping(method = RequestMethod.PATCH)
 *   @DeleteMapping = @RequestMapping(method = RequestMethod.DELETE)
 *
 * HTTP Methods & Semantics:
 *   GET    - Retrieve resource        | idempotent: YES | safe: YES | body: NO
 *   POST   - Create resource          | idempotent: NO  | safe: NO  | body: YES
 *   PUT    - Replace entire resource  | idempotent: YES | safe: NO  | body: YES
 *   PATCH  - Partial update           | idempotent: NO* | safe: NO  | body: YES
 *   DELETE - Delete resource          | idempotent: YES | safe: NO  | body: NO
 *   HEAD   - Like GET but no body     | idempotent: YES | safe: YES | body: NO
 *   OPTIONS- Describe communication   | idempotent: YES | safe: YES | body: NO
 *
 * Idempotent: calling the operation multiple times = same result as calling once
 * Safe: operation doesn't modify server state (read-only)
 *
 * ResponseEntity<T>:
 *   - Full control over HTTP response: status code, headers, body
 *   - ResponseEntity.ok(body)           -> 200 OK with body
 *   - ResponseEntity.created(uri).body  -> 201 Created with Location header
 *   - ResponseEntity.noContent()        -> 204 No Content
 *   - ResponseEntity.notFound()         -> 404 Not Found (no body)
 *   - ResponseEntity.status(409).body() -> custom status with body
 *
 * Parameter Binding Annotations:
 *   @PathVariable  - extract value from URI template: /employees/{id}
 *   @RequestParam  - extract query/form param: /employees?dept=IT&active=true
 *   @RequestBody   - deserialize JSON/XML request body to Java object (uses HttpMessageConverter)
 *   @RequestHeader - extract HTTP header: @RequestHeader("Authorization")
 *   @CookieValue   - extract cookie: @CookieValue("sessionId")
 *   @ModelAttribute- bind request data to model object (typically form data)
 *   @MatrixVariable- extract key-value pairs from URI path segment: /employees;dept=IT
 *
 * Validation Annotations:
 *   @Valid      - triggers javax/jakarta Bean Validation (JSR-380) on parameter
 *   @Validated  - Spring's variant; also supports validation groups
 *   Both annotations cause MethodArgumentNotValidException on failure
 *
 * HTTP Status Codes (essential ones):
 *   2xx Success:
 *     200 OK           - successful GET, PUT, PATCH
 *     201 Created      - successful POST (include Location header)
 *     202 Accepted     - request accepted for async processing
 *     204 No Content   - successful DELETE (no body to return)
 *   3xx Redirection:
 *     301 Moved Permanently
 *     304 Not Modified - used with ETags for caching
 *   4xx Client Error:
 *     400 Bad Request  - invalid input / validation failure
 *     401 Unauthorized - authentication required
 *     403 Forbidden    - authenticated but not authorized
 *     404 Not Found    - resource doesn't exist
 *     405 Method Not Allowed
 *     409 Conflict     - state conflict (e.g., duplicate email)
 *     422 Unprocessable Entity - semantically invalid request
 *     429 Too Many Requests    - rate limiting
 *   5xx Server Error:
 *     500 Internal Server Error - unexpected server error
 *     502 Bad Gateway
 *     503 Service Unavailable
 *
 * REST API Best Practices:
 *   1. Use NOUNS for resources, not verbs: /employees not /getEmployees
 *   2. Use HTTP methods to express actions (GET/POST/PUT/DELETE)
 *   3. Use plural nouns for collections: /employees not /employee
 *   4. Nest for relationships: /employees/{id}/departments
 *   5. Use proper HTTP status codes
 *   6. Version your API: /api/v1/employees
 *   7. Use HATEOAS for discoverability (HAL format)
 *   8. Support filtering, sorting, pagination for collections
 *   9. Return meaningful error messages
 *   10. Use HTTPS in production
 *
 * HATEOAS (Hypermedia As The Engine Of Application State):
 *   - REST constraint: responses include links to related resources
 *   - Client navigates API by following links (self-descriptive)
 *   - HAL (Hypertext Application Language): _links, _embedded conventions
 *   - Spring HATEOAS library provides EntityModel<T>, CollectionModel<T>
 *
 * Content Negotiation:
 *   - Client sends Accept header: Accept: application/json
 *   - Server sends Content-Type header: Content-Type: application/json
 *   - Spring uses HttpMessageConverter to serialize/deserialize
 *   - Jackson: JSON, JAXB: XML
 *
 * Interview Questions:
 *   Q: Difference between @Controller and @RestController?
 *   A: @Controller returns view names (used with Thymeleaf/JSP).
 *      @RestController = @Controller + @ResponseBody; returns data (JSON/XML).
 *
 *   Q: When to use PUT vs PATCH?
 *   A: PUT replaces entire resource (send all fields). PATCH updates specific fields.
 *
 *   Q: What is idempotency and why does it matter?
 *   A: Idempotent = multiple identical requests = same result as one request.
 *      Matters for retries on network failure. GET/PUT/DELETE are idempotent.
 *
 *   Q: Why return ResponseEntity instead of just the object?
 *   A: ResponseEntity gives control over status code and headers.
 *      Returning object alone always returns 200 OK.
 * ============================================================
 */
@RestController
@RequestMapping("/api/v1/employees")
@Validated  // enables method-level validation with @Min, @NotBlank, etc.
public class EmployeeController {

    // -------------------------------------------------------
    // In-memory store (replaces actual Service/Repository for demo)
    // In real app: @Autowired EmployeeService employeeService;
    // -------------------------------------------------------
    private final Map<Long, EmployeeDTO> employeeStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public EmployeeController() {
        // seed some data
        EmployeeDTO e1 = new EmployeeDTO(1L, "Alice Johnson", "alice@company.com", "Engineering", 95000.0, "SENIOR", LocalDateTime.now());
        EmployeeDTO e2 = new EmployeeDTO(2L, "Bob Smith",     "bob@company.com",   "Marketing",   75000.0, "MID",    LocalDateTime.now());
        EmployeeDTO e3 = new EmployeeDTO(3L, "Carol White",   "carol@company.com", "Engineering", 110000.0,"LEAD",   LocalDateTime.now());
        EmployeeDTO e4 = new EmployeeDTO(4L, "David Brown",   "david@company.com", "HR",          65000.0, "JUNIOR", LocalDateTime.now());
        employeeStore.put(1L, e1);
        employeeStore.put(2L, e2);
        employeeStore.put(3L, e3);
        employeeStore.put(4L, e4);
        idGenerator.set(5L);
    }

    // ==========================================================
    // GET /api/v1/employees
    // GET /api/v1/employees?department=Engineering
    // ==========================================================
    /**
     * GET all employees, optionally filtered by department.
     *
     * @RequestParam(required = false) means the param is optional.
     * If department is null, return all. If provided, filter.
     *
     * Returns: 200 OK with list in body
     */
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        List<EmployeeDTO> employees = new ArrayList<>(employeeStore.values());

        // Filter by department if provided
        if (department != null && !department.isBlank()) {
            employees = employees.stream()
                    .filter(e -> e.getDepartment().equalsIgnoreCase(department))
                    .collect(Collectors.toList());
        }

        // ResponseEntity.ok() is shorthand for ResponseEntity.status(200).body(...)
        return ResponseEntity.ok(employees);
    }

    // ==========================================================
    // GET /api/v1/employees/{id}
    // ==========================================================
    /**
     * GET employee by ID.
     *
     * @PathVariable - extracts {id} from URI path
     * Throws EmployeeNotFoundException (handled by GlobalExceptionHandler -> 404)
     *
     * Returns: 200 OK with employee, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @PathVariable @Min(value = 1, message = "ID must be a positive number") Long id) {

        EmployeeDTO employee = findEmployeeOrThrow(id);
        return ResponseEntity.ok(employee);
    }

    // ==========================================================
    // GET /api/v1/employees/search?name=john
    // ==========================================================
    /**
     * Search employees by name (case-insensitive, partial match).
     *
     * @RequestParam(required = true) is the default (required = true if not specified).
     * @NotBlank ensures the name is not empty.
     *
     * Returns: 200 OK with matching employees (empty list if none found)
     */
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> searchByName(
            @RequestParam @NotBlank(message = "Search name cannot be blank") String name) {

        List<EmployeeDTO> results = employeeStore.values().stream()
                .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        // Note: returning 200 with empty list is better than 404 for searches
        return ResponseEntity.ok(results);
    }

    // ==========================================================
    // GET /api/v1/employees/page?page=0&size=10&sort=salary,desc
    // ==========================================================
    /**
     * Paginated employee listing with sorting.
     *
     * Pagination concepts:
     *   page  - zero-based page index (first page = 0)
     *   size  - number of records per page
     *   sort  - field,direction (e.g., salary,desc or name,asc)
     *
     * In real app with JPA:
     *   Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
     *   Page<Employee> result = employeeRepository.findAll(pageable);
     *
     * Returns: 200 OK with Page object containing content + pagination metadata
     */
    @GetMapping("/page")
    public ResponseEntity<PageResponse<EmployeeDTO>> getEmployeesPaginated(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {

        // Validate parameters
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10;

        List<EmployeeDTO> all = new ArrayList<>(employeeStore.values());

        // Sort
        Comparator<EmployeeDTO> comparator = switch (sortBy.toLowerCase()) {
            case "salary" -> Comparator.comparingDouble(EmployeeDTO::getSalary);
            case "name"   -> Comparator.comparing(EmployeeDTO::getName);
            default       -> Comparator.comparing(EmployeeDTO::getName);
        };
        if ("desc".equalsIgnoreCase(sortDir)) comparator = comparator.reversed();
        all.sort(comparator);

        // Paginate
        int totalElements = all.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<EmployeeDTO> content = all.subList(fromIndex, toIndex);

        PageResponse<EmployeeDTO> response = new PageResponse<>(
                content, page, size, totalElements, totalPages);

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // GET /api/v1/employees/department/{dept}/stats
    // ==========================================================
    /**
     * Department statistics.
     *
     * Demonstrates using a DTO projection to return computed data.
     * In production: use JPQL aggregation query or custom repository method.
     *
     * Returns: 200 OK with DepartmentStats DTO
     */
    @GetMapping("/department/{dept}/stats")
    public ResponseEntity<DepartmentStats> getDepartmentStats(@PathVariable String dept) {
        List<EmployeeDTO> deptEmployees = employeeStore.values().stream()
                .filter(e -> e.getDepartment().equalsIgnoreCase(dept))
                .collect(Collectors.toList());

        if (deptEmployees.isEmpty()) {
            // Return 404 if department doesn't exist
            throw new EmployeeNotFoundException("No employees found in department: " + dept);
        }

        DoubleSummaryStatistics stats = deptEmployees.stream()
                .mapToDouble(EmployeeDTO::getSalary)
                .summaryStatistics();

        DepartmentStats result = new DepartmentStats(
                dept,
                deptEmployees.size(),
                stats.getAverage(),
                stats.getMin(),
                stats.getMax(),
                stats.getSum()
        );

        return ResponseEntity.ok(result);
    }

    // ==========================================================
    // POST /api/v1/employees
    // ==========================================================
    /**
     * Create a new employee.
     *
     * @Valid triggers Bean Validation on CreateEmployeeRequest.
     * If validation fails -> MethodArgumentNotValidException -> 400 Bad Request
     * (handled by GlobalExceptionHandler)
     *
     * @RequestBody uses HttpMessageConverter (Jackson) to deserialize JSON body.
     * Content-Type: application/json must be set in request.
     *
     * Returns: 201 Created with Location header pointing to new resource
     * Location: /api/v1/employees/5
     *
     * Best practice: POST returns 201, not 200
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {

        // Check for duplicate email
        boolean emailExists = employeeStore.values().stream()
                .anyMatch(e -> e.getEmail().equalsIgnoreCase(request.getEmail()));
        if (emailExists) {
            // 409 Conflict - throw DuplicateEmailException
            throw new com.interview.master.springboot.exception.DuplicateEmailException(
                    "Employee with email already exists: " + request.getEmail());
        }

        long newId = idGenerator.getAndIncrement();
        EmployeeDTO newEmployee = new EmployeeDTO(
                newId,
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                request.getSalary(),
                request.getLevel(),
                LocalDateTime.now()
        );
        employeeStore.put(newId, newEmployee);

        // Build Location URI: /api/v1/employees/{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId)
                .toUri();

        // ResponseEntity.created(uri) sets status=201 and Location header
        return ResponseEntity.created(location).body(newEmployee);
    }

    // ==========================================================
    // PUT /api/v1/employees/{id}
    // ==========================================================
    /**
     * Full update (replace) of an employee.
     *
     * PUT semantics: client sends COMPLETE representation of resource.
     * Any field not included is set to null/default.
     * Idempotent: same request twice = same result.
     *
     * Returns: 200 OK with updated employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody CreateEmployeeRequest request) {

        // Throw 404 if not found
        EmployeeDTO existing = findEmployeeOrThrow(id);

        // Full replacement
        EmployeeDTO updated = new EmployeeDTO(
                id,
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                request.getSalary(),
                request.getLevel(),
                existing.getCreatedAt()  // preserve original creation timestamp
        );
        employeeStore.put(id, updated);

        return ResponseEntity.ok(updated);
    }

    // ==========================================================
    // PATCH /api/v1/employees/{id}/salary
    // ==========================================================
    /**
     * Partial update - update only salary.
     *
     * PATCH semantics: client sends PARTIAL representation.
     * Only the included fields are updated; others remain unchanged.
     *
     * PATCH is NOT necessarily idempotent (e.g., PATCH {increment: 1000}
     * applied twice gives different result).
     *
     * Returns: 200 OK with updated employee
     */
    @PatchMapping("/{id}/salary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateSalary(
            @PathVariable Long id,
            @Valid @RequestBody SalaryUpdateRequest request) {

        EmployeeDTO existing = findEmployeeOrThrow(id);

        // Only update the salary field, keep everything else
        EmployeeDTO updated = new EmployeeDTO(
                existing.getId(),
                existing.getName(),
                existing.getEmail(),
                existing.getDepartment(),
                request.getNewSalary(),   // only this changes
                existing.getLevel(),
                existing.getCreatedAt()
        );
        employeeStore.put(id, updated);

        return ResponseEntity.ok(updated);
    }

    // ==========================================================
    // DELETE /api/v1/employees/{id}
    // ==========================================================
    /**
     * Delete an employee by ID.
     *
     * DELETE semantics: remove the resource.
     * Idempotent: deleting same resource twice = same outcome (resource is gone).
     *
     * Returns: 204 No Content (no body needed after successful delete)
     * Note: Some APIs return 200 with deleted entity. Both are acceptable.
     * Best practice: 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        findEmployeeOrThrow(id);  // 404 if not found
        employeeStore.remove(id);

        // ResponseEntity.noContent() = 204 No Content with no body
        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // Additional Endpoint: GET with custom headers
    // ==========================================================
    /**
     * Demonstrates reading request headers and returning custom response headers.
     *
     * @RequestHeader("X-Correlation-ID") - extracts custom header from request.
     * Used for distributed tracing / request correlation.
     *
     * Returns correlation ID back in response header.
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<EmployeeDTO> getEmployeeDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-Correlation-ID", required = false,
                           defaultValue = "no-correlation-id") String correlationId,
            @RequestHeader(value = "Accept-Language", required = false,
                           defaultValue = "en") String language) {

        EmployeeDTO employee = findEmployeeOrThrow(id);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Correlation-ID", correlationId);
        responseHeaders.set("X-Response-Time", LocalDateTime.now().toString());

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(employee);
    }

    // ==========================================================
    // Helper method
    // ==========================================================
    private EmployeeDTO findEmployeeOrThrow(Long id) {
        EmployeeDTO employee = employeeStore.get(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee not found with id: " + id);
        }
        return employee;
    }

    // ==========================================================
    // Inner DTOs and Request/Response Objects
    // (In real project: separate files in dto/ package)
    // ==========================================================

    /**
     * EmployeeDTO - Data Transfer Object
     * Decouples API representation from domain/entity model.
     * Never expose JPA entity directly in API (security, coupling, performance).
     */
    public static class EmployeeDTO {
        private Long id;
        private String name;
        private String email;
        private String department;
        private Double salary;
        private String level;
        private LocalDateTime createdAt;

        public EmployeeDTO() {}

        public EmployeeDTO(Long id, String name, String email, String department,
                           Double salary, String level, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.department = department;
            this.salary = salary;
            this.level = level;
            this.createdAt = createdAt;
        }

        // Getters
        public Long getId()                   { return id; }
        public String getName()               { return name; }
        public String getEmail()              { return email; }
        public String getDepartment()         { return department; }
        public Double getSalary()             { return salary; }
        public String getLevel()              { return level; }
        public LocalDateTime getCreatedAt()   { return createdAt; }

        // Setters
        public void setId(Long id)                       { this.id = id; }
        public void setName(String name)                 { this.name = name; }
        public void setEmail(String email)               { this.email = email; }
        public void setDepartment(String department)     { this.department = department; }
        public void setSalary(Double salary)             { this.salary = salary; }
        public void setLevel(String level)               { this.level = level; }
        public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    }

    /**
     * CreateEmployeeRequest - request body DTO with validation annotations.
     *
     * Bean Validation (JSR-380) annotations:
     *   @NotNull   - field cannot be null
     *   @NotBlank  - field cannot be null, empty, or whitespace (Strings only)
     *   @NotEmpty  - field cannot be null or empty (works on strings, collections)
     *   @Size      - validates length/size
     *   @Min/@Max  - numeric boundaries
     *   @Email     - validates email format
     *   @Pattern   - validates against regex
     *   @Positive  - must be > 0
     *   @Future    - date must be in future
     *   @Past      - date must be in past
     */
    public static class CreateEmployeeRequest {

        @jakarta.validation.constraints.NotBlank(message = "Name is required")
        @jakarta.validation.constraints.Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        private String name;

        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Email must be a valid email address")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Department is required")
        private String department;

        @jakarta.validation.constraints.NotNull(message = "Salary is required")
        @jakarta.validation.constraints.DecimalMin(value = "0.0", inclusive = false,
                message = "Salary must be positive")
        @jakarta.validation.constraints.DecimalMax(value = "999999.99",
                message = "Salary cannot exceed 999,999.99")
        private Double salary;

        @jakarta.validation.constraints.Pattern(
                regexp = "INTERN|JUNIOR|MID|SENIOR|LEAD|PRINCIPAL",
                message = "Level must be one of: INTERN, JUNIOR, MID, SENIOR, LEAD, PRINCIPAL")
        private String level;

        public CreateEmployeeRequest() {}

        public String getName()       { return name; }
        public String getEmail()      { return email; }
        public String getDepartment() { return department; }
        public Double getSalary()     { return salary; }
        public String getLevel()      { return level; }

        public void setName(String name)             { this.name = name; }
        public void setEmail(String email)           { this.email = email; }
        public void setDepartment(String department) { this.department = department; }
        public void setSalary(Double salary)         { this.salary = salary; }
        public void setLevel(String level)           { this.level = level; }
    }

    /**
     * SalaryUpdateRequest - request body for PATCH /employees/{id}/salary
     */
    public static class SalaryUpdateRequest {

        @jakarta.validation.constraints.NotNull(message = "New salary is required")
        @jakarta.validation.constraints.Positive(message = "Salary must be positive")
        private Double newSalary;

        public SalaryUpdateRequest() {}
        public Double getNewSalary() { return newSalary; }
        public void setNewSalary(Double newSalary) { this.newSalary = newSalary; }
    }

    /**
     * PageResponse<T> - paginated response wrapper.
     * Spring Data Page<T> is not directly serializable in all configurations.
     * Custom wrapper gives explicit control over JSON structure.
     */
    public static class PageResponse<T> {
        private List<T> content;
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        public PageResponse(List<T> content, int currentPage, int pageSize,
                            long totalElements, int totalPages) {
            this.content = content;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = (currentPage == 0);
            this.last  = (currentPage == totalPages - 1);
        }

        public List<T> getContent()       { return content; }
        public int getCurrentPage()        { return currentPage; }
        public int getPageSize()           { return pageSize; }
        public long getTotalElements()     { return totalElements; }
        public int getTotalPages()         { return totalPages; }
        public boolean isFirst()           { return first; }
        public boolean isLast()            { return last; }
    }

    /**
     * DepartmentStats - aggregated statistics DTO.
     */
    public static class DepartmentStats {
        private String department;
        private int headCount;
        private double averageSalary;
        private double minSalary;
        private double maxSalary;
        private double totalPayroll;

        public DepartmentStats(String department, int headCount,
                               double averageSalary, double minSalary,
                               double maxSalary, double totalPayroll) {
            this.department = department;
            this.headCount = headCount;
            this.averageSalary = averageSalary;
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
            this.totalPayroll = totalPayroll;
        }

        public String getDepartment()   { return department; }
        public int getHeadCount()        { return headCount; }
        public double getAverageSalary() { return averageSalary; }
        public double getMinSalary()     { return minSalary; }
        public double getMaxSalary()     { return maxSalary; }
        public double getTotalPayroll()  { return totalPayroll; }
    }
}
