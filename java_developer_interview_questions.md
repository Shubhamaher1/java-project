# Java Developer Interview Questions — Compiled from All Transcripts

## 1. Core Java / OOPs
- - What are the OOPs concepts? (Abstraction, Encapsulation, Inheritance, Polymorphism)
- - Explain abstraction with a real-time example (e.g., ATM machine interface)
- - What is data hiding? How is it different from abstraction?
- - Which of these examples is true data abstraction vs encapsulation (scenario-based MCQ: private field, getter/setter, protected class, abstract car class)
- - Types of access modifiers — public, private, default, protected
- - Explain private and protected access modifiers
- - Composition vs Aggregation
- - Write a short coding example for inheritance
- - Explain the Java memory model (heap, stack, string constant pool)
- - What is metaspace?
- - What is method overloading? Does changing return type only make it valid overloading?
- - If a method call is ambiguous due to overloaded methods with `null` argument, is it a compile-time or runtime error?
- - Is method overloading compile-time or runtime polymorphism?
- - Method overriding: can the return type in the child class differ (covariant return types)?
- - If the parent method throws a checked exception, does the child override need to declare it too?
- - Static (Overloading)  vs dynamic (Overriding ) polymorphism — which decides which method to call and when
- - What is default method inside an interface, and its relevance (backward compatibility)?
- - Two interfaces with the same default method signature implemented by one class — ambiguity problem and how to resolve it (override the method)
- - Is there a risk of the "diamond problem" with default methods?
- - What is data hiding — can you access data without getters/setters?
- - What is the use of `default` and `static` thods in a functional interface?
- - What are the 5 features of Java? Why is Java "simple"? Why is it "almost" object-oriented?
- - Why is Java a platform-independent language? Role of JVM.
- - What is JVM? (classloader, bytecode verifier)
- - What is JDK? What components does it contain?
- - What is the entry point of a Java program (`main` method)? Syntax of `main`.
- - Why is `main` method static?
- - What happens if you remove `static` from `main`? (compiles, fails at runtime)
- - What are default values assigned to variables/instances (int, float, double, String)?
- - What is a package in Java? Advantages of using packages?
- - Difference between array and ArrayList; how to add elements to an array
- - What is garbage collection? Objective/purpose of GC
- - New generation vs old generation in GC; types of garbage collectors
- - Explain your approach if you had to force/trigger garbage collection explicitly
- - What is a memory leak? How does it happen? How do you identify/debug one (e.g., unclosed DB/file connections)?
- - Difference between `final`, `finally`, and `finalize`
- - Can we declare an entity/class as final? What's the effect (can't extend / immutable)?

## 2. Java 8 Features & Streams
- - What are the Java 8 features you've used? (Lambda expressions, functional interfaces, Stream API, method references, Optional, default/static methods)
- - What is a Lambda expression? Why was it introduced?
- - What are functional interfaces? Relevance to lambda expressions
- - What is a method reference (`::`)? When would you use it?
- - What is the difference between `map()` and `flatMap()` in streams?
- - What is a parallel stream? Difference from sequential stream — which method enables it?
- - Which method internally sorts elements when inserting into a TreeMap (merge sort)?
- - What are terminal operations in the Stream API?
- - Write Stream API code to filter employees who joined after a given date
- - Write Stream API code to collect employee name → first address into a `Map<String, Address>`
- - Group employees by department and find the highest salary in each department using Collectors
- - Find total transaction amount per category for transactions in the last 30 days using streams (filter + groupingBy + summingDouble)
- - Fetch employees with salary greater than 50,000 using Stream API
- - Find the longest word in a sentence using Stream API
- - Count character frequency in a string using `chars()` + `groupingBy`/`counting()`

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

---
*Compiled from 16 interview transcript excerpts (English and Hindi-language mock/real interview videos) covering experience levels from ~1.5 to ~8.5 years.*
