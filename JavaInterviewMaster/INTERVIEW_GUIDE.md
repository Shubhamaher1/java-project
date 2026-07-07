# Java Interview Master — Complete Study Guide

> **Goal:** 100% interview ready — Java Core + Spring Boot + Microservices + Kafka + Testing

---

## Project Structure

```
JavaInterviewMaster/
├── src/main/java/com/interview/master/
│   ├── JavaInterviewMasterApplication.java     ← Spring Boot entry point
│   │
│   ├── javacore/
│   │   ├── oop/           OOPConcepts.java      ← 4 pillars, inner classes, enums
│   │   ├── collections/   CollectionsConcepts.java ← HashMap internals, all collections
│   │   ├── java8/         Java8Features.java    ← Lambda, Stream, Optional, CompletableFuture
│   │   ├── multithreading/MultithreadingConcepts.java ← Threads, Locks, Concurrency utils
│   │   ├── exceptions/    ExceptionHandlingConcepts.java ← Checked/unchecked, custom, best practices
│   │   ├── designpatterns/DesignPatterns.java   ← All 16 GoF patterns with Spring mapping
│   │   └── advanced/      AdvancedJavaConcepts.java ← Generics, Records, Sealed, JMM, GC
│   │
│   └── springboot/
│       ├── entity/        Employee, Department, Skill ← JPA, relationships, lifecycle
│       ├── dto/           EmployeeDTO              ← DTOs, ApiResponse, PagedResponse
│       ├── repository/    EmployeeRepository       ← Spring Data JPA, @Query, projections
│       ├── service/       EmployeeService/Impl     ← @Transactional, @Cacheable, @Async
│       ├── controller/    EmployeeController       ← REST APIs, HTTP methods, ResponseEntity
│       ├── config/        SecurityConfig, AppConfig ← Spring Security, Beans, Profiles
│       ├── aop/           LoggingAspect            ← @Aspect, @Before, @Around, @After
│       ├── exception/     GlobalExceptionHandler   ← @ControllerAdvice, error handling
│       ├── events/        ApplicationEventsDemo    ← Spring Events, @EventListener
│       ├── scheduling/    SchedulingDemo           ← @Scheduled, Cron, Dynamic scheduling
│       ├── microservices/ MicroservicesConcepts    ← Patterns, Feign, Circuit Breaker, Service Mesh
│       └── kafka/         KafkaConcepts            ← Producer, Consumer, DLT, Transactions
│
├── src/test/java/com/interview/master/
│   └── EmployeeServiceTest.java  ← JUnit 5, Mockito, @WebMvcTest, @DataJpaTest
│
└── src/main/resources/
    ├── application.properties    ← All Spring Boot configs explained
    └── data.sql                  ← Seed data for H2
```

---

## Topic → File Quick Reference

| Topic | File | Key Concepts |
|-------|------|-------------|
| OOP (4 pillars) | `oop/OOPConcepts.java` | Encapsulation, Abstraction, Inheritance, Polymorphism |
| Collections | `collections/CollectionsConcepts.java` | HashMap internals, Big-O, fail-fast vs fail-safe |
| Java 8+ | `java8/Java8Features.java` | Lambda, Stream API, Optional, CompletableFuture |
| Multithreading | `multithreading/MultithreadingConcepts.java` | Thread lifecycle, synchronized, volatile, Executor |
| Exceptions | `exceptions/ExceptionHandlingConcepts.java` | Checked vs unchecked, try-with-resources |
| Design Patterns | `designpatterns/DesignPatterns.java` | 16 GoF patterns + Spring usage |
| Advanced Java | `advanced/AdvancedJavaConcepts.java` | Generics, Records, Sealed, GC, Reflection |
| JPA/Hibernate | `entity/Employee.java` | @Entity, relationships, FetchType, CascadeType |
| Spring Data JPA | `repository/EmployeeRepository.java` | Derived queries, @Query, @Modifying, Pagination |
| Transactions | `service/EmployeeServiceImpl.java` | Propagation, Isolation, @Cacheable |
| REST API | `controller/EmployeeController.java` | HTTP methods, status codes, @Valid |
| Spring Security | `config/SecurityConfig.java` | Authentication vs Authorization, JWT, CSRF |
| AOP | `aop/LoggingAspect.java` | Pointcut, Advice types, @Around |
| Exception Handling | `exception/GlobalExceptionHandler.java` | @ControllerAdvice, @ExceptionHandler |
| Microservices | `microservices/MicroservicesConcepts.java` | Patterns, Feign, Resilience4j |
| Kafka | `kafka/KafkaConcepts.java` | Producer, Consumer, Partitions, DLT |
| JUnit/Mockito | `test/EmployeeServiceTest.java` | @Mock, @InjectMocks, @WebMvcTest, @DataJpaTest |

