package com.interview.master.springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

/**
 * ============================================================
 * SPRING CONFIGURATION - Complete Interview Guide
 * ============================================================
 *
 * @Configuration:
 *   - Marks a class as a source of bean definitions
 *   - Spring processes it at startup to create beans
 *   - Methods annotated with @Bean return objects managed by Spring IoC container
 *   - @Configuration classes are CGLIB-proxied so @Bean methods calling other
 *     @Bean methods return the SAME singleton instance (not new objects)
 *
 * Interview Q: @Configuration vs @Component for declaring beans?
 *   @Configuration:
 *     - CGLIB proxy: method calls on @Bean methods go through the proxy
 *     - Guarantees singleton: calling beanA() twice returns the same object
 *     - Semantically: "this class is purely configuration"
 *   @Component:
 *     - No CGLIB proxy (unless proxyBeanMethods = true, which is default false for @Component)
 *     - Calling @Bean method multiple times creates new instances each time!
 *     - Used when the class also has other non-configuration responsibilities
 *   Best practice: use @Configuration for pure configuration classes.
 *
 * ---------------------------------------------------------------
 * @Bean:
 * ---------------------------------------------------------------
 *   - Declares a Spring-managed bean
 *   - Method name = default bean name (override with @Bean("myName"))
 *   - Return type = the bean's type (registered in ApplicationContext)
 *   - Method parameters = dependencies (Spring injects them automatically)
 *   - initMethod / destroyMethod attributes for lifecycle callbacks
 *
 * ---------------------------------------------------------------
 * SPRING BEAN SCOPES (@Scope):
 * ---------------------------------------------------------------
 *
 * singleton (DEFAULT):
 *   - ONE instance per Spring ApplicationContext
 *   - Same instance returned for every injection/getBean() call
 *   - Suitable for: stateless services, repositories, configuration
 *   - Memory efficient: shared instance
 *   - CAUTION: shared state = thread-safety concerns
 *
 * prototype:
 *   - NEW instance every time the bean is requested (injected or getBean())
 *   - Spring creates but does NOT manage lifecycle (no @PreDestroy call)
 *   - Suitable for: stateful beans, command objects, request-specific objects
 *   - CAUTION: injecting prototype into singleton = same prototype forever!
 *     Solution: inject ApplicationContext and call getBean() manually,
 *               or use @Lookup method injection, or scoped proxy.
 *
 * request (Web only):
 *   - ONE instance per HTTP request
 *   - Created when request starts, destroyed when request ends
 *   - Thread-safe by nature (one request = one thread typically)
 *   - Suitable for: request-scoped data, user input processing
 *
 * session (Web only):
 *   - ONE instance per HTTP session
 *   - Lives as long as the user's session
 *   - Suitable for: shopping cart, user preferences
 *
 * application (Web only):
 *   - ONE instance per ServletContext (similar to singleton but explicit)
 *   - Shared across all sessions and requests in the same web app
 *
 * websocket:
 *   - ONE instance per WebSocket session
 *
 * Custom scope: implement Scope interface and register with CustomScopeConfigurer.
 *
 * ---------------------------------------------------------------
 * @Value INJECTION:
 * ---------------------------------------------------------------
 *
 * Property placeholder syntax:
 *   @Value("${property.key}")              - required, throws if missing
 *   @Value("${property.key:defaultValue}") - optional with default
 *   @Value("${property.key:#{null}}")      - optional, default null
 *
 * SpEL (Spring Expression Language) syntax:
 *   @Value("#{5 + 7}")                   - arithmetic expression
 *   @Value("#{T(Math).PI}")              - static method/field access
 *   @Value("#{otherBean.someProperty}")  - access another bean's property
 *   @Value("#{systemProperties['os.name']}") - system property
 *   @Value("#{environment['spring.profiles.active']}") - environment
 *
 * Combined:
 *   @Value("#{${employee.max-count:100} * 2}") - property inside SpEL
 *
 * ---------------------------------------------------------------
 * @ConfigurationProperties:
 * ---------------------------------------------------------------
 *   - Binds a group of related properties to a POJO
 *   - Type-safe: validates types at startup
 *   - Supports nested objects, lists, maps
 *   - Better than many @Value annotations for related config
 *   - Requires @EnableConfigurationProperties(TheClass.class) or @Component
 *
 * ---------------------------------------------------------------
 * @Profile:
 * ---------------------------------------------------------------
 *   - Beans are only created when specified profile is active
 *   - Activate profiles: spring.profiles.active=dev
 *   - Multiple profiles: @Profile({"dev", "test"})
 *   - Negate: @Profile("!prod") = all profiles EXCEPT prod
 *   - Can be on @Configuration, @Component, @Bean method
 *
 * ---------------------------------------------------------------
 * @ConditionalOn* (Spring Boot auto-configuration conditions):
 * ---------------------------------------------------------------
 *   @ConditionalOnProperty(name="feature.x.enabled", havingValue="true")
 *   @ConditionalOnClass(name="com.example.SomeClass")  - class on classpath
 *   @ConditionalOnMissingBean(SomeService.class)       - no such bean yet
 *   @ConditionalOnBean(DataSource.class)               - bean exists
 *   @ConditionalOnExpression("${flag:false}")          - SpEL expression
 *   @ConditionalOnWebApplication                       - in web context
 *   @ConditionalOnNotWebApplication                    - NOT in web context
 *
 * These power Spring Boot's auto-configuration: each auto-config class has
 * @ConditionalOn* to only apply if certain conditions are met.
 * ============================================================
 */
