package com.interview.master.springboot.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * ============================================================
 * SPRING AOP - Complete Interview Guide
 * ============================================================
 *
 * AOP = Aspect-Oriented Programming
 * Purpose: Separates CROSS-CUTTING CONCERNS from business logic.
 * Cross-cutting concerns: logging, security, transactions, caching,
 *   auditing, performance monitoring, retry logic, circuit breaking.
 *
 * Without AOP: every service method repeats logging/timing code (scattered & tangled).
 * With AOP:    declare "log every service method" once in one place.
 *
 * ---------------------------------------------------------------
 * AOP CORE TERMINOLOGY:
 * ---------------------------------------------------------------
 *
 * 1. ASPECT (@Aspect)
 *    - A class that encapsulates cross-cutting logic
 *    - Equivalent to a regular class but annotated with @Aspect
 *    - Contains Pointcuts and Advice
 *    - Example: LoggingAspect, SecurityAspect, TransactionAspect
 *
 * 2. JOIN POINT (JoinPoint)
 *    - A specific point during program execution where an aspect can be applied
 *    - In Spring AOP: ALWAYS a method execution (method call on a Spring bean)
 *    - AspectJ supports more: field access, constructor call, exception handling
 *    - Represented by JoinPoint interface in advice methods
 *    - Key methods: getSignature(), getTarget(), getArgs(), getThis()
 *
 * 3. POINTCUT (@Pointcut)
 *    - An expression that SELECTS which join points to intercept
 *    - "Apply this advice to all methods matching this expression"
 *    - Pointcut expressions use AspectJ pointcut language
 *    - Reusable: define once, reference multiple times
 *
 * 4. ADVICE
 *    - The ACTION taken at a matched join point (the actual code to execute)
 *    - 5 types: @Before, @After, @AfterReturning, @AfterThrowing, @Around
 *
 * 5. WEAVING
 *    - The process of linking aspects with the target object
 *    - Compile-time weaving: AspectJ compiler modifies .class files during compilation
 *    - Load-time weaving: AspectJ modifies .class files when loaded by classloader
 *    - Runtime weaving: Spring AOP uses PROXIES at runtime (default)
 *
 * 6. TARGET OBJECT
 *    - The object being advised (the original bean)
 *    - Spring wraps it in a proxy that applies advice
 *
 * 7. PROXY
 *    - Spring AOP creates a proxy wrapping the target
 *    - JDK Dynamic Proxy: if target implements an interface (uses java.lang.reflect.Proxy)
 *    - CGLIB Proxy: if target does NOT implement an interface (subclass-based)
 *
 * ---------------------------------------------------------------
 * ADVICE TYPES:
 * ---------------------------------------------------------------
 *
 * @Before          - Runs BEFORE the method. Cannot stop method unless throws exception.
 * @After           - Runs AFTER method (always, like finally block). No access to return value.
 * @AfterReturning  - Runs AFTER SUCCESSFUL return. Has access to return value.
 * @AfterThrowing   - Runs AFTER method THROWS exception. Has access to exception.
 * @Around          - Wraps the method. Most powerful:
 *                    - Can run code before AND after
 *                    - Can skip method execution entirely
 *                    - Can modify arguments before calling method
 *                    - Can modify/replace return value
 *                    - Must call pjp.proceed() to invoke the actual method
 *
 * Execution order (normal flow):
 *   @Around (before proceed()) -> @Before -> [method] -> @AfterReturning -> @After -> @Around (after proceed())
 *
 * Execution order (exception flow):
 *   @Around (before proceed()) -> @Before -> [method throws] -> @AfterThrowing -> @After -> @Around (exception propagates)
 *
 * ---------------------------------------------------------------
 * POINTCUT EXPRESSIONS:
 * ---------------------------------------------------------------
 *
 * execution(modifiers? return-type declaring-type? method-name(params) throws?)
 *   * = wildcard (any single element)
 *   .. = wildcard (any number of elements, used in package paths and params)
 *
 * Examples:
 *   execution(* com.interview.master..*.*(..))
 *     = any method, any class, in com.interview.master or any sub-package, any params
 *
 *   execution(public * *(..))
 *     = any public method in any class
 *
 *   execution(* com.interview..service.*Service.*(..))
 *     = any method on any class ending in "Service" in service package
 *
 *   execution(* com.interview..*.find*(..))
 *     = any method starting with "find"
 *
 *   @annotation(com.interview.master.aop.Loggable)
 *     = any method annotated with @Loggable
 *
 *   within(@org.springframework.stereotype.Service *)
 *     = any method in a class annotated @Service
 *
 *   within(com.interview.master.springboot.controller.*)
 *     = any method in the controller package (current package only, not sub-packages)
 *
 *   bean(employeeService)
 *     = any method on the Spring bean named "employeeService"
 *
 *   args(java.lang.String, ..)
 *     = any method where first arg is String
 *
 * Combining expressions:
 *   execution(..) && @annotation(..)   = AND
 *   execution(..) || within(..)        = OR
 *   !execution(..)                     = NOT
 *
 * ---------------------------------------------------------------
 * SPRING AOP vs ASPECTJ:
 * ---------------------------------------------------------------
 *
 *                Spring AOP          AspectJ
 * Weaving:       Runtime (proxy)     Compile/Load-time
 * Scope:         Method execution    Method, field, constructor, etc.
 * Overhead:      Low (proxy creation)Higher (requires AspectJ compiler)
 * Use case:      80% of needs        Complex AOP requirements
 * Setup:         Zero config         Requires ajc compiler or agent
 *
 * ---------------------------------------------------------------
 * PROXY INTERNALS:
 * ---------------------------------------------------------------
 *
 * Q: How does Spring create a proxy for AOP?
 *
 * Case 1: Target implements interface (e.g., EmployeeService implements IEmployeeService)
 *   -> JDK Dynamic Proxy (java.lang.reflect.Proxy.newProxyInstance)
 *   -> Proxy implements same interface, delegates to target, applies advice
 *   -> Client must use interface type (not concrete class) for injection
 *
 * Case 2: Target does NOT implement interface (e.g., plain EmployeeService class)
 *   -> CGLIB Proxy (Code Generation Library)
 *   -> Creates a SUBCLASS of target at runtime
 *   -> Overrides methods to apply advice
 *   -> Cannot proxy final classes or final methods (can't override them)
 *
 * Spring Boot uses CGLIB by default since Spring Boot 2.x
 * (spring.aop.proxy-target-class=true)
 *
 * Q: Why does AOP NOT work for self-invocation (calling a method on 'this')?
 *   -> The call bypasses the proxy! AOP intercepts external calls through the proxy.
 *   -> Solution: inject the bean into itself via @Autowired, or use AspectJ.
 *
 * ---------------------------------------------------------------
 * TRANSACTION AOP INTERNALS:
 * ---------------------------------------------------------------
 * @Transactional works via AOP:
 *   1. Spring wraps bean in TransactionInterceptor proxy
 *   2. @Around advice begins transaction before method
 *   3. Commits if method returns normally
 *   4. Rolls back if method throws RuntimeException (default)
 *   5. This is why @Transactional doesn't work for self-invocation!
 * ============================================================
 */

// ================================================================
// CUSTOM ANNOTATIONS (defined here; in real project: separate files)
// ================================================================

/**
 * @Loggable - Custom annotation to mark methods for detailed logging.
 *
 * @Retention(RUNTIME): annotation available at runtime (needed for reflection/AOP)
 * @Target(METHOD): can only be applied to methods
 * @Documented: included in Javadoc
 *
 * Usage: @Loggable on any method -> LoggingAspect will log it in detail.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@interface Loggable {
    String value() default "";  // optional description
}

/**
 * @TrackExecutionTime - marks methods whose execution time should be tracked.
 * Can be applied to methods or entire classes (all methods in class).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@interface TrackExecutionTime {
    long warnThresholdMs() default 1000;  // warn if execution exceeds this
}

// ================================================================
// MAIN LOGGING ASPECT
// ================================================================

/**
 * LoggingAspect - central logging cross-cutting concern.
 *
 * @Aspect  : marks this class as an AOP aspect
 * @Component: registers it as a Spring bean (needed for Spring to detect it)
 *
 * Both annotations are required. @Aspect alone won't make Spring manage the bean.
 * @Component alone won't tell Spring this is an aspect.
 */
@Aspect
@Component
public class LoggingAspect {

    // ================================================================
    // POINTCUT DEFINITIONS
    // ================================================================
    // Best practice: define reusable @Pointcut methods and reference them
    // by method name. Method body is always empty - it's just a handle.

    /**
     * Matches all methods in any class within the controller package.
     * within() matches at class level (all methods in matching class).
     */
    @Pointcut("within(com.interview.master.springboot.controller..*)")
    public void controllerLayer() {}

    /**
     * Matches all methods in any class annotated with @Service.
     * Preferred over within(..service..*) because it's annotation-driven.
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {}

    /**
     * Matches all methods in any class annotated with @Repository.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryLayer() {}

    /**
     * Matches ALL Spring-managed bean methods in the application packages.
     * The .. means "this package AND all sub-packages".
     */
    @Pointcut("within(com.interview.master..*)")
    public void applicationPackage() {}

    /**
     * Matches methods annotated with @Loggable (our custom annotation).
     * @annotation() matches methods with the given annotation.
     */
    @Pointcut("@annotation(com.interview.master.springboot.aop.Loggable)")
    public void loggableMethod() {}

    /**
     * Matches methods annotated with @TrackExecutionTime.
     */
    @Pointcut("@annotation(com.interview.master.springboot.aop.TrackExecutionTime)")
    public void trackExecutionTimeMethod() {}

    /**
     * Composite pointcut: any method in service OR controller layers.
     * Combines two pointcuts with || (OR).
     */
    @Pointcut("serviceLayer() || controllerLayer()")
    public void applicationLayer() {}

    // ================================================================
    // @Before ADVICE
    // ================================================================
    /**
     * @Before - runs BEFORE the matched method executes.
     *
     * JoinPoint parameter gives access to:
     *   getSignature()          : method name, declaring class, return type
     *   getArgs()               : actual argument values passed to the method
     *   getTarget()             : the target object (the actual bean, not proxy)
     *   getThis()               : the proxy object
     *   getKind()               : "method-execution"
     *
     * Note: @Before cannot prevent the method from running (unless it throws).
     * To conditionally skip: use @Around and don't call proceed().
     */
    @Before("applicationLayer()")
    public void logMethodEntry(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args     = joinPoint.getArgs();

        // In real app: use SLF4J logger -> private static final Logger log = LoggerFactory.getLogger(...)
        System.out.printf("[BEFORE] Entering %s.%s() with args: %s%n",
                className, methodName, Arrays.toString(args));
    }

    // ================================================================
    // @AfterReturning ADVICE
    // ================================================================
    /**
     * @AfterReturning - runs AFTER method returns successfully (no exception).
     *
     * returning = "result" : binds the return value to the 'result' parameter.
     *   - The parameter name must match the 'returning' attribute value exactly.
     *   - The type of 'result' parameter filters which return types trigger this advice.
     *     Object result -> matches any return type
     *     String result -> only matches methods returning String
     *
     * Note: You CANNOT modify the return value in @AfterReturning.
     * To modify return value: use @Around.
     */
    @AfterReturning(
            pointcut = "applicationLayer()",
            returning = "result"
    )
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // Avoid logging huge return values - truncate to 200 chars
        String resultStr = result != null ? result.toString() : "null";
        if (resultStr.length() > 200) resultStr = resultStr.substring(0, 200) + "...";

        System.out.printf("[AFTER-RETURNING] Exiting %s.%s() with result: %s%n",
                className, methodName, resultStr);
    }

    // ================================================================
    // @AfterThrowing ADVICE
    // ================================================================
    /**
     * @AfterThrowing - runs AFTER the method throws an exception.
     *
     * throwing = "exception" : binds the thrown exception to the 'exception' parameter.
     *   - Type filter: Throwable catches all, RuntimeException catches only unchecked.
     *
     * IMPORTANT: @AfterThrowing does NOT suppress the exception.
     * The exception still propagates after this advice runs.
     * To suppress: use @Around with try/catch around pjp.proceed().
     *
     * Use case: centralized error logging (log once here, not in every method).
     */
    @AfterThrowing(
            pointcut = "applicationLayer()",
            throwing = "exception"
    )
    public void logException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        System.err.printf("[AFTER-THROWING] Exception in %s.%s(): [%s] %s%n",
                className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    // ================================================================
    // @After ADVICE (finally)
    // ================================================================
    /**
     * @After - runs AFTER method execution regardless of outcome (return or throw).
     * Equivalent to a finally block.
     *
     * Less commonly used than @AfterReturning + @AfterThrowing separately,
     * because it has no access to return value or exception.
     *
     * Use case: releasing resources, audit trail ("method was called, period").
     */
    @After("loggableMethod()")
    public void logAfterLoggableMethod(JoinPoint joinPoint) {
        System.out.printf("[AFTER] @Loggable method %s.%s() completed (success or failure)%n",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName());
    }

    // ================================================================
    // @Around ADVICE - Execution Time Measurement
    // ================================================================
    /**
     * @Around - the most powerful advice type. Wraps the entire method.
     *
     * ProceedingJoinPoint extends JoinPoint with:
     *   proceed()        : invoke the target method with original args
     *   proceed(args)    : invoke target method with MODIFIED args
     *
     * MUST call proceed() to actually execute the target method.
     * If you don't call proceed(), the method is skipped entirely.
     *
     * Return value: whatever @Around returns becomes the method's return value.
     * This lets you modify/replace the actual return value.
     *
     * @Around can:
     *   - Execute code before AND after (unlike @Before/@After)
     *   - Catch and suppress exceptions (try/catch around proceed())
     *   - Retry the method (call proceed() multiple times)
     *   - Modify method arguments (pass different args to proceed())
     *   - Modify or replace return value
     *   - Short-circuit (return without calling proceed())
     */
    @Around("applicationLayer()")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        long startTime = System.currentTimeMillis();

        Object result;
        try {
            // THIS IS THE CRITICAL CALL - executes the actual method
            // Without this, the method never runs!
            result = pjp.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;

            // Warn if slow
            if (elapsed > 1000) {
                System.err.printf("[PERF-WARN] SLOW METHOD: %s.%s() took %dms (threshold: 1000ms)%n",
                        className, methodName, elapsed);
            } else {
                System.out.printf("[PERF] %s.%s() executed in %dms%n",
                        className, methodName, elapsed);
            }
        }

        return result;
    }

    // ================================================================
    // @Around for @TrackExecutionTime custom annotation
    // ================================================================
    /**
     * Handles methods annotated with @TrackExecutionTime.
     * Demonstrates how to access annotation attributes inside advice.
     *
     * Two ways to access the annotation:
     * 1. Via MethodSignature reflection (shown below)
     * 2. Bind it as an advice parameter:
     *    @Around("@annotation(trackAnnotation)")
     *    public Object track(ProceedingJoinPoint pjp, TrackExecutionTime trackAnnotation) throws Throwable
     *    -> Spring injects the annotation instance directly!
     */
    @Around("trackExecutionTimeMethod()")
    public Object trackAnnotatedMethod(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        // Get annotation instance via reflection to read its attributes
        TrackExecutionTime annotation = signature.getMethod()
                .getAnnotation(TrackExecutionTime.class);
        long warnThreshold = annotation.warnThresholdMs();

        String label = signature.getDeclaringType().getSimpleName()
                + "." + signature.getName() + "()";

        long start = System.nanoTime();
        Object result = pjp.proceed();  // execute the actual method
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        if (elapsedMs > warnThreshold) {
            System.err.printf("[TRACK] %s EXCEEDED threshold: %dms > %dms%n",
                    label, elapsedMs, warnThreshold);
        } else {
            System.out.printf("[TRACK] %s completed in %dms%n", label, elapsedMs);
        }

        return result;
    }

    // ================================================================
    // @Around for @Loggable custom annotation - detailed logging
    // ================================================================
    /**
     * Detailed logging for methods annotated with @Loggable.
     * Demonstrates binding annotation parameter directly in advice signature.
     *
     * The pointcut "@annotation(loggable)" binds the actual @Loggable
     * annotation instance to the 'loggable' parameter so we can read
     * its attributes (value() in this case).
     */
    @Around("@annotation(loggable)")
    public Object detailedLogging(ProceedingJoinPoint pjp, Loggable loggable) throws Throwable {
        String description = loggable.value().isBlank()
                ? pjp.getSignature().getName()
                : loggable.value();

        System.out.printf("[LOGGABLE] START: %s | Args: %s%n",
                description, Arrays.toString(pjp.getArgs()));

        try {
            Object result = pjp.proceed();
            System.out.printf("[LOGGABLE] END: %s | Result: %s%n", description, result);
            return result;
        } catch (Throwable t) {
            System.err.printf("[LOGGABLE] ERROR: %s | Exception: %s - %s%n",
                    description, t.getClass().getSimpleName(), t.getMessage());
            throw t;  // re-throw so normal exception handling still applies
        }
    }
}

