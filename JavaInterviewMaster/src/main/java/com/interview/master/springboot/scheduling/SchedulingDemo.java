package com.interview.master.springboot.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ============================================================
 * SPRING BOOT SCHEDULING COMPLETE INTERVIEW GUIDE
 * ============================================================
 *
 * Enabling Scheduling:
 * 1. Add @EnableScheduling to any @Configuration class (or main class)
 * 2. Annotate a method in a @Component or @Service with @Scheduled
 *
 * Important: By default, all @Scheduled tasks share a SINGLE thread pool
 * of size 1 (TaskScheduler backed by a single-threaded ScheduledExecutorService).
 * This means tasks run sequentially - one must finish before the next starts.
 * To run tasks concurrently, configure a ThreadPoolTaskScheduler.
 *
 * ─────────────────────────────────────────────────────────────
 * @Scheduled PARAMETERS:
 * ─────────────────────────────────────────────────────────────
 *
 * fixedRate    - Period between START of each execution (milliseconds)
 *               Task can overlap if previous execution takes longer than the rate
 *
 * fixedDelay   - Delay between END of previous execution and START of next (milliseconds)
 *               Tasks never overlap - next starts after previous finishes
 *
 * initialDelay - Delay before the FIRST execution (milliseconds)
 *               Useful to let the application fully start up before first run
 *
 * cron         - Cron expression for complex scheduling (time-based)
 *
 * timeUnit     - Unit for fixedRate/fixedDelay/initialDelay (default: MILLISECONDS)
 *               Can use TimeUnit.SECONDS, TimeUnit.MINUTES, etc.
 *
 * zone         - TimeZone for cron expressions (default: server's default timezone)
 *
 * ─────────────────────────────────────────────────────────────
 * CRON EXPRESSION FORMAT:
 * ─────────────────────────────────────────────────────────────
 *
 * Spring uses 6-field cron (seconds, minutes, hours, day-of-month, month, day-of-week):
 *
 *  ┌─────────── second       (0-59)
 *  │ ┌───────── minute       (0-59)
 *  │ │ ┌─────── hour         (0-23)
 *  │ │ │ ┌───── day-of-month (1-31)
 *  │ │ │ │ ┌─── month        (1-12 or JAN-DEC)
 *  │ │ │ │ │ ┌─ day-of-week  (0-7 or SUN-SAT, both 0 and 7 = Sunday)
 *  │ │ │ │ │ │
 *  * * * * * *
 *
 * Special characters:
 *  *  = every value (wildcard)
 *  ,  = list of values          → "MON,WED,FRI"
 *  -  = range of values         → "9-17" (9 AM to 5 PM)
 *  /  = increment               → "0/5" (every 5 starting from 0)
 *  ?  = no specific value       → used in day-of-month OR day-of-week (not both)
 *  L  = last                    → "L" in day-of-month = last day of month
 *  W  = nearest weekday         → "15W" = nearest weekday to 15th
 *  #  = nth weekday             → "2#3" = 3rd Monday of month
 *
 * COMMON CRON EXAMPLES:
 *  "0 * * * * *"              - every minute (at second 0)
 *  "0 0 * * * *"              - every hour (at minute 0, second 0)
 *  "0 0 9 * * *"              - every day at 9:00 AM
 *  "0 0 9 * * MON-FRI"        - every weekday at 9:00 AM
 *  "0 0 0 * * *"              - every day at midnight
 *  "0 0 0 1 * *"              - first day of every month at midnight
 *  "0 0 0 1 1 *"              - January 1st at midnight (New Year)
 *  "0 0/5 * * * *"            - every 5 minutes
 *  "0 0 9-17 * * MON-FRI"     - every hour from 9 AM to 5 PM on weekdays
 *  "0 0 12 ? * MON"           - every Monday at noon
 *  "0 30 10 L * *"            - last day of every month at 10:30 AM
 *  "0 0 6 * * ?"              - every day at 6 AM
 *
 * Spring provides named schedules via org.springframework.scheduling.annotation.Scheduled:
 *  @Scheduled(cron = "@hourly")   ≡ "0 0 * * * *"
 *  @Scheduled(cron = "@daily")    ≡ "0 0 0 * * *"   (same as @midnight)
 *  @Scheduled(cron = "@weekly")   ≡ "0 0 0 * * 0"
 *  @Scheduled(cron = "@monthly")  ≡ "0 0 0 1 * *"
 *  @Scheduled(cron = "@yearly")   ≡ "0 0 0 1 1 *"   (same as @annually)
 *
 * NOTE: Spring's cron is 6-field (with seconds). Standard Unix cron is 5-field (no seconds).
 * Quartz cron is 7-field (adds year). Be careful which format your tool uses.
 *
 * ─────────────────────────────────────────────────────────────
 * fixedRate vs fixedDelay (KEY INTERVIEW DIFFERENCE):
 * ─────────────────────────────────────────────────────────────
 *
 *  fixedRate=5000 (every 5 seconds from START):
 *  Timeline: |--RUN(3s)--|--2s--|--RUN(3s)--|--2s--|
 *  T=0: start. T=3: finish. T=5: start again (5s from T=0).
 *
 *  fixedDelay=5000 (5 seconds after END):
 *  Timeline: |--RUN(3s)--|-----5s-----|--RUN--|-----5s-----|
 *  T=0: start. T=3: finish. T=8: start again (3s execution + 5s delay).
 *
 *  Use fixedRate for: regular heartbeats, polling at fixed intervals (clock-driven)
 *  Use fixedDelay for: tasks that should rest between runs (completion-driven)
 */

// =========================================================
// CONFIGURATION: Enable Scheduling and Async
// =========================================================

@Configuration
@EnableScheduling   // Activates Spring's scheduling infrastructure
@EnableAsync        // Activates Spring's async execution infrastructure
class SchedulingConfig {

    /**
     * Custom ThreadPoolTaskScheduler to run scheduled tasks concurrently.
     *
     * DEFAULT behavior: Spring creates a single-threaded task scheduler.
     * All @Scheduled tasks share one thread → tasks queue up sequentially.
     *
     * With a pool: multiple tasks can execute concurrently.
     * poolSize = number of concurrent scheduled tasks allowed.
     *
     * Interview Q: What happens if a fixedRate task takes longer than the rate?
     * Default (single thread): next execution is delayed until current finishes.
     * With thread pool: tasks can overlap (concurrent execution).
     */
    @Bean
    public org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler taskScheduler() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler scheduler =
                new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);                               // 5 concurrent tasks
        scheduler.setThreadNamePrefix("scheduled-task-");       // helpful for debugging
        scheduler.setAwaitTerminationSeconds(60);               // wait up to 60s on shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);    // finish running tasks on shutdown
        scheduler.setErrorHandler(throwable ->
                LoggerFactory.getLogger(SchedulingConfig.class)
                        .error("Scheduled task error: {}", throwable.getMessage(), throwable)
        );
        return scheduler;
    }

    /**
     * Async thread pool for @Async methods.
     * Without this, @Async uses a simple SimpleAsyncTaskExecutor (creates new thread per call).
     * Better to configure an explicit thread pool.
     */
    @Bean
    public java.util.concurrent.Executor asyncTaskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor =
                new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
}