@Configuration
@EnableAsync        // enables @Async method execution (async event listeners, async service methods)
@EnableCaching      // enables Spring's caching abstraction (@Cacheable, @CacheEvict, @CachePut)
@EnableConfigurationProperties({
        AppConfig.EmailProperties.class,
        AppConfig.JwtProperties.class
})
public class AppConfig {

    // ================================================================
    // @Value INJECTION EXAMPLES
    // ================================================================

    /** Property placeholder: reads from application.properties / application.yml */
    @Value("${app.name:Java Interview Master}")
    private String appName;

    /** With default value if property not set */
    @Value("${app.max-employees:1000}")
    private int maxEmployees;

    /** SpEL expression: evaluate a mathematical expression at injection time */
    @Value("#{T(java.lang.Runtime).getRuntime().availableProcessors()}")
    private int availableProcessors;

    /** SpEL: read system property */
    @Value("#{systemProperties['java.version']}")
    private String javaVersion;

    /** Combine property + SpEL: use property value inside SpEL expression */
    @Value("#{${app.max-employees:1000} * 2}")
    private int doubleMaxEmployees;

    // ================================================================
    // RestTemplate BEAN
    // ================================================================
    /**
     * RestTemplate - synchronous HTTP client for calling external REST APIs.
     *
     * Usage:
     *   String result = restTemplate.getForObject("http://api.example.com/data", String.class);
     *   ResponseEntity<User> response = restTemplate.getForEntity(url, User.class);
     *   restTemplate.postForObject(url, requestBody, Response.class);
     *
     * Interview Q: RestTemplate vs WebClient vs Feign?
     *   RestTemplate: synchronous (blocking), simple, classic Spring (being deprecated in favor of WebClient)
     *   WebClient: reactive (non-blocking), part of Spring WebFlux, fluent API, Spring 5+
     *   Feign: declarative HTTP client (interface + annotations), used with Spring Cloud
     *
     * For new projects: use WebClient (even in non-reactive apps, it works synchronously).
     *
     * @Bean at method level: the name defaults to "restTemplate" (method name).
     * Any component can inject it: @Autowired RestTemplate restTemplate;
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // In production: configure timeouts, connection pool, interceptors
        // RestTemplate template = new RestTemplate(requestFactory());
        return restTemplate;
    }

    // ================================================================
    // ThreadPoolTaskExecutor BEAN (for @Async)
    // ================================================================
    /**
     * ThreadPoolTaskExecutor - Spring's thread pool for @Async methods.
     *
     * Why configure a custom executor?
     *   - Default SimpleAsyncTaskExecutor: creates a NEW thread per task (no pooling!)
     *   - ThreadPoolTaskExecutor: reuses threads, bounded pool, queue management
     *
     * Configuration:
     *   corePoolSize:  minimum threads kept alive even when idle
     *   maxPoolSize:   maximum threads that can be created
     *   queueCapacity: task queue size before creating new threads (up to maxPoolSize)
     *   Thread creation: starts with corePoolSize, fills queue, then expands to maxPoolSize
     *   If queue full AND maxPoolSize reached: RejectExecutionPolicy (throw or discard)
     *
     * Naming: setThreadNamePrefix helps with debugging (visible in thread dumps).
     *
     * Bean name "taskExecutor" is the DEFAULT name Spring Boot looks for.
     * Or qualify with @Async("myExecutor") to use a specific executor.
     *
     * Example @Async usage:
     *   @Async
     *   public CompletableFuture<String> doLongTask() {
     *       // runs in pool thread
     *       return CompletableFuture.completedFuture("done");
     *   }
     *
     * Interview Q: Difference between @Async return types?
     *   void:                  fire-and-forget (no result, exceptions swallowed)
     *   Future<T>:             check result later (isDone(), get())
     *   CompletableFuture<T>:  powerful async composition (thenApply, thenCompose, etc.)
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int cpus = availableProcessors > 0 ? availableProcessors : Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(cpus);          // start with as many threads as CPUs
        executor.setMaxPoolSize(cpus * 2);       // allow up to 2x CPU threads
        executor.setQueueCapacity(500);          // queue up to 500 tasks before creating more threads
        executor.setKeepAliveSeconds(60);        // idle threads above core size kept for 60s
        executor.setThreadNamePrefix("async-");  // threads named: async-1, async-2, etc.

        // Graceful shutdown: wait for running tasks to complete on context close
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);  // wait max 30s for tasks to finish

        executor.initialize();
        return executor;
    }

    // ================================================================
    // @Profile BEANS
    // ================================================================
    /**
     * Profile-specific beans: different implementations for different environments.
     *
     * How to activate profiles:
     *   application.properties: spring.profiles.active=dev
     *   JVM arg: -Dspring.profiles.active=prod
     *   Environment variable: SPRING_PROFILES_ACTIVE=prod
     *   Programmatic: SpringApplication.setAdditionalProfiles("dev")
     *
     * @Profile can be:
     *   @Profile("dev")                  - active for "dev" profile only
     *   @Profile({"dev", "test"})         - active for "dev" OR "test"
     *   @Profile("!prod")                 - active for all profiles EXCEPT "prod"
     *   @Profile("dev & !test")           - SpEL logic: dev AND NOT test
     */

