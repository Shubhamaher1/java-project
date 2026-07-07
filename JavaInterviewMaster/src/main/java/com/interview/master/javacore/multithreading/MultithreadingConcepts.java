package com.interview.master.javacore.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.*;

/**
 * ============================================================
 * MULTITHREADING & CONCURRENCY - Complete Interview Guide
 * ============================================================
 *
 * Q: What is a Thread?
 *    A thread is a lightweight subprocess — the smallest unit of processing.
 *    Threads share the process heap (memory) but each has its own call stack,
 *    program counter, and local variables.
 *
 * Q: What is a Process?
 *    A process is an executing instance of a program. It has its own isolated
 *    memory address space (code, heap, stack). Multiple processes do NOT share
 *    memory by default.
 *
 * Q: Process vs Thread?
 *    Process : isolated memory, heavy-weight, slow context switch,
 *              each process has at least one thread.
 *    Thread  : shared heap, light-weight, fast context switch,
 *              threads within a process share data.
 *
 * Q: Why use Multithreading?
 *    1. Parallelism       - utilize multiple CPU cores simultaneously.
 *    2. Responsiveness    - UI stays responsive while background work runs.
 *    3. Resource sharing  - threads share the process heap, lower memory overhead.
 *    4. Throughput        - more work done per unit time for I/O-bound tasks.
 *
 * Q: What is concurrency vs parallelism?
 *    Concurrency  - dealing with many things at once (interleaved execution,
 *                   may be on one core via context switching).
 *    Parallelism  - doing many things at once (simultaneous execution on
 *                   multiple CPU cores).
 * ============================================================
 */
public class MultithreadingConcepts {

    // ----------------------------------------------------------------
    // Shared counter used across many demos
    // ----------------------------------------------------------------
    private static int unsafeCounter = 0;
    private static volatile int volatileCounter = 0;
    private static final AtomicInteger atomicCounter = new AtomicInteger(0);

    // ----------------------------------------------------------------
    // 4. synchronized demo variables
    // ----------------------------------------------------------------
    private int syncCount = 0;
    private static int staticSyncCount = 0;
    private final Object lockA = new Object();
    private final Object lockB = new Object();

    // ================================================================
    // 1. THREAD CREATION — Three (+ lambda) Ways
    // ================================================================

    /**
     * Way 1: Extend the Thread class.
     *
     * Interview Q: Downside of extending Thread?
     * Answer: Java supports single inheritance only. If you extend Thread
     *         you cannot extend any other class. This couples the task logic
     *         with the threading mechanism — violates single responsibility.
     */
    static class MyThread extends Thread {
        private final String taskName;

        MyThread(String name) {
            super(name);          // sets thread name
            this.taskName = name;
        }

        @Override
        public void run() {
            System.out.println("[MyThread] " + taskName
                    + " running on thread: " + Thread.currentThread().getName());
        }
    }

    /**
     * Way 2: Implement Runnable interface.
     *
     * Interview Q: Why prefer Runnable over Thread extension?
     * Answer:
     *   1. Java single inheritance — with Runnable you can still extend another class.
     *   2. Separates the task (what to do) from execution mechanism (how to run it).
     *   3. Runnable task can be reused with different thread/executor strategies.
     *   4. Better OOP design — favour composition over inheritance.
     */
    static class MyRunnable implements Runnable {
        private final String taskName;

        MyRunnable(String name) { this.taskName = name; }

        @Override
        public void run() {
            System.out.println("[MyRunnable] " + taskName
                    + " running on: " + Thread.currentThread().getName());
        }
    }

    /**
     * Way 3: Implement Callable<V>.
     *
     * Interview Q: Runnable vs Callable?
     * Answer:
     *   Runnable: no return value, cannot throw checked exceptions.
     *   Callable: returns a value (V), can throw checked exceptions.
     *   Callable is used with Future / FutureTask to retrieve results.
     */
    static class MyCallable implements Callable<Integer> {
        private final int input;

        MyCallable(int input) { this.input = input; }

        @Override
        public Integer call() throws Exception {
            Thread.sleep(100); // simulate work
            return input * input; // return square of input
        }
    }

    private static void demo1_ThreadCreation() throws Exception {
        System.out.println("\n--- 1. THREAD CREATION ---");

        // Way 1: extend Thread
        MyThread t1 = new MyThread("ThreadExtension");
        t1.start(); // creates NEW OS thread, calls run() on it

        // Way 2: implement Runnable
        Thread t2 = new Thread(new MyRunnable("RunnableImpl"), "RunnableThread");
        t2.start();

        // Way 3: Callable with FutureTask (FutureTask implements both Runnable and Future)
        MyCallable callable = new MyCallable(7);
        FutureTask<Integer> futureTask = new FutureTask<>(callable);
        Thread t3 = new Thread(futureTask, "CallableThread");
        t3.start();
        Integer result = futureTask.get(); // blocks until callable finishes
        System.out.println("[Callable] Result: 7^2 = " + result);

        // Way 4: Lambda (Java 8+) — most concise for simple tasks
        Thread t4 = new Thread(() ->
            System.out.println("[Lambda] Running on: " + Thread.currentThread().getName()),
            "LambdaThread");
        t4.start();

        // Important: always join to let demo threads finish before moving on
        t1.join(); t2.join(); t3.join(); t4.join();
    }