// ================================================================
// SEPARATE AUDIT ASPECT
// ================================================================

/**
 * AuditingAspect - records WHO called WHAT operation and WHEN.
 *
 * Demonstrates a second separate aspect, showing that cross-cutting
 * concerns can be split into focused, single-responsibility aspects.
 *
 * In production: would write to an audit_log database table.
 */
@Aspect
@Component
class AuditingAspect {

    /**
     * Audit all state-modifying operations (POST, PUT, PATCH, DELETE controllers).
     * Matches controller methods starting with create, update, delete, patch, save, remove.
     */
    @Pointcut("within(com.interview.master.springboot.controller..*) && " +
              "execution(* create*(..)) || " +
              "within(com.interview.master.springboot.controller..*) && " +
              "execution(* update*(..)) || " +
              "within(com.interview.master.springboot.controller..*) && " +
              "execution(* delete*(..)) || " +
              "within(com.interview.master.springboot.controller..*) && " +
              "execution(* save*(..))")
    public void writeOperations() {}

    /**
     * @AfterReturning on write operations: log successful mutations.
     *
     * In production: get current user from SecurityContextHolder:
     *   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     *   String username = auth.getName();
     */
    @AfterReturning("writeOperations()")
    public void auditWriteOperation(JoinPoint joinPoint) {
        // In production: String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        String actor     = "current-user";  // placeholder
        String operation = joinPoint.getSignature().toShortString();
        String timestamp = LocalDateTime.now().toString();

        System.out.printf("[AUDIT] Actor=%s | Operation=%s | Time=%s%n",
                actor, operation, timestamp);
    }
}

// ================================================================
// PERFORMANCE MONITORING ASPECT
// ================================================================

/**
 * PerformanceAspect - dedicated to performance monitoring.
 * Collects metrics that could be exported to Prometheus/Micrometer.
 *
 * Demonstrates: @Around modifying arguments before passing to method.
 */
@Aspect
@Component
class PerformanceAspect {

    /**
     * Monitor all repository (database) operations - they're often the bottleneck.
     * Repository calls are most sensitive to performance degradation.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryMethod() {}

    @Around("repositoryMethod()")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        long start = System.nanoTime();

        try {
            Object result = pjp.proceed();
            long elapsedUs = (System.nanoTime() - start) / 1000;

            // In production: Micrometer timer.record(elapsed, TimeUnit.MICROSECONDS)
            System.out.printf("[DB-PERF] %s -> %dµs%n", method, elapsedUs);

            return result;
        } catch (Throwable t) {
            long elapsedUs = (System.nanoTime() - start) / 1000;
            System.err.printf("[DB-PERF-ERROR] %s failed after %dµs: %s%n",
                    method, elapsedUs, t.getMessage());
            throw t;
        }
    }
}