    /** Development data source: H2 in-memory (fast, disposable) */
    @Bean
    @Profile("dev")
    public String devDataSourceInfo() {
        System.out.println("[PROFILE:dev] Using H2 in-memory database");
        return "H2 in-memory datasource configured for DEV";
    }

    /** Production data source: PostgreSQL (persistent, high-performance) */
    @Bean
    @Profile("prod")
    public String prodDataSourceInfo() {
        System.out.println("[PROFILE:prod] Using PostgreSQL production database");
        return "PostgreSQL datasource configured for PROD";
    }

    /** Default profile bean: used when no profile is active */
    @Bean
    @Profile("default")
    public String defaultDataSourceInfo() {
        System.out.println("[PROFILE:default] Using default H2 database");
        return "Default datasource configured";
    }

    // ================================================================
    // @ConditionalOnProperty BEAN
    // ================================================================
    /**
     * @ConditionalOnProperty: bean is created only if the specified property
     * has the specified value.
     *
     * In application.properties: feature.email-notifications.enabled=true
     * -> EmailNotificationService bean is created
     *
     * matchIfMissing = false (default): bean NOT created if property is absent
     * matchIfMissing = true: bean created if property is absent (treated as enabled)
     *
     * Interview Q: How does Spring Boot auto-configuration use @ConditionalOnProperty?
     *   Every Spring Boot Starter's AutoConfiguration class is guarded by @ConditionalOn*
     *   annotations. E.g., DataSourceAutoConfiguration only activates if a DataSource
     *   dependency is on the classpath AND no DataSource bean is already defined.
     */
    @Bean
    @ConditionalOnProperty(
            name      = "feature.email-notifications.enabled",
            havingValue = "true",
            matchIfMissing = false  // don't create bean if property not set
    )
    public Object emailNotificationService() {
        System.out.println("[CONDITIONAL] Email notifications ENABLED - creating service");
        // Real implementation: return new EmailNotificationService(...)
        return new Object();  // placeholder
    }

