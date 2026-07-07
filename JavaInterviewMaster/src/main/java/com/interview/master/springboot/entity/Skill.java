package com.interview.master.springboot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * ============================================================
 * SKILL ENTITY
 * ============================================================
 * Demonstrates:
 *  - @ManyToOne (owner side of bidirectional relationship with Employee)
 *  - How to correctly model a dependent/child entity
 *  - equals/hashCode on a child entity
 *
 * RELATIONSHIP SUMMARY:
 *   Employee (1) <--------> (Many) Skill
 *
 *   Owner side  : Skill.employee   (has @JoinColumn, FK is in 'skills' table)
 *   Inverse side: Employee.skills  (has mappedBy="employee")
 *
 * BIDIRECTIONAL RELATIONSHIP RULES (Interview Must-Know):
 * -------------------------------------------------------
 * 1. The OWNER side controls the foreign key column in the DB.
 *    JPA only reads the owner side when deciding what to persist.
 *
 * 2. If you only set the INVERSE side:
 *      employee.getSkills().add(skill);  // sets inverse side only
 *    -> skill.employee is still NULL -> employees_id FK in DB will be NULL!
 *    -> The skill row will be saved with no employee reference (orphan or error).
 *
 * 3. ALWAYS set BOTH sides:
 *      skill.setEmployee(employee);      // owner side (sets FK)
 *      employee.getSkills().add(skill);  // inverse side (keeps in-memory consistent)
 *    OR use the Employee.addSkill(skill) helper method which does both.
 *
 * 4. With orphanRemoval=true on Employee.skills:
 *    Removing a skill from the collection will DELETE its row from DB on flush.
 *    Use Employee.removeSkill(skill) to maintain both sides cleanly.
 *
 * ============================================================
 */
@Entity
@Table(
    name = "skills",
    // A combination of employee + skillName must be unique
    // (one employee can't list the same skill twice)
    uniqueConstraints = @UniqueConstraint(
        name = "uk_employee_skill",
        columnNames = {"employee_id", "skill_name"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "employee") // exclude to avoid recursive toString -> StackOverflowError
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // @ManyToOne - OWNER SIDE
    // ============================================================
    // This is the OWNER side because this table (skills) holds the FK column (employee_id).
    //
    // fetch = FetchType.LAZY:
    //   Even though EAGER is the JPA default for @ManyToOne, explicitly setting LAZY here
    //   is a best practice. When you load a Skill, you rarely need the full Employee object.
    //   Only load it on demand.
    //
    // optional = false:
    //   Tells JPA (and DDL generation) that this FK is NOT NULL.
    //   A skill MUST belong to an employee. Hibernate uses INNER JOIN (not LEFT JOIN) for EAGER.
    //
    // @JoinColumn:
    //   name            = the FK column name in THIS (skills) table
    //   nullable = false = generates NOT NULL DDL constraint (maps to optional=false)
    //
    // Interview Q: What is the difference between optional=false on @ManyToOne
    //              and nullable=false on @JoinColumn?
    //   - @JoinColumn(nullable=false) affects DDL schema generation (adds NOT NULL to column).
    //   - @ManyToOne(optional=false) tells Hibernate to use INNER JOIN (performance hint).
    //   - Best practice: set BOTH consistently.
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "skill_name", nullable = false, length = 100)
    @NotBlank(message = "Skill name cannot be blank")
    @Size(min = 2, max = 100, message = "Skill name must be between 2 and 100 characters")
    private String skillName;

    // ============================================================
    // PROFICIENCY LEVEL as ENUM
    // ============================================================
    // Using an enum instead of a plain String or int gives:
    //  - Type safety (no typos like "Begginer")
    //  - Clear domain model
    //  - Restricted set of valid values
    // EnumType.STRING: stores "BEGINNER", "INTERMEDIATE", etc. in DB
    // ============================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", length = 20)
    private ProficiencyLevel proficiency;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    // ============================================================
    // equals() and hashCode()
    // ============================================================
    // For a child entity with a natural composite key (employee + skillName),
    // we implement equals/hashCode based on those fields.
    // This is safe because skillName is set before the entity is added to any collection,
    // and employee reference is always set before persistence.
    //
    // Interview Q: Why not use @EqualsAndHashCode(of = "id") with Lombok?
    //   Because before persist, id=null. Two different new Skills would both have id=null
    //   and would appear equal — causing duplicates to be silently dropped from Sets.
    // ============================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill)) return false;
        Skill skill = (Skill) o;
        // Natural composite key: employee + skillName
        if (skillName == null || employee == null) return false;
        return skillName.equals(skill.skillName) &&
               employee.getId() != null &&
               employee.getId().equals(skill.employee != null ? skill.employee.getId() : null);
    }

    @Override
    public int hashCode() {
        // Consistent with equals — use natural key fields
        return getClass().hashCode();
    }

    // ============================================================
    // PROFICIENCY ENUM
    // ============================================================
    public enum ProficiencyLevel {
        BEGINNER,       // 0-1 years, basic understanding
        INTERMEDIATE,   // 2-4 years, independent work
        ADVANCED,       // 5-7 years, deep knowledge, can mentor others
        EXPERT          // 8+ years, industry-level expertise
    }
}