---

## Top 50 Java Interview Questions — Quick Answers

### Core Java

**Q1: What is the difference between JDK, JRE, and JVM?**
- JVM: Runs bytecode. Platform-dependent runtime.
- JRE: JVM + libraries. Needed to run Java apps.
- JDK: JRE + compiler (javac) + tools. Needed to develop Java apps.

**Q2: What is the difference between `==` and `.equals()`?**
- `==`: compares references (memory address) for objects; compares values for primitives.
- `.equals()`: compares content/value. Always override with `hashCode()`.
- String pool: `"hello" == "hello"` is `true` (same pool object); `new String("hello") == new String("hello")` is `false`.

**Q3: What is String immutability? Why is String immutable?**
- String is `final`, char array is `private final`, no setter methods.
- Why: Security (DB URLs, passwords), Thread-safety, String pool (caching), HashCode caching.

**Q4: Difference between `StringBuilder` and `StringBuffer`?**
- `StringBuffer`: thread-safe (synchronized methods). Slower.
- `StringBuilder`: not thread-safe. Faster. Use in single-threaded contexts.

**Q5: What is autoboxing/unboxing?**
- Autoboxing: `int` → `Integer` (automatic wrapping by compiler).
- Unboxing: `Integer` → `int`. NullPointerException risk if Integer is null.

