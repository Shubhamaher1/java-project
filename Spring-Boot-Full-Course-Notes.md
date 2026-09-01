# Spring Boot Full Course 2026 — Complete Topic Reference
Source: [Spring-Framework-Full-Course by adityatandon15](https://github.com/adityatandon15/Spring-Framework-Full-Course) (Coder Army YouTube playlist "Spring Boot Full Course 2026", 40 videos).
Every concept below has a **Definition** followed by a **code snippet**.

---

## 1. Spring Framework & Spring Boot — The Big Picture (Lecture 1)

**Client–Server Architecture — Definition:** A **client** (browser, mobile app, Postman) asks for something; a **server** (your Spring Boot app) receives the request, processes it, and sends back a response. All web development is built on this request/response model.

**HTTP — Definition:** HyperText Transfer Protocol — the rulebook that defines how a request/response must look: which **method** is used, which **URL/path** is targeted, what **headers** carry (metadata like content type or auth tokens), and what the **body** contains (the actual payload, usually JSON).
```
POST /login
Content-Type: application/json

{ "email": "abc@gmail.com", "password": "12345" }
```
| Method | Meaning |
|---|---|
| GET | Read data |
| POST | Create data |
| PUT | Replace data completely |
| PATCH | Update data partially |
| DELETE | Remove data |

**Why Core Java alone isn't enough for the web — Definition:** Java's `java.net.ServerSocket` can open a port and receive raw TCP bytes, but the JVM has no built-in understanding of HTTP — someone must manually parse the method/URL/headers/body from that byte stream and route it to the right code. Doing this by hand for every request is repetitive, error-prone boilerplate.

**Servlet & Servlet Container — Definition:** A **Servlet** is a Java class designed to handle HTTP requests (`doGet()`, `doPost()`, etc.). A **Servlet Container** (e.g., Apache Tomcat, Jetty) sits between the raw network and your Java code — it opens the port, parses HTTP, builds request/response objects, manages threads, and invokes the right servlet method, so you never touch raw sockets yourself.
```
Browser --HTTP request--> Servlet Container (Tomcat) --parses & routes--> Servlet.doGet() --> your Java code
```

**Why Spring was needed — Definition:** Building large applications directly on raw Servlets led to heavy boilerplate, tight coupling, and hard-to-test code. The **Spring Framework** was created to organize object creation, manage dependencies, and reduce configuration overhead through **IoC**, **Dependency Injection**, and **Bean management**.

**The Spring ecosystem — Definition:** Spring is not one library but a family of projects, each solving a different concern:
| Module | Purpose |
|---|---|
| Spring Core | IoC container, Dependency Injection, Bean management (the foundation) |
| Spring MVC | Web apps & REST APIs, built on Servlets + Spring Core |
| Spring Data (JPA) | Database access, ORM, reduces JDBC boilerplate |
| Spring Security | Authentication, authorization, JWT, OAuth |
| Spring AOP | Cross-cutting concerns (logging, transactions) separated from business logic |
| Spring Boot | Auto-configuration layer on top of all the above |

**JDBC → Hibernate → Spring Data JPA — Definition:** **JDBC** requires manually writing SQL, opening connections, and mapping result sets. **Hibernate** is an ORM (Object-Relational Mapping) implementation of the **JPA** specification — it maps Java objects to database tables automatically. **Spring Data JPA** sits on top of Hibernate and eliminates even more boilerplate (auto-generated repository methods).
```
Spring Data JPA  →  Hibernate (a JPA implementation)  →  JDBC  →  Database
```

**Spring Boot vs Spring Framework — Definition:** Spring Boot is **not** a replacement for Spring — it's an opinionated automation layer on top of it, providing auto-configuration, starter dependencies, and an embedded server (e.g., Tomcat) so you can start writing business logic immediately instead of wiring everything by hand.
| Spring Framework | Spring Boot |
|---|---|
| Provides core features/modules | Provides auto-configuration & quick setup |
| Requires manual configuration | Reduces manual configuration via sensible defaults |
| Gives flexibility | Gives opinions (defaults) |

**Microservices — Definition:** An architecture style (not a Spring module) where a large application is split into small, independently deployable services (User Service, Order Service, etc.). Spring Boot is commonly used to build each service because it makes standalone, production-ready apps easy to create.

---

## 2. Your First Spring Boot Application & REST API (Lecture 2)

**`@SpringBootApplication` & the entry point — Definition:** The main class annotated with `@SpringBootApplication` bootstraps the entire application — it combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan` into one annotation, and `SpringApplication.run(...)` starts the embedded server and the IoC container.
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**`@RestController` & `@GetMapping` — Definition:** `@RestController` marks a class whose methods handle HTTP requests and return data directly as the response body (JSON by default), instead of a view name. `@GetMapping` maps an HTTP GET request on a given path to a method.
```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World";
    }
}
```
Running the app starts an **embedded Tomcat server** (bundled inside the JAR, no external server install needed) that listens on port 8080 by default.

---

## 3. Apache Maven (Lecture 3)

**Maven — Definition:** A build automation and dependency management tool for Java. Instead of manually downloading `.jar` files, you declare dependencies in `pom.xml` and Maven downloads them (with their transitive dependencies) from a remote repository.

**`pom.xml` (Project Object Model) — Definition:** The XML file describing a Maven project: its coordinates (groupId/artifactId/version), dependencies, and build plugins.
```xml
<project>
    <groupId>org.example</groupId>
    <artifactId>MavenDemo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>6.1.0</version>
        </dependency>
    </dependencies>
</project>
```

**Maven Lifecycle & common commands — Definition:** Maven executes a fixed sequence of **phases**; running a later phase runs all earlier ones first.
```bash
mvn compile    # compiles source code
mvn test        # runs unit tests
mvn package      # produces a .jar/.war in target/
mvn install       # installs the artifact into the local repo (~/.m2)
```

**Dependency scope & transitive dependencies — Definition:** Maven automatically pulls in the dependencies-of-your-dependencies (transitive), and a `<scope>` (e.g., `test`, `provided`) controls when a dependency is available (compile-time only, test-only, etc.).

---

## 4. Dependency Injection & IoC — The Problem (Lecture 4)

**Tight Coupling — Definition:** When a class directly creates (`new`s) the objects it depends on, it becomes hard-wired to a specific implementation — swapping implementations or unit-testing in isolation becomes difficult.
```java
class NotificationService {
    private EmailService emailService = new EmailService(); // tightly coupled to EmailService
    void notifyUser() { emailService.send(); }
}
```

**Dependency Injection (DI) — Definition:** Instead of a class creating its own dependencies, they are **injected** (supplied) from outside — typically through the constructor. This decouples "what a class needs" from "how that thing is created."
```java
class NotificationService {
    private final EmailService emailService;
    NotificationService(EmailService emailService) { this.emailService = emailService; } // injected, not created
    void notifyUser() { emailService.send(); }
}
```

**Inversion of Control (IoC) — Definition:** A broader principle where the **control of object creation and wiring is inverted** — instead of your code controlling when/how objects are made, a container (framework) does it for you. DI is the specific technique Spring uses to achieve IoC.

**Loose Coupling via interfaces — Definition:** Depending on an abstraction (interface) rather than a concrete class lets implementations be swapped without changing the dependent class.
```java
interface NotificationService { void send(); }
class EmailService implements NotificationService { public void send() { /* ... */ } }
class SmsService implements NotificationService { public void send() { /* ... */ } }

