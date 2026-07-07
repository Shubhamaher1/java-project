package com.interview.master.springboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * DEPARTMENT ENTITY
 * ============================================================
 * Demonstrates:
 *  - @OneToMany (inverse/parent side of bidirectional relationship)
 *  - @Version for Optimistic Locking
 *  - Clean bidirectional relationship management
 *
 * OPTIMISTIC vs PESSIMISTIC LOCKING (Interview Must-Know)
 * -------------------------------------------------------
 * The "Lost Update" problem:
 *   Thread A reads row (salary=50000)
 *   Thread B reads row (salary=50000)
 *   Thread A writes (salary=55000) -- a 10% raise
 *   Thread B writes (salary=52500) -- a 5% raise
 *   Thread A's raise is LOST. Final value is 52500 instead of 57750.
 *
 * OPTIMISTIC LOCKING (@Version):
 *   - Best for LOW CONTENTION environments (reads >> writes).
 *   - No DB lock is acquired on read.
 *   - A 'version' column tracks the current row version.
 *   - On UPDATE: WHERE id=? AND version=? is added to the SQL.
 *   - If another thread already updated (version changed), the UPDATE affects 0 rows.
 *   - Hibernate throws OptimisticLockException -> Spring wraps as ObjectOptimisticLockingFailureException.
 *   - The application must handle this: retry, or inform the user to re-read and retry.
 *   - Zero overhead on reads; conflict detection only at write time.
 *
 * PESSIMISTIC LOCKING (LockModeType):
 *   - Best for HIGH CONTENTION environments.
 *   - Acquires a DB-level lock on SELECT: SELECT ... FOR UPDATE (PESSIMISTIC_WRITE)
 *     or SELECT ... FOR SHARE (PESSIMISTIC_READ).
 *   - Other transactions are BLOCKED until the lock is released (on commit/rollback).
 *   - Guarantees no conflicts but reduces concurrency and can cause deadlocks.
 *   - Usage: entityManager.find(Dept.class, id, LockModeType.PESSIMISTIC_WRITE)
 *            or @Lock(LockModeType.PESSIMISTIC_WRITE) in Spring Data repository method.
 *
 * When to choose:
 *   - Low contention, many reads, few writes   -> Optimistic (better throughput)
 *   - High contention, many writes, conflicts likely -> Pessimistic (guaranteed consistency)
 *   - Financial transactions (debit/credit)    -> Pessimistic or Serializable isolation
 *
 * ============================================================
 */
@Entity
@Table(
    name = "departments",
    uniqueConstraints = @UniqueConstraint(name = "uk_dept_name", columnNames = {"name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "employees") // IMPORTANT: exclude collection to avoid recursive toString
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Department name cannot be blank")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;

    @Column(name = "location", length = 200)
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Column(name = "description", length = 500)
    private String description;

    // ============================================================
    // @Version - OPTIMISTIC LOCKING
    // ============================================================
    // Adds a 'version' column to the table (auto-managed by Hibernate).
    //
    // How it works step by step:
    //   1. Row is inserted with version = 0.
    //   2. Thread A reads: Department{id=1, name="IT", version=0}
    //   3. Thread B reads: Department{id=1, name="IT", version=0}
    //   4. Thread A updates: UPDATE departments SET name='IT Dept', version=1 WHERE id=1 AND version=0
    //      -> 1 row affected. SUCCESS. version is now 1.
    //   5. Thread B updates: UPDATE departments SET name='Information Tech', version=1 WHERE id=1 AND version=0
    //      -> 0 rows affected (version is now 1, not 0). CONFLICT!
    //      -> Hibernate throws OptimisticLockException.
    //   6. Thread B must catch the exception and decide: retry or report error to user.
    //
    // Supported types for @Version: int, Integer, long, Long, short, Short, Timestamp.
    // Use Long for safety (Integer overflows on very active entities).
    //
    // Interview Q: Does @Version work without @Transactional?
    //   No. The version check happens during the transaction commit/flush.
    //   @Transactional is required for @Version to protect against lost updates.
    // ============================================================
    @Version
    @Column(name = "version")
    private Long version;

    // ============================================================
    // @OneToMany - PARENT/INVERSE SIDE
    // ============================================================
    // This is the INVERSE side (mappedBy = "dept").
    // The OWNER side is Employee.dept (which has the @JoinColumn / FK column).
    //
    // mappedBy = "dept" means: "look at the 'dept' field in Employee to find the FK".
    // JPA does NOT create a join table for this relationship because the FK
    // is in the employees table (owner side).
    //
    // FetchType.LAZY (default for @OneToMany):
    //   The employees list is NOT loaded when Department is loaded.
    //   A proxy is returned; the actual SELECT fires only when .getEmployees() is called.
    //   ALWAYS keep @OneToMany as LAZY (EAGER causes performance disasters at scale).
    //
    // cascade = {PERSIST, MERGE}:
    //   Saving/updating a Department can also save/update its employees.
    //   CascadeType.REMOVE is intentionally excluded: we don't want to accidentally delete
    //   all employees by deleting a department (reassign first, then delete).
    //
    // orphanRemoval = false (default):
    //   If an employee is removed from the collection, it is NOT auto-deleted.
    //   (Different from Employee->Skills where orphanRemoval=true because skills are owned.)
    // ============================================================
    @OneToMany(
        mappedBy = "dept",
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    // ============================================================
    // BIDIRECTIONAL RELATIONSHIP HELPER METHODS
    // ============================================================
    // Best practice: provide helper methods to maintain BOTH sides of the relationship.
    // If you only call dept.getEmployees().add(employee), you've set the inverse side,
    // but employee.dept is still null -> the FK in employees table will be NULL.
    // Always call addEmployee() which sets BOTH sides.
    // ============================================================

    /**
     * Adds an employee to this department and sets the back-reference.
     * This ensures BOTH sides of the bidirectional relationship are consistent.
     *
     * @param employee the employee to add
     */
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDept(this); // set the owner side so FK column is populated
    }

    /**
     * Removes an employee from this department and clears the back-reference.
     *
     * @param employee the employee to remove
     */
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setDept(null); // clear the owner side reference
    }

    // ============================================================
    // equals() and hashCode() for JPA entities
    // ============================================================
    // Interview Q: Why NOT use @EqualsAndHashCode from Lombok on JPA entities?
    //
    // Problem: Lombok's @EqualsAndHashCode uses ALL fields by default.
    //   - Before persist: id=null  -> hashCode is based on null id.
    //   - After persist:  id=123   -> hashCode changes!
    //   - If the entity was stored in a HashSet/HashMap before persist,
    //     it can no longer be found (stored in wrong bucket).
    //
    // Best practices for JPA entity equals/hashCode:
    //   Option 1 (simple): Use only the @Id field.
    //     - Before persist (id=null), all transient entities are "equal" to each other.
    //     - Acceptable if you never add transient entities to Sets.
    //
    //   Option 2 (recommended): Use a natural/business key (like email, name).
    //     - Stable across all lifecycle states (transient, managed, detached).
    //     - Use this if you have a truly unique natural key.
    //
    //   Option 3: Use UUID as the @Id (assigned before persist).
    //     - hashCode is stable from creation.
    //
    // We implement Option 2 here (name is the natural key for Department).
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department)) return false;
        Department that = (Department) o;
        // Use business key (name) — stable across all lifecycle states
        return name != null && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        // Use a constant when name is null (transient state) to avoid issues
        // Return a fixed value so all transient entities go to the same bucket
        // (minor performance issue, but correctness is preserved)
        return getClass().hashCode();
    }
}