// =========================================================
// SCHEDULED TASKS DEMONSTRATION
// =========================================================

@Component
class ScheduledTasksDemo {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksDemo.class);

    // -------------------------------------------------------
    // fixedRate: every 5 seconds from START of previous execution
    // -------------------------------------------------------

    /**
     * FIXED RATE: executes every 5000ms from the START of last execution.
     *
     * Timeline with 2s execution time:
     *  T=0: starts. T=2: finishes. T=5: starts again (regardless of finish time).
     *
     * If execution takes LONGER than the rate (e.g., 7s):
     *  Single-threaded: next execution is queued and runs immediately after current finishes.
     *  Multi-threaded (pool): tasks overlap concurrently.
     *
     * Use case: heartbeat to external service, metrics collection, cache refresh.
     */
    @Scheduled(fixedRate = 5000) // every 5 seconds
    public void fixedRateTask() {
        log.info("[fixedRate] Task started at: {}", LocalDateTime.now());
        // Simulate work
        try {
            Thread.sleep(2000); // 2 seconds of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[fixedRate] Task completed at: {}", LocalDateTime.now());
    }

    // -------------------------------------------------------
    // fixedDelay: 5 seconds AFTER previous execution completes
    // -------------------------------------------------------

    /**
     * FIXED DELAY: executes 5000ms AFTER the END of previous execution.
     *
     * Timeline with 2s execution time:
     *  T=0: starts. T=2: finishes. T=7: starts again (2s execution + 5s delay).
     *
     * Total period = execution time + delay.
     * Tasks NEVER overlap (safe for non-thread-safe operations).
     *
     * Use case: polling database, processing work queue (don't want to overlap).
     */
    @Scheduled(fixedDelay = 5000) // 5 seconds after completion
    public void fixedDelayTask() {
        log.info("[fixedDelay] Task started at: {}", LocalDateTime.now());
        try {
            Thread.sleep(2000); // simulate 2 seconds of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[fixedDelay] Task completed at: {}", LocalDateTime.now());
    }

    // -------------------------------------------------------
    // initialDelay + fixedRate: wait before first run
    // -------------------------------------------------------

    /**
     * INITIAL DELAY: wait 10 seconds before first execution, then every 5 seconds.
     *
     * Use case: tasks that require the application to fully start up first.
     * For example: warming up caches, establishing connections, loading configuration.
     *
     * Interview Q: Why use initialDelay?
     * Some resources may not be ready immediately after Spring context loads.
     * E.g., @KafkaListener, database connection, Feign client discovery.
     */
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    public void taskWithInitialDelay() {
        log.info("[initialDelay] Running at: {} (started 10s after app start)", LocalDateTime.now());
    }

    // -------------------------------------------------------
    // initialDelay + fixedDelay
    // -------------------------------------------------------

    /**
     * Initial delay of 15 seconds, then 3 seconds after each completion.
     */
    @Scheduled(initialDelay = 15000, fixedDelay = 3000)
    public void taskWithInitialDelayAndFixedDelay() {
        log.info("[initialDelay+fixedDelay] Running at: {}", LocalDateTime.now());
    }

    // -------------------------------------------------------
    // TimeUnit: use SECONDS/MINUTES instead of milliseconds
    // -------------------------------------------------------

    /**
     * timeUnit parameter avoids large millisecond numbers (since Spring 5.3.10 / Boot 2.5.5).
     * Much more readable than fixedRate=3600000 for "every hour".
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)        // every hour
    public void hourlyTask() {
        log.info("[hourly] Running hourly task at: {}", LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)    // every 30 seconds after completion
    public void thirtySecondDelayTask() {
        log.info("[30s delay] Running task at: {}", LocalDateTime.now());
    }

    @Scheduled(initialDelay = 1, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void everyFiveMinutes() {
        log.info("[5min] Running every 5 minutes at: {}", LocalDateTime.now());
    }

    // -------------------------------------------------------
    // Cron expressions
    // -------------------------------------------------------

    /**
     * CRON: every weekday at 9:00 AM.
     * "0 0 9 * * MON-FRI"
     *   sec=0, min=0, hour=9, any day-of-month, any month, Mon-Fri
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void weekdayMorningTask() {
        log.info("[cron] Weekday morning report at: {}", LocalDateTime.now());
        // Generate and send daily reports
    }

    /**
     * CRON: every day at midnight (00:00:00).
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightCleanupTask() {
        log.info("[cron] Midnight cleanup at: {}", LocalDateTime.now());
        // Clean up expired sessions, temp files, etc.
    }

    /**
     * CRON: every minute (useful for testing; use fixedRate for production).
     * "0 * * * * *" - at second 0 of every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void everyMinuteTask() {
        log.info("[cron] Every minute task at: {}", LocalDateTime.now());
    }

    /**
     * CRON: every 5 minutes.
     * "0 0/5 * * * *" - at second 0, every 5 minutes
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void everyFiveMinutesCron() {
        log.info("[cron] Every 5 minutes at: {}", LocalDateTime.now());
    }

    /**
     * CRON: first day of each month at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 1 * *")
    public void monthlyTask() {
        log.info("[cron] Monthly billing run at: {}", LocalDateTime.now());
    }

    /**
     * CRON: every Sunday at 2:00 AM for weekly maintenance.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyMaintenanceTask() {
        log.info("[cron] Weekly maintenance at: {}", LocalDateTime.now());
    }

    /**
     * CRON with explicit timezone.
     * Run at 9 AM IST (India Standard Time) regardless of server timezone.
     * Interview Q: Why set zone? Server may be in UTC, but business logic is timezone-specific.
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Kolkata")
    public void taskInIndiaTimezone() {
        log.info("[cron IST] India timezone task at: {}", LocalDateTime.now());
    }

    /**
     * CRON using Spring named macro (cleaner syntax).
     */
    @Scheduled(cron = "@daily")   // equivalent to "0 0 0 * * *"
    public void dailyTaskWithMacro() {
        log.info("[cron @daily] Daily task at: {}", LocalDateTime.now());
    }

    // -------------------------------------------------------
    // Reading cron/rate from application.properties
    // -------------------------------------------------------

    /**
     * EXTERNALIZED SCHEDULE: read from application.properties.
     * application.properties:
     *   app.scheduling.cron.report=0 0 8 * * MON-FRI
     *   app.scheduling.rate.sync=30000
     *
     * Allows changing schedule without recompiling.
     * Spring EL (SpEL) ${...} resolves the property value.
     */
    @Scheduled(cron = "${app.scheduling.cron.report:0 0 8 * * MON-FRI}")
    // default value after ':' used if property not found
    public void configurableReportTask() {
        log.info("[configurable cron] Report task at: {}", LocalDateTime.now());
    }

    @Scheduled(fixedRateString = "${app.scheduling.rate.sync:30000}")
    public void configurableSyncTask() {
        log.info("[configurable fixedRate] Sync task at: {}", LocalDateTime.now());
    }

    @Scheduled(fixedDelayString = "${app.scheduling.delay.cleanup:60000}")
    public void configurableCleanupTask() {
        log.info("[configurable fixedDelay] Cleanup task at: {}", LocalDateTime.now());
    }
}

// =========================================================
// @Async ON SCHEDULER: Non-Blocking Execution
// =========================================================

/**
 * PROBLEM with @Scheduled alone:
 * By default, all @Scheduled methods run on a single-thread scheduler.
 * If a task blocks (e.g., slow DB query), other tasks are delayed.
 *
 * SOLUTION: Add @Async to the @Scheduled method.
 * - @Scheduled triggers the task on the scheduler thread
 * - @Async immediately delegates to the async thread pool
 * - Scheduler thread is freed immediately for next task
 *
 * Interview Q: Can you use @Async and @Scheduled together?
 * Yes. @Scheduled triggers timing, @Async handles execution on a separate thread.
 * Requires BOTH @EnableScheduling AND @EnableAsync.
 *
 * WARNING: With @Async, tasks can overlap!
 * If fixedRate=5s and task takes 7s, next task starts on a new thread at T=5s
 * while previous task is still running. Use fixedDelay (not fixedRate) with @Async
 * if overlap is not desired, OR use synchronization.
 */
@Component
class AsyncScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(AsyncScheduledTasks.class);

    // AtomicBoolean ensures thread-safe check to prevent overlap
    private final AtomicBoolean taskRunning = new AtomicBoolean(false);

    /**
     * @Async + @Scheduled: task is triggered every 5 seconds,
     * but executed on a thread from the async pool (not the scheduler thread).
     * Scheduler thread is immediately free to trigger other tasks.
     */
    @Async("asyncTaskExecutor") // use the named executor bean
    @Scheduled(fixedRate = 5000)
    public void asyncScheduledTask() {
        log.info("[async] Task started on thread: {}", Thread.currentThread().getName());
        try {
            // Simulate long-running task (would block the scheduler if not @Async)
            Thread.sleep(8000); // 8 seconds - longer than fixedRate of 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[async] Task completed on thread: {}", Thread.currentThread().getName());
    }

    /**
     * @Async + @Scheduled with overlap prevention.
     * Uses AtomicBoolean as a lock to prevent concurrent task execution.
     */
    @Async
    @Scheduled(fixedRate = 5000)
    public void asyncScheduledTaskNoOverlap() {
        // compareAndSet: atomically set to true only if currently false (acquire lock)
        if (!taskRunning.compareAndSet(false, true)) {
            log.warn("[async-noOverlap] Previous task still running, skipping this trigger");
            return; // Skip if already running
        }

        try {
            log.info("[async-noOverlap] Task started at: {}", LocalDateTime.now());
            Thread.sleep(3000); // simulate work
            log.info("[async-noOverlap] Task completed at: {}", LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            taskRunning.set(false); // always release lock
        }
    }

    /**
     * Async I/O bound task (e.g., calling external API, writing to S3).
     * @Async prevents blocking the scheduler thread on I/O waits.
     */
    @Async
    @Scheduled(cron = "0 0 * * * *") // every hour
    public void asyncHourlyExternalApiSync() {
        log.info("[async-cron] Starting hourly external API sync on thread: {}",
                Thread.currentThread().getName());
        // simulateExternalApiCall();
        log.info("[async-cron] Completed hourly sync");
    }
}

// =========================================================
// DYNAMIC SCHEDULING WITH ScheduledTaskRegistrar
// =========================================================

/**
 * DYNAMIC SCHEDULING:
 * ─────────────────────────────────────────────────────────────
 * @Scheduled is static - the schedule is fixed at startup.
 * SchedulingConfigurer allows programmatic, dynamic task registration.
 *
 * Use cases:
 * - Schedule from database configuration (user-configurable schedules)
 * - Conditional scheduling (enable/disable at runtime)
 * - Different schedules per environment
 * - Registering tasks that depend on other beans/data
 *
 * ScheduledTaskRegistrar:
 * - addFixedRateTask(Runnable, intervalMs)
 * - addFixedDelayTask(Runnable, delayMs)
 * - addCronTask(Runnable, cronExpression)
 * - addTriggerTask(Runnable, Trigger) - most flexible
 *
 * Trigger: interface with nextExecutionTime(TriggerContext) method.
 * CronTrigger: standard cron-based trigger.
 * PeriodicTrigger: periodic trigger (fixed rate or fixed delay).
 * Custom Trigger: full control over next execution time.
 */
@Configuration
@EnableScheduling
class DynamicSchedulingConfig implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulingConfig.class);

    // Simulates a schedule repository (in real app, query DB for schedules)
    private volatile String currentCronExpression = "0 0/2 * * * *"; // every 2 minutes

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        // Set custom thread pool for dynamic tasks
        taskRegistrar.setScheduler(dynamicTaskScheduler());

        // -------------------------------------------------------
        // 1. Register a fixed-rate task programmatically
        // -------------------------------------------------------
        taskRegistrar.addFixedRateTask(
                () -> log.info("[dynamic-fixedRate] Running at: {}", LocalDateTime.now()),
                5000L // 5 seconds
        );

        // -------------------------------------------------------
        // 2. Register a cron task programmatically
        // -------------------------------------------------------
        taskRegistrar.addCronTask(
                () -> log.info("[dynamic-cron] Cron task at: {}", LocalDateTime.now()),
                "0 0 * * * *" // every hour
        );

        // -------------------------------------------------------
        // 3. Register a Trigger task (dynamic - reads schedule each time)
        // -------------------------------------------------------

        /**
         * TriggerTask: most powerful option.
         * Trigger.nextExecutionTime() is called AFTER each execution to determine
         * when to schedule the NEXT run.
         * This allows changing the schedule at runtime!
         *
         * Interview Q: How do you implement a schedule that can be updated at runtime?
         * A: Use TriggerTask with a custom Trigger that reads schedule from DB/config.
         */
        taskRegistrar.addTriggerTask(
                // The task to execute
                () -> log.info("[dynamic-trigger] Running with dynamic schedule at: {}",
                        LocalDateTime.now()),

                // The trigger: called after each execution to get next run time
                triggerContext -> {
                    // In real app: query DB for the latest cron expression
                    // This way, if admin updates schedule in DB, it takes effect immediately
                    String cronExpr = getCurrentCronFromDatabase();
                    CronTrigger trigger = new CronTrigger(cronExpr);
                    return trigger.nextExecutionTime(triggerContext);
                }
        );

        // -------------------------------------------------------
        // 4. PeriodicTrigger - programmatic fixed-rate/delay trigger
        // -------------------------------------------------------

        PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofSeconds(10));
        periodicTrigger.setInitialDelay(Duration.ofSeconds(5));
        periodicTrigger.setFixedRate(true); // true=fixedRate, false=fixedDelay

        taskRegistrar.addTriggerTask(
                () -> log.info("[periodic-trigger] Running at: {}", LocalDateTime.now()),
                periodicTrigger
        );

        // -------------------------------------------------------
        // 5. Custom Trigger implementation
        // -------------------------------------------------------

        taskRegistrar.addTriggerTask(
                () -> log.info("[custom-trigger] Running at: {}", LocalDateTime.now()),
                new BusinessHoursTrigger()
        );
    }

    /**
     * Simulates reading cron expression from a database.
     * In real code: @Autowired repository, then repository.findScheduleConfig().getCronExpression()
     */
    private String getCurrentCronFromDatabase() {
        // In production: return scheduleRepository.findByCronName("myTask").getCronExpression();
        return currentCronExpression;
    }

    /**
     * Update the schedule at runtime (e.g., via REST endpoint or admin panel).
     * The TriggerTask will pick up the new expression on next trigger evaluation.
     */
    public void updateCronExpression(String newCronExpression) {
        log.info("Updating cron expression from '{}' to '{}'",
                currentCronExpression, newCronExpression);
        this.currentCronExpression = newCronExpression;
    }

    @Bean(name = "dynamicTaskScheduler", destroyMethod = "shutdown")
    public java.util.concurrent.ScheduledExecutorService dynamicTaskScheduler() {
        return Executors.newScheduledThreadPool(3);
    }
}