class OrderService {
    private final NotificationService notificationService; // depends on the abstraction, not a concrete class
    OrderService(NotificationService notificationService) { this.notificationService = notificationService; }
}
```

---

## 5. Spring IoC Container: Beans, `@Component`, `@Autowired`, `@Bean` (Lecture 5)

**IoC Container / `ApplicationContext` — Definition:** The core Spring object that creates, configures, wires, and manages the complete lifecycle of application objects (**beans**). You never call `new` for these objects — the container does.

**Bean — Definition:** Any object whose lifecycle (creation, wiring, destruction) is managed by the Spring IoC container.

**`@Component` & `@ComponentScan` — Definition:** `@Component` marks a class to be auto-detected and registered as a bean. `@ComponentScan` (implicitly included by `@SpringBootApplication`) tells Spring which packages to scan for such annotated classes.
```java
@Component
public class CartService { /* Spring auto-registers this as a bean */ }

@Configuration
@ComponentScan("in.coderarmy")
public class AppConfig { }
```

**`@Autowired` — Definition:** Tells Spring to automatically inject a matching bean into a field, constructor, or setter — resolved by type (and by name/qualifier if there are multiple matches).
```java
@Component
public class OrderService {
    @Autowired
    private PaymentService paymentService; // Spring injects the matching bean automatically
}
```

**`@Configuration` & `@Bean` — Definition:** `@Configuration` marks a class as a source of bean definitions. `@Bean` on a method inside it tells Spring to call that method and register its return value as a bean — used when you need to configure third-party classes you don't own, or want explicit control over construction.
```java
@Configuration
public class AppConfig {
    @Bean
    public OrderService createOrderService(PaymentService paymentService) {
        return new OrderService(paymentService); // explicit construction, still container-managed
    }
}
```

**`@Qualifier` & `@Primary` — Definition:** When multiple beans of the same type exist, Spring can't pick one automatically. `@Qualifier("name")` disambiguates by an explicit bean name; `@Primary` marks one candidate as the default choice when no qualifier is given.
```java
@Bean @Qualifier("cardPayment")
public PaymentService cardPayment() { return new CardPayment(); }

@Bean @Qualifier("upiPayment")
public PaymentService upiPayment() { return new UpiPayment(); }

@Bean
public OrderService orderService(@Qualifier("cardPayment") PaymentService paymentService) {
    return new OrderService(paymentService); // explicitly picks the card-payment bean
}
```

---

## 6. Circular Dependency, Bean Scope, Lazy & Eager Beans (Lecture 6)

**Circular Dependency — Definition:** When Bean A depends on Bean B, and Bean B depends on Bean A — Spring can resolve this for **field/setter injection** (it creates a half-initialized bean and fills the field afterward) but **cannot** resolve it for pure **constructor injection**, which throws `BeanCurrentlyInCreationException` at startup.
```java
@Component
class OrderService {
    @Autowired private PaymentService paymentService; // field injection -> circular dependency CAN be resolved
}
@Component
class PaymentService {
    @Autowired private OrderService orderService;       // this pairing would fail if BOTH used constructor injection
}
```
A circular dependency is usually a design smell — the real fix is to break the cycle (e.g., extract shared logic into a third service) rather than relying on field-injection resolution.

**Bean Scope — Definition:** Controls how many instances of a bean the container creates and how they're shared.
```java
@Component
@Scope("singleton")   // DEFAULT: exactly ONE shared instance for the entire application context
public class ConfigService { }

@Component
@Scope("prototype")   // a NEW instance is created every time the bean is requested/injected
public class CartService { }
```
Other scopes exist for web apps: `request` (one bean per HTTP request) and `session` (one bean per HTTP session).

**Eager vs Lazy Initialization — Definition:** By default, **singleton** beans are created **eagerly** at application startup. `@Lazy` defers creation until the bean is first actually requested — useful for expensive beans that aren't always needed.
```java
@Component
@Lazy
public class ExpensiveReportService {
    public ExpensiveReportService() { System.out.println("Created only when first needed"); }
}
```

---

## 7. Bean Lifecycle: `@PostConstruct`, `@PreDestroy`, Scopes & `@Lazy` (Lecture 7)

**Bean Lifecycle — Definition:** The sequence a managed bean goes through: instantiation → dependency injection → **post-initialization callback** → ready for use → **pre-destruction callback** (on container shutdown, singleton beans only).
```java
@Component
public class CartService {
    public CartService() { System.out.println("1. Constructor called"); }

    @PostConstruct
    public void init() { System.out.println("2. Bean is ready — run setup logic here"); }

    @PreDestroy
    public void cleanup() { System.out.println("3. Bean is being destroyed — release resources here"); }
}
```

**Aware interfaces — Definition:** Marker interfaces a bean can implement to have the container inject framework-level context, such as its own bean name or the `ApplicationContext` itself.
```java
@Component
public class CartService implements BeanNameAware, ApplicationContextAware {
    @Override public void setBeanName(String name) { System.out.println("My bean name is " + name); }
    @Override public void setApplicationContext(ApplicationContext ctx) { /* store or use the context */ }
}
```

**`InitializingBean` / `DisposableBean` — Definition:** Older, interface-based alternatives to `@PostConstruct`/`@PreDestroy` — implementing them ties your class directly to the Spring API (less preferred than annotations, which keep the class framework-agnostic).
```java
@Component
public class LegacyService implements InitializingBean, DisposableBean {
    @Override public void afterPropertiesSet() { /* like @PostConstruct */ }
    @Override public void destroy() { /* like @PreDestroy */ }
}
```

**`@Bean(initMethod=..., destroyMethod=...)` — Definition:** For beans defined via `@Bean` methods (e.g., from a third-party class you can't annotate directly), you can name existing methods to call at init/destroy time instead of using annotations.
```java
@Bean(initMethod = "start", destroyMethod = "stop")
public CartService cartService() { return new CartService(); }
```

---

## 8. Spring XML Configuration (Lecture 8)

**XML-based Bean Configuration — Definition:** The original way to declare beans and wire dependencies, before annotations existed — beans and their properties/constructor-args are declared in an XML file instead of Java code.
```xml
<!-- beans.xml -->
<beans xmlns="http://www.springframework.org/schema/beans" ...>
    <bean id="cardPaymentService" class="in.strikes.payment.CardPaymentService" />

    <bean id="orderService" class="in.strikes.OrderService">
        <constructor-arg ref="cardPaymentService" />
    </bean>
</beans>
```
```java
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
OrderService orderService = context.getBean(OrderService.class);
```
Modern Spring Boot apps almost always use annotation/Java-based configuration instead — XML config is mostly seen in legacy codebases, but understanding it clarifies that "beans" are just container-managed objects, regardless of how they're declared.

---

## 9. Spring Boot Core: `@SpringBootApplication` & Auto-Configuration (Lecture 9)

**Auto-Configuration — Definition:** Spring Boot inspects what's on the classpath (which starter dependencies you added) and **automatically configures** matching beans — e.g., adding `spring-boot-starter-web` auto-configures an embedded Tomcat, a `DispatcherServlet`, and JSON message converters, with zero manual XML/Java config required.
```java
@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
```

**`@EnableAutoConfiguration` — Definition:** The specific annotation (bundled inside `@SpringBootApplication`) that triggers Spring Boot's classpath-scanning auto-configuration mechanism, based on conditional annotations like `@ConditionalOnClass`.

**Starter dependencies — Definition:** Curated dependency bundles (e.g., `spring-boot-starter-web`, `spring-boot-starter-data-jpa`) that pull in a compatible, version-tested set of libraries for a given concern, so you don't have to hand-pick and version-match every jar yourself.

---

## 10. `application.properties`, `@Value`, `@ConfigurationProperties` & `CommandLineRunner` (Lecture 10)

**`application.properties` — Definition:** The default externalized configuration file for a Spring Boot app — key/value pairs read at startup, letting you change behavior without recompiling code.
```properties
server.port=8081
payment.gateway.url=https://api.payments.example.com
payment.gateway.timeout=5000
```

**`@Value` — Definition:** Injects a single property value directly into a field, with an optional default if the key is absent.
```java
@Component
public class PaymentGateway {
    @Value("${payment.gateway.url}")
    private String gatewayUrl;