    @Bean
    @ConditionalOnProperty(
            name        = "feature.audit-log.enabled",
            havingValue = "true",
            matchIfMissing = true  // create bean even if property not set (enabled by default)
    )
    public Object auditLogService() {
        System.out.println("[CONDITIONAL] Audit log service ENABLED - creating service");
        return new Object();  // placeholder
    }

    // ================================================================
    // @Scope EXAMPLES
    // ================================================================
    /**
     * Singleton scope (default): ONE instance per ApplicationContext.
     * You don't need to explicitly declare @Scope("singleton") — it's the default.
     */
    @Bean
    @Scope("singleton")  // explicitly declared but redundant (just for demonstration)
    public String singletonBean() {
        System.out.println("[SCOPE] Creating SINGLETON bean");
        return "I am a singleton: " + System.nanoTime();
        // same string returned for every injection
    }

    /**
     * Prototype scope: NEW instance every time bean is requested.
     *
     * IMPORTANT: Spring does NOT call @PreDestroy on prototype beans.
     * The container gives the bean to the requester and forgets about it.
     * Client is responsible for cleanup.
     *
     * Problem: injecting prototype into singleton:
     *   @Autowired PrototypeBean prototypeBean;  // in a singleton
     *   -> prototypeBean is injected ONCE at singleton creation time
     *   -> every call to prototypeBean uses the SAME (first) instance
     *   -> This defeats the purpose of prototype scope!
     *
     * Solution: use ApplicationContext.getBean() each time you need a new instance,
     *   or use Spring's @Lookup method injection.
     */
    @Bean
    @Scope("prototype")
    public Object prototypeBean() {
        System.out.println("[SCOPE] Creating PROTOTYPE bean - new instance");
        return new Object();  // new instance every time
    }