// =========================================================
// CUSTOM TRIGGER: Run only during business hours
// =========================================================

/**
 * Custom Trigger that only runs during business hours (9 AM - 5 PM, Mon-Fri).
 * If current time is outside business hours, schedules for next business day at 9 AM.
 *
 * Demonstrates the full power of the Trigger API.
 */
class BusinessHoursTrigger implements org.springframework.scheduling.Trigger {

    private static final Logger log = LoggerFactory.getLogger(BusinessHoursTrigger.class);
    private static final Duration CHECK_INTERVAL = Duration.ofMinutes(30); // run every 30 min

    @Override
    public Date nextExecutionTime(org.springframework.scheduling.TriggerContext triggerContext) {
        Date lastActualExecution = triggerContext.lastActualExecutionTime();
        Instant now = Instant.now();

        // First run: add 30 minutes from now
        if (lastActualExecution == null) {
            return Date.from(now.plus(CHECK_INTERVAL));
        }

        // Calculate next run time
        Instant nextRun = lastActualExecution.toInstant().plus(CHECK_INTERVAL);

        // Adjust to business hours (9 AM - 5 PM Monday-Friday)
        nextRun = adjustToBusinessHours(nextRun);

        log.debug("BusinessHoursTrigger: next run scheduled at {}", nextRun);
        return Date.from(nextRun);
    }