    @Value("${payment.gateway.retries:3}") // ":3" is the default if the property is missing
    private int retries;
}
```

**`@ConfigurationProperties` — Definition:** Binds a whole group of related properties (sharing a prefix) into a strongly-typed Java object in one shot, instead of many individual `@Value` fields.
```java
@Component
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentProperties {
    private String url;
    private int timeout;
    // getters and setters — Spring binds "payment.gateway.url" -> url, "payment.gateway.timeout" -> timeout
}
```

**`CommandLineRunner` — Definition:** A functional interface for code that should run **once, right after** the Spring application context has fully started — commonly used for startup checks, seeding data, or quick demos.
```java
@Component
public class DemoRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("Application started — running startup logic here");
    }
}
```

---

## 11. Building a CRUD REST API from Scratch — Layered Architecture (Lecture 11)

**Layered Architecture — Definition:** Separating an application into distinct responsibility layers so each layer only knows about the one directly below it — improves testability and maintainability.
```
Controller (HTTP layer)  →  Service (business logic)  →  Repository (data access)  →  Database
```

**`@RestController` + mapping annotations — Definition:** The controller layer exposes HTTP endpoints and delegates real work to the service layer.
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    StudentController(StudentService studentService) { this.studentService = studentService; }

    @PostMapping
    public Student create(@RequestBody Student student) { return studentService.create(student); }

    @GetMapping("/{id}")
    public Student getById(@PathVariable Long id) { return studentService.getById(id); }
}
```

**`@Entity` — Definition:** Marks a plain Java class as a JPA-managed persistent entity, mapped to a database table.
```java
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int age;
    // getters and setters
}
```

**`JpaRepository` — Definition:** A Spring Data interface that, once extended, gives you CRUD methods (`save`, `findById`, `findAll`, `deleteById`, ...) for free — no implementation code needed, Spring generates it at runtime.
```java
public interface StudentRepository extends JpaRepository<Student, Long> { }
```

**Service layer — Definition:** Holds business logic and orchestrates repository calls, keeping the controller thin and the persistence details out of the HTTP layer.
```java
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    StudentService(StudentRepository studentRepository) { this.studentRepository = studentRepository; }

    public Student create(Student student) { return studentRepository.save(student); }
    public Student getById(Long id) { return studentRepository.findById(id).orElseThrow(); }
}
```

---

## 12. CRUD with MySQL & Spring Data JPA Query Methods (Lecture 12)

**Connecting to MySQL — Definition:** Configuring the datasource URL/credentials and JPA/Hibernate behavior in `application.properties` so Spring Boot auto-configures a real database connection instead of an in-memory one.
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/school_db
spring.datasource.username=root
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update   # auto create/update tables from your @Entity classes
spring.jpa.show-sql=true                # logs generated SQL, useful for learning/debugging
```

**Derived Query Methods — Definition:** Spring Data JPA can generate the query implementation just from a method's **name**, following a keyword convention (`findBy`, `And`, `OrderBy`, etc.) — no SQL or JPQL needs to be written.
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByName(String name);                 // WHERE name = ?
    List<Student> findByAgeGreaterThan(int age);              // WHERE age > ?
    Optional<Student> findByNameAndAge(String name, int age);  // WHERE name = ? AND age = ?
    boolean existsByName(String name);                          // SELECT EXISTS(...)
}
```

---

## 13. Soft Delete & Custom JPA Queries (Lecture 13)

**Soft Delete — Definition:** Instead of physically removing a row (`DELETE`), you mark it as deleted with a flag/timestamp column, so the data is preserved for auditing/recovery and "deleted" records can be excluded from normal queries.
```java
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean deleted = false;   // soft-delete flag instead of an actual row removal
}
```
```java
@Service
public class StudentService {
    public void softDelete(Long id) {
        Student student = studentRepository.findById(id).orElseThrow();
        student.setDeleted(true);
        studentRepository.save(student);  // UPDATE, not DELETE
    }
}
```

**`@Query` — Definition:** Lets you write a custom JPQL (or native SQL) query explicitly, for logic too complex to express via a derived method name.
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT s FROM Student s WHERE s.deleted = false")
    List<Student> findAllActive();

    @Query(value = "SELECT * FROM student WHERE deleted = false", nativeQuery = true)
    List<Student> findAllActiveNative();
}
```

---

## 14. How Java Web Apps Actually Work: Servlets, Tomcat & WAR Files (Lecture 14)

**Raw Servlet — Definition:** A class extending `HttpServlet` and overriding lifecycle methods like `doGet()`/`doPost()` to handle a specific URL, registered manually — this is the low-level mechanism Spring MVC builds on top of.
```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Hello from a raw Servlet");
    }
}
```

**Servlet lifecycle — Definition:** A Servlet Container creates **one instance** of a servlet and reuses it (calling `service()` → `doGet()`/`doPost()`) for every matching request — `init()` runs once at startup, `destroy()` once at shutdown.

**WAR (Web Application Archive) — Definition:** A packaging format for deploying a Java web app onto an **external** servlet container (like a standalone Tomcat install), as opposed to Spring Boot's default executable JAR with an **embedded** server.
```xml
<packaging>war</packaging>
```
```bash
mvn package             # produces target/app.war
# deploy app.war into Tomcat's webapps/ folder for the container to pick up
```

---

## 15. Spring MVC Architecture: DispatcherServlet, HandlerMapping & JSP Flow (Lecture 15)

**`DispatcherServlet` — Definition:** The single "front controller" servlet that Spring MVC registers to intercept **all** incoming requests, then delegates each one to the right controller method — this is what auto-configuration wires up for you in Spring Boot.
```
Request → DispatcherServlet → HandlerMapping (finds matching controller method)
        → Controller executes → returns data/view name → DispatcherServlet sends the response
```

**`HandlerMapping` — Definition:** The component that matches an incoming request's URL/method to the correct controller method, based on annotations like `@GetMapping("/hello")`.

**JSP (JavaServer Pages) — Definition:** An older server-side templating technology for generating HTML dynamically; a controller returns a **view name**, which Spring MVC resolves to a `.jsp` file to render (as opposed to `@RestController`, which returns raw data/JSON directly).
```java
@Controller  // NOT @RestController — this one returns a VIEW NAME, not raw data
public class HelloController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Hello from JSP");
        return "home"; // resolves to /WEB-INF/views/home.jsp
    }
}
```

---

## 16. DTOs & Bean Validation (Lecture 16)

**DTO (Data Transfer Object) — Definition:** A plain object shaped specifically for what an API request/response should look like, decoupled from the internal `@Entity` structure — prevents leaking database columns you don't want exposed, and lets request/response shapes evolve independently of the persistence model.
```java
public class CreateStudentRequestDto {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Must be at least 18 years old")
    private Integer age;