    // ================================================================
    // 2. THREAD LIFECYCLE
    // ================================================================
    /**
     * Thread State Diagram:
     *
     *   NEW ──start()──► RUNNABLE ◄──────────────────────────────────┐
     *                       │                                         │
     *                  scheduler                                      │
     *                       │                                         │
     *                       ▼                                         │
     *                   RUNNING ─────────────────────────────────────┤
     *                       │                                         │
     *              sleep() / wait() / I/O / join()          I/O done / notify() / timeout
     *                       │                                         │
     *                       ▼                                         │
     *       BLOCKED/WAITING/TIMED_WAITING ────────────────────────────┘
     *                       │
     *                   run() ends
     *                       │
     *                       ▼
     *                  TERMINATED
     *
     * NEW           : Thread object created, start() not yet called.
     * RUNNABLE      : start() called; thread may be running or waiting for CPU time.
     * BLOCKED       : Waiting to acquire a monitor lock (e.g., entering synchronized block).
     * WAITING       : wait(), join() (no timeout), LockSupport.park() — indefinite wait.
     * TIMED_WAITING : sleep(ms), wait(ms), join(ms) — waiting with timeout.
     * TERMINATED    : run() completed or thread threw an uncaught exception.
     *
     * Note: BLOCKED is specifically for monitor lock acquisition.
     *       WAITING/TIMED_WAITING is for deliberate pause via wait/sleep/join.
     */
    private static void demo2_ThreadLifecycle() throws Exception {
        System.out.println("\n--- 2. THREAD LIFECYCLE ---");

        Thread t = new Thread(() -> {
            try {
                System.out.println("[Lifecycle] State inside run: "
                        + Thread.currentThread().getState()); // RUNNABLE
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("State before start(): " + t.getState()); // NEW
        t.start();
        Thread.sleep(50); // let it enter sleep
        System.out.println("State while sleeping: " + t.getState()); // TIMED_WAITING
        t.join();
        System.out.println("State after join():   " + t.getState()); // TERMINATED
    }

    // ================================================================
    // 3. KEY THREAD METHODS
    // ================================================================

    /**
     * CRITICAL Interview Q: start() vs run()
     *   start() : JVM creates a NEW thread, then calls run() on that new thread.
     *   run()   : If called directly, run() executes on the CURRENT thread —
     *             NO new thread is created. A very common interview mistake!
     */
    private static void demo3_ThreadMethods() throws Exception {
        System.out.println("\n--- 3. THREAD METHODS ---");

        // ---- start() vs run() ----
        Thread t = new Thread(() ->
            System.out.println("Running on: " + Thread.currentThread().getName()));
        t.start();               // correct: new thread "Thread-X" runs it
        // t.run();              // WRONG: main thread runs it, no new thread created

        // ---- sleep() ----
        // sleep() pauses the current thread for the specified duration.
        // DOES NOT release any monitor lock the thread holds.
        // Throws InterruptedException if interrupted while sleeping.
        Thread.sleep(50); // main thread sleeps 50ms

        // ---- join() ----
        // join() makes the calling thread wait until the target thread terminates.
        // join(ms) — wait at most ms milliseconds.
        Thread worker = new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("[join] Worker finished");
        });
        worker.start();
        worker.join();   // main waits here until worker is done
        System.out.println("[join] Main continues after worker");

        // ---- yield() ----
        // yield() is a HINT to the scheduler that the current thread is willing
        // to give up CPU. Scheduler may ignore it. Use rarely (mostly in tight loops).
        Thread.yield();

        // ---- interrupt() / isInterrupted() ----
        // interrupt() sets the interrupt flag on the target thread.
        // If the thread is in sleep/wait/join it receives InterruptedException.
        // isInterrupted() checks the flag WITHOUT clearing it.
        // Thread.interrupted() (static) checks AND clears the flag.
        Thread interruptable = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // do work...
            }
            System.out.println("[interrupt] Thread was interrupted, stopping gracefully");
        });
        interruptable.start();
        Thread.sleep(20);
        interruptable.interrupt(); // signal it to stop
        interruptable.join();

        // ---- setDaemon(true) ----
        // Daemon threads are background threads (e.g., GC, JVM internal threads).
        // JVM exits when ALL non-daemon threads finish — daemon threads are killed automatically.
        // Must call setDaemon() BEFORE start().
        Thread daemon = new Thread(() -> {
            while (true) { /* background work */ }
        });
        daemon.setDaemon(true); // JVM won't wait for this thread
        daemon.start();
        System.out.println("[daemon] isDaemon: " + daemon.isDaemon());

        // ---- Thread priority ----
        // Priority is a HINT to the OS scheduler (1=MIN, 5=NORM, 10=MAX).
        // Actual behavior depends on OS — do not rely on priority for correctness.
        Thread lowPriority = new Thread(() -> {});
        lowPriority.setPriority(Thread.MIN_PRIORITY);  // 1
        Thread highPriority = new Thread(() -> {});
        highPriority.setPriority(Thread.MAX_PRIORITY); // 10
        System.out.println("[priority] Low=" + lowPriority.getPriority()
                + " High=" + highPriority.getPriority()
                + " Normal=" + Thread.NORM_PRIORITY);

        // ---- getName / getId ----
        System.out.println("[name] Current thread: "
                + Thread.currentThread().getName()
                + " id=" + Thread.currentThread().getId());

        t.join();
    }

    // ================================================================
    // 4. synchronized KEYWORD
    // ================================================================

    /**
     * Interview Q: What is an intrinsic lock (monitor lock)?
     * Answer: Every Java object has a built-in mutex called an intrinsic lock.
     *         synchronized uses this lock. Only one thread at a time can hold
     *         the intrinsic lock for a given object.
     *
     * Interview Q: Is synchronized reentrant?
     * Answer: YES. If a thread already holds the lock, it can re-enter
     *         synchronized blocks on the SAME object without deadlocking.
     */

    // Synchronized INSTANCE method — locks on 'this' object
    public synchronized void synchronizedInstanceMethod() {
        syncCount++;
        System.out.println("[syncMethod] count=" + syncCount
                + " thread=" + Thread.currentThread().getName());
    }

    // Synchronized STATIC method — locks on the Class object (MultithreadingConcepts.class)
    // Different lock from instance method!
    public static synchronized void synchronizedStaticMethod() {
        staticSyncCount++;
        System.out.println("[staticSync] count=" + staticSyncCount);
    }

    // Synchronized BLOCK — more granular, better performance than full method lock
    public void synchronizedBlock() {
        // non-critical section — no lock held here
        int localComputation = 42 * 2; // can run concurrently

        synchronized (this) {
            // critical section — lock held only here
            syncCount++;
        }

        // non-critical section again — lock released
        System.out.println("[syncBlock] local=" + localComputation);
    }

    // Using separate lock objects for independent resources
    public void methodWithSeparateLocks() {
        synchronized (lockA) {
            // operations on resource A
        }
        synchronized (lockB) {
            // operations on resource B (independent of A)
        }
        // Tip: never acquire lockA inside lockB or vice versa — potential deadlock!
    }

    // Demonstrating reentrancy
    public synchronized void outerSynced() {
        System.out.println("[reentrant] outer entered");
        innerSynced(); // same thread re-acquires the same lock — works fine
    }
    public synchronized void innerSynced() {
        System.out.println("[reentrant] inner entered (same lock, same thread)");
    }

    private static void demo4_Synchronized() throws Exception {
        System.out.println("\n--- 4. SYNCHRONIZED ---");

        MultithreadingConcepts obj = new MultithreadingConcepts();

        // Run two threads calling the synchronized instance method
        Thread ta = new Thread(obj::synchronizedInstanceMethod, "SyncA");
        Thread tb = new Thread(obj::synchronizedInstanceMethod, "SyncB");
        ta.start(); tb.start();
        ta.join();  tb.join();

        // Static synchronized
        synchronizedStaticMethod();

        // Demonstrate reentrancy
        obj.outerSynced();

        // Synchronized block demo
        obj.synchronizedBlock();
    }

    // ================================================================
    // 5. volatile KEYWORD
    // ================================================================

    /**
     * volatile guarantees VISIBILITY:
     *   Every write to a volatile variable is immediately flushed to main memory.
     *   Every read fetches the latest value from main memory (bypasses CPU cache).
     *
     * volatile does NOT guarantee ATOMICITY:
     *   volatile int i; i++ is read-modify-write — NOT atomic even with volatile!
     *   Use AtomicInteger or synchronized for atomic compound operations.
     *
     * When to use volatile:
     *   - Simple flags / status variables (boolean stop flag)
     *   - Single-writer, multiple-reader scenarios
     *   - Double-checked locking pattern (singleton)
     *
     * volatile vs synchronized:
     *   volatile    : visibility only, no mutual exclusion, lighter weight
     *   synchronized: visibility + atomicity + mutual exclusion (one thread at a time)
     */

    private volatile boolean running = true; // visible stop flag

    // Double-checked locking with volatile (correct singleton pattern)
    private static volatile MultithreadingConcepts instance = null;
    public static MultithreadingConcepts getInstance() {
        if (instance == null) {                // first check (no lock)
            synchronized (MultithreadingConcepts.class) {
                if (instance == null) {         // second check (with lock)
                    instance = new MultithreadingConcepts();
                }
            }
        }
        return instance;
        // volatile ensures the half-constructed object is never visible to other threads
    }

    private static void demo5_Volatile() throws InterruptedException {
        System.out.println("\n--- 5. VOLATILE ---");

        MultithreadingConcepts obj = new MultithreadingConcepts();

        Thread worker = new Thread(() -> {
            int count = 0;
            while (obj.running) { // reads volatile — always gets latest value
                count++;
            }
            System.out.println("[volatile] Worker stopped after " + count + " iterations");
        });
        worker.start();
        Thread.sleep(5);
        obj.running = false; // write to volatile — immediately visible to worker
        worker.join();

        // Demonstrate volatile does NOT guarantee atomicity
        // (in real code you'd see lost updates; here we just show the concept)
        System.out.println("[volatile] volatile int++ is NOT atomic — use AtomicInteger!");

        // Double-checked locking
        MultithreadingConcepts singleton1 = MultithreadingConcepts.getInstance();
        MultithreadingConcepts singleton2 = MultithreadingConcepts.getInstance();
        System.out.println("[volatile] Same singleton instance: " + (singleton1 == singleton2));
    }

    // ================================================================
    // 6. DEADLOCK
    // ================================================================

    /**
     * Deadlock: Two (or more) threads are each waiting for the other to release
     *           a lock, so neither can proceed.
     *
     * Four Conditions for Deadlock (Coffman Conditions) — ALL must hold:
     *   1. Mutual Exclusion  : resource can be held by only one thread at a time.
     *   2. Hold and Wait     : thread holds a resource while waiting for another.
     *   3. No Preemption     : resources cannot be forcibly taken away.
     *   4. Circular Wait     : T1 waits for T2, T2 waits for T1 (cycle).
     *
     * Detection: jstack <pid>, VisualVM, JConsole — shows "Found one Java-level deadlock"
     *
     * Prevention strategies:
     *   1. Consistent lock ordering — always acquire locks in the same global order.
     *   2. tryLock() with timeout — give up if can't acquire all locks.
     *   3. Lock timeout + retry with backoff.
     *   4. Use higher-level concurrency primitives (avoid manual locking).
     */
    static class DeadlockExample {
        private final Object resource1 = new Object();
        private final Object resource2 = new Object();

        // Thread 1: locks resource1 then resource2
        void thread1Task() throws InterruptedException {
            synchronized (resource1) {
                System.out.println("[Deadlock] Thread1 holds resource1, waiting for resource2");
                Thread.sleep(50); // give thread2 time to grab resource2
                synchronized (resource2) {
                    System.out.println("[Deadlock] Thread1 acquired both resources");
                }
            }
        }

        // Thread 2: locks resource2 then resource1 — opposite order = DEADLOCK!
        void thread2Task() throws InterruptedException {
            synchronized (resource2) {
                System.out.println("[Deadlock] Thread2 holds resource2, waiting for resource1");
                Thread.sleep(50);
                synchronized (resource1) {
                    System.out.println("[Deadlock] Thread2 acquired both resources");
                }
            }
        }

        // FIXED version: both threads acquire locks in the same order
        void thread1Fixed() throws InterruptedException {
            synchronized (resource1) {
                synchronized (resource2) {
                    System.out.println("[DeadlockFix] Thread1 fixed acquired both");
                }
            }
        }
        void thread2Fixed() throws InterruptedException {
            synchronized (resource1) { // same order as thread1Fixed!
                synchronized (resource2) {
                    System.out.println("[DeadlockFix] Thread2 fixed acquired both");
                }
            }
        }
    }

    private static void demo6_Deadlock() throws Exception {
        System.out.println("\n--- 6. DEADLOCK (showing fixed version only) ---");

        // We only demo the FIXED version to avoid hanging the program.
        // The broken version is shown in comments above.
        DeadlockExample de = new DeadlockExample();
        Thread t1 = new Thread(() -> {
            try { de.thread1Fixed(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread t2 = new Thread(() -> {
            try { de.thread2Fixed(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.println("[Deadlock] Both threads completed successfully (fixed version)");
    }

    // ================================================================
    // 7. RACE CONDITION & THREAD SAFETY
    // ================================================================

    /**
     * Race Condition: program result depends on the relative ordering / timing
     *                 of thread execution. Non-deterministic bugs, hard to reproduce.
     *
     * Classic example: count++ is read-modify-write (three operations):
     *   1. READ  count (e.g., 5)
     *   2. ADD   1  -> 6
     *   3. WRITE 6 back to count
     *   If two threads do this simultaneously both read 5, both write 6 -> lost update.
     *
     * Thread Safety Options:
     *   1. synchronized keyword      — mutual exclusion
     *   2. volatile                  — visibility only (not for compound ops)
     *   3. Atomic classes            — lock-free CAS operations
     *   4. java.util.concurrent      — higher-level thread-safe data structures
     *   5. Immutability              — immutable objects need no synchronization
     *   6. ThreadLocal               — each thread has its own copy, no sharing
     */
    static class ThreadSafeCounter {
        private int unsafeCount = 0;
        private volatile int volatileCount = 0;          // not safe for count++
        private final AtomicInteger atomicCount = new AtomicInteger(0);
        private int syncCount = 0;

        // NOT thread-safe: read-modify-write is not atomic
        public void unsafeIncrement() { unsafeCount++; }

        // NOT safe for increment: volatile guarantees visibility but not atomicity
        // Two threads can still both read same value before either writes back
        public void volatileIncrement() { volatileCount++; }

        // Thread-safe: synchronized makes the operation atomic
        public synchronized void syncIncrement() { syncCount++; }

        // Thread-safe: AtomicInteger uses CAS (hardware-level atomic instruction)
        public void atomicIncrement() { atomicCount.incrementAndGet(); }

        public int getSyncCount()   { return syncCount; }
        public int getAtomicCount() { return atomicCount.get(); }
    }

    private static void demo7_RaceCondition() throws Exception {
        System.out.println("\n--- 7. RACE CONDITION & THREAD SAFETY ---");

        ThreadSafeCounter counter = new ThreadSafeCounter();
        int numThreads = 10;
        int increments = 1000;

        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            futures.add(exec.submit(() -> {
                for (int j = 0; j < increments; j++) {
                    counter.syncIncrement();
                    counter.atomicIncrement();
                    counter.unsafeIncrement(); // intentionally racy
                }
            }));
        }
        for (Future<?> f : futures) f.get();
        exec.shutdown();

        int expected = numThreads * increments;
        System.out.println("[Race] Expected:      " + expected);
        System.out.println("[Race] syncCount:     " + counter.getSyncCount()
                + (counter.getSyncCount() == expected ? " CORRECT" : " WRONG"));
        System.out.println("[Race] atomicCount:   " + counter.getAtomicCount()
                + (counter.getAtomicCount() == expected ? " CORRECT" : " WRONG"));
        System.out.println("[Race] unsafeCount:   " + counter.unsafeCount
                + " (likely < " + expected + " due to lost updates)");
    }

    // ================================================================
    // 8. EXECUTOR FRAMEWORK (java.util.concurrent)
    // ================================================================

    /**
     * Why Executor Framework?
     *   Creating a new thread for every task is EXPENSIVE:
     *     - OS thread creation overhead (~1ms)
     *     - Memory per thread stack (~512KB default)
     *   Thread pools REUSE threads: create once, run many tasks.
     *
     * Key interfaces:
     *   Executor         : execute(Runnable) — fire-and-forget
     *   ExecutorService  : extends Executor + submit/shutdown/invokeAll/invokeAny
     *   ScheduledExecutorService: extends ExecutorService + schedule/scheduleAtFixedRate
     *
     * Factory methods (Executors class):
     *   newFixedThreadPool(n)      — exactly n threads, tasks queue if all busy
     *   newCachedThreadPool()      — creates threads as needed, reuses idle ones (60s TTL)
     *   newSingleThreadExecutor()  — single thread, tasks execute sequentially
     *   newScheduledThreadPool(n)  — supports delayed and periodic task execution
     *   newWorkStealingPool()      — ForkJoinPool with parallelism = CPU cores
     *
     * Interview Q: What is RejectionPolicy?
     *   When the thread pool is full AND the queue is full, what happens to new tasks?
     *   AbortPolicy (default) : throws RejectedExecutionException
     *   CallerRunsPolicy      : caller thread executes the task (natural backpressure)
     *   DiscardPolicy         : silently drops the task
     *   DiscardOldestPolicy   : drops oldest queued task, retries submission
     */
    private static void demo8_ExecutorFramework() throws Exception {
        System.out.println("\n--- 8. EXECUTOR FRAMEWORK ---");

        // --- FixedThreadPool ---
        ExecutorService fixedPool = Executors.newFixedThreadPool(3);
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final int taskId = i;
            futures.add(fixedPool.submit(() -> {
                Thread.sleep(50);
                return taskId * taskId;
            }));
        }
        for (int i = 0; i < futures.size(); i++) {
            System.out.println("[FixedPool] Task " + i + " result: " + futures.get(i).get());
        }
        fixedPool.shutdown();
        fixedPool.awaitTermination(5, TimeUnit.SECONDS);

        // --- CachedThreadPool ---
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        cachedPool.submit(() -> System.out.println("[CachedPool] Task 1 on "
                + Thread.currentThread().getName()));
        cachedPool.submit(() -> System.out.println("[CachedPool] Task 2 on "
                + Thread.currentThread().getName()));
        cachedPool.shutdown();

        // --- SingleThreadExecutor ---
        ExecutorService single = Executors.newSingleThreadExecutor();
        single.submit(() -> System.out.println("[SingleThread] Task A"));
        single.submit(() -> System.out.println("[SingleThread] Task B")); // queued after A
        single.submit(() -> System.out.println("[SingleThread] Task C")); // queued after B
        single.shutdown();

        // --- ScheduledExecutorService ---
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        // Execute once after 100ms delay
        scheduler.schedule(() ->
            System.out.println("[Scheduler] Delayed task executed"), 100, TimeUnit.MILLISECONDS);
        // Execute repeatedly every 200ms starting after 50ms
        ScheduledFuture<?> periodic = scheduler.scheduleAtFixedRate(() ->
            System.out.println("[Scheduler] Periodic task"), 50, 200, TimeUnit.MILLISECONDS);

        Thread.sleep(550); // let a few periodic executions happen
        periodic.cancel(false); // stop periodic task
        scheduler.shutdown();

        // --- invokeAll / invokeAny ---
        ExecutorService pool2 = Executors.newFixedThreadPool(4);

        // invokeAll: submit all, wait for ALL to finish, returns List<Future>
        List<Callable<String>> tasks = Arrays.asList(
            () -> { Thread.sleep(100); return "Task-A"; },
            () -> { Thread.sleep(50);  return "Task-B"; },
            () -> { Thread.sleep(75);  return "Task-C"; }
        );
        List<Future<String>> allResults = pool2.invokeAll(tasks);
        allResults.forEach(f -> {
            try { System.out.println("[invokeAll] " + f.get()); }
            catch (Exception e) { e.printStackTrace(); }
        });

        // invokeAny: returns result of FIRST completed task, cancels the rest
        String fastest = pool2.invokeAny(tasks);
        System.out.println("[invokeAny] Fastest result: " + fastest);

        pool2.shutdown();

        // --- ThreadPoolExecutor (manual configuration — production best practice) ---
        ThreadPoolExecutor customPool = new ThreadPoolExecutor(
            2,                                    // corePoolSize: min live threads
            4,                                    // maximumPoolSize: max threads
            60L, TimeUnit.SECONDS,                // keepAliveTime: idle thread TTL
            new ArrayBlockingQueue<>(10),          // workQueue: bounded queue
            Executors.defaultThreadFactory(),      // threadFactory
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
        customPool.submit(() -> System.out.println("[CustomPool] Task running"));
        customPool.shutdown();
        System.out.println("[ThreadPool] Core=" + customPool.getCorePoolSize()
                + " Max=" + customPool.getMaximumPoolSize());
    }

    // ================================================================
    // 9. ReentrantLock & ReadWriteLock
    // ================================================================

    /**
     * ReentrantLock advantages over synchronized:
     *   1. tryLock()          — non-blocking attempt; avoid deadlock
     *   2. tryLock(time, unit) — attempt with timeout
     *   3. lockInterruptibly() — can be interrupted while waiting
     *   4. Fair mode          — new ReentrantLock(true) — FIFO ordering
     *   5. Multiple Condition variables (newCondition())
     *   6. getQueueLength()   — how many threads are waiting
     *
     * RULE: Always call unlock() in a finally block to prevent lock leaks!
     *
     * ReadWriteLock (ReentrantReadWriteLock):
     *   Multiple threads can hold the READ lock simultaneously (no contention).
     *   Only ONE thread can hold the WRITE lock (exclusive).
     *   Great for read-heavy workloads (caches, config maps, etc.)
     *
     * Interview Q: StampedLock (Java 8)?
     *   Even more optimistic — try-optimistic-read that avoids locking entirely
     *   if no write happened. Faster than ReadWriteLock for read-heavy loads.
     */
    static class ReentrantLockDemo {
        private final ReentrantLock lock = new ReentrantLock();
        private final ReentrantLock fairLock = new ReentrantLock(true); // fair
        private int value = 0;

        // Basic lock/unlock pattern
        public void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock(); // MUST be in finally!
            }
        }

        // tryLock — non-blocking; won't deadlock
        public boolean tryIncrement() {
            if (lock.tryLock()) {
                try {
                    value++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false; // lock was not available
        }

        // tryLock with timeout
        public boolean timedIncrement() throws InterruptedException {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    value++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            System.out.println("[ReentrantLock] Timed out, could not acquire lock");
            return false;
        }

        public int getValue() { return value; }
    }

    static class ReadWriteLockDemo {
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Map<String, String> cache = new HashMap<>();

        // Multiple threads can read simultaneously
        public String get(String key) {
            rwLock.readLock().lock();
            try {
                return cache.get(key);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        // Only one thread can write (exclusive)
        public void put(String key, String value) {
            rwLock.writeLock().lock();
            try {
                cache.put(key, value);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    // Condition variable — like wait/notify but for ReentrantLock
    static class BoundedBufferWithCondition {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull  = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();
        private final Queue<Integer> buffer = new LinkedList<>();
        private final int capacity;

        BoundedBufferWithCondition(int capacity) { this.capacity = capacity; }

        public void produce(int item) throws InterruptedException {
            lock.lock();
            try {
                while (buffer.size() == capacity) {
                    notFull.await(); // release lock, wait until not full
                }
                buffer.add(item);
                System.out.println("[Condition] Produced: " + item);
                notEmpty.signal(); // wake one waiting consumer
            } finally {
                lock.unlock();
            }
        }

        public int consume() throws InterruptedException {
            lock.lock();
            try {
                while (buffer.isEmpty()) {
                    notEmpty.await(); // release lock, wait until not empty
                }
                int item = buffer.poll();
                System.out.println("[Condition] Consumed: " + item);
                notFull.signal(); // wake one waiting producer
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    private static void demo9_ReentrantLock() throws Exception {
        System.out.println("\n--- 9. REENTRANT LOCK & READ-WRITE LOCK ---");

        // Basic ReentrantLock
        ReentrantLockDemo rld = new ReentrantLockDemo();
        ExecutorService exec = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 1000; i++) exec.submit(rld::increment);
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("[ReentrantLock] value (expect 1000): " + rld.getValue());

        // tryLock
        System.out.println("[ReentrantLock] tryLock succeeded: " + rld.tryIncrement());
        System.out.println("[ReentrantLock] value after try: " + rld.getValue());

        // ReadWriteLock
        ReadWriteLockDemo rwDemo = new ReadWriteLockDemo();
        rwDemo.put("key1", "value1");
        System.out.println("[ReadWriteLock] get key1: " + rwDemo.get("key1"));

        // Condition variable (bounded buffer)
        BoundedBufferWithCondition buf = new BoundedBufferWithCondition(3);
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try { buf.produce(i); Thread.sleep(20); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try { buf.consume(); Thread.sleep(50); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        producer.start(); consumer.start();
        producer.join();  consumer.join();
    }

    // ================================================================
    // 10. ATOMIC CLASSES (java.util.concurrent.atomic)
    // ================================================================

    /**
     * CAS — Compare And Swap (Compare And Exchange):
     *   Hardware-level atomic operation supported by modern CPUs.
     *   Operation: "If memory[addr] == expected, then set memory[addr] = newValue, else fail"
     *   This is done ATOMICALLY at the hardware level — no OS lock needed.
     *
     * Advantages over synchronized:
     *   - Non-blocking (lock-free) — faster under low-to-medium contention
     *   - No deadlock risk
     *   - Scales better with many threads
     *
     * ABA Problem:
     *   CAS sees value A, value changes to B then back to A — CAS incorrectly
     *   thinks nothing changed. Fix: AtomicStampedReference (adds version stamp).
     *
     * Available atomic classes:
     *   AtomicInteger, AtomicLong, AtomicBoolean, AtomicReference<V>
     *   AtomicIntegerArray, AtomicLongArray, AtomicReferenceArray<E>
     *   AtomicStampedReference<V> (solves ABA problem)
     *   LongAdder, DoubleAdder   (Java 8 — better for high-contention counters)
     *   LongAccumulator, DoubleAccumulator (Java 8 — custom accumulation function)
     */
    private static void demo10_AtomicClasses() throws Exception {
        System.out.println("\n--- 10. ATOMIC CLASSES ---");

        // AtomicInteger operations
        AtomicInteger ai = new AtomicInteger(0);
        System.out.println("[Atomic] Initial: "   + ai.get());
        System.out.println("[Atomic] getAndIncrement (i++): " + ai.getAndIncrement()); // 0, now 1
        System.out.println("[Atomic] incrementAndGet (++i): " + ai.incrementAndGet()); // 2
        System.out.println("[Atomic] getAndAdd(10):         " + ai.getAndAdd(10));     // 2, now 12
        System.out.println("[Atomic] addAndGet(5):          " + ai.addAndGet(5));      // 17
        System.out.println("[Atomic] getAndDecrement (i--): " + ai.getAndDecrement()); // 17, now 16
        System.out.println("[Atomic] decrementAndGet (--i): " + ai.decrementAndGet()); // 15

        // CAS — compareAndSet
        System.out.println("[Atomic] compareAndSet(15,100): " + ai.compareAndSet(15, 100)); // true
        System.out.println("[Atomic] value after CAS: " + ai.get()); // 100
        System.out.println("[Atomic] compareAndSet(99,200): " + ai.compareAndSet(99, 200)); // false
        System.out.println("[Atomic] value unchanged: " + ai.get()); // still 100

        // Java 8+ updateAndGet / accumulateAndGet (lambda-based CAS)
        ai.set(5);
        int squared = ai.updateAndGet(x -> x * x); // atomic compute & set
        System.out.println("[Atomic] updateAndGet(x*x) from 5: " + squared); // 25

        // AtomicLong
        AtomicLong al = new AtomicLong(1_000_000L);
        al.incrementAndGet();
        System.out.println("[Atomic] AtomicLong: " + al.get());

        // AtomicBoolean
        AtomicBoolean flag = new AtomicBoolean(false);
        boolean wasSet = flag.compareAndSet(false, true); // set to true if it was false
        System.out.println("[Atomic] AtomicBoolean CAS(false->true): " + wasSet
                + " value=" + flag.get());

        // AtomicReference — for arbitrary objects
        AtomicReference<String> ar = new AtomicReference<>("hello");
        ar.compareAndSet("hello", "world");
        System.out.println("[Atomic] AtomicReference: " + ar.get());

        // ABA problem demo concept
        // AtomicStampedReference<String> stamped = new AtomicStampedReference<>("A", 0);
        // int[] stamp = new int[1];
        // String val = stamped.get(stamp); // gets value AND current stamp
        // stamped.compareAndSet("A", "B", stamp[0], stamp[0] + 1);
        // Now even if value goes A->B->A, the stamp prevents false success

        // LongAdder — splits counter into cells, reduces contention
        // Much faster than AtomicLong when many threads update concurrently
        LongAdder adder = new LongAdder();
        ExecutorService exec = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 10000; i++) exec.submit(adder::increment);
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("[Atomic] LongAdder sum (expect 10000): " + adder.sum());

        // LongAccumulator — generalised LongAdder with custom function
        LongAccumulator maxAccum = new LongAccumulator(Long::max, Long.MIN_VALUE);
        maxAccum.accumulate(42L);
        maxAccum.accumulate(99L);
        maxAccum.accumulate(7L);
        System.out.println("[Atomic] LongAccumulator max: " + maxAccum.get()); // 99
    }

    // ================================================================
    // 11. SYNCHRONIZATION UTILITIES
    // ================================================================

    /**
     * CountDownLatch:
     *   - One thread (or multiple) WAIT for N events to occur.
     *   - Other threads call countDown() after each event.
     *   - When count reaches zero all waiting threads are released.
     *   - ONE-SHOT: cannot be reset once count hits zero.
     *
     * CyclicBarrier:
     *   - N threads all wait for each other at a synchronization point.
     *   - When all N have called await(), all are released simultaneously.
     *   - REUSABLE (cyclic) — barrier resets after each phase.
     *   - Optional Runnable runs when all threads arrive.
     *
     * CountDownLatch vs CyclicBarrier:
     *   CountDownLatch : one side waits, other side counts down; not reusable.
     *   CyclicBarrier  : all threads mutually wait; reusable; barrier action.
     *
     * Semaphore:
     *   - Controls access to N permits (like a pool of N resources).
     *   - acquire() — get a permit (blocks if none available).
     *   - release() — return a permit.
     *   - Semaphore(1) ≈ mutex (but NOT reentrant like synchronized).
     *
     * Phaser (Java 7):
     *   - Like CyclicBarrier but parties can be added/removed dynamically.
     *   - Supports multiple phases with per-phase actions.
     *   - More flexible but more complex.
     *
     * Exchanger:
     *   - Two threads exchange data at a synchronization point.
     *   - Both threads call exchange(data), each gets the other's data.
     */
    private static void demo11_SyncUtilities() throws Exception {
        System.out.println("\n--- 11. SYNCHRONIZATION UTILITIES ---");

        // ---- CountDownLatch ----
        System.out.println("[CountDownLatch] Starting 3 services...");
        CountDownLatch startLatch = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int serviceId = i;
            new Thread(() -> {
                try {
                    Thread.sleep(serviceId * 30L);
                    System.out.println("[CDL] Service " + serviceId + " ready");
                    startLatch.countDown();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }).start();
        }

        startLatch.await(); // main thread blocks until all 3 services ready
        System.out.println("[CountDownLatch] All services ready — main continues");

        // await with timeout
        CountDownLatch timeoutLatch = new CountDownLatch(1);
        boolean completed = timeoutLatch.await(100, TimeUnit.MILLISECONDS);
        System.out.println("[CountDownLatch] Timed await completed: " + completed); // false

        // ---- CyclicBarrier ----
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties,
            () -> System.out.println("[CyclicBarrier] === All threads reached barrier! ==="));

        for (int i = 0; i < parties; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("[CB] Thread " + id + " doing phase 1 work...");
                    Thread.sleep(id * 20L);
                    System.out.println("[CB] Thread " + id + " waiting at barrier");
                    barrier.await(); // all must reach here before any proceed
                    System.out.println("[CB] Thread " + id + " proceeding to phase 2");
                    Thread.sleep(id * 10L);
                    barrier.await(); // reusable! second phase barrier
                    System.out.println("[CB] Thread " + id + " phase 2 done");
                } catch (Exception e) { Thread.currentThread().interrupt(); }
            }).start();
        }

        Thread.sleep(500); // wait for barrier threads to finish

        // ---- Semaphore ----
        System.out.println("\n[Semaphore] Limiting to 2 concurrent DB connections");
        Semaphore dbPool = new Semaphore(2); // max 2 concurrent accesses

        ExecutorService exec = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            exec.submit(() -> {
                try {
                    dbPool.acquire(); // blocks if 2 connections already in use
                    System.out.println("[Semaphore] Thread " + threadId + " acquired connection. Permits left: " + dbPool.availablePermits());
                    Thread.sleep(50); // simulate DB work
                    System.out.println("[Semaphore] Thread " + threadId + " releasing connection");
                    dbPool.release();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        // tryAcquire — non-blocking
        System.out.println("[Semaphore] tryAcquire: " + dbPool.tryAcquire()); // true
        dbPool.release();

        // Semaphore(1) as mutex (non-reentrant)
        Semaphore mutex = new Semaphore(1);
        mutex.acquire();
        // critical section
        mutex.release();

        // ---- Phaser ----
        Phaser phaser = new Phaser(1); // register self (main thread)
        System.out.println("\n[Phaser] Starting phaser demo");

        for (int i = 0; i < 3; i++) {
            final int id = i;
            phaser.register(); // register new party
            new Thread(() -> {
                System.out.println("[Phaser] Thread " + id + " phase 0");
                phaser.arriveAndAwaitAdvance(); // arrive + wait for all
                System.out.println("[Phaser] Thread " + id + " phase 1");
                phaser.arriveAndDeregister();   // arrive + permanently leave
            }).start();
        }

        phaser.arriveAndAwaitAdvance(); // main arrives at phase 0
        System.out.println("[Phaser] Main: phase 0 complete, all threads advanced");
        phaser.arriveAndDeregister();   // main deregisters

        // ---- Exchanger ----
        Exchanger<String> exchanger = new Exchanger<>();
        Thread threadA = new Thread(() -> {
            try {
                String received = exchanger.exchange("DATA_FROM_A");
                System.out.println("[Exchanger] Thread A received: " + received);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread threadB = new Thread(() -> {
            try {
                String received = exchanger.exchange("DATA_FROM_B");
                System.out.println("[Exchanger] Thread B received: " + received);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        threadA.start(); threadB.start();
        threadA.join();  threadB.join();
    }

    // ================================================================
    // 12. BLOCKINGQUEUE — PRODUCER-CONSUMER PATTERN
    // ================================================================

    /**
     * BlockingQueue is the cleanest way to implement Producer-Consumer.
     * No explicit synchronized/wait/notify needed — BlockingQueue handles it.
     *
     * Key blocking methods:
     *   put(e)    — inserts element, BLOCKS if queue is full
     *   take()    — retrieves & removes, BLOCKS if queue is empty
     *
     * Non-blocking / timed methods:
     *   offer(e)                    — returns false if full (non-blocking)
     *   offer(e, timeout, unit)     — returns false if can't add in time
     *   poll()                      — returns null if empty (non-blocking)
     *   poll(timeout, unit)         — returns null if can't get in time
     *
     * Implementations:
     *   ArrayBlockingQueue(n)   — fixed capacity, FIFO, optional fairness
     *   LinkedBlockingQueue(n)  — optionally bounded (default MAX_INT), FIFO
     *   PriorityBlockingQueue   — unbounded, elements ordered by priority
     *   SynchronousQueue        — zero capacity, direct hand-off between threads
     *   DelayQueue              — elements available only after their delay expires
     *   LinkedTransferQueue     — like SynchronousQueue but can also buffer
     */
    static class ProducerConsumerDemo {
        private final BlockingQueue<Integer> queue;

        ProducerConsumerDemo(int capacity) {
            this.queue = new LinkedBlockingQueue<>(capacity);
        }

        void produce(int count) throws InterruptedException {
            for (int i = 1; i <= count; i++) {
                queue.put(i);
                System.out.println("[Producer] Produced: " + i
                        + " | Queue size: " + queue.size());
                Thread.sleep(30);
            }
        }

        void consume(int count) throws InterruptedException {
            for (int i = 0; i < count; i++) {
                Integer item = queue.take();
                System.out.println("[Consumer] Consumed: " + item);
                Thread.sleep(60); // consume slower than produce
            }
        }
    }

    private static void demo12_BlockingQueue() throws Exception {
        System.out.println("\n--- 12. BLOCKINGQUEUE (PRODUCER-CONSUMER) ---");

        ProducerConsumerDemo pcDemo = new ProducerConsumerDemo(3); // bounded queue size 3

        Thread producer = new Thread(() -> {
            try { pcDemo.produce(6); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try { pcDemo.consume(6); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        // SynchronousQueue demo — zero buffer, direct hand-off
        System.out.println("[SynchronousQueue] Demo:");
        SynchronousQueue<String> syncQ = new SynchronousQueue<>();
        Thread sender   = new Thread(() -> {
            try {
                syncQ.put("HELLO"); // blocks until receiver calls take()
                System.out.println("[SyncQ] Sender: handed off message");
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread receiver = new Thread(() -> {
            try {
                Thread.sleep(50);
                String msg = syncQ.take(); // rendezvous with sender
                System.out.println("[SyncQ] Receiver: got '" + msg + "'");
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        sender.start(); receiver.start();
        sender.join();  receiver.join();

        // PriorityBlockingQueue demo
        PriorityBlockingQueue<Integer> pbq = new PriorityBlockingQueue<>();
        pbq.offer(30); pbq.offer(10); pbq.offer(20);
        System.out.println("[PriorityBQ] Poll order (min first): "
                + pbq.poll() + ", " + pbq.poll() + ", " + pbq.poll()); // 10, 20, 30
    }

    // ================================================================
    // 13. THREAD LOCAL
    // ================================================================

    /**
     * ThreadLocal provides thread-local variables — each thread accessing the
     * variable gets its own, independently initialised copy.
     *
     * Common use cases:
     *   - SimpleDateFormat (not thread-safe), made safe per-thread.
     *   - JDBC Connection per thread.
     *   - Spring Security context (SecurityContextHolder).
     *   - JPA EntityManager / Hibernate Session.
     *   - Request context in web frameworks.
     *   - Logging MDC (Mapped Diagnostic Context).
     *
     * MEMORY LEAK WARNING:
     *   In a thread pool, threads live forever (reused).
     *   If you set a ThreadLocal value but never call remove(), the value
     *   stays alive as long as the thread lives — preventing GC.
     *   ALWAYS call threadLocal.remove() in a finally block when using
     *   ThreadLocal in thread pool contexts.
     *
     * InheritableThreadLocal:
     *   Allows child threads to inherit the parent thread's ThreadLocal values.
     *   Useful for passing context (user ID, request ID) from parent to child.
     */
    private static final ThreadLocal<Integer> threadLocalUserId =
        ThreadLocal.withInitial(() -> -1); // default value if not set

    private static final ThreadLocal<SimpleDateFormat> dateFormatter =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private static final InheritableThreadLocal<String> inheritableContext =
        new InheritableThreadLocal<>();

    private static void demo13_ThreadLocal() throws Exception {
        System.out.println("\n--- 13. THREAD LOCAL ---");

        // Each thread has its own userId — no interference
        Thread user1Thread = new Thread(() -> {
            threadLocalUserId.set(101);
            System.out.println("[ThreadLocal] Thread1 userId: " + threadLocalUserId.get());
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("[ThreadLocal] Thread1 userId still: " + threadLocalUserId.get()); // still 101
            threadLocalUserId.remove(); // CRITICAL: prevent memory leak in thread pools
        });

        Thread user2Thread = new Thread(() -> {
            threadLocalUserId.set(202);
            System.out.println("[ThreadLocal] Thread2 userId: " + threadLocalUserId.get());
            threadLocalUserId.remove();
        });

        user1Thread.start(); user2Thread.start();
        user1Thread.join();  user2Thread.join();

        // ThreadLocal makes non-thread-safe classes thread-safe
        System.out.println("[ThreadLocal] Main date: " + dateFormatter.get().format(new Date()));

        // Demonstrate ThreadLocal in thread pool (memory leak risk!)
        ExecutorService exec = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 4; i++) {
            final int userId = 1000 + i;
            exec.submit(() -> {
                try {
                    threadLocalUserId.set(userId);
                    System.out.println("[ThreadLocal-Pool] task userId: " + threadLocalUserId.get()
                            + " on thread: " + Thread.currentThread().getName());
                } finally {
                    threadLocalUserId.remove(); // MUST do this in thread pools!
                }
            });
        }
        exec.shutdown();
        exec.awaitTermination(3, TimeUnit.SECONDS);

        // InheritableThreadLocal
        inheritableContext.set("REQUEST_ID_ABC123");
        Thread childThread = new Thread(() -> {
            System.out.println("[InheritableThreadLocal] Child sees: " + inheritableContext.get()); // ABC123
        });
        childThread.start();
        childThread.join();
        inheritableContext.remove();
    }

    // ================================================================
    // 14. COMPLETABLE FUTURE (Java 8)
    // ================================================================

    /**
     * CompletableFuture implements both Future<T> and CompletionStage<T>.
     * Enables composable, non-blocking asynchronous programming.
     *
     * Key creation methods:
     *   supplyAsync(Supplier)          — async task that returns a value
     *   runAsync(Runnable)             — async task with no return value
     *   completedFuture(value)         — already-completed future with a value
     *
     * Chaining (pipeline):
     *   thenApply(fn)      — transform result (Function<T,U>), runs on same pool thread
     *   thenApplyAsync(fn) — transform result on different async thread
     *   thenAccept(fn)     — consume result (Consumer<T>), returns void
     *   thenRun(fn)        — run after completion, no input/output (Runnable)
     *   thenCompose(fn)    — flatMap; fn returns CompletableFuture<U> (avoid nesting)
     *
     * Combining:
     *   thenCombine(other, BiFunction)  — combine two futures when both complete
     *   allOf(futures...)               — wait for ALL futures
     *   anyOf(futures...)               — wait for FIRST future
     *
     * Error handling:
     *   exceptionally(fn)  — handle exception, return fallback value
     *   handle(BiFunction) — handle both success and failure in one callback
     *   whenComplete(fn)   — called on completion (success or failure), doesn't alter result
     *
     * Interview Q: get() vs join()?
     *   get()  throws checked Exception (ExecutionException, InterruptedException)
     *   join() throws unchecked CompletionException — easier in streams/lambdas
     */
    private static void demo14_CompletableFuture() throws Exception {
        System.out.println("\n--- 14. COMPLETABLE FUTURE ---");

        // Basic async supply
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("[CF] supplyAsync on: " + Thread.currentThread().getName());
            return "Hello";
        });

        // Chain: thenApply -> thenApply -> thenAccept
        CompletableFuture<Void> pipeline = cf1
            .thenApply(s -> {
                System.out.println("[CF] thenApply: " + s);
                return s + " World";
            })
            .thenApplyAsync(s -> {
                System.out.println("[CF] thenApplyAsync: " + s);
                return s.toUpperCase();
            })
            .thenAccept(s -> System.out.println("[CF] Final result: " + s));

        pipeline.join(); // wait for pipeline to complete

        // thenCompose — flatMap for futures (avoid CompletableFuture<CompletableFuture<T>>)
        CompletableFuture<String> composed = CompletableFuture
            .supplyAsync(() -> "userId-42")
            .thenCompose(userId ->
                CompletableFuture.supplyAsync(() -> "User[" + userId + "]"));
        System.out.println("[CF] thenCompose: " + composed.get());

        // thenCombine — combine two independent futures
        CompletableFuture<String> cfA = CompletableFuture.supplyAsync(() -> "Price: $100");
        CompletableFuture<String> cfB = CompletableFuture.supplyAsync(() -> "Stock: 50");
        String combined = cfA.thenCombine(cfB, (price, stock) -> price + " | " + stock).get();
        System.out.println("[CF] thenCombine: " + combined);

        // allOf — wait for all
        CompletableFuture<String> t1 = CompletableFuture.supplyAsync(() -> { try { Thread.sleep(100); } catch (InterruptedException e) {} return "Task1"; });
        CompletableFuture<String> t2 = CompletableFuture.supplyAsync(() -> { try { Thread.sleep(50);  } catch (InterruptedException e) {} return "Task2"; });
        CompletableFuture<String> t3 = CompletableFuture.supplyAsync(() -> "Task3");
        CompletableFuture.allOf(t1, t2, t3).join();
        System.out.println("[CF] allOf done: " + t1.get() + ", " + t2.get() + ", " + t3.get());

        // anyOf — result of fastest
        CompletableFuture<Object> fastest = CompletableFuture.anyOf(t1, t2, t3);
        System.out.println("[CF] anyOf fastest: " + fastest.get());

        // Exception handling — exceptionally
        CompletableFuture<String> failing = CompletableFuture
            .supplyAsync(() -> { throw new RuntimeException("Something went wrong!"); })
            .exceptionally(ex -> "Fallback: " + ex.getMessage());
        System.out.println("[CF] exceptionally: " + failing.get());

        // handle — deals with both success and failure
        CompletableFuture<String> handled = CompletableFuture
            .supplyAsync(() -> { throw new RuntimeException("Error"); })
            .handle((result, ex) -> ex != null
                ? "Handled error: " + ex.getMessage()
                : "Success: " + result);
        System.out.println("[CF] handle: " + handled.get());

        // whenComplete — observe without changing result
        CompletableFuture.supplyAsync(() -> 42)
            .whenComplete((result, ex) -> {
                if (ex != null) System.out.println("[CF] whenComplete error: " + ex);
                else System.out.println("[CF] whenComplete success: " + result);
            })
            .join();

        // Manual completion
        CompletableFuture<String> manual = new CompletableFuture<>();
        new Thread(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            manual.complete("Manual result");
        }).start();
        System.out.println("[CF] manual.get(): " + manual.get());

        // completeExceptionally
        CompletableFuture<String> manualFail = new CompletableFuture<>();
        manualFail.completeExceptionally(new RuntimeException("manual failure"));
        try { manualFail.get(); }
        catch (ExecutionException e) { System.out.println("[CF] completeExceptionally: " + e.getCause().getMessage()); }
    }

    // ================================================================
    // 15. FORK JOIN POOL (Divide and Conquer)
    // ================================================================

    /**
     * ForkJoinPool is designed for "divide and conquer" algorithms:
     *   1. If problem is small enough, solve directly (base case).
     *   2. Otherwise, FORK (split into sub-tasks) and JOIN (combine results).
     *
     * Work-stealing algorithm:
     *   Each worker thread has its own deque of tasks.
     *   When a thread's deque is empty, it STEALS tasks from the TAIL of
     *   another busy thread's deque — maximizing CPU utilization.
     *
     * RecursiveTask<V>  — computation that returns a result (like Callable)
     * RecursiveAction   — computation with no result (like Runnable)
     *
     * ForkJoinPool.commonPool() — shared pool used by parallel streams and
     *   CompletableFuture.supplyAsync() (when no Executor specified).
     *
     * Interview Q: When to use ForkJoinPool vs ExecutorService?
     *   ForkJoinPool  : CPU-intensive, divide-and-conquer tasks (merge sort,
     *                   matrix multiply, tree traversal).
     *   ExecutorService: I/O-bound tasks, independent tasks without sub-tasks.
     */
    static class ParallelSumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 500; // base case threshold
        private final long[] array;
        private final int start;
        private final int end;

        ParallelSumTask(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end   = end;
        }

        @Override
        protected Long compute() {
            int size = end - start;
            if (size <= THRESHOLD) {
                // Base case: compute directly (sequential)
                long sum = 0;
                for (int i = start; i < end; i++) sum += array[i];
                return sum;
            } else {
                // Divide: split into two halves
                int mid = start + size / 2;
                ParallelSumTask leftTask  = new ParallelSumTask(array, start, mid);
                ParallelSumTask rightTask = new ParallelSumTask(array, mid, end);

                leftTask.fork();               // submit left task asynchronously
                long rightResult = rightTask.compute(); // compute right on current thread
                long leftResult  = leftTask.join();     // wait for left and get result

                return leftResult + rightResult; // combine
            }
        }
    }

    // RecursiveAction — no return value
    static class ParallelArrayFill extends RecursiveAction {
        private static final int THRESHOLD = 1000;
        private final int[] array;
        private final int start, end, value;

        ParallelArrayFill(int[] array, int start, int end, int value) {
            this.array = array; this.start = start; this.end = end; this.value = value;
        }

        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                Arrays.fill(array, start, end, value); // base case
            } else {
                int mid = (start + end) / 2;
                ParallelArrayFill left  = new ParallelArrayFill(array, start, mid, value);
                ParallelArrayFill right = new ParallelArrayFill(array, mid, end, value);
                invokeAll(left, right); // fork both and wait
            }
        }
    }

    private static void demo15_ForkJoinPool() throws Exception {
        System.out.println("\n--- 15. FORK JOIN POOL ---");

        // Create test array: 1 to 10000
        long[] array = new long[10000];
        for (int i = 0; i < array.length; i++) array[i] = i + 1;
        long expected = (long) array.length * (array.length + 1) / 2; // n*(n+1)/2

        // Use common pool
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        System.out.println("[ForkJoin] Common pool parallelism: " + commonPool.getParallelism());

        ParallelSumTask task = new ParallelSumTask(array, 0, array.length);
        long result = commonPool.invoke(task);
        System.out.println("[ForkJoin] Sum result: " + result);
        System.out.println("[ForkJoin] Expected:   " + expected);
        System.out.println("[ForkJoin] Correct: " + (result == expected));

        // Custom ForkJoinPool with specific parallelism
        ForkJoinPool customPool = new ForkJoinPool(4);
        long result2 = customPool.invoke(new ParallelSumTask(array, 0, array.length));
        System.out.println("[ForkJoin] Custom pool (4 threads) result: " + result2);
        customPool.shutdown();

        // RecursiveAction demo
        int[] fillArray = new int[5000];
        ForkJoinPool.commonPool().invoke(new ParallelArrayFill(fillArray, 0, fillArray.length, 7));
        System.out.println("[ForkJoin] RecursiveAction fill[0]=" + fillArray[0]
                + " fill[4999]=" + fillArray[4999]); // all 7

        // ForkJoinPool with Callable (submit returns ForkJoinTask<T>)
        ForkJoinTask<String> ftask = ForkJoinPool.commonPool().submit(() -> "ForkJoin Callable result");
        System.out.println("[ForkJoin] " + ftask.get());
    }

    // ================================================================
    // 16. JAVA MEMORY MODEL (JMM)
    // ================================================================

    /**
     * Java Memory Model (JSR-133) defines:
     *   - How threads interact through shared memory.
     *   - What values a read is allowed to see.
     *   - When writes by one thread become visible to other threads.
     *
     * Model:
     *   Main Memory  — shared among all threads (heap, static fields).
     *   Working Memory — each thread has a local cache (CPU cache/registers).
     *   Threads work on copies; JMM controls when copies are synced with main memory.
     *
     * Three Problems JMM Addresses:
     *   1. Visibility   : changes made by T1 may not be seen by T2 (CPU cache).
     *   2. Atomicity    : compound operations (i++) may interleave.
     *   3. Ordering     : compiler/CPU reorders instructions for performance
     *                     (reordering is invisible within a single thread but
     *                      can cause problems across threads).
     *
     * Happens-Before (HB) Guarantee:
     *   If action A happens-before action B, then A's effects are visible to B.
     *
     *   Key HB rules:
     *   1. Program Order      : each statement HB the next in the same thread.
     *   2. Monitor Lock       : unlock() HB subsequent lock() of same monitor.
     *   3. Volatile Write     : volatile write HB subsequent volatile read.
     *   4. Thread Start       : Thread.start() HB any action in the started thread.
     *   5. Thread Join        : all actions in thread HB Thread.join() returning.
     *   6. Thread Interrupt   : interrupt() HB detection of the interrupt.
     *   7. Object Construction: constructor end HB finalizer start.
     *   8. Transitivity       : if A HB B and B HB C, then A HB C.
     *
     * Memory Barriers (fences):
     *   Volatile reads/writes and synchronized blocks insert memory barriers
     *   that prevent instruction reordering across the barrier.
     *
     * Interview Q: What is instruction reordering?
     *   JIT compiler and CPU reorder instructions for performance.
     *   Safe within single thread (as-if-serial semantics).
     *   Dangerous across threads without proper synchronization.
     *
     * Interview Q: What is a memory barrier?
     *   A CPU instruction that prevents reordering across it.
     *   Load barrier: ensures all reads before barrier complete first.
     *   Store barrier: ensures all writes before barrier are visible.
     */
    static class JMMDemo {
        // Without synchronization:
        int x = 0;
        boolean flag = false;

        // Thread 1 writes:
        void writer() {
            x = 42;     // might be reordered!
            flag = true; // might be reordered with x = 42!
        }

        // Thread 2 reads: might see flag=true but x=0 due to reordering!
        void reader() {
            if (flag) {
                System.out.println("[JMM] x = " + x); // might print 0 without HB!
            }
        }

        // Fixed with volatile — establishes HB:
        volatile boolean vFlag = false;
        void writerFixed() {
            x = 42;        // HB: this is guaranteed to be visible before vFlag write
            vFlag = true;  // volatile write — flush all pending writes
        }
        void readerFixed() {
            if (vFlag) {   // volatile read — re-read from main memory
                System.out.println("[JMM] x (fixed) = " + x); // guaranteed to see 42
            }
        }
    }

    private static void demo16_JavaMemoryModel() throws Exception {
        System.out.println("\n--- 16. JAVA MEMORY MODEL ---");

        JMMDemo jmm = new JMMDemo();

        // Demonstrate volatile visibility fix
        Thread writer = new Thread(() -> {
            jmm.x = 99;
            jmm.vFlag = true; // volatile write establishes HB
        });
        Thread reader = new Thread(() -> {
            while (!jmm.vFlag) { /* spin */ } // volatile read
            System.out.println("[JMM] After volatile HB, x=" + jmm.x); // guaranteed to be 99
        });

        writer.start();
        reader.start();
        writer.join();
        reader.join();

        // Demonstrate synchronized visibility fix
        Object monitor = new Object();
        int[] shared = {0};

        Thread t1 = new Thread(() -> {
            synchronized (monitor) {
                shared[0] = 42;
            } // unlock establishes HB
        });
        Thread t2 = new Thread(() -> {
            synchronized (monitor) { // lock after t1's unlock sees all writes
                System.out.println("[JMM] Synchronized HB, shared=" + shared[0]); // 42
            }
        });
        t1.start(); t1.join();
        t2.start(); t2.join();

        System.out.println("[JMM] Key HB rules: program order, unlock->lock, volatile write->read, start, join");
    }

    // ================================================================
    // 17. WAIT / NOTIFY — Object Monitor Pattern
    // ================================================================

    /**
     * wait() / notify() / notifyAll() are methods on Object (every object has them).
     * They MUST be called from within a synchronized block/method on that object.
     * Violation throws IllegalMonitorStateException.
     *
     * wait():
     *   1. Releases the monitor lock.
     *   2. Thread enters WAITING state.
     *   3. Woken by notify()/notifyAll() or interrupt().
     *   4. Re-acquires monitor before continuing.
     *
     * notify():   Wakes ONE arbitrary waiting thread (JVM chooses).
     * notifyAll(): Wakes ALL waiting threads (they then compete for the lock).
     *
     * Interview Q: Why prefer notifyAll() over notify()?
     *   notify() can cause "missed notification" — wakes wrong thread while
     *   correct thread keeps waiting. notifyAll() is safer.
     *
     * Interview Q: Why put wait() in a while loop, not if?
     *   Spurious wakeups: threads can wake without notify/notifyAll (JVM/OS artifact).
     *   Always re-check the condition after waking up:
     *   while (!condition) { wait(); }
     *
     * sleep() vs wait():
     *   sleep(): pauses thread, does NOT release lock, does not need sync block.
     *   wait():  releases lock, MUST be in synchronized block, needs notify to wake.
     */
    static class WaitNotifyDemo {
        private boolean dataReady = false;
        private int data;

        public synchronized void produce(int value) throws InterruptedException {
            while (dataReady) {
                wait(); // consumer hasn't consumed yet — wait
            }
            data = value;
            dataReady = true;
            System.out.println("[wait/notify] Produced: " + data);
            notifyAll(); // wake all waiting consumers
        }

        public synchronized int consume() throws InterruptedException {
            while (!dataReady) { // use while for spurious wakeup safety
                wait(); // no data yet — release lock and wait
            }
            dataReady = false;
            System.out.println("[wait/notify] Consumed: " + data);
            notifyAll(); // wake producer
            return data;
        }
    }

    private static void demo17_WaitNotify() throws Exception {
        System.out.println("\n--- 17. WAIT / NOTIFY ---");

        WaitNotifyDemo wnd = new WaitNotifyDemo();

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    wnd.produce(i * 10);
                    Thread.sleep(20);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "ProducerWN");

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    wnd.consume();
                    Thread.sleep(40);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "ConsumerWN");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }

    // ================================================================
    // 18. CONCURRENT COLLECTIONS
    // ================================================================

    /**
     * Thread-safe collections in java.util.concurrent:
     *
     * ConcurrentHashMap:
     *   - Segment-level locking in Java 7; CAS + bin-level locking in Java 8.
     *   - Higher concurrency than Hashtable (full lock) or
     *     Collections.synchronizedMap (full lock).
     *   - null keys and values NOT allowed.
     *
     * CopyOnWriteArrayList:
     *   - On every write (add/remove), copies the ENTIRE underlying array.
     *   - Reads are completely lock-free — great for read-heavy, rarely-modified lists.
     *   - Iteration never throws ConcurrentModificationException.
     *   - Expensive for frequent writes — O(n) per write.
     *
     * CopyOnWriteArraySet: backed by CopyOnWriteArrayList.
     *
     * Collections.synchronizedXxx:
     *   - Wraps any collection with a mutex (the collection itself is the lock).
     *   - Must manually synchronize when iterating!
     *
     * Interview Q: HashMap vs Hashtable vs ConcurrentHashMap?
     *   HashMap              : not thread-safe, allows null key/value.
     *   Hashtable            : thread-safe (synchronized methods), no null, legacy.
     *   ConcurrentHashMap    : thread-safe, high concurrency, no null key/value.
     *   synchronizedMap      : thread-safe, full lock (like Hashtable), allows null.
     */
    private static void demo18_ConcurrentCollections() throws Exception {
        System.out.println("\n--- 18. CONCURRENT COLLECTIONS ---");

        // --- ConcurrentHashMap ---
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        ExecutorService exec = Executors.newFixedThreadPool(4);

        // Multiple threads writing concurrently — no explicit synchronization needed
        for (int i = 0; i < 100; i++) {
            final int idx = i;
            exec.submit(() -> map.put("key" + idx, idx));
        }
        exec.shutdown();
        exec.awaitTermination(3, TimeUnit.SECONDS);
        System.out.println("[ConcurrentMap] Size: " + map.size()); // 100

        // Atomic operations on ConcurrentHashMap
        map.putIfAbsent("special", 999);    // only puts if key absent
        map.computeIfAbsent("computed", k -> k.length()); // compute if absent
        map.merge("special", 1, Integer::sum);             // merge with existing value
        System.out.println("[ConcurrentMap] special: " + map.get("special")); // 1000
        System.out.println("[ConcurrentMap] computed: " + map.get("computed")); // 8

        // Java 8 bulk operations on ConcurrentHashMap
        // forEach, reduce, search — all support parallelism threshold
        map.forEach(2, (k, v) -> { /* process each entry with parallelism */ });
        long count = map.mappingCount(); // preferred over size() for large maps
        System.out.println("[ConcurrentMap] mappingCount: " + count);

        // --- CopyOnWriteArrayList ---
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("A"); cowList.add("B"); cowList.add("C");

        // Safe to iterate while another thread modifies — snapshot semantics
        Thread modifier = new Thread(() -> {
            try {
                Thread.sleep(20);
                cowList.add("D"); // this copies the array, won't affect ongoing iteration
                cowList.remove("A");
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        modifier.start();

        System.out.print("[COWAL] Iteration (sees snapshot): ");
        for (String s : cowList) { // iterates over snapshot — no ConcurrentModificationException
            System.out.print(s + " ");
            Thread.sleep(10);
        }
        modifier.join();
        System.out.println();
        System.out.println("[COWAL] After modifications: " + cowList);

        // --- ConcurrentLinkedQueue ---
        ConcurrentLinkedQueue<Integer> clq = new ConcurrentLinkedQueue<>();
        clq.offer(1); clq.offer(2); clq.offer(3);
        System.out.println("[CLQ] poll: " + clq.poll() + ", peek: " + clq.peek());

        // --- Collections.synchronizedList ---
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        syncList.add("x"); syncList.add("y");
        // MUST synchronize manually when iterating!
        synchronized (syncList) {
            for (String s : syncList) System.out.print("[SyncList] " + s + " ");
        }
        System.out.println();
    }

    // ================================================================
    // 19. COMMON INTERVIEW Q&A
    // ================================================================

    /**
     * ============================================================
     * COMPREHENSIVE INTERVIEW Q&A
     * ============================================================
     *
     * Q: What is thread starvation?
     * A: A thread is perpetually denied CPU time or resources because higher-priority
     *    threads or unfair scheduling always preempts it. Fix: use fair locks
     *    (ReentrantLock(true)), avoid strict priority reliance.
     *
     * Q: What is livelock?
     * A: Two or more threads are not blocked but keep changing state in response
     *    to each other, making no progress (like two people in a corridor both
     *    stepping aside in the same direction). Unlike deadlock, they ARE running.
     *    Fix: add randomness/backoff to retry logic.
     *
     * Q: What is thread contention?
     * A: Multiple threads competing for the same shared resource (lock, cache line).
     *    High contention degrades performance. Reduce by: finer-grained locking,
     *    lock striping, CAS operations, thread-local storage, immutable data.
     *
     * Q: Are immutable objects thread-safe?
     * A: Yes. Immutable objects cannot be modified after construction, so no
     *    synchronization is needed. (String, Integer, all Java primitive wrappers
     *    are immutable.) Make fields final and don't expose mutable state.
     *
     * Q: Are static methods thread-safe?
     * A: No, not by default. Static methods that access shared static state
     *    (static variables) can have race conditions. The method being static
     *    has no bearing on thread safety — it's about shared mutable state.
     *
     * Q: What is a monitor in Java?
     * A: Every Java object has an associated monitor (also called intrinsic lock
     *    or mutex). Only one thread at a time can own an object's monitor.
     *    synchronized keyword acquires/releases this monitor automatically.
     *
     * Q: sleep() vs wait()?
     * A: sleep(ms): defined in Thread; pauses thread for given time; does NOT
     *              release monitor lock; can be called from anywhere.
     *    wait():    defined in Object; releases lock; must be in synchronized block;
     *              wakes on notify()/notifyAll()/interrupt().
     *
     * Q: notify() vs notifyAll()?
     * A: notify()    : wakes ONE arbitrary waiting thread (JVM chooses — unpredictable).
     *    notifyAll() : wakes ALL waiting threads; they compete for the lock.
     *    Prefer notifyAll() to avoid missed signals and starvation.
     *
     * Q: How many threads should I create?
     * A: CPU-bound tasks  : N+1 threads (N = number of CPU cores).
     *                       Extra thread handles occasional interruptions.
     * A: I/O-bound tasks  : N * (1 + W/C) threads where W=wait time, C=compute time.
     *                       More threads are fine since they spend most time waiting.
     *    Formula: optimalThreads = numCores * (1 + waitTime/computeTime)
     *
     * Q: What is thread pool sizing in practice?
     * A: For I/O-bound: often 2x-10x CPU cores.
     *    For CPU-bound: approximately CPU cores (e.g., Runtime.getRuntime().availableProcessors())
     *    Always benchmark and tune for your specific workload.
     *
     * Q: What is context switching?
     * A: The OS saving and restoring thread state when switching from one thread
     *    to another. Has CPU overhead — too many threads causes excessive context
     *    switching and degrades performance.
     *
     * Q: What is false sharing?
     * A: Multiple threads modify different variables that happen to share the same
     *    CPU cache line. The cache line is invalidated on every write, causing
     *    performance degradation even though threads don't share variables.
     *    Fix: pad data to separate cache lines (@Contended in Java 8 with JVM flag).
     *
     * Q: Difference between synchronized and Lock?
     * A: synchronized: built-in, auto-releases on exception, simpler, no timeout,
     *                  not interruptible while waiting, unfair by default.
     * A: Lock (ReentrantLock): explicit unlock (must use finally), tryLock timeout,
     *                  interruptible, can be fair, multiple conditions.
     *
     * Q: What are the states of a thread?
     * A: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.
     *    (Thread.getState() returns Thread.State enum)
     *
     * Q: How do you stop a thread safely?
     * A: 1. Use a volatile boolean flag: while(!stopped) { doWork(); }
     *    2. Use Thread.interrupt() + check isInterrupted() or catch InterruptedException.
     *    NEVER use Thread.stop() — it's deprecated, unsafe (leaves objects in bad state).
     *
     * Q: What is the difference between Runnable and Thread?
     * A: Thread is a class representing a thread of execution.
     *    Runnable is an interface for the task (what to run).
     *    Using Runnable is preferred because it separates task from execution.
     *
     * Q: What is an executor service?
     * A: A higher-level thread management abstraction. It manages a pool of
     *    worker threads, queues submitted tasks, and provides lifecycle management
     *    (shutdown, awaitTermination). Avoids manual thread creation.
     *
     * Q: What is a Future?
     * A: Represents the result of an asynchronous computation. Methods:
     *    get() — blocks until result ready.
     *    get(timeout, unit) — blocks with timeout.
     *    isDone() — checks if computation finished.
     *    cancel(mayInterruptIfRunning) — cancels if possible.
     *    isCancelled() — checks if was cancelled.
     *
     * Q: ThreadLocal memory leak — how and why?
     * A: ThreadLocal values are stored in a Map keyed by ThreadLocal reference in
     *    the Thread object itself. In a thread pool, threads live forever.
     *    If you set a ThreadLocal value but never call remove(), the key becomes
     *    weakly referenced but the VALUE is strongly referenced — it never GCs.
     *    FIX: always call threadLocal.remove() when done, ideally in a finally block.
     *
     * Q: What is CAS (Compare and Swap)?
     * A: An atomic CPU instruction: "if value at address == expected, then
     *    update to new value, else do nothing." Returns success/failure.
     *    Used internally by AtomicInteger, ConcurrentHashMap, etc.
     *    Lock-free — no OS mutex, works purely in user space via hardware atomics.
     *
     * Q: What is the Java happens-before guarantee?
     * A: A formal memory model guarantee that if action A happens-before B,
     *    all effects of A (writes to memory) are visible to B. Without HB
     *    guarantees, compiler/CPU reordering can cause threads to see stale data.
     *
     * Q: Explain ForkJoinPool work stealing.
     * A: Each worker thread has a deque (double-ended queue) of tasks.
     *    Normally a thread takes tasks from its own queue front (LIFO locally).
     *    When idle, it steals tasks from the BACK (tail) of other busy threads'
     *    queues (FIFO for stealing). This reduces contention at the head.
     *
     * Q: ConcurrentHashMap vs synchronizedMap?
     * A: ConcurrentHashMap: fine-grained locking (per bin/segment), allows
     *    concurrent reads, atomic putIfAbsent/computeIfAbsent, no null keys.
     *    synchronizedMap: coarse-grained lock (whole map), must manually sync
     *    when iterating, allows null key.
     *
     * Q: What is the producer-consumer problem?
     * A: Classic concurrency problem: producer creates items, consumer uses them,
     *    coordinated via a shared buffer. Challenges: buffer full (producer must wait),
     *    buffer empty (consumer must wait). Best solved with BlockingQueue in Java.
     *
     * Q: What is a semaphore?
     * A: A synchronization primitive with a counter of "permits."
     *    acquire() decrements (blocks if 0), release() increments.
     *    Binary semaphore (1 permit) ≈ mutex. Semaphore(N) limits concurrency to N.
     *
     * Q: Explain CountDownLatch vs CyclicBarrier.
     * A: CountDownLatch: one-shot, one-directional (workers count down, waiter awaits);
     *                    cannot be reused; count starts at N and goes to 0.
     *    CyclicBarrier : bidirectional (all threads wait for each other); reusable;
     *                    optional action when all arrive.
     *
     * Q: What is StampedLock (Java 8)?
     * A: An alternative to ReadWriteLock with three modes:
     *    Write lock (exclusive), Read lock (shared), Optimistic read (no lock).
     *    Optimistic read: read data, validate stamp (no write occurred), retry if stale.
     *    Faster than ReadWriteLock for read-dominated workloads.
     *
     * Q: What is a thread dump?
     * A: Snapshot of all threads' states and stack traces.
     *    Useful for diagnosing deadlocks, hangs, high CPU usage.
     *    Generate with: jstack <pid>, kill -3 <pid>, VisualVM, JConsole.
     *
     * Q: What is the difference between process synchronization and thread synchronization?
     * A: Thread synchronization coordinates threads within the SAME JVM process
     *    using shared memory (synchronized, volatile, concurrent classes).
     *    Process synchronization coordinates separate processes using IPC mechanisms
     *    (pipes, sockets, shared memory segments, OS-level semaphores).
     */

    // ================================================================
    // 20. ADVANCED PATTERNS
    // ================================================================

    /**
     * Thread-Safe Singleton (Double-Checked Locking)
     * Already shown above with volatile field.
     * Alternative: Enum Singleton (most thread-safe, handles serialization too)
     */
    enum EnumSingleton {
        INSTANCE;
        // thread-safe, serialization-safe, reflection-safe
        public void doWork() { System.out.println("[EnumSingleton] working"); }
    }

    /**
     * Initialization-on-Demand Holder (Bill Pugh Singleton)
     * Leverages class loading guarantee — inner class loaded lazily on first use.
     * Thread-safe without explicit synchronization.
     */
    static class LazyHolder {
        private LazyHolder() {}
        private static class Holder {
            static final LazyHolder INSTANCE = new LazyHolder();
        }
        public static LazyHolder getInstance() { return Holder.INSTANCE; }
    }

    /**
     * Thread-Safe Observer / Pub-Sub using CopyOnWriteArrayList
     */
    interface EventListener { void onEvent(String event); }
    static class EventBus {
        private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
        public void subscribe(EventListener l)   { listeners.add(l); }
        public void unsubscribe(EventListener l) { listeners.remove(l); }
        public void publish(String event) {
            // Iteration is safe even if listeners are added/removed concurrently
            listeners.forEach(l -> l.onEvent(event));
        }
    }

    private static void demo20_AdvancedPatterns() throws Exception {
        System.out.println("\n--- 20. ADVANCED PATTERNS ---");

        // Enum singleton
        EnumSingleton.INSTANCE.doWork();

        // Holder singleton
        LazyHolder lh = LazyHolder.getInstance();
        System.out.println("[Holder] instance: " + lh);

        // Thread-safe event bus
        EventBus bus = new EventBus();
        bus.subscribe(event -> System.out.println("[EventBus] Listener1: " + event));
        bus.subscribe(event -> System.out.println("[EventBus] Listener2: " + event));
        bus.publish("USER_LOGGED_IN");

        // CompletableFuture timeout (Java 9+)
        // cf.orTimeout(5, TimeUnit.SECONDS)       // complete exceptionally after timeout
        // cf.completeOnTimeout("default", 5, ...)  // complete with value after timeout

        // Parallel stream uses ForkJoinPool.commonPool() internally
        long sum = java.util.stream.LongStream.rangeClosed(1, 1000)
            .parallel()
            .sum();
        System.out.println("[ParallelStream] sum 1..1000 = " + sum); // 500500

        // Virtual Threads (Java 21 Preview - conceptual note only)
        // Thread.ofVirtual().start(() -> System.out.println("Virtual thread!"));
        // Virtual threads are lightweight (not OS threads), millions can exist,
        // great for I/O-bound workloads. Part of Project Loom.

        // ReadWriteLock with StampedLock (Java 8 optimistic read)
        StampedLock stampedLock = new StampedLock();
        long[] data = {42L};

        // Optimistic read (no lock acquired)
        long stamp = stampedLock.tryOptimisticRead();
        long value = data[0]; // read
        if (!stampedLock.validate(stamp)) { // check if a write occurred during read
            // fallback to read lock
            stamp = stampedLock.readLock();
            try { value = data[0]; }
            finally { stampedLock.unlockRead(stamp); }
        }
        System.out.println("[StampedLock] Optimistic read value: " + value);

        // Write with StampedLock
        long writeStamp = stampedLock.writeLock();
        try { data[0] = 100L; }
        finally { stampedLock.unlockWrite(writeStamp); }
        System.out.println("[StampedLock] After write: " + data[0]);
    }

    // ================================================================
    // MAIN METHOD — runs all demos sequentially
    // ================================================================

    public static void main(String[] args) throws Exception {
        System.out.println("=======================================================");
        System.out.println("   MULTITHREADING & CONCURRENCY — Interview Master      ");
        System.out.println("=======================================================");
        System.out.println("Available CPUs: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Java version:   " + System.getProperty("java.version"));
        System.out.println();

        demo1_ThreadCreation();
        demo2_ThreadLifecycle();
        demo3_ThreadMethods();
        demo4_Synchronized();
        demo5_Volatile();
        demo6_Deadlock();
        demo7_RaceCondition();
        demo8_ExecutorFramework();
        demo9_ReentrantLock();
        demo10_AtomicClasses();
        demo11_SyncUtilities();
        demo12_BlockingQueue();
        demo13_ThreadLocal();
        demo14_CompletableFuture();
        demo15_ForkJoinPool();
        demo16_JavaMemoryModel();
        demo17_WaitNotify();
        demo18_ConcurrentCollections();
        // Section 19 is all Q&A in Javadoc comments (no runtime code needed)
        demo20_AdvancedPatterns();

        System.out.println("\n=======================================================");
        System.out.println("   ALL DEMOS COMPLETE                                   ");
        System.out.println("=======================================================");
    }
}