    private Instant adjustToBusinessHours(Instant proposedTime) {
        java.time.ZonedDateTime zdt = proposedTime.atZone(ZoneId.systemDefault());
        int hour = zdt.getHour();
        java.time.DayOfWeek dayOfWeek = zdt.getDayOfWeek();

        // If weekend, move to Monday 9 AM
        if (dayOfWeek == java.time.DayOfWeek.SATURDAY) {
            zdt = zdt.plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        } else if (dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            zdt = zdt.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        }
        // If before 9 AM, move to 9 AM same day
        else if (hour < 9) {
            zdt = zdt.withHour(9).withMinute(0).withSecond(0).withNano(0);
        }
        // If after 5 PM, move to next weekday 9 AM
        else if (hour >= 17) {
            zdt = zdt.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
            // Skip weekend
            if (zdt.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
                zdt = zdt.plusDays(2);
            } else if (zdt.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                zdt = zdt.plusDays(1);
            }
        }

        return zdt.toInstant();
    }
}

// =========================================================
// MANUAL SCHEDULING WITH TaskScheduler
// =========================================================

/**
 * TaskScheduler: programmatic API for scheduling tasks at runtime.
 * More flexible than @Scheduled: can start/stop/reschedule tasks from code.
 *
 * Use case: admin REST endpoint to start/stop a scheduled job on demand.
 */