    @NotBlank @Email(message = "Must be a valid email")
    private String email;
    // getters and setters
}
```

**Bean Validation (`jakarta.validation`) — Definition:** Declarative constraint annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Email`, ...) placed on DTO fields, automatically checked by Spring when `@Valid` is used on a controller parameter.
```java
@PostMapping
public CreateStudentResponseDto create(@Valid @RequestBody CreateStudentRequestDto request) {
    // if validation fails, Spring throws MethodArgumentNotValidException BEFORE this method body runs
    return studentService.create(request);
}
```

**Mapping between DTO and Entity — Definition:** Manually (or via a mapping library) converting an incoming request DTO into the persistence `@Entity`, and the saved `@Entity` back into a response DTO — keeps the two models independently evolvable.
```java
Student student = new Student(request.getName(), request.getAge(), request.getEmail());
Student saved = studentRepository.save(student);
return new CreateStudentResponseDto(saved.getId(), saved.getName());
```

---

## 17. Global Exception Handling (Lecture 17)

**Custom Exceptions — Definition:** Domain-specific exception classes that clearly communicate *what* went wrong, instead of throwing generic `RuntimeException`s everywhere.
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}
```

**`@RestControllerAdvice` + `@ExceptionHandler` — Definition:** `@RestControllerAdvice` marks a class as a **global** exception handler for every `@RestController` in the app. `@ExceptionHandler(SomeException.class)` on a method inside it defines how to turn that specific exception into an HTTP response — centralizing error-handling logic instead of repeating try/catch in every controller method.
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        ExceptionResponseDto body = new ExceptionResponseDto(
            LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // thrown by failed @Valid checks
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(fieldErrors);
    }

    @ExceptionHandler(Exception.class) // catch-all fallback for anything unhandled
    public ResponseEntity<String> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError().body("Something went wrong");
    }
}
```
`@ExceptionHandler` methods are matched **most-specific-first** — a handler for `ResourceNotFoundException` wins over a broader `RuntimeException` handler for the same exception.

---

## 18. Profiles & YAML Configuration (Lecture 18)

**Spring Profiles — Definition:** Named configuration environments (e.g., `dev`, `staging`, `prod`) that let you swap property values or entire bean implementations depending on which profile is active — without changing code.
```properties
# application.properties
spring.profiles.active=dev
```
```properties
# application-dev.properties       (only loaded when the "dev" profile is active)
notification.enabled=false
# application-prod.properties      (only loaded when the "prod" profile is active)
notification.enabled=true
```

**`@Profile` — Definition:** Restricts a bean's registration to a specific active profile — commonly used to swap real vs. fake/dummy implementations of an interface between environments.
```java
public interface NotificationService { void send(String message); }

@Service @Profile("prod")
public class NotificationServiceImpl implements NotificationService {
    public void send(String message) { /* actually calls a real SMS/email provider */ }
}

@Service @Profile("dev")
public class DummyNotificationServiceImpl implements NotificationService {
    public void send(String message) { System.out.println("DEV MODE: pretending to send " + message); }
}
```

**YAML configuration (`application.yml`) — Definition:** An alternative, more compact and hierarchical format to `.properties` for the same configuration data — often preferred for deeply nested config or profile-specific sections in a single file.
```yaml
spring:
  profiles:
    active: dev
---
spring:
  config:
    activate:
      on-profile: prod
notification:
  enabled: true
```

---

## 19. Servlet Filters: FilterChain, Logging & Auth (Lecture 19)

**Servlet Filter — Definition:** A component that intercepts **every** matching HTTP request/response **before it reaches** (and after it leaves) the servlet/controller — implemented via the raw `jakarta.servlet.Filter` interface, operating below Spring MVC at the servlet-container level.
```java
@Component
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("Incoming request: " + ((HttpServletRequest) request).getRequestURI());
        chain.doFilter(request, response); // MUST call this to pass control to the next filter/servlet
        System.out.println("Response status: " + ((HttpServletResponse) response).getStatus());
    }
}
```

**`FilterChain` — Definition:** Represents the pipeline of filters a request passes through; calling `chain.doFilter(request, response)` hands control to the **next** filter (or the servlet itself if this is the last one) — omitting this call short-circuits the request entirely.

**Authentication Filter example — Definition:** A common real use: reject unauthenticated requests **before** they ever reach a controller, by checking a token header and short-circuiting with an error response if missing/invalid.
```java
@Component
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        if (httpReq.getHeader("token") == null) {
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return; // chain.doFilter() is NOT called -> request stops here
        }
        chain.doFilter(req, res);
    }
}
```

---

## 20. Filters: Modifying Requests, Responses, Headers & Bodies (Lecture 20)

**`FilterRegistrationBean` — Definition:** Gives explicit control over how/where a filter is registered — its URL patterns and execution **order** — instead of relying on `@Component` auto-registration (which applies it to every URL with no controllable order).
```java
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<DummyFilter> dummyFilterRegistration() {
        FilterRegistrationBean<DummyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new DummyFilter());
        registration.addUrlPatterns("/api/*");   // only applies to /api/** requests
        registration.setOrder(1);                 // lower number = runs earlier in the chain
        return registration;
    }
}
```

**Wrapping request/response to modify them — Definition:** Because the raw `HttpServletRequest`/`HttpServletResponse` streams can only be read **once**, modifying headers or the body requires wrapping them in a custom subclass (e.g., extending `HttpServletRequestWrapper`) that can be read multiple times or altered before being passed down the chain.
```java
public class ResponseHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpRes = (HttpServletResponse) res;
        httpRes.setHeader("X-App-Version", "1.0");  // add a custom header to EVERY response
        chain.doFilter(req, res);
    }
}
```

---

## 21. Interceptors: `preHandle`, `postHandle` & `afterCompletion` (Lecture 21)

**`HandlerInterceptor` — Definition:** A Spring-MVC-level (not raw-servlet-level) interception mechanism, sitting **inside** the `DispatcherServlet`, with access to Spring-specific context like the resolved controller method — richer than a plain Filter, but only applicable to requests that reach Spring MVC.
```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        System.out.println("Before controller method runs: " + req.getRequestURI());
        return true; // false would stop the request from reaching the controller
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mav) {
        System.out.println("After controller method runs, before the response is sent");
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        System.out.println("After the FULL response is sent (even if an exception occurred)");
    }
}
```

**Filter vs Interceptor — Definition:** A Filter operates at the raw **servlet container** level (sees every request, including static resources, no Spring context); an Interceptor operates **inside** Spring MVC's `DispatcherServlet` (only sees requests routed to a controller, has access to the matched `HandlerMethod`).

**`WebMvcConfigurer` & registration order — Definition:** Interceptors are registered via a `WebMvcConfigurer`, with path patterns to include/exclude and an explicit execution `.order(...)`.
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthenticationInterceptor authInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    public WebConfig(AuthenticationInterceptor a, LoggingInterceptor l) { this.authInterceptor = a; this.loggingInterceptor = l; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login")
                .order(1);
        registry.addInterceptor(loggingInterceptor).order(2);
    }
}
```

---

## 22. Spring AOP Introduction: Cross-Cutting Concerns & Proxies (Lecture 22)

**Cross-cutting concern — Definition:** A piece of functionality (logging, execution-time tracking, security checks, transactions) needed across **many unrelated** classes/methods — if implemented by hand in each one, it duplicates code and tangles business logic with infrastructure concerns.

**The problem AOP solves (manual Decorator pattern) — Definition:** Before AOP, you'd manually wrap a class with a "decorator" that adds the cross-cutting behavior around calls to the real implementation — functional, but requires writing a new decorator for every service you want to instrument.
```java
public interface StudentService { Student createStudent(Student s); }