    /**
     * Request scope: one instance per HTTP request.
     * Only valid in a web application context.
     * Automatically destroyed when request ends.
     *
     * Usage: store request-specific data (correlation IDs, user context).
     * Requires proxyMode = ScopedProxyMode.TARGET_CLASS for injection
     * into singleton beans (otherwise you'd get the first request's scope).
     */
    @Bean
    @Scope(value = "request",
           proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
    public Object requestScopedBean() {
        return new Object();
        // In real app: return new RequestContext() containing request metadata
    }

    /**
     * Session scope: one instance per HTTP session.
     * Lives as long as the user's browser session.
     */
    @Bean
    @Scope(value = "session",
           proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
    public Object sessionScopedBean() {
        return new Object();
        // In real app: return new UserSession() containing user preferences, cart, etc.
    }

    // ================================================================
    // @ConfigurationProperties BINDING (inner classes)
    // ================================================================
    /**
     * @ConfigurationProperties - binds properties to a type-safe POJO.
     *
     * application.properties / application.yml:
     *   app.email.host=smtp.gmail.com
     *   app.email.port=587
     *   app.email.username=noreply@company.com
     *   app.email.password=secret
     *   app.email.from-name=Company HR
     *
     * Benefits over @Value:
     *   - Groups related properties (cohesion)
     *   - Type-safe: validation at startup if fields are invalid types
     *   - IDE support: autocompletion in application.properties
     *   - Relaxed binding: app.email.from-name binds to fromName (camelCase)
     *
     * @ConstructorBinding (Spring Boot 3.x): use constructor injection instead of setters.
     *   Useful for immutable configuration objects.
     */
    @ConfigurationProperties(prefix = "app.email")
    public static class EmailProperties {
        private String host = "localhost";
        private int port    = 25;
        private String username;
        private String password;
        private String fromName = "Application";

        // Getters and setters required for non-constructor-binding
        public String getHost()      { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort()         { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername()  { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword()  { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFromName()  { return fromName; }
        public void setFromName(String fromName) { this.fromName = fromName; }

        @Override
        public String toString() {
            return "EmailProperties{host='" + host + "', port=" + port +
                   ", username='" + username + "', fromName='" + fromName + "'}";
        }
    }

    /**
     * JWT configuration bound from properties.
     *
     * application.yml:
     *   app:
     *     jwt:
     *       secret: mySecretKey256BitsLong
     *       expiration-ms: 86400000    # 24 hours in milliseconds
     *       refresh-expiration-ms: 604800000  # 7 days
     */
    @ConfigurationProperties(prefix = "app.jwt")
    public static class JwtProperties {
        private String secret         = "defaultSecretKey-ChangeInProduction-32+chars";
        private long expirationMs     = 86400000L;   // 24 hours
        private long refreshExpirationMs = 604800000L; // 7 days

        public String getSecret()               { return secret; }
        public void setSecret(String secret)    { this.secret = secret; }
        public long getExpirationMs()           { return expirationMs; }
        public void setExpirationMs(long ms)    { this.expirationMs = ms; }
        public long getRefreshExpirationMs()    { return refreshExpirationMs; }
        public void setRefreshExpirationMs(long ms) { this.refreshExpirationMs = ms; }
    }

    // ================================================================
    // @Bean with initMethod and destroyMethod
    // ================================================================
    /**
     * Bean lifecycle callbacks:
     *
     * 1. Constructor (bean instantiation)
     * 2. Dependency injection (@Autowired, setters)
     * 3. @PostConstruct method (or InitializingBean.afterPropertiesSet())
     * 4. Bean is ready for use
     * 5. @PreDestroy method (or DisposableBean.destroy()) - on context close
     *
     * Three ways to define init/destroy:
     *   a) @PostConstruct / @PreDestroy annotations on bean methods (preferred)
     *   b) @Bean(initMethod="init", destroyMethod="cleanup") - for third-party classes
     *   c) Implement InitializingBean / DisposableBean interfaces (least preferred - Spring coupling)
     *
     * @Bean(destroyMethod="") - empty string disables auto-detected destroy method
     *   Useful for singleton beans that should NOT be closed on context shutdown
     *   (e.g., shared DataSource that's managed externally).
     */
    @Bean(initMethod = "onInit", destroyMethod = "onDestroy")
    public ExampleManagedResource exampleManagedResource() {
        return new ExampleManagedResource();
    }

    /**
     * ExampleManagedResource demonstrates init/destroy lifecycle.
     * In real app: could be a connection pool, thread pool, cache manager.
     */
    public static class ExampleManagedResource {
        public void onInit() {
            System.out.println("[LIFECYCLE] ExampleManagedResource initialized (initMethod)");
        }

        public void onDestroy() {
            System.out.println("[LIFECYCLE] ExampleManagedResource destroyed (destroyMethod) - releasing resources");
        }
    }

    // ================================================================
    // SUMMARY: Key @Value and SpEL examples
    // ================================================================
    /**
     * Quick reference for @Value expressions:
     *
     * PROPERTY PLACEHOLDERS:
     *   @Value("${server.port}")              // required property
     *   @Value("${server.port:8080}")         // with default
     *   @Value("${app.list:a,b,c}")           // inject comma-separated as String
     *
     * SpEL EXPRESSIONS:
     *   @Value("#{1 + 2}")                    // 3 (arithmetic)
     *   @Value("#{'hello' + ' world'}")       // "hello world" (string concat)
     *   @Value("#{T(java.lang.Math).random()}") // static method call
     *   @Value("#{someBean.someProperty}")    // other bean's property
     *   @Value("#{someBean.someMethod()}")    // call other bean's method
     *
     * ENVIRONMENT:
     *   @Value("#{environment.getProperty('spring.profiles.active')}")
     *
     * SYSTEM PROPERTIES:
     *   @Value("#{systemProperties['user.home']}")
     *   @Value("#{systemEnvironment['PATH']}")
     *
     * INTERVIEW Q: @Value vs @ConfigurationProperties?
     *   @Value:
     *     - Single property injection
     *     - Works on fields, constructors, method params
     *     - Good for simple, unrelated properties
     *   @ConfigurationProperties:
     *     - Group of related properties -> one POJO
     *     - Type-safe, validated, IDE-autocomplete
     *     - Relaxed binding (kebab-case -> camelCase)
     *     - Better for configuration classes with many properties
     */
}
