# Java Developer Interview Questions — Compiled from All Transcripts

## 1. Core Java / OOPs
- What are the OOPs concepts? (Abstraction, Encapsulation, Inheritance, Polymorphism)
- Explain abstraction with a real-time example (e.g., ATM machine interface)
- What is data hiding? How is it different from abstraction?
- Which of these examples is true data abstraction vs encapsulation (scenario-based MCQ: private field, getter/setter, protected class, abstract car class)
- Types of access modifiers — public, private, default, protected
- Explain private and protected access modifiers
- Composition vs Aggregation
- Write a short coding example for inheritance
- Explain the Java memory model (heap, stack, string constant pool)
- What is metaspace?
- What is method overloading? Does changing return type only make it valid overloading?
- If a method call is ambiguous due to overloaded methods with `null` argument, is it a compile-time or runtime error?
- Is method overloading compile-time or runtime polymorphism?
- Method overriding: can the return type in the child class differ (covariant return types)?
- If the parent method throws a checked exception, does the child override need to declare it too?
- Static vs dynamic polymorphism — which decides which method to call and when
- What is default method inside an interface, and its relevance (backward compatibility)?
- Two interfaces with the same default method signature implemented by one class — ambiguity problem and how to resolve it (override the method)
- Is there a risk of the "diamond problem" with default methods?
- What is data hiding — can you access data without getters/setters?
- What is the use of `default` and `static` methods in a functional interface?
- What are the 5 features of Java? Why is Java "simple"? Why is it "almost" object-oriented?
- Why is Java a platform-independent language? Role of JVM.
- What is JVM? (classloader, bytecode verifier)
- What is JDK? What components does it contain?
- What is the entry point of a Java program (`main` method)? Syntax of `main`.
- Why is `main` method static?
- What happens if you remove `static` from `main`? (compiles, fails at runtime)
- What are default values assigned to variables/instances (int, float, double, String)?
- What is a package in Java? Advantages of using packages?
- Difference between array and ArrayList; how to add elements to an array
- What is garbage collection? Objective/purpose of GC
- New generation vs old generation in GC; types of garbage collectors
- Explain your approach if you had to force/trigger garbage collection explicitly
- What is a memory leak? How does it happen? How do you identify/debug one (e.g., unclosed DB/file connections)?
- Difference between `final`, `finally`, and `finalize`
- Can we declare an entity/class as final? What's the effect (can't extend / immutable)?

## 2. Java 8 Features & Streams
- What are the Java 8 features you've used? (Lambda expressions, functional interfaces, Stream API, method references, Optional, default/static methods)
- What is a Lambda expression? Why was it introduced?
- What are functional interfaces? Relevance to lambda expressions
- What is a method reference (`::`)? When would you use it?
- What is the difference between `map()` and `flatMap()` in streams?
- What is a parallel stream? Difference from sequential stream — which method enables it?
- Which method internally sorts elements when inserting into a TreeMap (merge sort)?
- What are terminal operations in the Stream API?
- Write Stream API code to filter employees who joined after a given date
- Write Stream API code to collect employee name → first address into a `Map<String, Address>`
- Group employees by department and find the highest salary in each department using Collectors
- Find total transaction amount per category for transactions in the last 30 days using streams (filter + groupingBy + summingDouble)
- Fetch employees with salary greater than 50,000 using Stream API
- Find the longest word in a sentence using Stream API
- Count character frequency in a string using `chars()` + `groupingBy`/`counting()`

## 3. Strings & Immutability
- Why is String immutable in Java?
- How do you "change" a String's value if it's immutable? (creates new object; use StringBuilder/StringBuffer for same reference)
- Advantages of immutability (memory efficiency via string pool, thread safety)
- Disadvantages of immutable classes
- How to create your own immutable class? (final class, private final fields, no setters, parameterized constructor)
- If a field inside an immutable class is itself mutable (e.g., `LocalDate`, `Address`, `List`), how do you prevent that from breaking immutability? (deep copy in constructor/getter)
- Difference between deep copy and shallow copy — programmatic example
- What is the use of the `clone()` method? Shallow copy via `clone()`

## 4. Collections Framework
- Which collections have you worked on? (List, Set, Map — ArrayList, LinkedList, HashSet, HashMap)
- Explain HashSet (no order, no duplicates)
- Explain ArrayList (duplicates allowed, insertion order preserved, random access)
- Difference between ArrayList and LinkedList (search/read vs update/insert performance)
- Difference between List and Set (duplicates, insertion order)
- How can you preserve insertion order in a Set? (LinkedHashSet)
- Difference between Comparable and Comparator
- How to sort a list of employees by multiple fields (id, name) using Comparator, and using `thenComparing`
- Is HashMap synchronized/thread-safe? How to make it thread-safe (`Collections.synchronizedMap`, `ConcurrentHashMap`, Hashtable)
- Explain internal working of HashMap (hashing, buckets, collisions)
- Contract between `hashCode()` and `equals()` methods
- Can we use a custom object (e.g., Employee) as a HashMap key? What must you override?
- What happens if you override `equals()` but not `hashCode()` (or vice versa)?
- Why does Java not just give every object a unique/different hash code (why do we need to group equal objects into the same bucket)?
- Why did Java switch from LinkedList to a Red-Black Tree for bucket collisions from Java 8 onward?
- Fail-fast vs fail-safe iterators (ConcurrentModificationException, examples: ArrayList/HashMap vs ConcurrentHashMap)
- Best practices/guidelines for using a custom object as a Map key
- How to serialize an object while excluding a field (`transient` keyword)

## 5. Multithreading & Concurrency
- Why is synchronization necessary in multithreading? (real-world seat-booking analogy)
- Lock on an object vs lock on a class — difference
- Difference between `sleep()` and `wait()`
- Difference between synchronized collections and Concurrent collections (e.g., ConcurrentHashMap segment-level locking)
- How many ways can you create a thread?
- How to ensure Thread T2 runs after T1, and T3 runs after T2 (`join()` method)
- How to make a specific thread run last among five threads (Executor framework / join)
- What is the Executor framework? Why/when do you need it?
- What is a deadlock? Explain with an example (T1 and T2 each holding a resource the other needs)
- Scenario: shared HashMap in a singleton bean causing cross-user data leakage under load — explain root cause and fix (ConcurrentHashMap/synchronized map)
- Optimistic locking / version mismatch when multiple threads update the same record concurrently
- Have you faced any performance issues? How did you resolve them (multithreading)?
- Thread dump — have you used it?

## 6. Exception Handling
- How is exception handling done in Java? (try-catch, throws, finally)
- Exception hierarchy: Throwable → Exception/Error; checked vs unchecked exceptions
- Difference between checked and unchecked exceptions, with examples (SQLException/IOException vs ArithmeticException)
- Difference between `throw` and `throws`
- Can you have multiple catch blocks for a single try block? Can you nest try blocks?
- Can we create our own checked exception? How (extend `Exception` vs `RuntimeException`)?
- How does the compiler recognize a custom exception as "checked"?
- How is global exception handling done in Spring Boot? (`@ControllerAdvice` + `@ExceptionHandler`)
- Example: custom `EmployeeNotFoundException` thrown from service layer — how does the controller handle/propagate it?