public class StudentServiceImpl implements StudentService {
    public Student createStudent(Student s) { /* real business logic */ return s; }
}

public class LoggingDecorator implements StudentService { // manually wraps the real service
    private final StudentService delegate;
    public LoggingDecorator(StudentService delegate) { this.delegate = delegate; }
    public Student createStudent(Student s) {
        System.out.println("About to create student");
        Student result = delegate.createStudent(s);
        System.out.println("Student created");
        return result;
    }
}
```

**AOP (Aspect-Oriented Programming) — Definition:** A programming paradigm that lets you declare cross-cutting behavior **once**, separately from business logic, and have the framework automatically "weave" it into all matching methods — Spring implements this via runtime **proxies** wrapping your beans.

**Proxy — Definition:** A generated stand-in object that sits in front of the real bean; calls to the bean actually go through the proxy first, which is how Spring AOP inserts logging/timing/security logic without touching your original class's code.

---

## 23. AOP Advice Types: `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around` (Lecture 23)

**`@Aspect` — Definition:** Marks a class as containing AOP advice; combined with `@Component` so Spring registers it and applies its advice to matching methods elsewhere in the app.

**Pointcut expression (`execution(...)`) — Definition:** A string pattern identifying **which** method(s) the advice should apply to — by return type, package, class, method name, and parameters.
```java
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* in.strikes.service.StudentService.createStudent(..))") // runs BEFORE the target method
    public void logBefore(JoinPoint jp) {
        System.out.println("About to call: " + jp.getSignature().getName());
    }

    @AfterReturning(value = "execution(* in.strikes.service.StudentService.createStudent(..))", returning = "result")
    public void logAfterReturning(Student result) { // runs after SUCCESSFUL return, can inspect the result
        System.out.println("Method returned: " + result);
    }

    @AfterThrowing(value = "execution(* in.strikes.service.StudentService.createStudent(..))", throwing = "ex")
    public void logAfterThrowing(RuntimeException ex) { // runs only if the method THROWS
        System.out.println("Method failed: " + ex.getMessage());
    }

    @After("execution(* in.strikes.service.StudentService.createStudent(..))") // runs regardless of outcome
    public void logAfter() {
        System.out.println("Method execution finished (success or failure)");
    }
}
```

**`@Around` & `ProceedingJoinPoint` — Definition:** The most powerful advice type — wraps the **entire** method call, giving full control to run code before/after, inspect or modify arguments and the return value, or even skip/retry the call by controlling when (or whether) `joinPoint.proceed()` is invoked.
```java
@Around("execution(* in.strikes.service.StudentService.dummyMethod(..))")
public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    try {
        return joinPoint.proceed(); // actually invokes the real target method
    } finally {
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms");
    }
}
```

---

## 24. AOP Pointcuts & Types of Proxies (Lecture 24)

**Reusable named Pointcuts — Definition:** Instead of repeating a long `execution(...)` string in every advice, `@Pointcut` lets you declare it once under a memorable method name and reference it (even combine several with `&&`/`||`) from multiple advices.
```java
public class ApplicationPointcuts {
    @Pointcut("within(in.strikes.aopDemo.controller..*)")   // matches any method in this package (and subpackages)
    public void controllerLayer() { }

    @Pointcut("within(in.strikes.aopDemo.service..*)")
    public void serviceLayer() { }

    @Pointcut("execution(public * *(..))")                    // matches any PUBLIC method, any return type/name/args
    public void publicMethod() { }

    @Pointcut("serviceLayer() && publicMethod()")              // COMBINES two pointcuts
    public void publicServiceMethod() { }
}
```
```java
@Aspect @Component
public class LoggingAspect {
    @Before("in.strikes.aopDemo.aspect.ApplicationPointcuts.publicServiceMethod()")
    public void log(JoinPoint jp) { System.out.println("Public service method called: " + jp.getSignature()); }
}
```

**JDK Dynamic Proxy vs CGLIB Proxy — Definition:** Spring AOP creates proxies one of two ways: a **JDK dynamic proxy** (implements the same interface as the target — used when the target class implements at least one interface) or a **CGLIB proxy** (a runtime-generated subclass of the target class — used when there's no interface). This is why AOP won't intercept calls on a class with no interface unless CGLIB proxying is available, and why `final` classes/methods can't be proxied by CGLIB.
```java
public interface StudentServiceInterface { Student createStudent(Student s); }
// StudentService implementing an interface -> Spring AOP can use a JDK dynamic proxy
```

---

## 25. Custom Annotations in Spring AOP (Lecture 25)

**Custom Annotation — Definition:** A user-defined annotation (`@interface`) that can carry metadata (via its elements), used to **mark** methods that should receive specific AOP behavior — cleaner than matching by package/class name when the marked methods span multiple unrelated classes.
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // MUST be RUNTIME for AOP to see it via reflection
public @interface TrackExecutionTime {
    long warnAfter() default 2000;   // custom elements act like configurable "parameters" on the annotation
    String operation() default "";
}
```

**`@annotation(...)` pointcut — Definition:** A pointcut expression that matches any method **carrying a specific annotation**, regardless of which class it's in.
```java
@Aspect @Component
public class SimpleAspect {
    @Around("@annotation(trackExecutionTime)") // matches ANY method annotated with @TrackExecutionTime
    public Object measure(ProceedingJoinPoint joinPoint, TrackExecutionTime trackExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (duration >= trackExecutionTime.warnAfter()) {
                System.out.println("SLOW OPERATION: " + duration + "ms");
            }
        }
    }
}
```
```java
@Service
public class StudentService {
    @TrackExecutionTime(warnAfter = 500, operation = "createStudent") // just annotate the method — no aspect wiring needed here
    public Student createStudent(Student s) { /* ... */ return s; }
}
```

---

## 26. JDBC From Scratch (Lecture 26)

**Raw JDBC — Definition:** The lowest-level, standard Java API for talking to a relational database — manually opening a `Connection`, building a `PreparedStatement`, executing it, and mapping the `ResultSet` back into objects. This is exactly the boilerplate that Spring JDBC/Hibernate exist to eliminate.
```java
public class StudentRepository {
    public Student findById(int id) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/school";
        try (Connection conn = DriverManager.getConnection(url, "root", "password");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM student WHERE id = ?")) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(rs.getInt("id"), rs.getString("name"));  // manual row -> object mapping
                }
            }
        }
        return null;
    }
}
```
Every query repeats this same connection/statement/result-set boilerplate and manual resource cleanup — a strong motivator for Spring JDBC's `JdbcTemplate`.

---

## 27. Spring JDBC: `JdbcTemplate`, `DataSource` & HikariCP (Lecture 27)

**`JdbcTemplate` — Definition:** A Spring helper class that eliminates JDBC boilerplate (opening/closing connections, exception translation) while still letting you write raw SQL — a middle ground between manual JDBC and full ORM.
```java
@Repository
public class StudentRepository {
    private final JdbcTemplate jdbcTemplate;
    StudentRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Student findById(int id) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM student WHERE id = ?",
            new StudentRowMapper(),
            id
        );
    }
}
```

**`RowMapper<T>` — Definition:** A callback interface telling `JdbcTemplate` how to convert **one row** of a `ResultSet` into a domain object.
```java
public class StudentRowMapper implements RowMapper<Student> {
    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Student(rs.getInt("id"), rs.getString("name"));
    }
}
```