@Service
class ManualSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ManualSchedulerService.class);

    private final TaskScheduler taskScheduler;

    // Holds reference to the scheduled task so we can cancel it
    private ScheduledFuture<?> scheduledFuture;

    @Autowired
    public ManualSchedulerService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    /**
     * Start a periodic task at runtime.
     * Can be called from a REST endpoint: POST /api/scheduler/start
     */
    public void startTask(long intervalMs) {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            log.warn("Task is already running. Stop it before starting again.");
            return;
        }

        Runnable task = () -> log.info("[manual] Manual task running at: {}", LocalDateTime.now());

        // scheduleAtFixedRate: same semantics as @Scheduled(fixedRate=...)
        scheduledFuture = taskScheduler.scheduleAtFixedRate(task, Duration.ofMillis(intervalMs));
        log.info("Started manual task with interval={}ms", intervalMs);
    }

    /**
     * Stop the running task.
     * Can be called from a REST endpoint: POST /api/scheduler/stop
     */
    public void stopTask() {
        if (scheduledFuture != null) {
            boolean cancelled = scheduledFuture.cancel(false); // false=don't interrupt if running
            log.info("Task cancellation requested. Cancelled: {}", cancelled);
        } else {
            log.warn("No running task to stop.");
        }
    }

    /**
     * Schedule a task to run once at a specific time in the future.
     */
    public void scheduleOneTime(Runnable task, LocalDateTime runAt) {
        Instant scheduledTime = runAt.atZone(ZoneId.systemDefault()).toInstant();
        taskScheduler.schedule(task, scheduledTime);
        log.info("One-time task scheduled for: {}", runAt);
    }

    /**
     * Reschedule: stop current task and start with new interval.
     */
    public void reschedule(long newIntervalMs) {
        log.info("Rescheduling task with new interval={}ms", newIntervalMs);
        stopTask();
        startTask(newIntervalMs);
    }

    /**
     * Schedule a task with a cron trigger via TaskScheduler.
     */
    public void scheduleWithCron(String cronExpression) {
        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        scheduledFuture = taskScheduler.schedule(
                () -> log.info("[manual-cron] Cron task at: {}", LocalDateTime.now()),
                cronTrigger
        );
        log.info("Scheduled task with cron: {}", cronExpression);
    }
}