## 7. Design Patterns
- How many ways can you break a Singleton pattern? (Serialization, Cloning, Reflection API)
- How to implement a Singleton class (private constructor, static final instance, getter only)
- Implement lazy initialization for a Singleton; thread safety via synchronized block / double-checked locking
- What design patterns have you used? (Singleton, Factory, Abstract Factory)
- Give an example/scenario where you'd use the Abstract Factory pattern (e.g., a bank issuing different loan types)
- What is the Facade design pattern? Real-world use case
- Explain SOLID principles with examples (SRP, OCP, LSP, ISP, DIP)
- Do you always follow SOLID principles fully or partially on a project? What if you can't apply one?
- What is DispatcherServlet? What design pattern does it follow (Front Controller)?
- How does a servlet handle multiple concurrent requests?

## 8. Spring Core / Spring Boot
- What is Dependency Injection? How do you inject dependencies (`@Autowired`, constructor/setter injection, XML beans)
- Types of autowiring (by name, by type); what does `@Qualifier` do?
- What is `@RestController`? Difference from `@Controller` + `@ResponseBody`
- What is the Dispatcher Servlet in Spring MVC?
- How do you create a new Spring Boot project? (Spring Initializr)
- Advantages of using Spring Boot (embedded server, auto-configuration, actuators, production-ready)
- Explain Spring Boot auto-configuration internals (`spring.factories` / `AutoConfiguration.imports`, `@ConditionalOnClass`, `@ConditionalOnMissingBean`)
- Bean scopes in Spring — singleton, prototype, request, session, global-session; which is default?
- Scenario: parent bean is prototype-scoped but its inner/child bean is singleton-scoped — what happens?
- Explain the full lifecycle of a Spring bean (instantiation → DI → `@PostConstruct` → ready → `@PreDestroy`)
- What is circular dependency between two `@Autowired` beans? How do you fix it (`@Lazy`, setter injection, redesign)?
- What is `NoUniqueBeanDefinitionException`? How to fix it (`@Qualifier`, `@Primary`)?
- What is `LazyInitializationException` when accessing a lazily-loaded collection? How to fix (`fetch = EAGER`, DTO projection, fetch inside `@Transactional`)?
- Debugging a bean creation error at startup — approach
- What is `@Value` annotation used for?
- What are actuators in Spring Boot? What do they expose (health, metrics, info)?
- How to create a custom actuator endpoint?
- What is profiling/`@Profile` in Spring (dev/QA/prod environment configs)?
- What is `@Transactional`? Key attributes (propagation, isolation, rollbackFor, timeout)
- If a `@Transactional` method is called from another method within the same class, does the transaction apply? Why not (proxy bypass)? How to fix?
- How do you debug an API that works fine locally but is slow in production?
- CI/CD process and deployment pipeline used in a project

## 9. Spring Data JPA / Hibernate
- What is the advantage of using JPA?
- How do you configure/use a JPA Repository? Writing custom queries with `@Query`
- Difference between `CrudRepository` and `JpaRepository`
- Consequences of using `CrudRepository` instead of `JpaRepository` in a large, complex application
- Default methods provided by `JpaRepository` (save, findById, findAll, delete, count, existsById, pagination/sorting)
- Typical use of `findAll`, `save`, `delete` in a blog application
- How to write a custom query using `@Query` (JPQL/native SQL) with `@Param`
- Challenges with JPA in a many-to-many relationship (infinite loops, lazy vs eager loading, large joins, cascading)
- How to implement dynamic queries using the `Specification` interface
- How do you achieve pagination and sorting with JPA repositories?
- Explain `@ManyToOne` / `@OneToMany` mapping annotations
- What does `@EmbeddedId` do (composite primary keys)?
- Transaction management via Entity Manager vs Spring repositories