**`DataSource` & Connection Pooling (HikariCP) — Definition:** A `DataSource` is a factory for database connections. Opening a fresh TCP connection per query is slow, so Spring Boot auto-configures **HikariCP** (the default connection pool) — a set of pre-opened, reused connections — dramatically improving throughput under load.
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
```

---

## 28. Hibernate Fundamentals: CRUD & Entity Mapping (Lecture 28)

**ORM (Object-Relational Mapping) — Definition:** A technique that automatically translates between Java objects and relational database rows — Hibernate is the most widely used ORM implementation of the JPA specification.

**Entity mapping annotations — Definition:** Annotations that describe how a class and its fields map to a table and its columns.
```java
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)      // stores the enum's NAME (e.g., "ACTIVE"), not its ordinal number
    private StudentStatus status;

    @Embedded                          // inlines another class's fields into THIS table (no separate table/join)
    private Address address;
}

public enum StudentStatus { ACTIVE, GRADUATED, SUSPENDED }

@Embeddable
public class Address {
    private String city;
    private String zipCode;
}
```

**`AttributeConverter` — Definition:** A custom bidirectional mapping between a Java field type and its stored database column type/format, for cases the built-in mappings don't cover.
```java
@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {
    @Override public String convertToDatabaseColumn(Boolean attribute) { return attribute ? "Y" : "N"; }
    @Override public Boolean convertToEntityAttribute(String dbData) { return "Y".equals(dbData); }
}
```

---

## 29. Hibernate Internals: Persistence Context, L1 Cache & Transactions (Lecture 29)

**Persistence Context — Definition:** A per-transaction "session" that tracks every entity Hibernate has loaded or saved during that unit of work — it's the mechanism behind automatic dirty checking and the first-level cache.

**First-Level (L1) Cache — Definition:** Built into the Persistence Context itself (cannot be disabled) — if you request the **same entity by ID** twice within the same transaction/session, Hibernate returns the already-loaded object from memory instead of hitting the database again.
```java
@Transactional
public void demoL1Cache() {
    Student s1 = studentRepository.findById(1L).get(); // hits the DATABASE
    Student s2 = studentRepository.findById(1L).get(); // returns the SAME object from the L1 cache — no 2nd query
    System.out.println(s1 == s2); // true — same object reference
}
```

**Dirty Checking & automatic flush — Definition:** Hibernate compares a managed entity's current field values against a snapshot taken when it was loaded; if it detects a difference, it automatically generates and issues an `UPDATE` at flush/commit time — **without an explicit `.save()` call**.
```java
@Transactional
public void updateName(Long id, String newName) {
    Student student = studentRepository.findById(id).get(); // managed entity, tracked by the persistence context
    student.setName(newName);                                  // just modify the field...
    // ...no explicit save() needed — Hibernate detects the change and issues an UPDATE automatically at commit
}
```

**Entity states — Definition:** An entity moves through **Transient** (a plain `new`'d object, not yet tracked), **Managed/Persistent** (tracked by the persistence context, changes auto-sync to the DB), **Detached** (was managed, but the session/transaction ended), and **Removed** (marked for deletion).

---

## 30. JPA Relationships: `@OneToMany`, `@ManyToOne`, `@OneToOne` & `@ManyToMany` (Lecture 30)

**`@ManyToOne` / `@OneToMany` — Definition:** Models a "many rows relate to one row" relationship (e.g., many Students belong to one Department). The **owning side** (holding the foreign key column) uses `@ManyToOne` + `@JoinColumn`; the inverse side uses `@OneToMany(mappedBy = "...")`.
```java
@Entity
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department")          // inverse side — "department" = the field name on Student
    private List<Student> students = new ArrayList<>();
}

@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne                                     // owning side — this table gets the foreign key column
    @JoinColumn(name = "dept_id")
    private Department department;
}
```

**`@OneToOne` — Definition:** Models a strict one-to-one relationship (e.g., one User has exactly one Profile) — the owning side again uses `@JoinColumn` to hold the foreign key.
```java
@Entity
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
```

**`@ManyToMany` — Definition:** Models a relationship where many rows on each side relate to many rows on the other (e.g., Students enroll in many Courses, each Course has many Students) — implemented via a hidden **join table** that Hibernate manages automatically.
```java
@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "courses") // inverse side
    private List<Student> students = new ArrayList<>();
}

@Entity
public class Student {
    // ...
    @ManyToMany
    @JoinTable(
        name = "student_course",                                   // the hidden join table
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}
```

---

## 31. JPA Cascading, Lazy Loading & the N+1 Problem (Lecture 31)

**Cascading — Definition:** Propagates an operation (persist, remove, merge, ...) performed on a **parent** entity automatically to its related **child** entities, so you don't have to save/delete each one manually.
```java
@Entity
public class Department {
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students = new ArrayList<>();
    // CascadeType.ALL: saving/deleting a Department automatically saves/deletes its Students too
    // orphanRemoval: removing a Student from this list also deletes it from the database
}
```

**Lazy vs Eager Loading — Definition:** `FetchType.LAZY` defers loading a related entity/collection until it's **actually accessed** in code (returns a proxy initially); `FetchType.EAGER` loads it **immediately**, as part of the original query. `@ManyToOne`/`@OneToOne` default to EAGER; `@OneToMany`/`@ManyToMany` default to LAZY.
```java
@ManyToOne(fetch = FetchType.LAZY) // department is only fetched from the DB when .getDepartment() is actually called
@JoinColumn(name = "dept_id")
private Department department;
```

**The N+1 Problem — Definition:** A classic performance bug: fetching N parent rows (1 query), then lazily accessing a related collection/entity on **each** of them individually triggers N **additional** queries — N+1 total, instead of one efficient join.
```java
List<Department> departments = departmentRepository.findAll();  // 1 query
for (Department d : departments) {
    System.out.println(d.getStudents().size()); // triggers ONE extra query PER department (lazy loading)
}
// Total: 1 + N queries instead of a single JOIN — this is the N+1 problem
```

**`@EntityGraph` — Definition:** Tells JPA to fetch specific lazy associations **eagerly, in the same query** (via a JOIN) for a particular repository method call — solving N+1 without changing the entity's default fetch type globally.
```java
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @EntityGraph(attributePaths = "students") // forces students to be loaded in the SAME query, via JOIN
    List<Department> findAll();
}
```

---

## 32. Spring Data JPA: `JpaRepository`, JPQL & Pagination (Lecture 32)

**`JpaRepository<T, ID>` — Definition:** The richest Spring Data repository interface — extends `CrudRepository` and `PagingAndSortingRepository`, giving you CRUD, pagination, and sorting for free, plus batch operations like `saveAll`/`flush`.
```java
public interface StudentRepository extends JpaRepository<Student, Long> { }
```

**JPQL (Java Persistence Query Language) — Definition:** An object-oriented query language similar to SQL, but operating on **entity names and field names** instead of table/column names — portable across different databases since Hibernate translates it to the right SQL dialect.
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT s FROM Student s WHERE s.department.name = :deptName") // "Student"/"department" = entity/field names
    List<Student> findByDepartmentName(@Param("deptName") String deptName);
}
```

**Pagination & Sorting — Definition:** `Pageable` lets you request a specific page of results with a defined size and sort order, instead of loading an entire (potentially huge) result set into memory at once.
```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending()); // page 0, 10 per page, sorted by name
Page<Student> page = studentRepository.findAll(pageable);

page.getContent();       // the actual list of students on this page
page.getTotalElements();  // total count across ALL pages
page.getTotalPages();      // total number of pages
```

