package com.interview.master.springboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * JPA ENTITY - Complete Interview Guide
 * ============================================================
 *
 * @Entity - Marks this class as a JPA entity (maps to a DB table).
 *           Every JPA entity MUST have a no-arg constructor (Lombok @NoArgsConstructor handles this).
 *
 * @Table  - Specifies the target table name, schema, unique constraints and indexes.
 *           If omitted, the table name defaults to the class name.
 *
 * JPA INTERVIEW QUESTIONS:
 * -------------------------
 * Q: What is JPA?
 *    Java Persistence API — a specification (JSR-338) for ORM (Object-Relational Mapping) in Java.
 *    It defines the API; vendors implement it.
 *
 * Q: What is Hibernate?
 *    The most popular JPA implementation (provider). Other providers: EclipseLink, OpenJPA.
 *    Spring Boot auto-configures Hibernate when spring-boot-starter-data-jpa is on the classpath.
 *
 * Q: What is ORM?
 *    Object Relational Mapping — technology that maps Java objects to database tables,
 *    so you work with objects instead of raw SQL.
 *
 * Q: What are the 4 Entity life-cycle states?
 *    1. TRANSIENT   - Object created with 'new', not associated with any session/EntityManager.
 *                     No DB row exists. Garbage collected if no reference.
 *    2. MANAGED     - Associated with an open EntityManager (persistence context).
 *                     Changes are automatically tracked and synced to DB (dirty checking).
 *    3. DETACHED    - Was managed, but EntityManager was closed / entity was evicted.
 *                     Changes are NOT tracked. Must call merge() to re-attach.
 *    4. REMOVED     - Scheduled for deletion. EntityManager.remove() was called.
 *                     Actual DELETE happens on flush/commit.
 *
 * Q: What is the Persistence Context?
 *    A first-level cache managed by EntityManager. Within a transaction, if you load the same
 *    entity twice by ID, Hibernate returns the cached instance — only one SELECT is issued.
 *    It also tracks changes (dirty checking) and flushes them to DB before queries/commit.
 *
 * Q: @Id vs @EmbeddedId?
 *    @Id          - simple single-column primary key.
 *    @EmbeddedId  - composite primary key using an @Embeddable class.
 *    @IdClass     - another composite PK option (less preferred).
 *
 * Q: What is the N+1 SELECT problem?
 *    When loading a list of N entities, JPA fires 1 query to get parents,
 *    then N additional queries to load each child (LAZY collection accessed in a loop).
 *    Fix: use JOIN FETCH in JPQL, @EntityGraph, or Hibernate @BatchSize.
 *
 * ============================================================
 */
@Entity
@Table(
    name = "employees",
    // uniqueConstraints - enforce uniqueness at DB level (DDL constraint)
    // Combined with @Column(unique=true) which does the same for single columns
    uniqueConstraints = @UniqueConstraint(
        name = "uk_employee_email",   // constraint name (good practice for readable DB errors)
        columnNames = {"email"}
    ),
    // indexes - create DB indexes for faster querying on frequently filtered columns
    // Does NOT enforce uniqueness (use uniqueConstraints for that)
    indexes = {
        @Index(name = "idx_department", columnList = "department"),
        @Index(name = "idx_status",     columnList = "status")
    }
)
// ============================================================
// LOMBOK ANNOTATIONS
// ============================================================
// @Getter       - generates getXxx() for all fields
// @Setter       - generates setXxx() for all fields
// @NoArgsConstructor - generates Employee() {}  — REQUIRED by JPA spec
// @AllArgsConstructor - generates constructor with all fields
// @Builder      - generates builder pattern: Employee.builder().firstName("John").build()
// @ToString     - generates toString(); exclude="department" avoids StackOverflowError
//                 caused by bidirectional relationship (Employee <-> Department)
// ============================================================
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "dept")   // exclude bidirectional side to prevent recursive toString
public class Employee {