// =========================================================
// INTERVIEW RECAP (in comments at the bottom)
// =========================================================

/**
 * ─────────────────────────────────────────────────────────────
 * QUICK INTERVIEW RECAP - SPRING SCHEDULING
 * ─────────────────────────────────────────────────────────────
 *
 * Q: What is the difference between fixedRate and fixedDelay?
 * A: fixedRate: period between START of executions (can overlap with thread pool).
 *    fixedDelay: period between END of one execution and START of next (never overlaps).
 *    fixedRate = clock-driven. fixedDelay = completion-driven.
 *
 * Q: How many threads does Spring use for @Scheduled by default?
 * A: One single thread (single-threaded scheduler). All tasks queue up.
 *    Configure ThreadPoolTaskScheduler bean to increase pool size.
 *
 * Q: What is the cron expression format in Spring?
 * A: 6 fields: "second minute hour day-of-month month day-of-week"
 *    "0 0 9 * * MON-FRI" = 9 AM on weekdays.
 *    Spring uses 6 fields (includes seconds). Unix cron has 5 fields (no seconds).
 *
 * Q: How do you run a scheduled task asynchronously?
 * A: Add @Async to the @Scheduled method. Requires @EnableAsync.
 *    Scheduler thread triggers the task; execution is delegated to async thread pool.
 *
 * Q: How do you change a schedule at runtime without restarting?
 * A: Implement SchedulingConfigurer.configureTasks() and register a TriggerTask.
 *    The Trigger.nextExecutionTime() is called after each run - reads fresh schedule from DB.
 *
 * Q: How do you programmatically start/stop a scheduled task?
 * A: Use TaskScheduler bean. It returns ScheduledFuture<?> which has cancel() method.
 *    Store the ScheduledFuture reference; call cancel(false) to stop.
 *
 * Q: What is initialDelay?
 * A: Wait period before the FIRST execution. Useful when app needs time to initialize
 *    (connections, caches, service discovery registration, etc.).
 *
 * Q: Can you use @Scheduled on a method that returns a value?
 * A: No. @Scheduled requires void return type. Return values are ignored.
 *
 * Q: What annotations are needed for scheduling to work?
 * A: @EnableScheduling on a @Configuration class (or main class).
 *    @Component/@Service on the class containing @Scheduled methods.
 *    The @Scheduled class must be a Spring-managed bean.
 *
 * Q: Can you externalize cron expressions from application.properties?
 * A: Yes. @Scheduled(cron = "${app.cron.expression:0 0 * * * *}")
 *    Uses SpEL ${} for property injection with : for default value.
 *    fixedRateString and fixedDelayString also accept property placeholders.
 *
 * Q: How to ensure a scheduled task never runs concurrently with itself?
 * A: Use fixedDelay (next run starts after current finishes) with single thread.
 *    Or use @Async with AtomicBoolean lock guard.
 *    Or use ShedLock (distributed lock for clustered environments).
 *
 * Q: What is ShedLock? (advanced)
 * A: Library that prevents the same scheduled task from running simultaneously
 *    on multiple instances of the same service (in a cluster).
 *    Uses a shared lock store (DB, Redis, ZooKeeper) to elect one winner per execution.
 */
class SchedulingInterviewRecap {
    // Recap class - see all comments above for details.
}