---

## 33. Spring Transactions: ACID & `@Transactional` (Lecture 33)

**ACID — Definition:** The four guarantees a database transaction must provide: **Atomicity** (all steps succeed or none do), **Consistency** (the database moves between valid states), **Isolation** (concurrent transactions don't interfere with each other), **Durability** (once committed, changes survive a crash).

**`@Transactional` — Definition:** Wraps a method in a database transaction automatically — if the method completes normally, Spring **commits**; if it throws an **unchecked** exception, Spring **rolls back** every change made during that method (undoing partial work).
```java
@Service
public class TransferService {
    private final AccountRepository accountRepository;

    @Transactional // ALL database operations inside this method succeed together, or none do
    public void transferMoney(Long fromId, Long toId, double amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.setBalance(from.getBalance() - amount);
        accountRepository.save(from);

        if (amount > 10000) throw new RuntimeException("Suspicious transfer amount"); // triggers a FULL rollback

        to.setBalance(to.getBalance() + amount);
        accountRepository.save(to);
    }
}
```
By default, `@Transactional` rolls back only on **unchecked** exceptions (`RuntimeException`/`Error`) — a checked exception does **not** trigger rollback unless you explicitly declare `rollbackFor = Exception.class`.

---

## 34. `@Transactional` Propagation & Isolation Levels (Lecture 34)

**Propagation — Definition:** Controls how a `@Transactional` method behaves when it's called from **inside another already-running transaction** — does it join the existing one, or start a brand-new independent one?
```java
@Service
public class OrderService {
    private final PaymentAuditService paymentAuditService;

    @Transactional
    public void placeOrder(Order order) {
        orderRepository.save(order);
        paymentAuditService.audit(order); // called from WITHIN this already-open transaction
    }
}

@Service
public class PaymentAuditService {
    @Transactional(propagation = Propagation.REQUIRED) // DEFAULT: joins the caller's existing transaction if one is open
    public void audit(Order order) {
        paymentAuditRepository.save(new PaymentAudit(order));
        // if THIS method fails, it can roll back the ENTIRE outer placeOrder() transaction too
    }
}
```
Other common propagation types: `REQUIRES_NEW` (always suspends any existing transaction and starts a fresh, independent one — a failure here won't roll back the caller's transaction), `NESTED` (a savepoint within the existing transaction).

**Isolation Level — Definition:** Controls how much one transaction can "see" of another **concurrently running** transaction's uncommitted or in-progress changes — trading off consistency guarantees against performance/concurrency.
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void audit(Order order) {
    // guarantees that if this transaction reads the same row twice, it sees the SAME data both times
    // (protects against "non-repeatable reads" from other transactions committing in between)
}
```
| Isolation Level | Prevents |
|---|---|
| READ_UNCOMMITTED | Nothing — can see other transactions' uncommitted changes ("dirty reads") |
| READ_COMMITTED | Dirty reads |
| REPEATABLE_READ | Dirty reads + non-repeatable reads |
| SERIALIZABLE | Dirty reads + non-repeatable reads + phantom reads (strictest, slowest) |

---

## 35. Spring Security: Authentication, Authorization & SecurityContext (Lecture 35)

**Authentication vs Authorization — Definition:** **Authentication** answers "who are you?" (verifying identity, e.g., checking a username/password). **Authorization** answers "what are you allowed to do?" (checking permissions/roles **after** identity is established).

**Spring Security auto-configuration — Definition:** Simply adding the `spring-boot-starter-security` dependency automatically secures **every** endpoint by default, requiring login via an auto-generated form and a randomly generated password (printed in the startup logs) — until you provide custom configuration.

**`SecurityFilterChain` — Definition:** The bean where you declare custom security rules — which URLs are public, which require authentication, and how login should work — replacing the all-or-nothing default.
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()   // no authentication required
                .anyRequest().authenticated()                       // everything else needs a logged-in user
            )
            .formLogin(Customizer.withDefaults());
        return http.build();
    }
}
```

**`SecurityContext` — Definition:** A thread-local holder for the current request's `Authentication` object (who's logged in, their granted authorities) — accessible anywhere during that request via `SecurityContextHolder.getContext().getAuthentication()`.

---

## 36. Spring Security DB Authentication & Password Security (Lecture 36)

**Password Hashing with `BCryptPasswordEncoder` — Definition:** Passwords must **never** be stored in plaintext. `BCryptPasswordEncoder` is a one-way, salted hashing algorithm — it's computationally expensive by design (slowing brute-force attacks), and even hashing the same password twice produces different output (due to a random salt), yet both still verify correctly.
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```
```java
String hashed = passwordEncoder.encode("myPassword123");     // store ONLY this hash, never the raw password
boolean matches = passwordEncoder.matches("myPassword123", hashed); // true — used during login to verify
```

**Database-backed user registration — Definition:** Storing users (with hashed passwords) and roles in the database instead of hardcoding credentials, letting the app authenticate against real, dynamic user data.
```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;    // ALWAYS the bcrypt hash, never plaintext
    private boolean enabled;

    @ManyToMany
    private List<Role> roles = new ArrayList<>();
}
```
```java
@Service
public class AuthService {
    public User register(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword)); // hash BEFORE saving
        return userRepository.save(user);
    }
}
```

---

## 37. Spring Security Authentication Internals — Complete Login Flow (Lecture 37)

**`UserDetails` — Definition:** The interface Spring Security uses internally to represent "a user" during authentication — you implement it (often wrapping your own `User` entity) to expose the username, password hash, granted authorities, and account-enabled status in the shape Spring Security expects.
```java
public class CustomUserDetails implements UserDetails {
    private final User user;
    public CustomUserDetails(User user) { this.user = user; }