**Q6: What is the difference between `final`, `finally`, `finalize`?**
- `final`: keyword for constant variable, non-overridable method, non-extendable class.
- `finally`: block always executed after try-catch (even on exception).
- `finalize()`: deprecated method called by GC before reclaiming object (don't rely on it).

**Q7: What is static keyword?**
- `static` variable: shared across all instances (class-level, one copy).
- `static` method: called on class, not instance. Can't use `this` or `super`.
- `static` block: runs once when class is loaded (before constructor).
- `static` inner class: doesn't need outer class instance.

**Q8: What is the difference between `ArrayList` and `LinkedList`?**
- `ArrayList`: backed by `Object[]`. O(1) get, O(n) insert/delete in middle. Better for read-heavy.
- `LinkedList`: doubly-linked nodes. O(n) get, O(1) insert/delete at known position. Better for frequent insert/delete.

**Q9: How does `HashMap` work internally?**
1. `key.hashCode()` → spread with `hash()` → bucket index = `hash & (n-1)`.
2. If bucket empty → store `Entry(key, value)`.
3. If collision → chain (LinkedList). After 8 entries → convert to TreeMap (O(log n)).
4. Load factor 0.75 → when 75% full → resize (double capacity, rehash all entries).
- `null` key allowed (goes to bucket 0). Not thread-safe. Use `ConcurrentHashMap` for thread-safety.

**Q10: What is the difference between `HashMap` and `ConcurrentHashMap`?**
- `HashMap`: not thread-safe. Null key/value allowed.
- `ConcurrentHashMap`: thread-safe using CAS + segment locking. No null key/value. Better than `Hashtable` (which locks entire map).

---

### OOP

**Q11: Four pillars of OOP?**
- **Encapsulation**: hide state, expose via methods (`private` + getters/setters).
- **Abstraction**: hide implementation details, show only interface (`abstract`, `interface`).
- **Inheritance**: child acquires parent properties (`extends`). Single inheritance for classes.
- **Polymorphism**: one interface, multiple implementations. Compile-time (overloading) + runtime (overriding).

**Q12: Abstract class vs Interface?**
| | Abstract Class | Interface |
|---|---|---|
| Methods | abstract + concrete | abstract (default/static in Java 8+) |
| Variables | any | `public static final` only |
| Constructor | yes | no |
| Inheritance | single (`extends`) | multiple (`implements`) |
| Use when | IS-A with shared code | CAN-DO capability, multiple inheritance |

**Q13: What is method overloading vs overriding?**
- **Overloading**: same method name, different parameters. Resolved at **compile-time** (static polymorphism).
- **Overriding**: subclass provides different implementation. Resolved at **runtime** (dynamic polymorphism). Must have same signature. Use `@Override`.

---

### Multithreading

**Q14: How to create a thread?**
1. `extends Thread` + override `run()`.
2. `implements Runnable` + pass to `new Thread(runnable).start()`.
3. `implements Callable<V>` + wrap in `FutureTask` + pass to `Thread`.
4. Lambda: `new Thread(() -> { }).start()`.
- Prefer `Runnable`/`Callable` over extending `Thread` (Java has single inheritance).

**Q15: `synchronized` vs `volatile`?**
- `volatile`: guarantees visibility only. Changes to volatile variable are visible to all threads immediately. Does NOT guarantee atomicity (`volatile int i; i++` is NOT atomic).
- `synchronized`: guarantees visibility + atomicity + mutual exclusion. One thread at a time.

**Q16: What is deadlock? How to prevent it?**
- Deadlock: circular wait — Thread A holds Lock1, waits for Lock2; Thread B holds Lock2, waits for Lock1.
- Prevention: always acquire locks in same order; use `tryLock()` with timeout; avoid nested locks.

**Q17: `wait()` vs `sleep()`?**
- `wait()`: releases the lock, must be in `synchronized` block, woken by `notify()`/`notifyAll()`.
- `sleep()`: does NOT release the lock, pauses thread for specified time, no synchronization needed.

**Q18: What is `ThreadLocal`?**
- Provides thread-local variables — each thread has its own isolated copy.
- Use case: storing user session, transaction context, SimpleDateFormat per thread.
- **Warning**: always call `remove()` in thread pool to prevent memory leaks.

---

### Java 8+

**Q19: What is a functional interface?**
- An interface with exactly ONE abstract method. Can have default/static methods.
- Annotated with `@FunctionalInterface` (optional but recommended).
- Built-in: `Predicate<T>`, `Function<T,R>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>`.

**Q20: What is a lambda expression?**
- Anonymous function: `(params) -> body`.
- Can only be used where a functional interface is expected.
- Captures effectively-final local variables.

**Q21: Difference between `map()` and `flatMap()` in Streams?**
- `map()`: one-to-one transformation. `Stream<List<String>>` → `Stream<List<String>>`.
- `flatMap()`: one-to-many, flattens result. `Stream<List<String>>` → `Stream<String>`.

**Q22: `Optional` — what and why?**
- Container for a value that may or may not be present. Avoids `NullPointerException`.
- `Optional.of(val)`: throws NPE if null. `Optional.ofNullable(val)`: allows null.
- `orElse(default)` vs `orElseGet(supplier)`: `orElseGet` is lazy (supplier not called if value present).

---

### Spring Boot

**Q23: What is Spring Boot?**
- Spring Boot = Spring Framework + Auto-configuration + Embedded server (Tomcat) + Production-ready features (Actuator).
- Removes boilerplate XML configuration. Convention over configuration.

**Q24: What is Dependency Injection (DI)?**
- Objects don't create their dependencies — they're provided (injected) by the container.
- Types: Constructor injection (recommended), Setter injection, Field injection.
- Benefits: loose coupling, testability, easier to swap implementations.

**Q25: What is Spring IoC Container?**
- Inversion of Control: framework controls object creation and lifecycle, not the programmer.
- Container types: `BeanFactory` (lazy, lightweight) → `ApplicationContext` (eager, full-featured).

**Q26: `@Component` vs `@Service` vs `@Repository` vs `@Controller`?**
- All are specializations of `@Component` — all register as Spring beans.
- `@Repository`: adds exception translation (DB exceptions → `DataAccessException`).
- `@Service`: semantic clarity; signals business logic layer.
- `@Controller`: Spring MVC processes for HTTP mapping.

**Q27: What is `@Transactional`?**
- Wraps method in a database transaction. Commits on success, rolls back on `RuntimeException`.
- Default propagation: `REQUIRED` (join existing or create new).
- Default rollback: only `RuntimeException`. Add `rollbackFor=Exception.class` for checked exceptions.
- `readOnly=true`: optimization hint for read-only queries (no dirty checking, flush mode NEVER).

**Q28: What are Transaction Propagation types?**
- `REQUIRED`: join existing tx or create new (default).
- `REQUIRES_NEW`: always create new tx, suspend existing.
- `SUPPORTS`: join if exists, run without tx if none.
- `NOT_SUPPORTED`: always run without tx, suspend existing.
- `MANDATORY`: must have existing tx, else throw exception.
- `NEVER`: must NOT have tx, else throw exception.
- `NESTED`: nested savepoint within existing tx.

**Q29: What is Spring AOP?**
- Aspect-Oriented Programming for cross-cutting concerns (logging, security, transactions).
- Spring AOP is proxy-based (JDK dynamic proxy or CGLIB).
- `@Aspect`, `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around`.
- `@Around` is most powerful — can skip method, modify args/return value.

**Q30: What is `@SpringBootApplication`?**
- Combination of: `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- `@EnableAutoConfiguration`: reads `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and configures beans based on classpath.

---

### JPA / Hibernate

**Q31: What is the N+1 problem?**
- When fetching N parent entities, Hibernate fires 1 query for parents + N queries for each child collection.
- Fix: `@EntityGraph`, `JOIN FETCH` in JPQL, `@BatchSize`, or DTO projections.

**Q32: `FetchType.LAZY` vs `FetchType.EAGER`?**
- `LAZY`: load related entity only when accessed. Default for `@OneToMany`, `@ManyToMany`.
- `EAGER`: load immediately with parent. Default for `@ManyToOne`, `@OneToOne`.
- Always prefer `LAZY` to avoid loading unnecessary data. Use `JOIN FETCH` when needed.

**Q33: What is `@Transient` in JPA?**
- Field not persisted to database. Different from `transient` keyword (serialization).

**Q34: Optimistic vs Pessimistic locking?**
- **Optimistic** (`@Version`): no DB lock, checks version on update — fails if conflict. Good for low-contention.
- **Pessimistic** (`LockModeType.PESSIMISTIC_WRITE`): DB-level lock on row. Good for high-contention, short operations.

---

### Microservices

**Q35: Monolith vs Microservices?**
- Monolith: single deployable unit, simple dev, hard to scale individual parts.
- Microservices: independent services, independent deployment/scaling, complex distributed system.

**Q36: What is Circuit Breaker pattern?**
- Prevents cascading failures. States: CLOSED (normal) → OPEN (rejecting calls, fast-fail) → HALF_OPEN (testing recovery).
- Tool: Resilience4j `@CircuitBreaker`.

**Q37: What is the difference between API Gateway and Load Balancer?**
- Load Balancer: distributes traffic across instances of SAME service.
- API Gateway: routes to DIFFERENT services, handles auth, rate limiting, SSL, logging.

**Q38: What is SAGA pattern?**
- Manages distributed transactions without 2PC.
- Choreography: services react to events (no central coordinator).
- Orchestration: central orchestrator directs steps.

**Q39: Synchronous vs Asynchronous communication in microservices?**
- Sync (REST/gRPC): immediate response needed (payment, auth). Tight coupling.
- Async (Kafka/RabbitMQ): fire-and-forget, loose coupling, higher throughput (notifications, audit logs).

---

### Kafka

**Q40: What is Apache Kafka?**
- Distributed event streaming platform. High-throughput, fault-tolerant, durable message log.
- Topic → Partitions → Offsets. Producer → Broker → Consumer.

**Q41: What is a Consumer Group?**
- Multiple consumers sharing a topic's partitions. Each partition consumed by exactly ONE consumer in a group. Multiple groups can independently consume same topic.

**Q42: Delivery guarantees in Kafka?**
- **At most once**: commit before processing. May lose messages.
- **At least once**: commit after processing. May duplicate (default).
- **Exactly once**: idempotent producer + transactional consumer. Hardest, most overhead.

**Q43: Kafka vs RabbitMQ?**
- Kafka: pull-based, persistent log, replay, very high throughput, event streaming.
- RabbitMQ: push-based, message deleted after consumption, complex routing, lower latency for simple queues.

---

### Testing

**Q44: Unit test vs Integration test?**
- Unit: tests single class in isolation. Fast. Mock dependencies.
- Integration: tests multiple layers together (e.g., controller + service + DB). Slower. Use `@SpringBootTest`.

**Q45: `@Mock` vs `@MockBean`?**
- `@Mock` (Mockito): creates a Mockito mock. No Spring context. Use with `@ExtendWith(MockitoExtension.class)`.
- `@MockBean` (Spring Boot Test): creates Mockito mock AND registers it in Spring ApplicationContext.

**Q46: `@WebMvcTest` vs `@SpringBootTest`?**
- `@WebMvcTest`: loads ONLY web layer (Controller + Security + MockMvc). Fast. Mock service layer with `@MockBean`.
- `@SpringBootTest`: loads FULL context. Slow. Use for true integration tests.

**Q47: What is MockMvc?**
- Test MVC controllers without starting a real HTTP server. Dispatches through `DispatcherServlet`.
- `perform(get("/api/employees")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))`.

---

### Advanced

**Q48: What is Java Memory Model (JMM)?**
- Defines how threads interact through memory. Specifies happens-before relationships.
- Key: writes to shared variables may not be visible to other threads without synchronization.

**Q49: What is Garbage Collection?**
- JVM automatically reclaims memory of unreachable objects.
- Heap: Young Gen (Eden + S0 + S1) → Old Gen. Minor GC (Young) vs Major GC (Old).
- Algorithms: Serial, Parallel, CMS, G1GC (default Java 9+), ZGC, Shenandoah.

**Q50: What is the difference between `Comparable` and `Comparator`?**
- `Comparable`: natural ordering. `compareTo()` in the class itself. One ordering only.
- `Comparator`: external ordering. `compare()` in separate class. Multiple orderings possible.
- `Comparator.comparing(Employee::getSalary).thenComparing(Employee::getName)`.

---

## How to Run This Project

```bash
# Navigate to project
cd "C:/Users/Shubham Aher/OneDrive/Desktop/TestJava/JavaInterviewMaster"

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/master-1.0.0.jar
```

### Endpoints to test
| Endpoint | Description |
|----------|-------------|
| `GET  /api/v1/employees` | List all employees |
| `GET  /api/v1/employees/1` | Get employee by ID |
| `POST /api/v1/employees` | Create employee (JSON body) |
| `PUT  /api/v1/employees/1` | Update employee |
| `DELETE /api/v1/employees/1` | Delete employee |
| `GET  /h2-console` | H2 Database console (username: sa, no password) |
| `GET  /actuator/health` | App health check |
| `GET  /actuator/info` | App info |
| `GET  /actuator/metrics` | App metrics |

---

## Study Order Recommendation

### Week 1 — Java Core
1. `OOPConcepts.java` — Read + run main()
2. `CollectionsConcepts.java` — Focus on HashMap internals
3. `ExceptionHandlingConcepts.java`
4. `MultithreadingConcepts.java` — Most important for senior roles

### Week 2 — Modern Java
5. `Java8Features.java` — Lambda, Streams, Optional (interview favourite)
6. `AdvancedJavaConcepts.java` — Generics, Records, GC
7. `DesignPatterns.java` — Singleton, Factory, Builder, Strategy, Observer

### Week 3 — Spring Boot
8. `entity/Employee.java` + `repository/EmployeeRepository.java` — JPA
9. `service/EmployeeServiceImpl.java` — @Transactional, @Cacheable
10. `controller/EmployeeController.java` — REST API
11. `config/SecurityConfig.java` — Spring Security
12. `aop/LoggingAspect.java` — AOP

### Week 4 — Advanced / Distributed
13. `microservices/MicroservicesConcepts.java` — Patterns, Feign, Circuit Breaker
14. `kafka/KafkaConcepts.java` — Producer, Consumer, Guarantees
15. `test/EmployeeServiceTest.java` — JUnit 5, Mockito, Slice Tests

---

*Good luck with your interview! You've got this.*
