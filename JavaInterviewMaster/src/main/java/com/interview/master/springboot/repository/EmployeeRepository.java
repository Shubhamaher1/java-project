package com.interview.master.springboot.repository;

import com.interview.master.springboot.entity.Employee;
import com.interview.master.springboot.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * SPRING DATA JPA REPOSITORY - Complete Interview Guide
 * ============================================================
 *
 * REPOSITORY INTERFACE HIERARCHY:
 *
 *   Repository<T, ID>                         (marker interface — no methods)
 *      └── CrudRepository<T, ID>              (+save, findById, findAll, delete, count, existsById)
 *               └── PagingAndSortingRepository<T, ID>  (+findAll(Pageable), findAll(Sort))
 *                        └── JpaRepository<T, ID>      (+saveAll, flush, saveAndFlush,
 *                                                        deleteAllInBatch, getReferenceById)
 *
 *   JpaSpecificationExecutor<T>               (separate interface — adds Specification support)
 *
 * Extending JpaRepository<Employee, Long> gives us ALL CRUD + paging + sorting methods for free.
 * Extending JpaSpecificationExecutor<Employee> adds dynamic query support via Specifications.
 *
 * -------------------------------------------------------
 * Q: What does @Repository do?
 *    1. Marks this interface as a Spring-managed bean (component scan picks it up).
 *    2. Enables EXCEPTION TRANSLATION:
 *       Raw JDBC/JPA throws vendor-specific exceptions (SQLException, PersistenceException).
 *       Spring's @Repository wraps them into Spring's unified DataAccessException hierarchy.
 *       This lets you write DB-agnostic catch blocks:
 *         catch (DataAccessException e) { ... }   // works for MySQL, PostgreSQL, Oracle, etc.
 *    Spring Data repositories get @Repository behavior automatically; the annotation
 *    is technically redundant here but documents intent clearly.
 *
 * -------------------------------------------------------
 * Q: How does Spring Data JPA GENERATE the implementation at runtime?
 *    Spring scans for interfaces extending Repository<T,ID>.
 *    It creates a JDK dynamic proxy (SimpleJpaRepository by default) that implements
 *    each method by:
 *      1. DERIVED QUERIES : parsing the method name into JPQL.
 *      2. @Query           : executing the provided JPQL or native SQL.
 *      3. Specifications   : building a Criteria API query programmatically.
 *    You never write the implementation class. Spring generates it at startup.
 *
 * -------------------------------------------------------
 * Q: 4 ways to create queries with Spring Data JPA?
 *    1. Derived Query Methods  - from method name (covered below)
 *    2. @Query (JPQL)          - custom JPQL (entity-based, DB-agnostic)
 *    3. @Query (nativeQuery)   - raw SQL (DB-specific features, window functions)
 *    4. Specifications         - type-safe dynamic criteria queries
 *    (5. QueryDSL              - type-safe queries with generated Q-classes — separate library)
 *
 * ============================================================
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>,
                                             JpaSpecificationExecutor<Employee> {

    // ============================================================
    // SECTION 1: DERIVED QUERY METHODS
    // ============================================================
    // Spring Data JPA parses the method name into JPQL automatically at startup.
    // If the method name can't be parsed, it fails fast with a descriptive error.
    //
    // NAMING CONVENTION:
    //   Prefix:    findBy, readBy, getBy, queryBy, searchBy
    //              countBy (returns long), existsBy (returns boolean)
    //              deleteBy, removeBy (returns void or long — count of deleted)
    //
    //   Subject:   the entity field name (camelCase, matches field name exactly)
    //
    //   Keywords:  And, Or, Is, Equals, Not, Like, NotLike, Containing (LIKE %x%),
    //              StartingWith (LIKE x%), EndingWith (LIKE %x),
    //              Between, LessThan, LessThanEqual, GreaterThan, GreaterThanEqual,
    //              IsNull, IsNotNull, In, NotIn,
    //              OrderBy, Asc, Desc,
    //              IgnoreCase, AllIgnoreCase,
    //              First/Top (LIMIT), Distinct
    //
    // GENERATED JPQL examples (shown in comments):
    // ============================================================

    // -> SELECT e FROM Employee e WHERE e.email = ?1
    Optional<Employee> findByEmail(String email);

    // -> SELECT e FROM Employee e WHERE e.department = ?1
    List<Employee> findByDepartment(String department);

    // -> SELECT e FROM Employee e WHERE e.department = ?1 AND e.status = ?2
    List<Employee> findByDepartmentAndStatus(String department, EmployeeStatus status);

    // -> SELECT e FROM Employee e WHERE e.salary BETWEEN ?1 AND ?2
    List<Employee> findBySalaryBetween(Double minSalary, Double maxSalary);

    // -> SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER('%' || ?1 || '%')
    List<Employee> findByFirstNameContainingIgnoreCase(String name);

    // -> SELECT e FROM Employee e WHERE e.age >= ?1
    List<Employee> findByAgeGreaterThanEqual(Integer age);

    // -> SELECT COUNT(e) > 0 FROM Employee e WHERE e.email = ?1
    boolean existsByEmail(String email);

    // -> SELECT COUNT(e) FROM Employee e WHERE e.department = ?1
    long countByDepartment(String department);

    // -> SELECT e FROM Employee e WHERE e.department = ?1 ORDER BY e.salary DESC
    List<Employee> findByDepartmentOrderBySalaryDesc(String department);

    // -> SELECT e FROM Employee e WHERE e.status IN (?1)
    List<Employee> findByStatusIn(List<EmployeeStatus> statuses);

    // -> SELECT e FROM Employee e WHERE e.department = ?1 ORDER BY e.salary DESC LIMIT 3
    // First3 / Top3 — limits results. Equivalent to SQL: FETCH FIRST 3 ROWS ONLY
    List<Employee> findTop3ByDepartmentOrderBySalaryDesc(String department);

    // -> SELECT DISTINCT e FROM Employee e WHERE e.department = ?1
    // Useful when joins produce duplicate rows
    List<Employee> findDistinctByDepartment(String department);

    // -> SELECT e FROM Employee e WHERE e.salary > ?1 ORDER BY e.salary DESC
    List<Employee> findBySalaryGreaterThanOrderBySalaryDesc(Double salary);

    // -> SELECT e FROM Employee e WHERE e.firstName LIKE ?1% (case-insensitive)
    List<Employee> findByFirstNameStartingWithIgnoreCase(String prefix);

    // Sorting via method name — useful for simple cases
    // -> SELECT e FROM Employee e WHERE e.department = ?1 ORDER BY e.lastName ASC
    List<Employee> findByDepartmentOrderByLastNameAsc(String department);

    // Dynamic sorting via Sort parameter (more flexible than naming)
    // Caller decides the sort at runtime:
    //   Sort sort = Sort.by(Sort.Direction.DESC, "salary");
    //   repo.findByDepartment("Engineering", sort);
    List<Employee> findByDepartment(String department, Sort sort);

    // ============================================================
    // SECTION 2: PAGINATION
    // ============================================================
    // Pageable encapsulates: page number (0-based), page size, sort direction.
    // Page<T> extends Slice<T> and includes a COUNT query for total elements.
    //
    // Usage in service:
    //   Pageable pageable = PageRequest.of(0, 10, Sort.by("salary").descending());
    //   Page<Employee> page = repo.findByDepartment("Engineering", pageable);
    //   page.getContent()       // List<Employee> for current page
    //   page.getTotalElements() // total matching rows (from COUNT query)
    //   page.getTotalPages()    // total pages
    //   page.isLast()           // is this the last page?
    //
    // Q: Page<T> vs Slice<T>?
    //   Page<T>  - runs COUNT(*) query -> knows totalElements, totalPages. More overhead.
    //   Slice<T> - no COUNT query -> only knows if next page exists (hasNext()). More performant.
    //   Use Slice for infinite scroll / "load more"; use Page when you show "Page X of Y".
    // ============================================================
    Page<Employee> findByDepartment(String department, Pageable pageable);

    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    Page<Employee> findBySalaryBetween(Double minSalary, Double maxSalary, Pageable pageable);

    // ============================================================
    // SECTION 3: @Query with JPQL
    // ============================================================
    // JPQL = Java Persistence Query Language.
    // Operates on ENTITY CLASSES and FIELD NAMES (not table/column names).
    // DB-agnostic: same JPQL works on MySQL, PostgreSQL, Oracle, H2.
    //
    // NAMED PARAMETERS (:paramName) vs POSITIONAL PARAMETERS (?1, ?2):
    //   Named:      @Param("dept") — more readable, order-independent
    //   Positional: ?1, ?2         — order must match method parameter order
    //   Prefer named parameters in team code (self-documenting).
    //
    // Q: When to use @Query over derived method names?
    //   - Complex queries that would generate unreadable method names
    //   - JOIN FETCH to solve N+1 problem
    //   - Aggregations (GROUP BY, HAVING)
    //   - Subqueries
    //   - Projections with constructor expressions
    // ============================================================

    // Named parameters with @Param
    @Query("SELECT e FROM Employee e WHERE e.department = :dept AND e.salary > :minSalary")
    List<Employee> findByDeptAndMinSalary(@Param("dept") String department,
                                          @Param("minSalary") Double minSalary);

    // Positional parameters (?1, ?2)
    @Query("SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Employee> searchByName(String name);

    // JOIN FETCH — solves N+1 problem for collections
    // Without JOIN FETCH: loading 100 employees fires 1 + 100 queries (each skill list lazy-loaded).
    // With JOIN FETCH: single query with JOIN loads employees AND their skills together.
    // Interview Q: When would you NOT use JOIN FETCH?
    //   With pagination (Pageable) — Hibernate warns "HHH90003004: firstResult/maxResults specified
    //   with collection fetch; applying in memory!" which loads ALL rows then paginates in RAM.
    //   Solution: use @EntityGraph or a two-query approach (fetch IDs first, then fetch by IDs).
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.skills WHERE e.department = :dept")
    List<Employee> findByDepartmentWithSkills(@Param("dept") String department);

    // Constructor expression — map query result directly into a DTO
    // No need for a separate mapper step. Class must have a matching constructor.
    // Q: What is a constructor expression in JPQL?
    //   NEW com.example.dto.EmployeeSummary(e.id, e.firstName, e.lastName) — Hibernate calls
    //   that constructor with the projected values. Full entity is NOT loaded.
    @Query("SELECT new com.interview.master.springboot.dto.EmployeeDTO$EmployeeSummary(" +
           "e.id, e.firstName, e.lastName, e.department, e.status) " +
           "FROM Employee e WHERE e.department = :dept")
    List<com.interview.master.springboot.dto.EmployeeDTO.EmployeeSummary>
        findSummaryByDepartment(@Param("dept") String department);

    // Aggregation query
    // Returns Object[] per row: [0]=department String, [1]=count Long, [2]=avgSalary Double
    // Service layer maps Object[] -> DepartmentStats DTO
    @Query("SELECT e.department, COUNT(e), AVG(e.salary) FROM Employee e GROUP BY e.department")
    List<Object[]> getDepartmentStats();

    // Pageable with @Query — Spring Data auto-adds the offset/limit SQL
    @Query("SELECT e FROM Employee e WHERE e.salary > :minSalary ORDER BY e.salary DESC")
    Page<Employee> findHighEarners(@Param("minSalary") Double minSalary, Pageable pageable);

    // ============================================================
    // SECTION 4: NATIVE SQL QUERIES (nativeQuery = true)
    // ============================================================
    // Use native SQL when:
    //   - DB-specific features: window functions (ROW_NUMBER, RANK), CTEs (WITH clause)
    //   - Full-text search (MATCH AGAINST in MySQL)
    //   - Performance: hand-tuned SQL that the JPQL query planner can't generate efficiently
    //   - Legacy queries from DBAs
    //
    // Downsides:
    //   - DB-specific: won't work if you switch databases
    //   - Uses TABLE/COLUMN names (not entity/field names)
    //   - Harder to refactor when schema changes
    //   - Pagination with countQuery param requires a separate count query string
    // ============================================================

    // Simple native query — above average salary
    @Query(
        value = "SELECT * FROM employees WHERE salary > (SELECT AVG(salary) FROM employees)",
        nativeQuery = true
    )
    List<Employee> findAboveAverageSalary();

    // Native query with named parameter
    @Query(
        value = "SELECT * FROM employees WHERE department = :dept ORDER BY salary DESC LIMIT :limit",
        nativeQuery = true
    )
    List<Employee> findTopEarnersByDepartment(@Param("dept") String department,
                                              @Param("limit") int limit);

    // Native query with pagination — MUST provide countQuery separately
    // Spring uses countQuery to calculate totalElements for Page<T>
    @Query(
        value = "SELECT * FROM employees WHERE status = :status",
        countQuery = "SELECT COUNT(*) FROM employees WHERE status = :status",
        nativeQuery = true
    )
    Page<Employee> findByStatusNative(@Param("status") String status, Pageable pageable);

    // ============================================================
    // SECTION 5: @Modifying — UPDATE and DELETE queries
    // ============================================================
    // @Modifying is REQUIRED for any DML that changes data (UPDATE, DELETE, INSERT).
    // Without it, Spring throws: InvalidDataAccessApiUsageException:
    //   "Modifying queries can only be used with void or int/Integer return types"
    //
    // clearAutomatically = true:
    //   After the bulk UPDATE/DELETE, clears the first-level cache (persistence context).
    //   Why? Hibernate's in-memory entity cache still has old values after a bulk DML.
    //   Example: you bulk-update salary for all "Engineering" employees.
    //            Without clearAutomatically, loading an Engineering employee from cache
    //            returns the OLD salary (cache is stale). clearAutomatically forces a
    //            fresh SELECT on the next access.
    //   Downside: clears ALL entities from the session — may cause extra SELECTs.
    //
    // flushAutomatically = true:
    //   Flushes pending changes to DB before executing the query.
    //   Ensures in-memory changes are persisted before the DML runs.
    //
    // @Transactional: @Modifying queries REQUIRE a transaction.
    //   You can annotate the repository method directly, or ensure the calling service method
    //   has @Transactional. Both work; service-level @Transactional is the cleaner approach.
    //
    // Return type:
    //   int / Integer — number of rows affected
    //   void          — if you don't need the count
    // ============================================================

    // Bulk salary update — more efficient than loading all employees and updating one by one
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Employee e SET e.salary = e.salary * :multiplier WHERE e.department = :dept")
    int updateSalaryByDepartment(@Param("dept") String department,
                                 @Param("multiplier") Double multiplier);

    // Bulk status update
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Employee e SET e.status = :newStatus WHERE e.status = :oldStatus")
    int updateStatusBulk(@Param("oldStatus") EmployeeStatus oldStatus,
                         @Param("newStatus") EmployeeStatus newStatus);

    // Bulk delete — more efficient than findAll + deleteAll
    // Interview Q: @Modifying DELETE vs repository.delete(entity)?
    //   repository.delete(entity): Hibernate fires SELECT first to load entity, then DELETE.
    //                              Also fires cascades and orphanRemoval.
    //   @Modifying DELETE query  : Single SQL DELETE. NO SELECT. NO cascades. NO orphanRemoval.
    //                              Use for bulk operations where cascades are not needed.
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Employee e WHERE e.status = 'TERMINATED'")
    int deleteTerminatedEmployees();

    // ============================================================
    // SECTION 6: INTERFACE-BASED PROJECTIONS
    // ============================================================
    // Projections load only specific columns instead of the full entity.
    // This reduces memory usage and speeds up queries on wide tables.
    //
    // Interface projection: Spring generates a proxy at runtime that implements this interface.
    // Hibernate generates: SELECT e.first_name, e.last_name FROM employees WHERE ...
    // instead of:          SELECT * FROM employees WHERE ...
    //
    // Q: Interface projection vs DTO projection (constructor expression)?
    //   Interface projection : Runtime proxy, no constructor needed, supports nested projections.
    //   DTO projection       : Regular Java class, needs constructor, slightly faster (no proxy).
    //
    // Q: What is an OPEN projection vs CLOSED projection?
    //   Closed: Only maps entity fields -> Hibernate can optimize to SELECT only those columns.
    //   Open: Uses @Value("#{target.firstName + ' ' + target.lastName}") SpEL expression
    //         -> Hibernate must load all columns (can't optimize) — defeats the purpose of projection.
    //   Prefer closed projections.
    // ============================================================

    /**
     * Closed projection interface — only first and last name.
     * Spring generates a proxy that implements this interface.
     * Hibernate generates: SELECT e.first_name, e.last_name FROM employees WHERE department = ?
     */
    interface EmployeeNameOnly {
        String getFirstName();
        String getLastName();
    }

    /**
     * Closed projection with email for duplicate checking.
     */
    interface EmployeeEmailOnly {
        Long getId();
        String getEmail();
    }

    // Using the projection — Spring infers the right projection from return type
    List<EmployeeNameOnly> findByDepartmentOrderByLastNameAsc(String department, Sort sort);

    List<EmployeeEmailOnly> findByStatus(EmployeeStatus status);

    // ============================================================
    // SECTION 7: CUSTOM METHODS VIA DEFAULT
    // ============================================================
    // Java 8 default methods in interfaces allow adding utility methods
    // to repositories without a separate implementation class.
    // ============================================================

    /**
     * Finds an employee by email or throws a descriptive exception.
     * Default method — no need for a separate implementation.
     * The service layer can call this instead of handling Optional manually everywhere.
     */
    default Employee findByEmailOrThrow(String email) {
        return findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
    }
}