    // ============================================================
    // PRIMARY KEY
    // ============================================================
    // @Id - designates this field as the primary key column
    //
    // @GeneratedValue strategies:
    //   IDENTITY  - relies on DB auto-increment (MySQL AUTO_INCREMENT, PostgreSQL SERIAL).
    //               Hibernate must flush immediately on persist to get the generated ID.
    //               Cannot batch INSERT statements efficiently — minor performance trade-off.
    //
    //   SEQUENCE  - uses a DB sequence object (Oracle, PostgreSQL).
    //               Hibernate pre-fetches a range of IDs (allocationSize=50 default) — best for bulk inserts.
    //               Most performant strategy for high-throughput applications.
    //
    //   AUTO      - JPA picks a strategy based on the underlying database dialect (default).
    //               Unpredictable — avoid in production.
    //
    //   TABLE     - uses a separate DB table to simulate a sequence.
    //               Worst performance (row-level locks). Avoid.
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // COLUMN MAPPINGS WITH BEAN VALIDATION (JSR-380 / Jakarta Validation)
    // ============================================================
    // @Column(name=...)       - maps field to a specific column name (snake_case convention)
    // @Column(nullable=false) - generates NOT NULL DDL constraint
    // @Column(length=100)     - generates VARCHAR(100)
    // @Column(updatable=false)- column cannot be changed after insert
    // @Column(insertable=false)- column is excluded from INSERT statements
    //
    // Bean Validation annotations are checked BEFORE Hibernate sends SQL to DB.
    // Must have spring-boot-starter-validation on classpath.
    // They are evaluated when @Valid / @Validated is used on request bodies.
    // ============================================================

    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name cannot be blank")
    // @NotBlank   - not null AND not empty AND not whitespace-only (for Strings)
    // @NotEmpty   - not null AND not empty (whitespace passes — usually NOT what you want)
    // @NotNull    - not null (empty string would pass — use for non-String types)
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    @Email(message = "Invalid email format")
    // @Email validates format (x@y.z). Does NOT check if the address actually exists.
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Column(name = "salary", nullable = false)
    @NotNull(message = "Salary cannot be null")
    @Min(value = 0, message = "Salary cannot be negative")
    // @Min / @Max work on integer types (int, long, BigInteger)
    @DecimalMin(value = "0.01", inclusive = true, message = "Salary must be at least 0.01")
    // @DecimalMin / @DecimalMax work on decimal types (double, BigDecimal)
    private Double salary;

    @Column(name = "department", nullable = false, length = 100)
    @NotBlank(message = "Department cannot be blank")
    private String department;

    @Column(name = "age")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;

    @Column(name = "phone", length = 15)
    @Pattern(
        regexp = "^[+]?[0-9]{10,13}$",
        message = "Phone number must be 10-13 digits, optionally starting with +"
    )
    // @Pattern - applies a regex. Null values PASS @Pattern (use @NotNull if required).
    private String phone;

