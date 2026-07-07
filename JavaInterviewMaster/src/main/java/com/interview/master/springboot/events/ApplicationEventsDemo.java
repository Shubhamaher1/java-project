package com.interview.master.springboot.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * ============================================================
 * SPRING APPLICATION EVENTS - Complete Interview Guide
 * ============================================================
 *
 * Spring's Event system implements the Observer (Publish-Subscribe) pattern.
 *
 * Benefits:
 *   - Decoupling: publisher doesn't know who listens (loose coupling)
 *   - SRP: each listener handles one specific reaction
 *   - Extensibility: add new listeners without changing publisher
 *
 * Core components:
 *   ApplicationEventPublisher  : publishes events (inject this into your service)
 *   ApplicationEvent           : base class for custom events
 *   @EventListener             : marks a method as event listener
 *   @TransactionalEventListener: like @EventListener but transaction-aware
 *
 * ---------------------------------------------------------------
 * SPRING BUILT-IN LIFECYCLE EVENTS (in order):
 * ---------------------------------------------------------------
 *
 * 1. ApplicationStartingEvent
 *      - Very early: listeners and initializers registered, nothing else started
 *      - Context NOT created yet
 *
 * 2. ApplicationEnvironmentPreparedEvent
 *      - Environment (properties, profiles) is ready
 *      - Context NOT created yet
 *
 * 3. ApplicationContextInitializedEvent
 *      - ApplicationContext created, ApplicationContextInitializers invoked
 *      - Beans NOT loaded yet
 *
 * 4. ApplicationPreparedEvent
 *      - Context prepared: bean definitions loaded, NOT refreshed
 *      - @Bean methods NOT called yet
 *
 * 5. ContextRefreshedEvent
 *      - ApplicationContext fully initialized (all beans created)
 *      - Fired when context is refreshed (startup + manual refresh())
 *      - Good for: initialization after all beans are ready
 *
 * 6. ApplicationStartedEvent
 *      - Spring Boot application started, before ApplicationRunner/CommandLineRunner
 *
 * 7. ApplicationReadyEvent
 *      - Application fully started and ready to serve requests
 *      - BEST PLACE for "do this on startup" logic
 *      - Fired after all ApplicationRunner and CommandLineRunner beans have run
 *
 * 8. ApplicationFailedEvent
 *      - Fired if startup fails with exception
 *
 * 9. ContextClosedEvent
 *      - Context is being closed (app shutdown)
 *      - Good for: cleanup, releasing resources
 *
 * ---------------------------------------------------------------
 * @EventListener vs @TransactionalEventListener:
 * ---------------------------------------------------------------
 *
 * @EventListener:
 *   - Fires immediately when event is published
 *   - If publisher is inside a transaction, listener runs WITHIN that transaction
 *   - If listener fails (exception), it can roll back the publisher's transaction
 *   - Synchronous by default (same thread as publisher)
 *
 * @TransactionalEventListener:
 *   - Transaction-aware: waits for the publisher's transaction to commit/rollback
 *   - Default phase: AFTER_COMMIT (most common use case)
 *   - Phases:
 *     AFTER_COMMIT   : fires after transaction COMMITS (most common)
 *     AFTER_ROLLBACK : fires after transaction ROLLS BACK (e.g., compensating action)
 *     AFTER_COMPLETION: fires after transaction ends (commit OR rollback)
 *     BEFORE_COMMIT  : fires just before commit (still within transaction)
 *
 * USE @TransactionalEventListener AFTER_COMMIT when:
 *   - You want to send an email ONLY if the DB transaction committed
 *   - You want to call an external API ONLY if the record was saved
 *   - Avoids sending emails for rolled-back transactions
 *
 * Interview Q: Why use AFTER_COMMIT instead of @EventListener?
 *   With @EventListener: email sent, then DB fails (rollback) -> email sent for no record!
 *   With AFTER_COMMIT: email sent ONLY after DB commit -> data consistency guaranteed.
 *
 * ---------------------------------------------------------------
 * ASYNC EVENT LISTENERS:
 * ---------------------------------------------------------------
 *
 * By default, event listeners run SYNCHRONOUSLY in the publisher's thread.
 * For long-running operations (send email, call API): use @Async.
 *
 * @Async requirements:
 *   1. @EnableAsync on a @Configuration class
 *   2. @Async on the listener method
 *   3. A TaskExecutor bean (or Spring Boot's auto-configured one)
 *
 * @Async runs the method in a separate thread from a thread pool.
 * Publisher continues without waiting for listener to finish.
 *
 * Limitation with @TransactionalEventListener + @Async:
 *   @Async + AFTER_COMMIT: the new thread has no transaction context.
 *   That's usually fine because you don't need the transaction anymore.
 * ============================================================
 */

// ================================================================
// CUSTOM EVENT: EmployeeCreatedEvent
// ================================================================

/**
 * EmployeeCreatedEvent - fired when a new employee is created.
 *
 * Extends ApplicationEvent (classic approach, Spring 4.2+).
 * Alternative (modern): use ANY object as an event (no need to extend ApplicationEvent).
 *   publisher.publishEvent(new EmployeeCreatedPayload(id, name, email));
 *   The POJO approach is simpler but loses the source reference from ApplicationEvent.
 *
 * ApplicationEvent constructor requires 'source' = the object that published the event.
 */
class EmployeeCreatedEvent extends ApplicationEvent {

    private final Long employeeId;
    private final String employeeName;
    private final String employeeEmail;
    private final LocalDateTime occurredAt;

    public EmployeeCreatedEvent(Object source, Long employeeId, String employeeName, String employeeEmail) {
        super(source);  // source = the bean that published this event
        this.employeeId    = employeeId;
        this.employeeName  = employeeName;
        this.employeeEmail = employeeEmail;
        this.occurredAt    = LocalDateTime.now();
    }

    public Long getEmployeeId()      { return employeeId; }
    public String getEmployeeName()  { return employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public LocalDateTime getOccurredAt() { return occurredAt; }

    @Override
    public String toString() {
        return "EmployeeCreatedEvent{id=" + employeeId + ", name=" + employeeName + "}";
    }
}

/**
 * EmployeeDeletedEvent - fired when an employee is deleted.
 * Demonstrates another custom event (no annotation, just a plain event class).
 */
class EmployeeDeletedEvent extends ApplicationEvent {
    private final Long employeeId;

    public EmployeeDeletedEvent(Object source, Long employeeId) {
        super(source);
        this.employeeId = employeeId;
    }

    public Long getEmployeeId() { return employeeId; }
}

/**
 * SalaryChangedEvent - demonstrates a POJO event (no ApplicationEvent extension).
 * Spring 4.2+ supports publishing any object as an event.
 */
class SalaryChangedEvent {
    private final Long employeeId;
    private final double oldSalary;
    private final double newSalary;

    public SalaryChangedEvent(Long employeeId, double oldSalary, double newSalary) {
        this.employeeId = employeeId;
        this.oldSalary  = oldSalary;
        this.newSalary  = newSalary;
    }

    public Long getEmployeeId() { return employeeId; }
    public double getOldSalary() { return oldSalary; }
    public double getNewSalary() { return newSalary; }
}

// ================================================================
// EVENT PUBLISHER SERVICE
// ================================================================

/**
 * EmployeeEventService - demonstrates how to publish events.
 *
 * ApplicationEventPublisher is injected by Spring automatically
 * (it is the ApplicationContext itself, which implements ApplicationEventPublisher).
 *
 * Alternatively: implement ApplicationEventPublisherAware interface.
 * Or: inject ApplicationContext directly (it implements both).
 */
@Service
class EmployeeEventService {

    /**
     * ApplicationEventPublisher - Spring's interface for publishing events.
     * Inject via @Autowired constructor injection (preferred) or field injection.
     *
     * publishEvent(event) - synchronous by default; all listeners run in THIS thread.
     *                       If any listener throws: exception propagates to publisher.
     */
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public EmployeeEventService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Example: create employee, then publish event for listeners to react.
     * In real app: this would be inside an @Transactional method.
     */
    public void createEmployee(Long id, String name, String email) {
        // Business logic: save to DB...
        System.out.println("[SERVICE] Saving employee to database: " + name);

        // Publish event AFTER successful save
        // Listeners will be notified (sync or async depending on their config)
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(this, id, name, email);
        eventPublisher.publishEvent(event);

        System.out.println("[SERVICE] EmployeeCreatedEvent published for: " + name);
    }

    /**
     * Demonstrates publishing a POJO event (no ApplicationEvent required since Spring 4.2).
     */
    public void updateSalary(Long id, double oldSalary, double newSalary) {
        System.out.println("[SERVICE] Updating salary in DB...");
        // POJO event - no need to extend ApplicationEvent
        eventPublisher.publishEvent(new SalaryChangedEvent(id, oldSalary, newSalary));
    }

    public void deleteEmployee(Long id) {
        System.out.println("[SERVICE] Deleting employee from DB: " + id);
        eventPublisher.publishEvent(new EmployeeDeletedEvent(this, id));
    }
}

// ================================================================
// EVENT LISTENERS
// ================================================================

/**
 * EmployeeEventListener - handles employee-related events.
 *
 * @Component: registers as a Spring bean so Spring can detect @EventListener methods.
 */
@Component
class EmployeeEventListener {

    // ----------------------------------------------------------------
    // @EventListener - basic synchronous listener
    // ----------------------------------------------------------------
    /**
     * Listens for EmployeeCreatedEvent.
     *
     * Spring detects this method via @EventListener annotation.
     * Fires SYNCHRONOUSLY in the same thread as the publisher.
     *
     * Method parameter type = the event type to listen for.
     * You can also listen to multiple event types:
     *   @EventListener(classes = {EmployeeCreatedEvent.class, EmployeeDeletedEvent.class})
     *
     * Conditional listening with SpEL:
     *   @EventListener(condition = "#event.employeeName.startsWith('A')")
     *   - Only triggers for employees whose name starts with 'A'
     */
    @EventListener
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        System.out.printf("[LISTENER] Employee created: id=%d, name=%s, email=%s at %s%n",
                event.getEmployeeId(), event.getEmployeeName(),
                event.getEmployeeEmail(), event.getOccurredAt());
        // Real actions: send welcome email, create audit record, update search index, etc.
        System.out.println("[LISTENER] Sending welcome email to: " + event.getEmployeeEmail());
    }

    // ----------------------------------------------------------------
    // @EventListener on POJO event
    // ----------------------------------------------------------------
    /**
     * Listens to SalaryChangedEvent (plain POJO, no ApplicationEvent extension).
     * Works exactly the same as ApplicationEvent-based listeners.
     *
     * Conditional expression: only trigger if salary increased.
     */
    @EventListener(condition = "#event.newSalary > #event.oldSalary")
    public void handleSalaryIncrease(SalaryChangedEvent event) {
        double increase = event.getNewSalary() - event.getOldSalary();
        System.out.printf("[LISTENER] Salary increased for employee %d: %.2f -> %.2f (increase: %.2f)%n",
                event.getEmployeeId(), event.getOldSalary(), event.getNewSalary(), increase);
        // Real action: notify HR, update compensation records
    }

    // ----------------------------------------------------------------
    // @EventListener returning a value = chaining events
    // ----------------------------------------------------------------
    /**
     * Event chaining: if a listener RETURNS a non-null value, Spring
     * publishes that returned value as a NEW event.
     *
     * Useful for event pipelines: Event A -> Listener -> Event B -> Listener
     *
     * Here: EmployeeDeletedEvent -> this listener -> publishes a String (for demo only)
     * In real use: return another ApplicationEvent subclass.
     */
    @EventListener
    public void handleEmployeeDeleted(EmployeeDeletedEvent event) {
        System.out.println("[LISTENER] Employee deleted: id=" + event.getEmployeeId());
        // Clean up: remove from cache, search index, audit log entry
    }
}

// ================================================================
// TRANSACTIONAL EVENT LISTENER
// ================================================================

/**
 * TransactionalEmployeeListener - demonstrates @TransactionalEventListener.
 *
 * KEY DIFFERENCE from @EventListener:
 *   @EventListener: fires immediately when publishEvent() is called
 *   @TransactionalEventListener: fires only after the transaction commits
 *
 * This is CRITICAL for operations like sending emails or calling external APIs.
 * You don't want to send an email if the DB transaction rolls back.
 */
@Component
class TransactionalEmployeeListener {

    /**
     * AFTER_COMMIT (DEFAULT phase): fires after the transaction that published
     * this event has SUCCESSFULLY committed.
     *
     * Use case: send email ONLY after employee record is in the database.
     * If transaction rolls back (e.g., DB constraint violation), email is NOT sent.
     *
     * fallbackExecution = true: also fires if there's NO active transaction.
     * (Without this, the event is silently dropped if no transaction is active)
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void onEmployeeCreatedAfterCommit(EmployeeCreatedEvent event) {
        System.out.printf("[TRANSACTIONAL-LISTENER AFTER_COMMIT] DB committed! " +
                "Sending welcome email to: %s%n", event.getEmployeeEmail());
        // SAFE to send email/call external API now - DB data is persisted
    }

    /**
     * BEFORE_COMMIT: fires JUST BEFORE commit, still WITHIN the transaction.
     * Use case: additional validation or audit before the transaction finalizes.
     * Can still roll back here (throw exception to roll back).
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEmployeeCreatedBeforeCommit(EmployeeCreatedEvent event) {
        System.out.println("[TRANSACTIONAL-LISTENER BEFORE_COMMIT] About to commit - " +
                "validating employee: " + event.getEmployeeName());
        // Could throw exception here to prevent commit
    }

    /**
     * AFTER_ROLLBACK: fires only if the transaction ROLLED BACK.
     * Use case: compensating action, alerting, cleanup after failed transaction.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onEmployeeCreatedAfterRollback(EmployeeCreatedEvent event) {
        System.out.println("[TRANSACTIONAL-LISTENER AFTER_ROLLBACK] Transaction rolled back for: "
                + event.getEmployeeName() + " - sending failure alert");
    }

    /**
     * AFTER_COMPLETION: fires regardless of commit or rollback.
     * Use case: cleanup that should always happen (close resources, release locks).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void onEmployeeCreatedAfterCompletion(EmployeeCreatedEvent event) {
        System.out.println("[TRANSACTIONAL-LISTENER AFTER_COMPLETION] Transaction completed " +
                "(committed or rolled back) for employee: " + event.getEmployeeId());
    }
}

// ================================================================
// ASYNC EVENT LISTENER
// ================================================================

/**
 * AsyncEmployeeListener - processes events in a separate thread.
 *
 * @Async makes the listener method execute in a separate thread from the
 * TaskExecutor thread pool (configured in AppConfig.java).
 *
 * Why async?
 *   - Long-running operations (sending email, generating PDF, calling external API)
 *     should not block the main request thread
 *   - Fire-and-forget: publisher doesn't wait for listener to complete
 *
 * Requirements:
 *   1. @EnableAsync on a @Configuration class (see AsyncConfig below)
 *   2. @Async on the listener method
 *   3. TaskExecutor bean configured (or use Spring Boot default)
 */
@Component
class AsyncEmployeeListener {

    /**
     * Async listener: runs in a separate thread from the TaskExecutor pool.
     *
     * The publisher's thread returns immediately after publishEvent().
     * This method runs concurrently in a pool thread.
     *
     * Exception handling: exceptions in @Async methods are NOT propagated
     * to the caller. Use AsyncUncaughtExceptionHandler to handle them.
     */
    @Async
    @EventListener
    public void handleEmployeeCreatedAsync(EmployeeCreatedEvent event) {
        System.out.printf("[ASYNC-LISTENER] Thread: %s | Processing employee: %s%n",
                Thread.currentThread().getName(), event.getEmployeeName());

        // Simulate slow operation (e.g., PDF generation, external API call)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf("[ASYNC-LISTENER] Finished async processing for: %s%n",
                event.getEmployeeName());
    }
}

// ================================================================
// BUILT-IN SPRING LIFECYCLE EVENT LISTENERS
// ================================================================

/**
 * ApplicationLifecycleListener - listens to Spring's built-in lifecycle events.
 *
 * These are useful for:
 *   - Running initialization code after application starts
 *   - Validating configuration at startup
 *   - Warm-up operations (pre-loading caches)
 *   - Graceful shutdown (cleanup)
 *
 * Alternative to @EventListener for startup: implement CommandLineRunner
 * or ApplicationRunner interface (also runs at startup).
 */
@Component
class ApplicationLifecycleListener {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * ContextRefreshedEvent:
     *   - Fired when ApplicationContext is initialized or refreshed
     *   - All beans are created and autowired by this point
     *   - Fired at startup AND if context.refresh() is called manually
     *
     * CAUTION: can fire MULTIPLE times in some configurations
     * (e.g., parent + child contexts in Spring MVC web apps).
     * Guard with a flag or use ApplicationReadyEvent instead.
     */
    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        // This fires when all beans are initialized
        System.out.println("[LIFECYCLE] ContextRefreshedEvent: All beans initialized. " +
                "Total beans: " + applicationContext.getBeanDefinitionCount());
    }

    /**
     * ApplicationReadyEvent:
     *   - Fired when the application is fully started and ready to serve requests
     *   - Best event for "do this once on startup" logic
     *   - Fires AFTER all ApplicationRunner and CommandLineRunner beans complete
     *   - Fires ONCE (unlike ContextRefreshedEvent which can fire multiple times)
     *
     * Use cases:
     *   - Pre-loading caches
     *   - Sending "application started" notification to monitoring
     *   - Scheduling first-time data sync
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        System.out.println("[LIFECYCLE] ApplicationReadyEvent: Application is READY to serve requests!");
        System.out.println("[LIFECYCLE] Application started in: "
                + event.getTimeTaken().toMillis() + "ms");
    }

    /**
     * ContextClosedEvent:
     *   - Fired when the ApplicationContext is being closed (app shutdown)
     *   - All beans are still available, not destroyed yet
     *   - Good for graceful shutdown: finish in-flight requests, flush queues
     *
     * Alternative: @PreDestroy on bean methods for per-bean cleanup.
     */
    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        System.out.println("[LIFECYCLE] ContextClosedEvent: Application is SHUTTING DOWN. " +
                "Cleaning up resources...");
    }

    /**
     * ContextStartedEvent:
     *   - Fired when context.start() is called (not the same as context refresh)
     *   - Less commonly used in Spring Boot (Boot calls refresh, not start)
     */
    @EventListener
    public void onContextStarted(ContextStartedEvent event) {
        System.out.println("[LIFECYCLE] ContextStartedEvent received");
    }

    /**
     * ContextStoppedEvent:
     *   - Fired when context.stop() is called
     *   - Less common in Boot applications
     */
    @EventListener
    public void onContextStopped(ContextStoppedEvent event) {
        System.out.println("[LIFECYCLE] ContextStoppedEvent received");
    }
}

// ================================================================
// @EnableAsync configuration (required for @Async listeners)
// ================================================================

/**
 * AsyncConfig - enables async execution for @Async-annotated methods.
 *
 * @EnableAsync: scans for @Async methods and wraps them in async proxy.
 * Without this, @Async is silently ignored (methods run synchronously).
 *
 * In the full project this lives in AppConfig.java; shown here for completeness.
 */
@Configuration
@EnableAsync
class AsyncEventsConfig {
    // The ThreadPoolTaskExecutor bean is configured in AppConfig.java
    // If no executor defined, Spring uses SimpleAsyncTaskExecutor (creates new thread per call)
    // For production: always define a proper ThreadPoolTaskExecutor
}