    @Override public String getUsername() { return user.getUsername(); }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public boolean isEnabled() { return user.isEnabled(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }
}
```

**`UserDetailsService` — Definition:** The single method Spring Security calls to **look up** a user by username during login — you implement it to query your own database via a repository.
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }
}
```

**`AuthenticationProvider` & `AuthenticationManager` — Definition:** `DaoAuthenticationProvider` is the built-in provider that ties your `UserDetailsService` and `PasswordEncoder` together to actually verify credentials. The `AuthenticationManager` (backed by a `ProviderManager` holding one or more providers) is the entry point that orchestrates the whole authentication attempt.
```java
@Bean
public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(uds);
    provider.setPasswordEncoder(encoder); // compares the submitted password against the stored hash
    return provider;
}

@Bean
public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
    return new ProviderManager(provider);
}
```
**Full login flow:** submitted credentials → `AuthenticationManager` → `DaoAuthenticationProvider` → calls `UserDetailsService.loadUserByUsername()` → compares the password hash via `PasswordEncoder` → on success, builds an `Authentication` object and stores it in the `SecurityContext`.

---

## 38. JWT Authentication — Internals & Stateless Security (Lecture 38)

**JWT (JSON Web Token) — Definition:** A compact, self-contained, cryptographically **signed** token representing an authenticated identity and its claims — the server can verify it wasn't tampered with (via the signature) without needing to store any session state, enabling **stateless** authentication.

**Stateless session policy — Definition:** Instead of Spring Security's default session-cookie-based login, a JWT-based API tells Spring **not** to create/use an HTTP session at all — every request must carry its own proof of identity (the token).
```java
http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

**Generating a JWT — Definition:** After successful login, the server builds a token containing claims (issuer, expiry, subject/username, authorities) and signs it with a secret key — that signed string is returned to the client to use on future requests.
```java
@Service
public class JwtService {
    @Autowired private JwtEncoder jwtEncoder;
    @Value("${jwt.issuer}") private String issuer;
    @Value("${jwt.expiry}") private Long expirySeconds;

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
```

**Validating a JWT as a Resource Server — Definition:** On every incoming request, Spring Security's OAuth2 Resource Server support decodes the `Authorization: Bearer <token>` header, verifies the signature and expiry using a `JwtDecoder`, and converts its claims into an `Authentication` object — all before the request reaches any controller.
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login").permitAll()
            .anyRequest().authenticated())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
    return http.build();
}
```

---

## 39. OAuth 2.0 Masterclass — Google Login, OIDC & PKCE (Lecture 39)

**OAuth 2.0 — Definition:** An authorization framework letting a user grant a third-party app **limited access** to their data on another service (e.g., Google) **without sharing their password** with that app — the app instead receives a scoped access token.

**OIDC (OpenID Connect) — Definition:** An identity layer built **on top of** OAuth 2.0 — while plain OAuth2 only proves "this app has permission to act on the user's behalf," OIDC additionally proves **who the user actually is** (via an ID Token containing identity claims like email/name).

**PKCE (Proof Key for Code Exchange) — Definition:** A security extension to the OAuth2 authorization code flow that prevents a malicious app from intercepting and reusing another app's authorization code — the client generates a random secret ("code verifier"), sends its hash upfront, and must present the original secret when exchanging the code for a token, proving it's the same client that started the flow.

**"Login with Google" via `oauth2Login` — Definition:** Spring Security's built-in support for delegating authentication entirely to an external OAuth2/OIDC provider — you configure the provider's client credentials, and Spring handles the full redirect → consent → callback → token exchange flow.
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOidcUserService oidcUserService) throws Exception {
    http.authorizeHttpRequests(auth -> auth.requestMatchers("/").permitAll().anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService)) // customize post-login user handling
            .defaultSuccessUrl("/profile", true));
    return http.build();
}
```

**Custom `OidcUserService` — Definition:** A hook to run your own logic (e.g., create-or-update a local `User` record) right after Google confirms the user's identity but before the login flow completes — commonly used to "sync" the external identity into your own database.
```java
@Service
public class CustomOidcUserService extends OidcUserService {
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest); // delegates to Spring's default Google-user loading
        // e.g.: userRepository.findByEmail(oidcUser.getEmail()).orElseGet(() -> createNewUser(oidcUser));
        return oidcUser;
    }
}
```

---

## 40. Testing Spring Boot Applications: JUnit 5, Mockito & MockMvc (Lecture 40)

**JUnit 5 basics — Definition:** The standard Java testing framework. `@Test` marks a test method; `@BeforeEach`/`@AfterEach` run setup/teardown before/after **every** test method; assertions verify expected outcomes.
```java
class PriceCalculatorTest {
    private PriceCalculator priceCalculator;

    @BeforeEach
    void setup() { priceCalculator = new PriceCalculator(); } // fresh instance before EACH test — avoids shared state

    @Test
    void shouldApplyDiscountToPrice() {
        // Arrange, Act, Assert — the standard 3-step test structure
        double actualPrice = priceCalculator.calculatePrice(1000, 20); // Arrange + Act
        assertEquals(800, actualPrice);                                  // Assert
    }

    @Test
    void shouldRejectDiscountAboveHundred() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> priceCalculator.calculatePrice(1000, 120));
        assertEquals("Discount should be within 0 to 100", ex.getMessage());
    }
}
```

**Mockito: `@Mock` & `@InjectMocks` — Definition:** `@Mock` creates a fake, controllable stand-in for a dependency (so tests don't hit a real database/network). `@InjectMocks` creates the real class under test and automatically injects the mocks into it — letting you test a service's logic in complete isolation from its collaborators.
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;      // fake — no real database involved
    @InjectMocks private ProductService productService;      // the REAL class under test, with the mock injected in

    @Test
    void shouldReturnProductWhenProductExists() {
        Product product = new Product(1L, "Laptop", 50000, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // stub the mock's behavior

        Product result = productService.getProductById(1L);

        assertEquals("Laptop", result.getName());
        verify(productRepository).findById(1L); // confirm the repository method was actually called
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(99L));
    }
}
```

**`@WebMvcTest` & `MockMvc` — Definition:** `@WebMvcTest` loads **only** the web layer (controllers, filters, JSON conversion) for a fast, focused test — not the full application context. `MockMvc` simulates real HTTP requests against a controller **without starting an actual server**, and `@MockitoBean` replaces the service layer with a mock so the controller is tested in isolation.
```java
@WebMvcTest
class ProductControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductService productService; // the controller's real dependency is swapped for a mock

    @Test
    void shouldReturnProductIfExists() throws Exception {
        when(productService.getProductById(1L)).thenReturn(new Product(1L, "Laptop", 50000, 10));

        mockMvc.perform(get("/api/product/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Laptop")); // asserts on the actual JSON response body
    }
}
```

**Test types — Definition:** A **unit test** (Mockito-based) tests one class in isolation with all dependencies mocked — fast, no Spring context. A **slice test** (`@WebMvcTest`, `@DataJpaTest`) loads only the relevant part of the Spring context — faster than a full integration test but still verifies real framework wiring (e.g., JSON serialization, request mapping) for that layer.

---

## Quick Index (all 40 lectures)
1: Spring/Spring Boot ecosystem overview, HTTP, Servlets · 2: first REST API · 3: Maven · 4: DI & IoC problem · 5: IoC container, `@Component`/`@Autowired`/`@Bean` · 6: circular dependency, bean scope, lazy/eager · 7: bean lifecycle, Aware interfaces · 8: XML config · 9: Spring Boot auto-configuration · 10: `application.properties`, `@Value`, `CommandLineRunner` · 11: CRUD from scratch, layered architecture · 12: CRUD + MySQL, derived query methods · 13: soft delete, `@Query` · 14: Servlets, Tomcat, WAR · 15: DispatcherServlet, HandlerMapping, JSP · 16: DTOs, Bean Validation · 17: `@RestControllerAdvice`, global exception handling · 18: Profiles, YAML · 19: Filters (logging/auth) · 20: Filters (modify req/res, `FilterRegistrationBean`) · 21: Interceptors (`preHandle`/`postHandle`/`afterCompletion`) · 22: AOP intro, proxies, cross-cutting concerns · 23: AOP advice types (`@Before`/`@After`/`@Around`) · 24: reusable pointcuts, JDK vs CGLIB proxies · 25: custom annotations in AOP · 26: raw JDBC · 27: `JdbcTemplate`, `RowMapper`, HikariCP · 28: Hibernate entity mapping · 29: persistence context, L1 cache, dirty checking · 30: JPA relationships (`@OneToMany` etc.) · 31: cascading, lazy loading, N+1, `@EntityGraph` · 32: `JpaRepository`, JPQL, pagination · 33: `@Transactional`, ACID · 34: propagation & isolation levels · 35: Spring Security basics, `SecurityFilterChain` · 36: DB auth, BCrypt password hashing · 37: `UserDetailsService`, full authentication flow · 38: JWT generation & validation · 39: OAuth2/OIDC/PKCE, Google login · 40: JUnit5, Mockito, `@WebMvcTest`/MockMvc
