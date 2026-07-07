package com.interview.master.springboot.service;

import com.interview.master.springboot.dto.EmployeeDTO.*;
import com.interview.master.springboot.entity.Employee;
import com.interview.master.springboot.entity.Employee.EmployeeStatus;
import com.interview.master.springboot.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ============================================================
 * SERVICE IMPLEMENTATION - Complete Interview Guide
 * ============================================================
 *
 * @Service
 * --------
 * Specialization of @Component for the SERVICE LAYER.
 * Functionally identical to @Component — Spring registers it as a bean.
 * Semantic value: signals this class contains BUSINESS LOGIC.
 * Spring's exception translation is also enabled (like @Repository).
 *
 * Three-layer architecture:
 *   Controller (@RestController) — handles HTTP, request/response mapping
 *   Service    (@Service)        — business logic, transactions, caching
 *   Repository (@Repository)     — data access, queries
 *
 * Q: @Component vs @Service vs @Repository vs @Controller?
 *   All four are Spring stereotype annotations — all register beans.
 *   @Repository : adds exception translation (DB exceptions -> DataAccessException).
 *   @Service    : semantic clarity; some AOP pointcuts target it specifically.
 *   @Controller : Spring MVC processes it for HTTP request mapping.
 *   @Component  : generic bean — use when none of the above fit.
 *
 * @RequiredArgsConstructor (Lombok)
 * ---------------------------------
 * Generates a constructor for all FINAL fields.
 * Spring uses CONSTRUCTOR INJECTION (the recommended approach).
 *
 * Q: Constructor injection vs @Autowired field injection?
 *   Constructor injection:
 *     + Dependencies explicit and required — can't create object without them.
 *     + Immutable (final fields) — thread-safe by design.
 *     + Testable without Spring container — just call new EmployeeServiceImpl(mockRepo).
 *     + Detects circular dependencies at startup (not at runtime).
 *   Field injection (@Autowired on field):
 *     - Hides dependencies — not obvious from outside.
 *     - Requires reflection to inject — slower, less transparent.
 *     - Hard to test without Spring (can't set private fields easily).
 *     - Can't make fields final.
 *   VERDICT: Always prefer constructor injection.
 *
 * @Slf4j (Lombok)
 * ---------------
 * Generates: private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
 * Uses SLF4J (Simple Logging Facade for Java) — abstracts over Logback, Log4j2, JUL.
 * Spring Boot auto-configures Logback as the default implementation.
 *
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    // Final field — Lombok @RequiredArgsConstructor generates the constructor
    private final EmployeeRepository employeeRepository;

    // ============================================================
    // @Transactional — COMPLETE INTERVIEW GUIDE
    // ============================================================
    //
    // @Transactional ensures ACID properties for a group of DB operations:
    //   A - Atomicity   : all operations succeed or all are rolled back
    //   C - Consistency : DB moves from one valid state to another
    //   I - Isolation   : concurrent transactions don't interfere with each other
    //   D - Durability  : committed data persists even after system failure
    //
    // HOW IT WORKS (AOP Proxy):
    //   Spring wraps the @Service bean in a proxy.
    //   When you call a @Transactional method:
    //     1. Proxy opens a transaction (BEGIN TRANSACTION)
    //     2. Method executes
    //     3a. No exception -> proxy commits (COMMIT)
    //     3b. RuntimeException/Error thrown -> proxy rolls back (ROLLBACK)
    //     3c. Checked exception thrown -> COMMITS by default! (use rollbackFor to change)
    //
    // CRITICAL GOTCHA — Self-invocation:
    //   @Transactional ONLY works when called through the PROXY (from outside the class).
    //   Calling a @Transactional method from WITHIN the same class bypasses the proxy!
    //   Example:
    //     public void methodA() {
    //         methodB(); // WRONG — calls directly, NOT through proxy, no transaction!
    //     }
    //     @Transactional
    //     public void methodB() { ... }
    //   Fix: inject self-reference, use AspectJ weaving, or restructure code.
    //
    // ============================================================
    // PROPAGATION TYPES — What happens when a @Transactional method
    //                     calls ANOTHER @Transactional method?
    // ============================================================
    //
    //  Scenario: ServiceA.methodA() calls ServiceB.methodB()
    //  TX_A = transaction started by methodA
    //
    //  REQUIRED (DEFAULT):
    //    If TX_A exists: methodB JOINS TX_A (same transaction).
    //    If no TX_A   : methodB starts a NEW transaction.
    //    Most common. Use for standard operations.
    //    Diagram: [  TX_A  [methodB joins TX_A]  ]
    //
    //  REQUIRES_NEW:
    //    ALWAYS starts a brand new transaction, suspending TX_A.
    //    methodB commits/rolls back INDEPENDENTLY of TX_A.
    //    Use for: audit logging (must persist even if main TX rolls back),
    //             sending notifications, independent operations.
    //    Diagram: [  TX_A suspended  ] [  TX_B  ] [  TX_A resumes  ]
    //
    //  NESTED:
    //    Starts a savepoint within TX_A. methodB can roll back to that savepoint
    //    without rolling back the entire TX_A.
    //    Only supported by JDBC (not all JPA providers/DBs support savepoints).
    //    Diagram: [  TX_A  [savepoint -> methodB -> rollback to savepoint]  TX_A continues  ]
    //
    //  SUPPORTS:
    //    If TX_A exists: methodB joins it.
    //    If no TX_A   : methodB runs WITHOUT a transaction.
    //    Use for: read operations that can work with or without a transaction.
    //
    //  NOT_SUPPORTED:
    //    ALWAYS runs without a transaction, suspending any existing TX.
    //    Use for: operations that must NOT be in a transaction (certain batch jobs).
    //
    //  MANDATORY:
    //    REQUIRES an existing transaction. If no TX exists, throws:
    //    IllegalTransactionStateException.
    //    Use for: methods that MUST be called within a transaction (enforce caller contract).
    //
    //  NEVER:
    //    MUST NOT run within a transaction. If a TX exists, throws:
    //    IllegalTransactionStateException.
    //    Use for: operations that are incompatible with transactions.
    //
    // ============================================================
    // ISOLATION LEVELS — Control visibility of uncommitted changes
    //                    between concurrent transactions
    // ============================================================
    //
    // Problems that isolation levels solve:
    //   Dirty Read        : TX_A reads data that TX_B wrote but hasn't committed yet.
    //                       If TX_B rolls back, TX_A read invalid data.
    //   Non-Repeatable Read: TX_A reads row twice; TX_B updates+commits between reads.
    //                        TX_A gets different values each time.
    //   Phantom Read      : TX_A runs same query twice; TX_B inserts new rows between reads.
    //                        TX_A sees different number of rows each time.
    //
    //  Level                 | Dirty Read | Non-Repeatable | Phantom | Performance
    //  ----------------------|------------|----------------|---------|------------
    //  READ_UNCOMMITTED      |   possible |       possible | possible|  fastest
    //  READ_COMMITTED (default on most DBs)|  prevented |  possible | possible| good
    //  REPEATABLE_READ       |  prevented |      prevented | possible| moderate
    //  SERIALIZABLE          |  prevented |      prevented |prevented| slowest
    //
    //  READ_UNCOMMITTED: Reads uncommitted changes. Fastest but dirtiest. Rarely used.
    //  READ_COMMITTED  : Default for PostgreSQL, Oracle, SQL Server.
    //                    Prevents dirty reads. Allows non-repeatable reads.
    //  REPEATABLE_READ : Default for MySQL/InnoDB. Prevents dirty + non-repeatable reads.
    //                    Allows phantom reads (new rows can appear).
    //  SERIALIZABLE    : Strictest. All phenomena prevented. Transactions execute as if serial.
    //                    Major performance impact. Use for financial transactions.
    //
    // ============================================================
    // ROLLBACK RULES
    // ============================================================
    //  DEFAULT behaviour:
    //    Rolls back on: RuntimeException (unchecked) and Error
    //    Does NOT roll back on: checked Exception
    //
    //  rollbackFor = Exception.class : roll back on ANY exception (checked or unchecked)
    //  noRollbackFor = MyException.class : don't roll back for this specific exception
    //
    // ============================================================

    /**
     * CREATE — Propagation.REQUIRED (default), rolls back on any Exception.
     *
     * readOnly = false (default) — this transaction writes data.
     * rollbackFor = Exception.class — roll back even on checked exceptions.
     */
    @Override
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    // @CacheEvict — after creating a new employee, evict stale list caches
    // allEntries=true: evict ALL entries in "employees" cache (list is now outdated)
    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        log.info("Creating employee with email: {}", request.getEmail());

        // Business rule: email must be unique
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                "Employee already exists with email: " + request.getEmail());
        }

        // Map DTO -> Entity (manual mapping — most explicit, no magic)
        Employee employee = mapCreateRequestToEntity(request);

        // Save — Hibernate generates INSERT INTO employees ...
        // After save, employee.getId() is populated (GenerationType.IDENTITY)
        Employee saved = employeeRepository.save(employee);

        log.info("Employee created successfully with id: {}", saved.getId());

        // Map Entity -> Response DTO and return
        return mapEntityToResponse(saved);
    }

    // ============================================================
    // @Cacheable — COMPLETE INTERVIEW GUIDE
    // ============================================================
    //
    // @Cacheable("employees") — cache the method's return value.
    //   Before method executes: check if "employees" cache has an entry for key=id.
    //     HIT : return cached value (method body NOT executed — DB not called).
    //     MISS: execute method, store result in cache, return result.
    //
    // Cache KEY:
    //   Default key: method parameters (id in this case).
    //   Custom key:  @Cacheable(value="employees", key="#id")
    //                @Cacheable(value="employees", key="'emp_' + #id")
    //                @Cacheable(value="employees", key="#request.email")
    //
    // Cache CONDITION (cache only if condition is true):
    //   @Cacheable(value="employees", condition="#id > 0")
    //
    // Cache UNLESS (cache unless result matches):
    //   @Cacheable(value="employees", unless="#result == null")
    //
    // CACHE PROVIDERS supported by Spring:
    //   Simple (ConcurrentHashMap) — default, in-memory, single-node, no TTL. Development only.
    //   Caffeine   — in-memory, single-node, supports TTL/LRU eviction. Best for single-node.
    //   Redis      — distributed, shared across nodes, supports TTL. Best for clustered apps.
    //   Ehcache    — in-memory or disk, clustered option available.
    //   Hazelcast  — distributed in-memory data grid.
    //
    // @EnableCaching on @SpringBootApplication or @Configuration class activates caching.
    //
    // Q: What is a cache stampede / thundering herd?
    //   When cache expires, many threads simultaneously find a MISS and all query the DB.
    //   Fix: probabilistic early expiration, request coalescing, or a dedicated refresh job.
    //
    // Q: @Cacheable gotcha — self-invocation?
    //   Same AOP proxy issue as @Transactional. Calling a @Cacheable method from within
    //   the same class bypasses the cache. Always call through the injected proxy.
    // ============================================================

    /**
     * GET BY ID — readOnly=true optimization.
     *
     * readOnly = true:
     *   1. Tells Hibernate to skip dirty checking (no need to track changes — no writes).
     *   2. Some databases/JDBC drivers route read-only transactions to read replicas.
     *   3. Minor performance gain (no flush before query, no snapshot for dirty check).
     *   NOTE: Does NOT mean "no transaction" — a transaction is still started.
     *         Does NOT prevent writes (just a hint); do NOT rely on it for write prevention.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "employees", key = "#id")
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee by id: {}", id);

        // findById returns Optional<Employee>
        // orElseThrow — clean way to handle missing entity (no null checks)
        // In production: throw a custom ResourceNotFoundException mapped to HTTP 404
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Employee not found with id: {}", id);
                return new RuntimeException("Employee not found with id: " + id);
            });

        return mapEntityToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "employeesByEmail", key = "#email")
    public EmployeeResponse getEmployeeByEmail(String email) {
        log.debug("Fetching employee by email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
        return mapEntityToResponse(employee);
    }

    // ============================================================
    // @CachePut — Update cache WITHOUT skipping method execution
    // ============================================================
    // Unlike @Cacheable (which skips method if cache hit), @CachePut ALWAYS executes
    // the method AND updates the cache with the new result.
    //
    // Use @CachePut when updating data — ensures cache is fresh after write.
    //
    // Q: @Cacheable vs @CacheEvict vs @CachePut?
    //   @Cacheable  : Read — skip method if cached (cache-aside read strategy).
    //   @CachePut   : Write — always execute, always update cache (write-through strategy).
    //   @CacheEvict : Invalidate — remove entry from cache so next read re-fetches from DB.
    //
    // Q: Write-through vs cache-aside vs write-behind caching strategies?
    //   Write-through : Write to cache AND DB simultaneously. Cache always consistent. Slower writes.
    //   Cache-aside   : App reads from cache; on miss, reads DB and populates cache.
    //                   Writes go to DB only; cache evicted or updated separately.
    //   Write-behind  : Write to cache first; sync to DB asynchronously. Fast writes; risk of data loss.
    // ============================================================

    /**
     * UPDATE — Uses @CachePut to keep cache consistent after update.
     */
    @Override
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = Exception.class
    )
    @Caching(
        // Update the employee-by-id cache with fresh data
        put = {
            @CachePut(value = "employees", key = "#id")
        },
        // Evict email-based cache entry in case email was changed
        evict = {
            @CacheEvict(value = "employeesByEmail", allEntries = true)
        }
    )
    // @Caching — groups multiple cache annotations on a single method.
    // Use when you need to combine @Cacheable, @CachePut, @CacheEvict on one method.
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        log.info("Updating employee id: {}", id);

        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Partial update (PATCH semantics) — only update non-null fields
        // This pattern is called "null-safe patching"
        if (request.getFirstName() != null) employee.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) employee.setLastName(request.getLastName());
        if (request.getEmail()     != null) {
            // Business rule: new email must not conflict with another employee
            if (!request.getEmail().equals(employee.getEmail())
                    && employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException(
                    "Email already in use: " + request.getEmail());
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getSalary()     != null) employee.setSalary(request.getSalary());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getAge()        != null) employee.setAge(request.getAge());
        if (request.getPhone()      != null) employee.setPhone(request.getPhone());
        if (request.getStatus()     != null) employee.setStatus(request.getStatus());

        // No explicit save() needed — entity is MANAGED (within transaction).
        // Hibernate dirty-checks at flush time and issues UPDATE automatically.
        // However, calling save() is also fine (makes intent explicit, no double SQL).
        Employee updated = employeeRepository.save(employee);

        log.info("Employee updated successfully: {}", id);
        return mapEntityToResponse(updated);
    }

    /**
     * DELETE — Evict all related cache entries.
     *
     * @CacheEvict(allEntries = true) — remove every entry in "employees" cache.
     * Alternative: @CacheEvict(key = "#id") — only remove the specific entry.
     * Use allEntries=true when you're unsure which list/page caches the deleted entry appears in.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "employees",         key = "#id"),
        @CacheEvict(value = "employeesByEmail",  allEntries = true)
    })
    public void deleteEmployee(Long id) {
        log.info("Deleting employee id: {}", id);

        // Verify exists before deleting — gives a clear error instead of silent no-op
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }

        // deleteById — Spring Data generates: DELETE FROM employees WHERE id = ?
        // Because Employee.skills has cascade=ALL and orphanRemoval=true,
        // Hibernate will also delete all Skill rows for this employee.
        employeeRepository.deleteById(id);

        log.info("Employee deleted successfully: {}", id);
    }

    // ============================================================
    // PAGINATION — Complete Interview Guide
    // ============================================================
    //
    // Q: What is pagination and why use it?
    //   Returning 1 million rows in one API call would:
    //     - Exhaust DB memory (full table scan held in RAM)
    //     - Exhaust application heap (List<Employee> with 1M objects)
    //     - Exhaust client memory (browser/mobile can't process 1M JSON objects)
    //   Pagination divides results into pages (e.g., 10 records at a time).
    //
    // Pageable — encapsulates pagination parameters:
    //   PageRequest.of(pageNumber, pageSize)
    //   PageRequest.of(pageNumber, pageSize, Sort.by("salary").descending())
    //   PageRequest.of(0, 10, Sort.Direction.ASC, "lastName", "firstName")
    //
    // Controller passes Pageable from query params:
    //   GET /employees?page=0&size=10&sort=salary,desc
    //   Spring MVC auto-creates Pageable from these params when method param is Pageable.
    //   Requires: @EnableSpringDataWebSupport or spring-boot-starter-web (auto-configured).
    //
    // Page<T> result:
    //   page.getContent()        — List<T> for current page
    //   page.getNumber()         — current page number (0-based)
    //   page.getSize()           — page size
    //   page.getTotalElements()  — total matching rows (COUNT query)
    //   page.getTotalPages()     — total pages = ceil(totalElements / pageSize)
    //   page.isFirst()           — is this page 0?
    //   page.isLast()            — are there no more pages?
    //   page.hasNext()           — is there a next page?
    //   page.hasPrevious()       — is there a previous page?
    //
    // Q: Offset pagination vs Cursor/Keyset pagination?
    //   Offset (LIMIT/OFFSET): Simple. SELECT ... LIMIT 10 OFFSET 100.
    //     Problem: Page drift — if rows are inserted/deleted between requests,
    //              you may skip or duplicate rows.
    //     Problem: Deep pagination — OFFSET 10000 LIMIT 10 still scans 10010 rows.
    //   Cursor (keyset): WHERE id > :lastSeenId LIMIT 10. Stable, performant.
    //     Problem: Can only paginate forward (no random page access).
    //     Use for: infinite scroll, feeds, real-time data.
    //   Spring Data supports offset pagination natively. Cursor requires custom queries.
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.debug("Fetching all employees, page: {}, size: {}",
                  pageable.getPageNumber(), pageable.getPageSize());

        // findAll(Pageable) fires two queries:
        //   1. SELECT * FROM employees LIMIT ? OFFSET ?  (the actual data)
        //   2. SELECT COUNT(*) FROM employees            (for totalElements)
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Page.map() — transforms Page<Employee> into Page<EmployeeResponse>
        // without losing pagination metadata (totalElements, totalPages, etc.)
        return employeePage.map(this::mapEntityToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable)
                                 .map(this::mapEntityToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesBySalaryRange(Double min, Double max) {
        if (min > max) {
            throw new IllegalArgumentException(
                "Minimum salary cannot be greater than maximum salary");
        }
        return employeeRepository.findBySalaryBetween(min, max)
                                 .stream()
                                 .map(this::mapEntityToResponse)
                                 .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentStats> getDepartmentStatistics() {
        // getDepartmentStats() returns List<Object[]>: [dept, count, avgSalary]
        return employeeRepository.getDepartmentStats()
                                 .stream()
                                 .map(row -> DepartmentStats.builder()
                                     .department((String) row[0])
                                     .employeeCount((Long)   row[1])
                                     .averageSalary((Double) row[2])
                                     .build())
                                 .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "employees", key = "#id")
    public EmployeeResponse updateStatus(Long id, EmployeeStatus status) {
        log.info("Updating status of employee {} to {}", id, status);
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setStatus(status);
        return mapEntityToResponse(employeeRepository.save(employee));
    }

    /**
     * Bulk salary raise — uses @Modifying repository method.
     *
     * PROPAGATION.REQUIRED (default): this method joins any existing transaction from the caller,
     * or starts a new one if none exists.
     *
     * @return number of employees whose salary was updated
     */
    @Override
    @Transactional(
        propagation = Propagation.REQUIRED,
        rollbackFor = Exception.class
    )
    @CacheEvict(value = "employees", allEntries = true) // all cached employees are now stale
    public int giveDepartmentRaise(String department, Double percentage) {
        if (percentage <= 0 || percentage > 100) {
            throw new IllegalArgumentException("Raise percentage must be between 0 and 100");
        }
        // multiplier: 10% raise = salary * 1.10
        double multiplier = 1.0 + (percentage / 100.0);
        int affected = employeeRepository.updateSalaryByDepartment(department, multiplier);
        log.info("Salary raise of {}% applied to {} employees in department '{}'",
                 percentage, affected, department);
        return affected;
    }

    // ============================================================
    // @Async — Asynchronous Method Execution
    // ============================================================
    //
    // @Async causes Spring to execute the method in a SEPARATE THREAD from a thread pool,
    // returning immediately to the caller.
    //
    // Requirements:
    //   1. @EnableAsync on a @Configuration class (or @SpringBootApplication).
    //   2. Method must return void, Future<T>, ListenableFuture<T>, or CompletableFuture<T>.
    //   3. CANNOT be called from within the same class (self-invocation issue — same as @Transactional).
    //   4. Must be on a public method.
    //
    // CompletableFuture<T>:
    //   Java 8+. Represents a future result. Caller can:
    //     - .get()          — block and wait for result
    //     - .thenApply()    — non-blocking callback when done
    //     - .thenAccept()   — consume result when done
    //     - .exceptionally()— handle exception
    //
    // Q: When to use @Async?
    //   - Long-running tasks (report generation, file processing, email sending)
    //   - Operations that don't need to block the HTTP response thread
    //   - Parallel data fetching (fan-out pattern)
    //
    // Q: Default thread pool for @Async?
    //   Spring uses SimpleAsyncTaskExecutor by default — creates a new thread per task (no pooling!).
    //   In production, ALWAYS configure a ThreadPoolTaskExecutor:
    //
    //   @Configuration
    //   @EnableAsync
    //   public class AsyncConfig {
    //       @Bean("asyncExecutor")
    //       public Executor asyncExecutor() {
    //           ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //           executor.setCorePoolSize(5);
    //           executor.setMaxPoolSize(20);
    //           executor.setQueueCapacity(100);
    //           executor.setThreadNamePrefix("AsyncThread-");
    //           executor.initialize();
    //           return executor;
    //       }
    //   }
    //   Then use: @Async("asyncExecutor")
    //
    // Q: @Async and @Transactional together?
    //   @Async starts a new thread. @Transactional transactions are thread-bound.
    //   Each async call gets its own transaction — they do NOT share the caller's transaction.
    //   This is usually desirable (independent async operations), but be aware of it.
    // ============================================================

    @Override
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<EmployeeResponse>> getAllEmployeesAsync() {
        log.info("Async fetch of all employees — running on thread: {}",
                 Thread.currentThread().getName());

        List<EmployeeResponse> employees = employeeRepository.findAll()
            .stream()
            .map(this::mapEntityToResponse)
            .collect(Collectors.toList());

        // CompletableFuture.completedFuture() wraps the result in an already-completed future.
        // Spring's @Async proxy replaces this with a real async execution.
        return CompletableFuture.completedFuture(employees);
    }

    // ============================================================
    // ENTITY <-> DTO MAPPING METHODS
    // ============================================================
    //
    // Q: Manual mapping vs MapStruct vs ModelMapper?
    //
    //   MANUAL (this approach):
    //     + Full control — no magic, easy to debug, explicit.
    //     + No extra dependency.
    //     - Verbose for large entities.
    //     - Easy to forget updating mappings when entity changes.
    //
    //   MapStruct (@Mapper interface, compile-time code generation):
    //     + Zero runtime overhead (generates plain Java code at compile time).
    //     + Type-safe — compilation fails if mapping is wrong.
    //     + Less verbose — just declare the mapping interface.
    //     + Easy to test the generated mapper.
    //     Recommended for production codebases.
    //
    //   ModelMapper (reflection-based, runtime):
    //     + Convention-over-configuration — maps by name automatically.
    //     - Runtime overhead (reflection).
    //     - Hard to debug when auto-mapping goes wrong.
    //     - No compile-time safety.
    //     Use only for rapid prototyping.
    //
    // ============================================================

    /**
     * Maps an Employee entity to an EmployeeResponse DTO.
     * Private helper — only used within this service.
     * Method reference: this::mapEntityToResponse used in stream().map()
     */
    private EmployeeResponse mapEntityToResponse(Employee employee) {
        return EmployeeResponse.builder()
            .id(employee.getId())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .salary(employee.getSalary())
            .department(employee.getDepartment())
            .age(employee.getAge())
            .phone(employee.getPhone())
            .status(employee.getStatus())
            .createdAt(employee.getCreatedAt())
            .updatedAt(employee.getUpdatedAt())
            .build();
    }

    /**
     * Maps an EmployeeCreateRequest DTO to an Employee entity.
     * Does NOT set id/createdAt/updatedAt — those are set by JPA/Hibernate.
     */
    private Employee mapCreateRequestToEntity(EmployeeCreateRequest request) {
        return Employee.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .salary(request.getSalary())
            .department(request.getDepartment())
            .age(request.getAge())
            .phone(request.getPhone())
            .status(EmployeeStatus.ACTIVE) // default on creation
            .build();
    }
}