    // ============================================================
    // ENUM PERSISTENCE
    // ============================================================
    // @Enumerated(EnumType.STRING)  - stores "ACTIVE", "INACTIVE", etc. in DB column
    //   RECOMMENDED: human-readable, survives enum value reordering, easy to query
    //
    // @Enumerated(EnumType.ORDINAL) - stores 0, 1, 2 (enum declaration order)
    //   DANGEROUS: if you insert a new enum value in the middle, all existing data breaks
    //
    // Interview Q: Which @Enumerated type should you use in production?
    //   Always STRING. Ordinal is fragile and hard to debug.
    // ============================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default   // required when using @Builder with field initializer
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    // ============================================================
    // AUDIT TIMESTAMPS
    // ============================================================
    // @CreationTimestamp - Hibernate-specific annotation (not JPA standard).
    //   Auto-sets the field value on the first INSERT. Never updates it again.
    //   JPA standard equivalent: use @PrePersist lifecycle callback (shown below).
    //
    // @UpdateTimestamp   - Hibernate-specific annotation.
    //   Auto-updates the field on every UPDATE.
    //   JPA standard equivalent: use @PreUpdate lifecycle callback.
    //
    // updatable = false on createdAt ensures the INSERT timestamp is immutable.
    // ============================================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // JPA RELATIONSHIPS
    // ============================================================
    /**
     * @ManyToOne - Many employees belong to one department.
     *
     * FetchType.LAZY  - related entity is loaded only when accessed (getter called).
     *   - DEFAULT for @OneToMany and @ManyToMany collections.
     *   - RECOMMENDED for @ManyToOne and @OneToOne to avoid unnecessary joins.
     *   - Hibernate creates a proxy object; actual SELECT fires when you call getDept().getName().
     *   - WARNING: accessing LAZY field outside an active transaction = LazyInitializationException.
     *
     * FetchType.EAGER - related entity is loaded immediately with a JOIN.
     *   - DEFAULT for @ManyToOne and @OneToOne (change to LAZY to avoid performance issues).
     *   - Can cause Cartesian product explosions when multiple EAGER collections exist.
     *
     * @JoinColumn - specifies the FK column in THIS table.
     *   name             = the FK column name in employees table
     *   referencedColumnName = the PK column in the referenced table (default = "id")
     *
     * CascadeType options:
     *   PERSIST  - saving Employee also saves associated Department (rarely desirable for @ManyToOne)
     *   MERGE    - merging Employee also merges Department
     *   REMOVE   - deleting Employee also deletes Department (VERY DANGEROUS for @ManyToOne — removes shared parent!)
     *   REFRESH  - refreshing Employee refreshes Department
     *   DETACH   - detaching Employee detaches Department
     *   ALL      - all of the above (use with caution on @ManyToOne)
     *
     * Interview Q: Why avoid CascadeType.ALL on @ManyToOne?
     *   Because Department is a shared entity. If you delete one Employee, you don't want to
     *   delete the entire Department (which other employees might belong to).
     *   Cascade ALL is appropriate on the "parent owns children" side (@OneToMany).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private Department dept;

    /**
     * @OneToMany - One employee can have many skills.
     *
     * mappedBy = "employee" — tells JPA that the OWNER side of the relationship is the
     *   'employee' field in the Skill class. The FK column (employee_id) is on the Skill table,
     *   not the Employee table.
     *
     * Interview Q: What is the OWNER side in a bidirectional relationship?
     *   The owner side holds the @JoinColumn (the FK column is on ITS table).
     *   The inverse side uses mappedBy to reference the owner side field.
     *   JPA only looks at the OWNER side to determine what to persist to DB.
     *   If you only set the inverse side (without setting the owner), the FK is NOT saved!
     *
     * cascade = CascadeType.ALL — any operation on Employee propagates to its Skills.
     *   Saving an Employee will save all its Skills.
     *   Deleting an Employee will delete all its Skills.
     *   This is appropriate because Skills are owned/private to an Employee.
     *
     * orphanRemoval = true — if a Skill is removed from the skills list, it is automatically
     *   deleted from the DB. This is stronger than CascadeType.REMOVE (which only fires on
     *   entity deletion, not on collection removal).
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default  // required so @Builder initializes this list (otherwise NPE risk)
    private List<Skill> skills = new ArrayList<>();

    // ============================================================
    // CONVENIENCE METHODS FOR BIDIRECTIONAL RELATIONSHIP MANAGEMENT
    // ============================================================
    // Best practice: always manage BOTH sides of a bidirectional relationship.
    // If you only set employee.skills.add(skill) but not skill.setEmployee(employee),
    // the FK is not saved (Skill is the owner side).
    // ============================================================

    /**
     * Adds a skill and maintains bidirectional consistency.
     * Always use this method instead of getSkills().add(skill) directly.
     */
    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setEmployee(this); // set the owner side so FK is persisted
    }

    /**
     * Removes a skill and maintains bidirectional consistency.
     * With orphanRemoval=true, the Skill will be deleted from DB.
     */
    public void removeSkill(Skill skill) {
        skills.remove(skill);
        skill.setEmployee(null); // clear the owner side reference
    }

    // ============================================================
    // JPA LIFECYCLE CALLBACKS
    // ============================================================
    // These methods are called by the JPA provider (Hibernate) at specific points
    // in the entity's lifecycle. No need to call them manually.
    //
    // @PrePersist  - before INSERT (entity is NEW, not yet in DB)
    // @PostPersist - after INSERT (entity has a DB-generated ID now)
    // @PreUpdate   - before UPDATE
    // @PostUpdate  - after UPDATE
    // @PreRemove   - before DELETE
    // @PostRemove  - after DELETE
    // @PostLoad    - after entity is loaded from DB (SELECT)
    //
    // Interview Q: When would you use @PrePersist?
    //   Setting default values, audit fields (createdBy), UUID generation, validation.
    //   (Hibernate's @CreationTimestamp does the same automatically for timestamps.)
    // ============================================================

    @PrePersist
    protected void onCreate() {
        // Called before the entity is first saved to the DB.
        // Ensures status has a default value even if not set via builder/constructor.
        if (status == null) {
            status = EmployeeStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Called before the entity is updated in the DB.
        // Good place to add audit logic: "who last modified this record?"
        // (Spring Data @LastModifiedBy / @CreatedBy via AuditingEntityListener is cleaner for auditing.)
    }

    @PostLoad
    protected void onLoad() {
        // Called after the entity is loaded from the DB.
        // Good place to decrypt encrypted fields, compute derived values.
    }

    // ============================================================
    // ENUM DEFINITION
    // ============================================================
    // Interview Q: Why use an enum instead of a plain String?
    //   1. Type-safety: compiler checks prevent typos ("ACTVE" would be a compile error).
    //   2. Readability: code is self-documenting.
    //   3. Validation: no need for extra @Pattern validation — invalid values are rejected at compile time.
    //   4. IDE support: auto-complete, refactoring, find-usages.
    // ============================================================
    public enum EmployeeStatus {
        ACTIVE,      // currently employed and working
        INACTIVE,    // temporarily not working (administrative hold)
        ON_LEAVE,    // on approved leave (maternity, medical, etc.)
        TERMINATED   // employment ended
    }
}