## 10. Spring Security / Authentication / Authorization
- How do you handle authentication for your REST APIs? (Spring Security starter, `WebSecurityConfigurerAdapter`, role-based config)
- What is JWT? Why is it used?
- What are the three parts of a JWT (header, payload, signature)?
- How is a JWT generated in a Spring Boot app?
- How does Spring Security validate an incoming JWT (filter, e.g. `OncePerRequestFilter`)?
- Security vulnerability if a JWT payload (e.g. role) is tampered with and signature isn't validated
- How to implement Role-Based Access Control (RBAC) using JWT (`@PreAuthorize`, roles in payload)
- Scenario: healthcare system restricting doctor/nurse/admin access — how would JWT + RBAC work together?
- Scenario: e-commerce order placement — how is the token validated before placing an order?
- What is OAuth / OAuth2? Roles involved (client, resource owner, auth server, resource server)
- Have you worked with OAuth? (many candidates hadn't)

## 11. Microservices & Distributed Systems
- What do you know about microservices? Advantages (scalability, tech independence, error handling, API Gateway)
- What is an API Gateway? How is it different from a Load Balancer, and why do you need both?
- What is Service Discovery? Steps to register a service (Eureka, `@EnableDiscoveryClient`)
- What is the Circuit Breaker pattern? States (closed, open, half-open)? How implemented (Resilience4j/Hystrix)?
- Scenario: your service's thread pool exhausts because a downstream dependency is flaky — what should have been in place? (circuit breaker, thread pool isolation, timeouts)
- Synchronous vs asynchronous communication between microservices — trade-offs (REST/gRPC vs Kafka/RabbitMQ)
- What is the Saga pattern for distributed transactions? Choreography vs Orchestration
- Scenario: order service, payment service, notification service — payment succeeds, inventory deducted, but notification fails — how do you handle it?
- How do you manage distributed transactions across microservices (Saga vs 2-Phase Commit)?
- Scenario: Service A needs data owned by Service B's database — how do you get it without direct DB access? (sync REST/gRPC call vs async event + local copy)
- How do you version REST APIs without breaking existing clients (URI versioning vs header versioning; deprecation policy)?
- What is CAP theorem?
- Difference between Kafka and a traditional message queue (RabbitMQ) — replay, throughput, consumption model
- What is centralized logging in microservices (ELK stack)?
- What is query planning in a database?
- How do you implement Kafka in Spring Boot (`@EnableKafka`, `@KafkaListener`, `KafkaTemplate`)?
- End-to-end microservices testing approach
- Continuous monitoring setup (Grafana, Kibana, actuators)
- How do you autoscale an EC2 instance in AWS?
- What is the use of Docker?

## 12. AWS-Specific
- Which AWS services have you used? (S3, SNS, SQS, API Gateway, Lambda, ElastiCache, Cognito)
- Explain a scenario where you used AWS in your project (asset management via S3, EventBridge + Lambda + SQS for data sync between systems)
- How did you secure REST endpoints using AWS (Cognito, SAML/IdP federation)?
- How does API Gateway provide security (API keys)?
- What are mapping templates in API Gateway used for (request/response transformation)?
- How do you autoscale EC2 instances?

## 13. Database / SQL
- Difference between primary key and unique key
- Types of joins (inner, outer, left, right, self join)
- Write a query to find the second-highest salary in an Employee table
- Write a query to create an empty table from an existing table's structure
- Write a query to find manager name and number of employees reporting to them (self-join)
- Difference between `PUT` and `POST`, and other HTTP methods (GET, POST, PUT, PATCH, DELETE) and their purposes
- ACID properties (Atomicity, Consistency, Isolation, Durability)

## 14. Coding Problems Asked
- Remove duplicate characters from a string (e.g., "Banaras" → "bans")
- Check if a number is an Armstrong number
- Write a program to check if a string is a palindrome
- Reverse the K-th word in a sentence
- Find the first non-repeating character in a string (e.g., "swiss" → "w")
- Find the top two most frequently occurring words in a list of strings (without built-in sort)
- Reverse a linked list using recursion
- Find the longest substring without repeating characters
- Write a JUnit 5 + Mockito test case for a "check prime number" method
- Write a basic CRUD REST API
- Design a method signature that only accepts an `Employee` object or a subclass of `Employee` (bounded type / generics)
- Design an "online document signature" portal (system design style question)

## 15. Testing, Tools & Process
- Which testing frameworks have you used? (JUnit, Mockito)
- Testing tools used for APIs (Swagger, Postman); UI testing (Selenium); load testing (JMeter)
- PMD and Checkstyle vs SonarQube — which have you used and why
- CI/CD tools used (Jenkins, GitHub Actions, GitLab CI)
- Version control and branching tools (Git, Jenkins, Jira)
- Agile/Scrum process, sprint size, team composition questions (non-technical but commonly paired)

## 16. Core Java — Extended (Advanced/Follow-up Questions)
- Difference between interface and abstract class; when to use each
- Can an abstract class implement multiple interfaces and also extend another abstract class simultaneously?
- Why is OOPs important? (organization, readability, scalability)
- Difference between JDK, JRE, and JVM; can JRE be installed separately without JDK?
- What happens if a static block throws an exception during class loading? (ExceptionInInitializerError)
- Difference between SOAP and REST; why do some financial/banking services still prefer SOAP over REST (WS-Security, reliable messaging, ACID compliance)
- Class loader hierarchy in Java (Bootstrap → Extension → Application); scenarios causing `ClassNotFoundException` / `ClassCastException` when different loaders load "the same" class
- Can a class be loaded twice? (Not by the same loader, but yes by different loaders — creates separate copies in memory)
- What happens if you start a thread twice, or call `run()` instead of `start()`?
- How does Java handle memory leaks despite having a garbage collector? (unused but still-referenced objects)
- How does a `HashSet` ensure uniqueness internally? (backed by HashMap)
- Difference between `ArrayList` and `CopyOnWriteArrayList`; when to prefer `CopyOnWriteArrayList`
- Difference between fail-fast and fail-safe iterators; which collections are fail-safe (ConcurrentHashMap, CopyOnWriteArrayList/Set)
- How does `ThreadPoolExecutor` manage threads internally? What happens when all threads are busy / queue is full (RejectedExecutionException)?
- Explain `CompletableFuture` and a real-world use case (parallel API calls + merge)
- What happens behind the scenes when a Spring Boot app starts? How does auto-configuration work internally (`spring.factories` / `AutoConfiguration.imports`, `@ConditionalOnClass`)?
- What are Spring Boot starters and how do they simplify configuration?
- Difference between `@Bean` and `@Component`; which takes precedence if both define the same bean?
- Does Spring manage dependency injection at compile time or runtime?
- Difference between `String`, `StringBuilder`, and `StringBuffer` — mutability, thread safety, memory handling
- String interning — how it affects `==` vs `.equals()` comparisons
- What happens when you concatenate strings (`str1 + str2`) under the hood? (creates new object via StringBuilder)
- When should you use `StringBuilder` vs `StringBuffer` in a multi-threaded application?
- Different types/states of polymorphism; can overriding cause unexpected issues (Liskov Substitution violations)?
- Difference between `Callable` and `Runnable`; use with `ExecutorService`
- What is a marker interface? Give examples (Serializable, Cloneable)
- Static keyword — class-level members, static blocks and their execution order across classes
- Difference between pass-by-value and pass-by-reference in Java (Java is always pass-by-value; reference copies for objects)
- Difference between primary key and unique key; clustered vs non-clustered index and their performance impact
- What's the initial size/load factor of a `Hashtable`/`HashMap`, and how does capacity grow (resize/rehash)?
- What is `ThreadLocal` and when would you use it?
- Difference between `wait()`, `sleep()`, and `yield()`
- Difference between `CountDownLatch` and `CyclicBarrier`; is a `CyclicBarrier` the same as a thread pool?
- Difference between atomic variables and `volatile` — which to prefer and why (atomicity vs visibility only)
- What is a reentrant lock (`ReentrantLock`) vs `synchronized`; when to prefer one over the other
- What is `BlockingQueue` vs `DelayQueue`?
- Pessimistic vs optimistic locking — which is better suited for banking/financial transactions and why
- Virtual threads (Java 21) — what they are and why they were introduced
- Producer-consumer problem and how to implement it safely
- What happens if a thread in an `ExecutorService` throws an unchecked exception?
- Class-loading process: loading, linking, initialization
- Ways to create an object in Java (new keyword, reflection, deserialization, factory methods, clone)
- What happens if a class doesn't have a default (no-arg) constructor and you try to instantiate it without arguments?
- Can a constructor be static or final? Can a constructor have a return type? Can a constructor be inherited (and how do you still call a superclass constructor via `super()`)?
- Can you create an object of an abstract class directly? Can you assign a concrete-class object to an abstract-class reference variable (polymorphism)?
- Explain coupling and cohesion in OOP; can you have low coupling and low cohesion at the same time?

## 17. Java 8+ Advanced / Functional Programming
- What is lazy evaluation in the Stream API, and why does it matter for performance on large datasets?
- When would you avoid using a parallel stream? (I/O-bound tasks, shared mutable state, order-sensitive processing, thread-pool-managed environments)
- Difference between `map()` and `mapToObj()`
- Difference between Lambda expressions and method references, with examples; difference between a Lambda expression and an anonymous class
- Can a functional interface have `default` and `static` methods and still be usable with lambdas?
- Can interfaces have `private` methods (Java 9+)? What's their purpose if they can't be called from outside the interface?
- Role of `Predicate`, `Function`, and `Consumer` in Java 8
- How do you handle an exception thrown inside a stream pipeline so it doesn't break the whole pipeline?
- What are terminal vs intermediate operations? Will intermediate operations run without a terminal operation?
- Purpose of `Optional` — what problem does it solve? When does using `Optional` become an anti-pattern (as a parameter type or persisted field)?
- What is the `reduce()` operation used for, with an example (summing a list)
- Difference between `flatMap()` and `map()`, with a "list of lists flattened to one list" example
- Can a variable declared outside a stream be referenced inside a `filter()` lambda (effectively final capture)?
- What is metaspace in Java, and how does it differ from PermGen?
- What are the new features in Java 17 (sealed classes, pattern matching for switch, enhanced random generators, LTS stability)? Key differences between Java 8 and Java 17

## 18. Multithreading & Concurrency — Extended
- Why is `volatile` insufficient to guarantee thread safety for compound operations like `counter++`?
- Risk of overriding `equals()` but not `hashCode()` — how it breaks HashMap/HashSet buckets
- What happens if two threads increment a shared counter simultaneously without synchronization (race condition)? How would you prevent it in a banking app (synchronized deposit/withdraw methods)?
- Would a static synchronized method and an instance synchronized method conflict if called simultaneously on the same object? (No — different lock scopes: class lock vs instance lock)
- Explain object-level vs class-level thread locks
- How do you design a thread-safe immutable value object shared across threads?
- Real-world example of a race condition (concurrent bank balance updates)
- How would you avoid deadlock in Java? (consistent lock ordering, tryLock with timeout, minimizing synchronized scope, avoiding nested locks)
- How to make a Singleton thread-safe in a multi-threaded logging application (synchronized method, double-checked locking with volatile, enum singleton)
- What happens if the main thread dies but other non-daemon threads are still running? (JVM keeps running until all non-daemon threads finish)
- If a thread pool task throws an unchecked exception, does it terminate other threads? How do you surface it (via `Future.get()`)?
- Suppose you have three threads that must run strictly in order (T1→T2→T3) — how do you enforce that (`join()`, `CountDownLatch`, semaphores)?

## 19. Immutability — Deep Dive
- How do you make a class immutable in Java (final class, private final fields, no setters, defensive copies)?
- If your immutable class has a mutable field (e.g., a `Date`/`List`) and you return it directly from a getter, is the class still truly immutable? Why not — and how do you fix it (defensive/deep copy)?
- Why is immutability preferred in multi-threaded systems, and how do you design such classes?
- How does immutability help with security, caching, and performance (string pooling)?
- How does immutability help avoid synchronization in concurrent code?

## 20. Serialization — Deep Dive
- What is serialization/deserialization, and why do we need it (persistence, network transfer, distributed systems)?
- What happens when you try to serialize an object containing a non-serializable field? How do you handle it (`transient`, custom `writeObject`/`readObject`)?
- If a field is marked `transient`, what happens to it on deserialization, and how do you restore it (manual reinitialization / `readObject` logic)?
- Challenges of serializing/deserializing objects in a distributed system (version mismatches, performance overhead, security risks)
- Purpose of serialization in a POJO and common use cases (caching, file storage, network transfer, session storage)

## 21. Design Patterns — Extended
- Different ways to implement a thread-safe Singleton (eager init, synchronized method, double-checked locking, static inner class, enum singleton) — which is most recommended and why
- When would you use the Builder pattern vs the Factory pattern?
- Give a real/hypothetical example using the Abstract Factory pattern (e.g., a bank issuing different loan types via an interface)
- How do you optimize a Singleton implementation (lazy init, double-checked locking, volatile, enum-based)?
- Explain a few core Java design patterns (Singleton, Factory, Builder, Observer, Strategy) and their purposes
- How would you make a Factory pattern thread-safe in a multi-threaded environment without hurting performance?
- Give 2-3 real scenarios where a Singleton is appropriate (DB connection pool, logging service, shared cache)
- Explain object cloning (shallow copy via `clone()`)
- What is a Facade design pattern, and what's a real use case (hiding complexity behind one clean API)?
- What are the Decomposition and Strangler patterns in microservices?

## 22. Design Principles (SOLID, DRY, Coupling/Cohesion)
- Explain SOLID principles with a real scenario (e.g., adding new payment methods to an e-commerce system)
- Can violating the Open/Closed Principle still be beneficial in certain situations? (quick fixes on small projects)
- Give an example of a bug caused by violating OCP (e.g., modifying a payment class directly instead of extending it)
- How would you apply the Dependency Inversion Principle while refactoring a payment module?
- What is the DRY principle?
- Explain coupling and cohesion — why minimize coupling and maximize cohesion? Can you have low coupling and low cohesion at the same time?

## 23. Spring / Spring Boot — Extended
- Difference between `ApplicationContext` and the IoC/BeanFactory container
- Difference between `@Primary` and `@Qualifier` — when to use which
- What happens when two beans of the same type exist and you don't use `@Qualifier`? (`NoUniqueBeanDefinitionException`)
- How does Spring Boot handle circular dependencies between beans? (setter injection, proxies, `@Lazy`; otherwise `BeanCurrentlyInCreationException`)
- Difference between `@Value` and `@ConfigurationProperties`
- How do you inject a dynamic value (e.g., port) from a separate config file using `@PropertySource` + `@Value`?
- What happens if you use `@Qualifier` on a class that only has one bean defined? (No error — it's just extra information)
- Difference between `application.properties` and `application.yml` — which does Spring Boot pick if both exist?
- What is `@ConditionalOnProperty` / conditional annotations, and where have you used them?
- How does Spring Boot reduce boilerplate, and what are the trade-offs (less control, hidden complexity, harder debugging)?
- What challenges in distributed systems does Spring Cloud solve (service discovery, config management, load balancing, circuit breaking, centralized logging)?
- RestTemplate vs WebClient — blocking vs non-blocking, when to use which
- Common HTTP status codes returned from a REST API, and how to handle exceptions with `@ControllerAdvice`
- What are DTOs and why use them (security, decoupling API from entity model)?
- What are idempotent HTTP methods? Which methods are idempotent (GET, PUT, DELETE, HEAD) and which aren't (POST)?
- How do you propagate a security token from an API Gateway to downstream services (token relay)?
- How do you protect sensitive endpoints from brute-force attacks (rate limiting, CAPTCHA, account lockout, IP blocking)?
- How do you implement pagination and sorting with `Pageable`/`Sort` in Spring Data JPA?
- Given `User` and `Order` entities where a user has multiple orders, how do you map the relationship (`@OneToMany` / `@ManyToOne`)?
- Difference between `@Controller` and `@RestController`; what happens if you return a plain String but forget `@ResponseBody` (Spring tries to resolve it as a view name)?
- Can a controller return both HTML views and JSON from the same class? How?
- What's the purpose of `@Service` vs `@Component`; can a class have both annotations (redundant but valid)?
- What is the main annotation in a Spring Boot project, and what does it combine (`@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`)?
- Can a Spring Boot app run without `@SpringBootApplication`, using the three annotations separately?
- What is DispatcherServlet, and its role as the front controller in Spring MVC?
- Difference between `@RequestParam` and `@PathVariable`; can they be used together in one method?
- Explain Spring MVC architecture (DispatcherServlet → Controller → Service → Model/View)
- Explain REST principles (statelessness, cacheability, layered system, uniform interface, code-on-demand) — is REST always fully stateless in practice?
- What happens if a circuit breaker stays open too long? How do you tune it to move to half-open/closed?
- How do you configure different databases for different environments using Spring Profiles?
- TDD vs BDD as testing paradigms, and the testing pyramid (more unit tests, fewer integration tests, least UI tests)
- Difference between `@RequestParam`, `@PathVariable`, and `@RequestBody`, with a "design an endpoint to update a user profile" example
- How does caching work in Spring Boot (`@Cacheable`, `@CachePut`, `@CacheEvict`)? Can Spring Boot's built-in caching be used in a distributed system (Redis/Hazelcast)?
- What is HATEOAS, and how would you use it in a product-catalog REST API?
- How do you implement API versioning in Spring Boot (URI versioning, headers, content negotiation)?
- How do you secure sensitive configuration values in Spring Boot (env vars, Vault, AWS Secrets Manager — never commit secrets)?
- How do you handle CSRF protection and enable CORS in a Spring MVC application (`@CrossOrigin`)?
- Difference between `yield()` and `join()` in multithreading
- What are Spring Boot Actuators, and which endpoints are commonly used (health, info, metrics, env, loggers)?
- How would you ensure compatibility when upgrading to a newer Spring Boot version?
- Explain the Spring bean life cycle in detail (instantiation → DI → `@PostConstruct`/`InitializingBean` → ready → `@PreDestroy`/`DisposableBean`)
- Different types of dependency injection (constructor, setter, field) — pros/cons of each
- Difference between `@Resource`, `@Autowired`, and `@Inject`
- What's the purpose of the `@Qualifier` annotation, and when would you create a custom qualifier?

## 24. Spring Data JPA / Hibernate — Extended
- Explain `FetchType.LAZY` vs `FetchType.EAGER`, and their defaults for collections vs single-valued associations
- What's the N+1 select problem, and how do you detect and fix it (fetch joins, entity graphs, adjusting fetch type)?
- How do you optimize a slow query (EXPLAIN, indexes, avoid SELECT *, efficient joins, filter early, limit result sets)?
- How do you connect two tables in Hibernate — which annotations for one-to-many, many-to-one, one-to-one?
- What's the default cascading type in JPA, and what are the cascading options (PERSIST, MERGE, REMOVE, REFRESH, DETACH, ALL)?
- How do you implement dynamic queries using the `Specification` interface?
- Explain a many-to-many relationship in Hibernate — how many tables are needed for it (including a join table)?
- Difference between `save()` and `saveAndFlush()`
- What's the purpose of `@Transient` in JPA — a field the ORM should ignore?
- Explain the difference between clustered and non-clustered indexes and their impact on query performance
- Explain Hibernate session methods (`save`, `update`, `delete`, `get`, `load`) and the typical session lifecycle
- How would you use multiple databases in a single Spring Boot application (separate DataSource, EntityManagerFactory, TransactionManager beans; `@Primary`/`@Qualifier`)?

## 25. Microservices & Distributed Systems — Extended
- What is the Bulkhead pattern? (isolating parts of a system with separate thread pools so failure in one doesn't cascade)
- What is Blue-Green deployment, and how does it ensure zero downtime?
- What is the Database-per-service pattern, and why is sharing a database between microservices discouraged?
- What is CQRS (Command Query Responsibility Segregation)?
- What is a Config Server, and how does centralizing configuration help (e.g., Spring Cloud Config with Git backend)?
- What is Service Discovery, and why is hardcoding URLs a bad idea (Eureka)?
- Explain the Saga pattern with a real example (e.g., order → payment → inventory → notification, with compensating transactions on failure); choreography vs orchestration
- How do you handle cascading failures when a downstream service is slow (circuit breakers, timeouts, retries with backoff, fallback)
- What is an LRU cache, and how would you implement one (e.g., using `LinkedHashMap`)?
- What is the difference between REST and gRPC (HTTP/JSON vs HTTP/2+Protobuf; simplicity/readability vs speed)?
- How do you design a system to handle millions of concurrent users (load balancers, autoscaling, caching, CDNs, horizontal scaling)?
- Explain CDN, Redis, and load balancing strategies (round robin, least connections, IP hash)
- SQL vs NoSQL for metadata storage in a large-scale system
- Monolithic vs microservices architecture — advantages/disadvantages of each, and scenarios where monolithic is still preferable
- Design a video-streaming platform (or similar system-design prompt) — expected to discuss microservices, CDN, transcoding, metadata DB choice, caching, auth, and observability
- What is token relay in an API Gateway context?
- Explain rolling, canary, shadow, and blue-green deployment types
- How do two microservices communicate internally (REST/HTTP vs message queues like Kafka/RabbitMQ)?
- How would you create a scheduler as a microservice (`@Scheduled` annotation)?
- How can a service identify whether a codebase in an IDE is a microservice or monolith just by inspecting it (multiple modules, application.yml per service, Feign clients, `@EnableDiscoveryClient`)?
- What annotations are commonly used in microservice architecture (`@RestController`, `@SpringBootApplication`, `@EnableDiscoveryClient`, `@FeignClient`, circuit breaker annotations)?
- Explain a Feign client and how it simplifies inter-service REST calls
- What's the difference between synchronous and asynchronous communication, and when to use each?
- What is a webhook, and why might you need async processing instead of relying purely on webhooks?
- Kafka basics — topics, partitions, offsets, brokers, producers/consumers, and how long messages are retained
- How do you check whether running services are healthy in a microservices setup (health/actuator endpoints, Prometheus, Eureka)?
- What is idempotency in REST APIs, and how do you design an idempotent endpoint (idempotency keys/request IDs)?
- How do you handle schema changes for event messages between services (versioning, backward compatibility, schema registry)?
- How do you manage configuration for multiple microservices (central config server)?
- What is API Gateway vs Load Balancer, and why do you need both?
- Explain the token-relay flow from API Gateway to downstream services
- Explain a "design a microservice for order management" style prompt — list the DBs, APIs, and services you'd create
- How do you implement distributed tracing and logging across services (Zipkin/Sleuth, ELK stack)?
- Explain unit vs integration vs contract testing for microservices
- How would you design a payment microservice that processes payments and notifies other services?
- How do you roll out a new version to production with minimal downtime (blue-green, rolling updates)?

## 26. AWS & Cloud
- Explain EC2, S3, and IAM at a high level
- How do you autoscale an EC2 instance based on metrics like CPU usage or traffic?
- What is the use of Docker, and how does it help package/deploy applications?
- What does Kubernetes do (orchestration, scaling, self-healing, service management across environments)?
- How do you dockerize a Spring Boot application (Dockerfile, build image, run container)?
- Different deployment types: shadow, canary, blue-green, rolling
- What checks do you perform before deploying code to production (tests, static analysis, config validation, DB migration checks, smoke tests, rollback readiness)?
- How does Spring Boot use an embedded server, and how would you switch from Tomcat to Jetty?

## 27. Database / SQL — Extended
- Difference between clustered and non-clustered indexes, and how they impact query performance
- Difference between a table and a view (a view is virtual and doesn't store data physically)
- Difference between DROP, DELETE, and TRUNCATE
- Difference between a stored procedure and a function
- Difference between primary key, unique key, and foreign key
- Second normal form (2NF) vs third normal form (3NF) — partial dependency vs transitive dependency
- What is the `EXISTS` keyword used for in a subquery?
- Why do searches using a primary key perform faster (indexing)?
- Difference between BYTE and CHAR/VARCHAR storage
- Write a query to find the 2nd/3rd/4th highest salary using `LIMIT`/`OFFSET`
- Write a query to find the average salary grouped by department/country
- Write a query to count duplicate values in a column
- SQL injection — what it is, and how to prevent it (prepared statements, parameterized queries, input validation, ORM, least privilege)
- HTTP 300-series status codes (redirection: 301 Moved Permanently, 302 Found, 304 Not Modified)
- Transaction isolation levels with examples (Read Committed, Serializable, etc.) and their trade-offs (consistency vs performance)
- Query tuning: tools/techniques to identify and resolve slow-query performance issues (EXPLAIN ANALYZE, indexing, rewriting queries)

## 28. Coding Problems — Additional
- Validate whether a mathematical equation/expression string is syntactically valid (balanced parentheses, valid operator sequences)
- Design a vending machine simulation in Java (accept coins, check balance, dispense item)
- Find the second/third/fourth highest salary via SQL
- Sum of all odd numbers in a list using Stream API
- Find the average salary per country/department using Stream API or SQL
- Merge two unsorted arrays and print the result sorted, using Stream API
- Check whether a given number is an Armstrong number
- Find the largest continuous subarray that sums to zero
- Rain water trapping problem
- Remove duplicates from an ArrayList without using built-in methods or other collections
- Check if two strings are anagrams (optimized, no built-ins)
- Sort a list of Student/Employee objects by a field (marks, age, salary, department) using Streams
- Print palindromic strings from a list using Streams
- Implement a thread-safe LRU cache (using LinkedHashMap or custom doubly linked list + HashMap)
- Reverse a singly linked list (iteratively and recursively); detect a palindrome in a linked-list-of-characters
- Detect a cycle in a linked list using the fast-and-slow pointer approach
- Find the longest subarray with a sum equal to k
- Implement your own Singleton class (thread-safe) — a very frequently repeated coding ask
- Write a program to sum a list using Streams; print numbers 1 to 10 using two threads alternately
- Find the top two most frequent words in a list without built-in sorting
- Group employees by department and find the highest-paid employee per department using Streams
- Find the first non-repeating character in a string using Streams (grouping by character, counting, filtering count==1)
- Write a circuit breaker pattern implementation (conceptual/code)
- Write a query/program to remove duplicate employee IDs from a list
- Reverse a string using recursion / find its length recursively; replace vowels/characters in a string using a given rule
- Remove duplicate characters from a string while preserving order
- Find the sum of all elements in an array using Stream API; find squares of numbers using Stream API
- Insert a node, delete a node, find min/max, and perform in-order traversal on a Binary Search Tree

## 29. Testing (Extended)
- Explain common JUnit annotations (`@Test`, `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`, `@Disabled`)
- What is mocking vs stubbing, and why use them?
- Common Mockito annotations (`@Mock`, `@InjectMocks`, `@Spy`, `@Captor`, `@ExtendWith(MockitoExtension.class)`, `when()`)
- What does `doNothing()` do in Mockito, and when would you use it (void methods)?
- How do you structure unit, integration, and contract tests for microservices?
- How do you ensure high/100% test coverage in a Spring Boot application (JUnit + Mockito for units, integration tests for APIs, mocked dependencies, coverage tools like JaCoCo)?

## 30. Behavioral / Scenario-Based (Common Follow-ups)
- Walk through your current project's flow from API request to database — what part do you own completely?
- Describe the last production bug you fixed and how you debugged it
- Give a real example where inheritance caused a problem, and how composition would have avoided it
- Give an example of a bug caused by incorrect `equals()`/`hashCode()` implementation in production
- Why did you avoid heavy OOP in a case where the requirement was simple?
- Describe a case where you replaced one collection type with another for performance reasons
- When did HashMap performance degrade in your project, and why (poor hash distribution, many collisions)?
- When did Optional create confusion in your codebase, and what did you correct?
- How did you use `CompletableFuture` in a real production flow?
- What checks do you run before deploying to production?
- Describe a disagreement with a teammate or senior developer, and how you resolved it
- If a client keeps changing requirements frequently, how do you manage the project?
- What would you do if a third-party API your service depends on goes down?
- Why do you use Eclipse over IntelliJ (or vice versa)? Which Spring Boot / JDK versions have you used recently, and why?
- How would you upgrade a project from Java 8 to Java 17, and what major changes would be involved?
- Explain your project architecture from scratch — did you build it from scratch or was it based on an existing template?
- How do you adapt to a new technology, and where do you learn about new tech trends?

## 31. Angular / Frontend (occasionally asked in full-stack rounds)
- Is TypeScript object-oriented? Is Angular a framework or a library (vs React)?
- Difference between lazy loading and eager loading of modules
- Types of directives in Angular (component, structural, attribute)
- Explain Angular lifecycle hooks

## 32. Scenario-Based System Design & Troubleshooting (Live Mock-Interview Style)
- Walk through the complete request flow for a Spring Boot app receiving a "create employee" request, from controller → service → repository → DB → back to client
- You added `@Transactional` on a service method — what happens if an exception occurs mid-transaction (e.g., a bank transfer where debit succeeds but credit fails)?
- Why do we use `@Component`, `@Service`, and `@Repository` instead of just one generic bean annotation, if all of them register Spring beans?
- An API receives invalid data — how do you implement request validation and return a proper error response (`@NotNull`, `@Email`, `@Valid`, `MethodArgumentNotValidException`, global handler)?
- Your application suddenly becomes slow in production — how would Spring Boot Actuator and logging help you investigate (health/metrics endpoints, comparing to prior release, checking cache hit rate)?
- Order service needs data from product service — what are the different ways microservices can communicate (RestTemplate, Feign, Kafka)?
- What if product service is down and order service starts failing — how do you handle it (circuit breaker + fallback via Resilience4j)?
- In a microservices architecture, how does a request reach the correct service without hardcoding URLs (API Gateway route predicates + Service Discovery)?
- Two users try to update the same employee record at the same time — how do you handle this in Spring Boot (optimistic locking via `@Version` field, causing an exception on version mismatch)?
- A user reports an API is returning HTTP 500 — how do you investigate and troubleshoot (check app health, logs/stack trace, DB connectivity, external service dependencies, restart if needed)?
- One microservice is receiving significantly more traffic than others (e.g., flash-sale traffic spike) — how do you ensure the application handles the increased load (horizontal scaling + load balancer distributing requests across instances)?
- Your e-commerce search API takes 8–10 seconds during peak hours — how do you investigate and improve performance (check slow SQL/missing indexes, add pagination, introduce Redis caching, check CPU/memory metrics)?
- Order service successfully creates an order, but the payment call fails due to a network issue, leaving an unpaid order in the system — how do you handle this (Saga pattern: pending status → publish event → payment service updates to confirmed/failed → compensating rollback)?
- Order service starts failing while fetching product details, though everything worked before — how do you troubleshoot (check product service health/actuator, check logs for timeouts, verify service registration in Eureka, check network connectivity, review recent deployments/config changes, check circuit breaker/rate limiter rules)
- A Spring Boot app's memory usage keeps increasing over months until it crashes with `OutOfMemoryError` — how do you investigate (JVM metrics, generate and analyze heap dumps with VisualVM/Eclipse MAT, look for growing collections/unclosed resources/improper caching, review recent code changes)?
- Customers double-click "place order," causing duplicate orders due to slow network — how do you prevent this (idempotency: unique request/transaction ID checked server-side before processing, DB-level uniqueness constraints)?
- A customer database has millions of records and user-management APIs are getting slower with high DB CPU utilization — how do you identify the bottleneck and improve performance (analyze slow queries/execution plans, add indexes, paginate, introduce caching)?
- A third-party payment gateway becomes intermittently slow/unavailable, causing request pile-up — how do you make the system more resilient (circuit breaker, configured timeouts, limited retries, fallback response)?
- Production database connection details need to change with zero downtime — how do you manage config changes (centralized Spring Cloud Config Server + `@RefreshScope` to pick up changes dynamically without restart)?
- Traffic spikes 5x during a festival sale and APIs slow down / requests start failing — what steps do you take (identify the bottleneck, scale horizontally, introduce caching/CDN, optimize indexes, process non-critical work asynchronously via Kafka)?
- A newly deployed Spring Boot version causes issues that weren't present in the previous version — how do you investigate and minimize business impact (check logs, compare code diffs between releases, roll back to the last stable version quickly if impact is large, then do root-cause analysis in a lower environment)?
- Design a microservice for order management — list the DBs, APIs, and services you'd create (order/payment/inventory/notification services, relational DB for transactional data, NoSQL for logs)
- One service needs to call multiple downstream microservices to serve a single request — how do you avoid performance degradation or cascading failures (parallelize downstream calls with `CompletableFuture`, add circuit breakers and timeouts, cache infrequently-changing data, use Kafka/RabbitMQ for non-critical async work)?
- What was the hardest part of migrating a monolith to microservices (typically: defining domain boundaries, splitting data ownership since cross-service joins aren't possible and require REST calls instead, choosing sync vs async inter-service communication, independent CI/CD with Docker/Kubernetes, and adding observability — logging, tracing, monitoring)?
- With database-per-service, how do you keep data consistent across services during a distributed transaction (Saga pattern — break into local transactions with compensating actions on failure; system becomes "eventually consistent")?
- Two microservices need to query the same underlying data (e.g., for reporting) without tight coupling — how do you design this (publish domain events like `OrderCreated`/`PaymentDone` to Kafka; a separate reporting service consumes and aggregates events asynchronously instead of calling the other services' APIs directly)?
- Best practices for REST API versioning (backward-compatible changes preferred; URI versioning like `/api/v1/orders` and `/api/v2/orders` kept alive in parallel; deprecate old versions gradually with `@Deprecated` + deprecation response headers + documentation, then sunset after a grace period)
- What is idempotency in REST API design, and how do you implement it (idempotency keys sent by the client, server checks/records the key before processing, unique DB constraints, storing request/response history to replay identical responses)?
- A service calls another service that's down — how does your system handle it end-to-end (circuit breaker via Hystrix/Resilience4j, configurable timeouts, fallback responses, retries with limits, monitoring/alerting on heartbeats)?
- How do you trace a request as it flows through multiple chained microservices (a shared trace ID injected at the entry point, propagated through logs across all services, centralized via ELK; metrics/dashboards via Prometheus + Grafana)?
- How do you deploy multiple microservices independently while keeping them able to interoperate (independent CI/CD pipelines per service, containerization + Kubernetes replicas for independent scaling, blue-green/canary deployments for zero downtime, API versioning so one service's changes don't break another)?

## 33. Spring Boot — Live-Coding / Deep-Dive Topics
- How do you configure a Spring Boot application to connect to multiple databases (e.g., MySQL and PostgreSQL) — separate `DataSource`, `EntityManagerFactory`, and `TransactionManager` beans per database, each in its own `@Configuration` class, with distinct base packages for entities/repositories per database
- How do you introduce caching to speed up APIs under heavy load — enable with `@EnableCaching`, mark methods with `@Cacheable(value = "products", key = "#id")`, and choose in-memory (Caffeine) vs distributed (Redis) cache depending on whether you have single or multiple instances
- What happens when cached data becomes "stale" (e.g., someone updates/deletes the underlying DB row directly)? How do you keep the cache in sync — `@CacheEvict` to remove the stale entry, or `@CachePut` to update it; also configure a Time-To-Live (TTL) so entries expire automatically
- How do you manage config across dev/QA/prod environments using Spring Profiles — naming convention `application-<profile>.yml`/`.properties`, activate with `spring.profiles.active`, and keep shared config in the base `application.yml` while overriding only what differs per environment
- How do you avoid hardcoding sensitive values like DB passwords/API keys/tokens — externalize via environment variables, or centralized secret stores (Spring Cloud Config, AWS Secrets Manager, Kubernetes Secrets, HashiCorp Vault); never commit secrets to source control
- Explain fault-tolerance mechanisms for inter-service calls in detail: timeout (cancel a call if no response within N ms), retry (reattempt N times with a wait duration between attempts), circuit breaker (`@CircuitBreaker(name=..., fallbackMethod=...)`, breaks after a configured number of failures and calls a fallback method instead), and fallback (return a safe/default response)
- What's the difference in purpose between retry and circuit breaker — retry handles transient failures by reattempting (but risks increasing load), while circuit breaker blocks calls temporarily to protect a struggling downstream service from being overwhelmed
- What are cascading failures, and how do timeouts/circuit breakers/fallbacks help when Service A → B → C → D each depend on the previous?
- How do you implement REST API versioning end-to-end, including the deprecation lifecycle: URI versioning, keeping old and new versions alive simultaneously, marking deprecated endpoints/controllers with `@Deprecated`, adding a `Deprecation: true` response header, documenting via Swagger, monitoring usage of the old version, and finally removing it after a sunset date
- How do you build a Spring Boot application from scratch: choose the JDK version, use Spring Initializr to select build tool/dependencies, download and import into an IDE, understand the layered package structure (controller → service → repository/DAO → entity/DTO → exception handling)
- What's the flow when a REST request hits a Spring Boot controller (DispatcherServlet → HandlerMapping → controller method → service → repository → response serialized back to client)?
- Difference between `CrudRepository`, `PagingAndSortingRepository`, and `JpaRepository` — layered feature sets (basic CRUD only; CRUD + pagination/sorting; full JPA feature set including both)
- How do you implement distributed tracing across microservices using Spring Cloud Sleuth (adds trace ID/span ID to logs) and Zipkin (collects and visualizes the trace data on a dashboard)?
- What is Spring Cloud Config Server, and how does `@RefreshScope` let services pick up configuration changes without a restart?
- How do you write and structure unit vs integration tests in Spring Boot — `@WebMvcTest`/`@MockBean`/Mockito for isolated unit tests, `@SpringBootTest` for full-context integration tests
- How do you monitor a Spring Boot application end-to-end using Actuator + Micrometer + Prometheus + Grafana (expose `/actuator/prometheus`, have Prometheus scrape it, visualize in Grafana dashboards)?
- How do you dockerize and deploy a Spring Boot application: write a Dockerfile (base JDK image, build/copy the jar, entrypoint), `docker build -t myapp .`, push to a registry, then `docker run` to spin up a container from the image; on EC2, install Docker and run the same commands
- What tools help measure/analyze Spring Boot API performance in production (Actuator, Micrometer, JProfiler, Prometheus/Grafana, Zipkin, thread dumps, heap dump analysis, GC logs, DB query profiling)?

## 34. Database Scalability & Architecture (Advanced)
- Explain database partitioning — the two broad approaches are replication (copying the full dataset across machines, either master-slave where writes go to the master and reads are served from replicas, or master-master where writes can go to either master but conflict resolution becomes complex) and sharding (splitting the dataset itself across machines, e.g., by a range of usernames, using range-based, location-based, or hash-based sharding)
- SQL vs NoSQL — when do you choose each for a large-scale system like an e-commerce app (SQL for a fixed/rigid schema with strong consistency needs; NoSQL/document-based when the schema is flexible or varies per record)?
- Explain the BASE properties for NoSQL systems: Basically Available, Soft state (data may change over time even without new input), and Eventual Consistency (replicas converge to the same value given enough time) — contrasted with ACID for relational systems
- What is database connection pooling, and why is it needed (creating a DB connection is expensive; a pool of pre-made, reusable connections avoids that cost per request)? Name common pooling frameworks (HikariCP, Apache Commons DBCP, C3P0) and key tunable parameters (pool size, connection idle timeout)
- Vertical scaling vs horizontal scaling — increasing a single machine's resources vs adding more machines
- What is database schema migration tooling (Flyway, Liquibase) used for, and how does it fit into a CI/CD pipeline?
- What is a unique constraint at the database level, and how does it help with data integrity / idempotency enforcement?
- Explain normalization briefly (2NF/3NF) and why denormalization is sometimes used to reduce joins and improve read performance in large systems

## 35. REST vs gRPC vs Messaging (Deep Dive)
- Compare REST (HTTP/JSON, request-response, easy to understand, generally more portable) vs gRPC (Google's RPC framework, HTTP/2, Protobuf-based, bidirectional streaming, faster/more efficient for internal service-to-service calls) vs Kafka/RabbitMQ (asynchronous, decoupled, event/message broker-based) — and when to use each
- What is the difference between Kafka as an "event broker" and RabbitMQ as a "message broker," conceptually, in terms of how messages are consumed and retained?
- Explain Feign client vs `RestTemplate` vs `WebClient` for inter-service calls — declarative interface-based calls (Feign) vs manual HTTP client (RestTemplate, now considered legacy) vs non-blocking reactive client (WebClient, for sync + async calls)

## 36. Service Discovery, API Gateway & Config Server (Live-Coding Detail)
- Walk through the exact registration flow for Eureka: services install a discovery client, register with the discovery server on startup with their host/port, and other services query the discovery server to resolve addresses dynamically instead of hardcoding URLs
- What is client-side load balancing, and how does it work when a service has multiple registered instances (the calling service receives multiple URLs from the discovery server and picks which instance to call itself)?
- What common cross-cutting responsibilities does an API Gateway typically own (routing, authentication/authorization, rate limiting, request/response aggregation from multiple services, logging/monitoring)?
- How does an API Gateway decide which downstream microservice to route a request to (URL path predicates, e.g., `/products/**` → product service, `/orders/**` → order service)?
- What is a Config Server, and how does centralizing configuration (rather than keeping it in each service) simplify configuration management across many microservices?

## 37. Java Fundamentals — Additional Deep-Dive Points
- How does Java achieve platform independence in practice (source → bytecode via `javac` → `.class` file → executed by the JVM on any platform)?
- Explain the class loader delegation model in detail: Bootstrap (loads `rt.jar` / core `java.lang`/`java.util` classes) → Extension (loads classes from the `JRE/lib/ext` directory) → System/Application (loads classes from your application's classpath) — each loader delegates to its parent first before attempting to load a class itself
- How does the Java String Pool optimize memory — string literals are stored/reused from the pool, while `new String(...)` explicitly creates a new object on the heap; comparing pooled references with `==` returns true, but comparing a pooled reference to a heap-allocated one returns false even with equal content
- Detailed comparison of `String` vs `StringBuffer` vs `StringBuilder` — immutability, thread safety (String is inherently thread-safe; StringBuffer is thread-safe via synchronized methods; StringBuilder is not thread-safe but faster), and performance implications for concatenation-heavy code
- Why/how are wrapper classes needed for working with Java Collections (which only accept objects, not primitives), and what's autoboxing/unboxing?
- Difference between static (class-level) variables and instance (object-level) variables — where each lives in memory (method area vs heap) and when each is initialized (class loading vs object creation)
- What is the `transient` keyword used for during serialization, and what happens to a `transient` field on deserialization (it gets the type's default value and must be manually reinitialized if needed)?
- What are the four access specifiers in Java (public, private, protected, default/package-private), and their visibility scopes?
- What is a marker interface (an interface with no methods/fields, e.g., `Serializable`, `Cloneable`) used to signal to the JVM/framework?
- Detailed abstract class vs interface comparison: method types allowed, instance variables vs constants-only, single vs multiple inheritance support, constructors, and default access modifiers for methods
- What is encapsulation, achieved via private fields + public getters/setters, and why does it matter for controlled access to internal state?
- Compile-time polymorphism (method overloading) vs runtime polymorphism (method overriding) — with a worked dog/animal example showing which overridden method actually executes based on the object's runtime type, not the reference type
- What is inheritance, and the difference between superclass/parent and subclass/child terminology; benefits (code reusability, faster development, easier maintenance)
- What is abstraction — hiding implementation details while exposing only necessary behavior, typically via abstract classes or interfaces

## 38. Java 8 Streams — Additional Coding Patterns from Live-Coding Demos
- Given a list of `Employee` objects, find the department-wise highest-paid employee using `Collectors.groupingBy(Employee::getDepartment, Collectors.maxBy(Comparator.comparing(Employee::getSalary)))`
- Given a list of `Employee` objects, find the department-wise average salary using `Collectors.groupingBy(Employee::getDepartment, Collectors.averagingDouble(Employee::getSalary))`
- Find duplicate elements in a list of integers using a `Set`'s `add()` return value inside a stream `filter()`
- Find the second-highest number in a list using `distinct()` → `sorted(Comparator.reverseOrder())` → `skip(1)` → `findFirst()`, wrapped in an `Optional`
- Count the number of vowels in a string using `chars()`/`stream()`, mapping to objects, and filtering against a vowel set
- Given a `Book` entity (title, author, year, price), using Java 8 streams: print titles published after a given year (filter by year), calculate total price of all books (`mapToDouble` + `sum`), find the most expensive book (`stream().max(Comparator.comparing(Book::getPrice))`), and create a filtered list of books under a price threshold
- Flatten a list-of-lists of strings into a single list using `flatMap()`, then filter for elements starting with a specific letter
- Filter even numbers from a list of integers using `stream().filter(i -> i % 2 == 0).collect(Collectors.toList())`
- What are terminal operations in Java Streams, with worked examples for each: `forEach` (perform an action per element), `collect` (gather results into a collection), `reduce` (combine all elements into a single result with an identity and accumulator), `count` (count matching elements), `findFirst` (retrieve the first matching element, wrapped in `Optional`), `allMatch`/`anyMatch`/`noneMatch` (boolean checks against a predicate), and `toArray` (convert a stream back into an array)
- Explain the difference between intermediate operations (lazy, return a new stream — `map`, `filter`, `sorted`, `distinct`) and terminal operations (eager, produce a final result and consume the stream)

## 39. SQL — Additional Query & Concept Examples
- Write a query using an inner join to fetch employee name along with department name from two tables (`employee` and `department`), joining on `department_id`
- What is a unique constraint, and how does it enforce that no two rows share the same value in a given column?
- What database migration tools (Flyway, Liquibase) are used for managing schema changes safely across environments and as part of CI/CD?

## 40. Immutable Class — Additional Worked Example
- Live-coding walkthrough of creating an immutable `Student` class: mark the class `final` (prevent subclassing), mark all fields `private final` (including a `List<String> subjects` field), initialize all fields only via the constructor, provide only getters (no setters), and — critically — for a mutable field like a `List`, return `Collections.unmodifiableList(subjects)` from the getter rather than the raw reference, so external code can't mutate internal state through the getter

---
*Compiled from 50+ interview transcript excerpts and mock-interview videos (English and Hindi-language) — spanning real "interview experience" recaps from companies including Genpact, Airtel, Capgemini, EPAM, Sopra Steria, Cognizant, EXL, Oracle, Delight/Deloitte, TCS, NTT Data, Infosys, Coforge, Tech Mahindra, Paytm, Walmart, HCL, HashedIn, Nagarro, IBM, Accenture, and others — plus structured mock-interview/training sessions (e.g., CloudTech-style Spring Boot & microservices deep dives) covering experience levels from fresher/1.5 years up to ~8.5 years senior engineer level.*
