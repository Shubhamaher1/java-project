package com.interview.master.springboot.microservices;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================
 * MICROSERVICES COMPLETE INTERVIEW GUIDE
 * ============================================================
 *
 * Q: What are Microservices?
 * Architectural style where application is built as collection of small,
 * independently deployable services, each running in its own process,
 * communicating via lightweight mechanisms (REST/messaging).
 *
 * Monolith vs Microservices:
 * ┌─────────────────┬────────────────────────────────────────────┐
 * │ Monolith        │ Microservices                               │
 * ├─────────────────┼────────────────────────────────────────────┤
 * │ Single deploy   │ Independent deployment per service          │
 * │ Single DB       │ Each service has its own DB                 │
 * │ Simple dev      │ Complex distributed system                  │
 * │ Scales as whole │ Scale individual services                   │
 * │ Single failure  │ Partial failures (resilience needed)        │
 * └─────────────────┴────────────────────────────────────────────┘
 *
 * MICROSERVICES DESIGN PATTERNS:
 * ─────────────────────────────────────────────────────────────
 * 1. API GATEWAY PATTERN
 *    - Single entry point for all clients
 *    - Routes requests to appropriate microservices
 *    - Handles cross-cutting: auth, rate limiting, SSL termination, logging
 *    - Tools: Spring Cloud Gateway, Netflix Zuul, Kong, AWS API Gateway
 *    - Q: Why API Gateway? Avoid N*M client-service connections
 *
 * 2. SERVICE REGISTRY & DISCOVERY
 *    - Services register themselves on startup
 *    - Clients discover services dynamically (no hardcoded URLs)
 *    - Tools: Netflix Eureka, Consul, Zookeeper
 *    - Client-side discovery: client queries registry, picks instance
 *    - Server-side discovery: load balancer queries registry
 *    Q: @EnableEurekaServer, @EnableDiscoveryClient
 *
 * 3. CIRCUIT BREAKER PATTERN
 *    - Prevents cascading failures in distributed systems
 *    - States: CLOSED (normal), OPEN (rejecting calls), HALF_OPEN (testing)
 *    - Tools: Resilience4j, Netflix Hystrix (deprecated)
 *    - @CircuitBreaker(name="employeeService", fallbackMethod="fallback")
 *    Q: Difference between Circuit Breaker and Retry?
 *       Retry: try again after failure (transient errors)
 *       Circuit Breaker: stop trying when service is down (fast-fail)
 *
 * 4. SAGA PATTERN (Distributed Transactions)
 *    - Manages data consistency across microservices without 2PC
 *    - Two types:
 *      a) Choreography: services emit events, others react (no central coordinator)
 *      b) Orchestration: central saga orchestrator directs services
 *    Q: Why not 2-phase commit (2PC)? Blocking, doesn't scale, single point of failure
 *
 * 5. EVENT SOURCING
 *    - Store state changes as sequence of events
 *    - Rebuild current state by replaying events
 *    - Benefits: audit trail, replay, time travel
 *
 * 6. CQRS (Command Query Responsibility Segregation)
 *    - Separate read model from write model
 *    - Command: changes state (create/update/delete)
 *    - Query: reads state (no side effects)
 *    - Often used with Event Sourcing
 *
 * 7. DATABASE PER SERVICE
 *    - Each microservice owns its data
 *    - Prevents tight coupling via shared DB
 *    - Challenge: joins across services (use API calls or denormalization)
 *
 * 8. SIDECAR PATTERN
 *    - Deploy helper service alongside main service in same pod (Kubernetes)
 *    - Handles: logging, monitoring, service mesh (Istio/Envoy)
 *
 * 9. STRANGLER FIG PATTERN
 *    - Gradually migrate monolith to microservices
 *    - Route some traffic to new service, rest to monolith
 *    - Over time, strangle the monolith completely
 *
 * 10. BULKHEAD PATTERN
 *    - Isolate failures to prevent cascading
 *    - Like ship bulkheads: one compartment floods, others stay dry
 *    - Implement via thread pool isolation per downstream service
 *
 * ─────────────────────────────────────────────────────────────
 * SPRING CLOUD COMPONENTS:
 * ─────────────────────────────────────────────────────────────
 * Spring Cloud Config    - Centralized configuration server
 * Spring Cloud Eureka    - Service registry & discovery
 * Spring Cloud Gateway   - API Gateway
 * Spring Cloud OpenFeign - Declarative HTTP client
 * Spring Cloud Sleuth    - Distributed tracing (trace IDs across services)
 * Spring Cloud Zipkin    - Visualize distributed traces
 * Spring Cloud Bus       - Propagate config changes via messaging
 * Resilience4j           - Circuit breaker, retry, rate limiter, bulkhead
 *
 * ─────────────────────────────────────────────────────────────
 * INTER-SERVICE COMMUNICATION:
 * ─────────────────────────────────────────────────────────────
 * Synchronous:
 * - REST (HTTP/HTTPS) - simple, familiar, request-response
 * - gRPC - Protocol Buffers, binary, faster, strongly typed, HTTP/2
 * - GraphQL - flexible querying, reduces over/under-fetching
 *
 * Asynchronous:
 * - Message Queue: point-to-point (one consumer) - RabbitMQ, ActiveMQ
 * - Pub/Sub: one-to-many (multiple consumers) - Kafka, Redis Pub/Sub
 * - Event Streaming: Kafka (durable, replay, ordered)
 *
 * Q: When to use sync vs async?
 * Sync: need immediate response (payment verification, auth)
 * Async: fire-and-forget, loose coupling, high throughput (notifications, auditing)
 *
 * ─────────────────────────────────────────────────────────────
 * SERVICE MESH:
 * ─────────────────────────────────────────────────────────────
 * Infrastructure layer for service-to-service communication
 * Tools: Istio, Linkerd, Consul Connect
 * Features: mTLS, load balancing, circuit breaking, distributed tracing
 *
 * ─────────────────────────────────────────────────────────────
 * CONTAINERIZATION & ORCHESTRATION:
 * ─────────────────────────────────────────────────────────────
 * Docker - containerize each microservice
 * Kubernetes - orchestrate containers
 *   Pod       - smallest deployable unit (one or more containers)
 *   Service   - stable network endpoint for pods
 *   Deployment - manages pod replicas
 *   ConfigMap/Secret - externalized configuration
 *   Ingress   - HTTP routing rules
 *   HPA       - Horizontal Pod Autoscaler
 *
 * ─────────────────────────────────────────────────────────────
 * MONITORING & OBSERVABILITY (Three Pillars):
 * ─────────────────────────────────────────────────────────────
 * Logs    - Structured logging, centralized (ELK: Elasticsearch+Logstash+Kibana)
 * Metrics - Numerical measurements (Prometheus + Grafana)
 * Traces  - Request flow across services (Jaeger, Zipkin, OpenTelemetry)
 * Q: What is correlation ID? Unique ID propagated across all service calls for a request
 *
 * ─────────────────────────────────────────────────────────────
 * SECURITY IN MICROSERVICES:
 * ─────────────────────────────────────────────────────────────
 * OAuth 2.0 + OIDC      - standard for authorization
 * JWT                   - stateless token for inter-service auth
 * mTLS                  - mutual TLS for service-to-service (service mesh)
 * API Key               - for external clients
 * Spring Authorization Server - OAuth 2.0 provider
 */
public class MicroservicesConcepts {

    // =========================================================
    // 1. FEIGN CLIENT - Declarative REST Client
    // =========================================================

    /**
     * FEIGN CLIENT:
     * - Declarative HTTP client: write an interface, Spring generates implementation
     * - Integrates with Eureka (service discovery by name, not URL)
     * - Integrates with Resilience4j circuit breaker via fallback
     * - Much less boilerplate than RestTemplate
     *
     * Dependency: spring-cloud-starter-openfeign
     * Enable on main class: @EnableFeignClients
     *
     * application.properties:
     *   feign.circuitbreaker.enabled=true
     *   feign.client.config.default.connectTimeout=5000
     *   feign.client.config.default.readTimeout=5000
     */

    /**
     * Basic Feign Client - talks to "employee-service" (registered in Eureka)
     * URL is resolved via service discovery, not hardcoded
     */
    @FeignClient(
            name = "employee-service",                  // Eureka service name
            url = "${employee.service.url:}",           // fallback URL if no Eureka
            fallback = EmployeeServiceFallback.class    // fallback when service is down
    )
    interface EmployeeServiceClient {

        @GetMapping("/api/employees/{id}")
        EmployeeDto getEmployeeById(@PathVariable("id") Long id);

        @GetMapping("/api/employees")
        List<EmployeeDto> getAllEmployees();

        @PostMapping("/api/employees")
        EmployeeDto createEmployee(@RequestBody EmployeeDto employeeDto);

        @PutMapping("/api/employees/{id}")
        EmployeeDto updateEmployee(@PathVariable("id") Long id, @RequestBody EmployeeDto employeeDto);

        @DeleteMapping("/api/employees/{id}")
        void deleteEmployee(@PathVariable("id") Long id);

        // Pass headers (e.g., JWT token for inter-service auth)
        @GetMapping("/api/employees/{id}/details")
        EmployeeDto getEmployeeDetails(
                @PathVariable("id") Long id,
                @RequestHeader("Authorization") String token
        );
    }

    /**
     * Feign Client with custom configuration (timeouts, error decoder, etc.)
     */
    @FeignClient(
            name = "department-service",
            configuration = FeignClientConfig.class
    )
    interface DepartmentServiceClient {
        @GetMapping("/api/departments/{id}")
        DepartmentDto getDepartmentById(@PathVariable("id") Long id);
    }

    /**
     * Fallback implementation for EmployeeServiceClient
     * Returned when the remote service is unavailable
     */
    @Component
    static class EmployeeServiceFallback implements EmployeeServiceClient {
        private static final Logger log = LoggerFactory.getLogger(EmployeeServiceFallback.class);

        @Override
        public EmployeeDto getEmployeeById(Long id) {
            log.warn("Fallback: employee-service is down. Returning default for id={}", id);
            return new EmployeeDto(id, "Unknown", "unknown@fallback.com", null);
        }

        @Override
        public List<EmployeeDto> getAllEmployees() {
            log.warn("Fallback: getAllEmployees returning empty list");
            return List.of();
        }

        @Override
        public EmployeeDto createEmployee(EmployeeDto employeeDto) {
            log.error("Fallback: cannot create employee, service is down");
            throw new RuntimeException("employee-service is unavailable");
        }

        @Override
        public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
            log.error("Fallback: cannot update employee, service is down");
            throw new RuntimeException("employee-service is unavailable");
        }

        @Override
        public void deleteEmployee(Long id) {
            log.error("Fallback: cannot delete employee, service is down");
        }

        @Override
        public EmployeeDto getEmployeeDetails(Long id, String token) {
            return new EmployeeDto(id, "Unknown", "unknown@fallback.com", null);
        }
    }

    // =========================================================
    // 2. RestTemplate vs WebClient vs Feign (COMPARISON)
    // =========================================================

    /**
     * ─────────────────────────────────────────────────────────
     * RestTemplate vs WebClient vs OpenFeign:
     * ─────────────────────────────────────────────────────────
     *
     * RestTemplate:
     *   - Synchronous, blocking HTTP client
     *   - Simple to use, widely understood
     *   - Spring 5+ deprecated (still functional, not removed)
     *   - Good for: simple sync calls, legacy code
     *   - Example:
     *     EmployeeDto emp = restTemplate.getForObject(
     *         "http://employee-service/api/employees/{id}", EmployeeDto.class, id);
     *
     * WebClient (Spring WebFlux):
     *   - Asynchronous, non-blocking HTTP client
     *   - Reactive streams (Mono/Flux)
     *   - Can be used in both Servlet (Spring MVC) and Reactive (WebFlux) apps
     *   - Higher throughput under load (fewer threads)
     *   - Good for: async/reactive, I/O-intensive operations
     *   - Example:
     *     Mono<EmployeeDto> empMono = webClient.get()
     *         .uri("/api/employees/{id}", id)
     *         .retrieve()
     *         .bodyToMono(EmployeeDto.class);
     *
     * OpenFeign:
     *   - Declarative: write interface, Spring generates implementation
     *   - Integrates natively with Eureka, Resilience4j, Spring Cloud LoadBalancer
     *   - Reduces boilerplate significantly
     *   - Synchronous (async Feign possible but less common)
     *   - Good for: microservices where you want clean, readable clients
     *   - Best choice for standard Spring Cloud microservices
     *
     * ┌─────────────────┬────────────┬─────────────┬──────────────┐
     * │ Feature         │RestTemplate│  WebClient  │   Feign      │
     * ├─────────────────┼────────────┼─────────────┼──────────────┤
     * │ Blocking?       │  Blocking  │ Non-blocking│  Blocking    │
     * │ Reactive?       │    No      │    Yes      │    No        │
     * │ Boilerplate     │  Medium    │   Medium    │    Low       │
     * │ Service Disc.   │  Manual    │   Manual    │  Automatic   │
     * │ Load Balancing  │  @LB bean  │  @LB bean   │  Automatic   │
     * │ Circuit Breaker │  Manual    │   Manual    │  Integrated  │
     * │ Status          │ Deprecated │  Preferred  │  Preferred   │
     * └─────────────────┴────────────┴─────────────┴──────────────┘
     */

    // =========================================================
    // 3. RESILIENCE4J - CIRCUIT BREAKER
    // =========================================================

    /**
     * CIRCUIT BREAKER STATES:
     * ─────────────────────────────────────────────────────────
     *
     *  CLOSED ──(failure rate > threshold)──> OPEN
     *  OPEN   ──(wait duration expires)────> HALF_OPEN
     *  HALF_OPEN ──(success)──────────────> CLOSED
     *  HALF_OPEN ──(failure)──────────────> OPEN
     *
     * Configuration in application.properties/yml:
     *   resilience4j.circuitbreaker.instances.employeeService.registerHealthIndicator=true
     *   resilience4j.circuitbreaker.instances.employeeService.slidingWindowSize=10
     *   resilience4j.circuitbreaker.instances.employeeService.minimumNumberOfCalls=5
     *   resilience4j.circuitbreaker.instances.employeeService.permittedNumberOfCallsInHalfOpenState=3
     *   resilience4j.circuitbreaker.instances.employeeService.waitDurationInOpenState=5s
     *   resilience4j.circuitbreaker.instances.employeeService.failureRateThreshold=50
     *   resilience4j.circuitbreaker.instances.employeeService.eventConsumerBufferSize=10
     */
    @Service
    static class EmployeeServiceWithCircuitBreaker {
        private static final Logger log = LoggerFactory.getLogger(EmployeeServiceWithCircuitBreaker.class);

        @Autowired
        private EmployeeServiceClient employeeServiceClient;

        /**
         * @CircuitBreaker wraps this method.
         * If employeeService fails past threshold -> circuit OPENS -> fallbackGetEmployee called.
         * name must match resilience4j.circuitbreaker.instances.<name> in config.
         */
        @CircuitBreaker(name = "employeeService", fallbackMethod = "fallbackGetEmployee")
        public EmployeeDto getEmployee(Long id) {
            log.info("Calling employee-service for id: {}", id);
            return employeeServiceClient.getEmployeeById(id);
        }

        /**
         * FALLBACK METHOD RULES:
         * 1. Same class as the annotated method
         * 2. Same return type
         * 3. Same parameters PLUS a Throwable parameter at the end
         * 4. Method name must match fallbackMethod attribute
         */
        public EmployeeDto fallbackGetEmployee(Long id, Throwable ex) {
            log.error("Circuit breaker fallback for getEmployee id={}, error={}", id, ex.getMessage());
            return new EmployeeDto(id, "Service Unavailable", "N/A", null);
        }

        /**
         * Combining Circuit Breaker + Retry:
         * Retry is applied first (inner), Circuit Breaker is outer.
         * Order: call -> Retry -> CircuitBreaker -> actual call
         * (Retry tries multiple times, each failure counted by Circuit Breaker)
         */
        @CircuitBreaker(name = "employeeService", fallbackMethod = "fallbackGetEmployeeList")
        @Retry(name = "employeeService")
        public List<EmployeeDto> getAllEmployees() {
            return employeeServiceClient.getAllEmployees();
        }

        public List<EmployeeDto> fallbackGetEmployeeList(Throwable ex) {
            log.error("Fallback getAllEmployees: {}", ex.getMessage());
            return List.of();
        }
    }

    // =========================================================
    // 4. RESILIENCE4J - RETRY
    // =========================================================

    /**
     * RETRY:
     * Automatically retry a failed call N times before giving up.
     * Good for: transient network errors, temporary unavailability.
     * NOT good for: non-idempotent operations (POST that creates data - may duplicate).
     *
     * Configuration:
     *   resilience4j.retry.instances.employeeService.maxAttempts=3
     *   resilience4j.retry.instances.employeeService.waitDuration=1s
     *   resilience4j.retry.instances.employeeService.enableExponentialBackoff=true
     *   resilience4j.retry.instances.employeeService.exponentialBackoffMultiplier=2
     *   resilience4j.retry.instances.employeeService.retryExceptions=java.io.IOException,...
     *   resilience4j.retry.instances.employeeService.ignoreExceptions=...
     */
    @Service
    static class EmployeeRetryService {
        private static final Logger log = LoggerFactory.getLogger(EmployeeRetryService.class);

        @Autowired
        private EmployeeServiceClient employeeServiceClient;

        /**
         * Will retry up to maxAttempts times with waitDuration between attempts.
         * If all retries fail, fallback is called.
         */
        @Retry(name = "employeeService", fallbackMethod = "fallbackRetryGetEmployee")
        public EmployeeDto getEmployeeWithRetry(Long id) {
            log.info("Attempting to get employee id={}", id);
            return employeeServiceClient.getEmployeeById(id);
        }

        public EmployeeDto fallbackRetryGetEmployee(Long id, Exception ex) {
            log.error("All retries exhausted for id={}: {}", id, ex.getMessage());
            return new EmployeeDto(id, "Retry Exhausted", "N/A", null);
        }

        /**
         * Retry for idempotent GET - safe to retry.
         * With exponential backoff: wait 1s, 2s, 4s between attempts.
         */
        @Retry(name = "employeeServiceWithBackoff")
        public List<EmployeeDto> getAllEmployeesWithRetry() {
            return employeeServiceClient.getAllEmployees();
        }
    }

    // =========================================================
    // 5. RESILIENCE4J - RATE LIMITER
    // =========================================================

    /**
     * RATE LIMITER:
     * Limits how many calls are allowed in a time period.
     * Protects downstream service from overload.
     * Returns RequestNotPermitted exception when limit exceeded.
     *
     * Configuration:
     *   resilience4j.ratelimiter.instances.employeeService.limitForPeriod=10
     *   resilience4j.ratelimiter.instances.employeeService.limitRefreshPeriod=1s
     *   resilience4j.ratelimiter.instances.employeeService.timeoutDuration=500ms
     *
     * Meaning: allow 10 calls per 1 second; wait up to 500ms for a permit.
     */
    @Service
    static class EmployeeRateLimitedService {
        private static final Logger log = LoggerFactory.getLogger(EmployeeRateLimitedService.class);

        @Autowired
        private EmployeeServiceClient employeeServiceClient;

        @RateLimiter(name = "employeeService", fallbackMethod = "rateLimitFallback")
        public EmployeeDto getEmployeeRateLimited(Long id) {
            return employeeServiceClient.getEmployeeById(id);
        }

        public EmployeeDto rateLimitFallback(Long id, Throwable ex) {
            log.warn("Rate limit exceeded for employee id={}: {}", id, ex.getMessage());
            // Return cached response or 429 Too Many Requests
            return new EmployeeDto(id, "Rate Limited", "N/A", null);
        }

        /**
         * Combining all Resilience4j annotations:
         * Execution order (outermost to innermost):
         * Bulkhead -> TimeLimiter -> RateLimiter -> CircuitBreaker -> Retry -> actual call
         */
        @CircuitBreaker(name = "employeeService", fallbackMethod = "combinedFallback")
        @RateLimiter(name = "employeeService")
        @Retry(name = "employeeService")
        public EmployeeDto getEmployeeFullResilience(Long id) {
            return employeeServiceClient.getEmployeeById(id);
        }

        public EmployeeDto combinedFallback(Long id, Throwable ex) {
            log.error("Combined resilience fallback for id={}: {}", id, ex.getMessage());
            return new EmployeeDto(id, "Service Unavailable", "N/A", null);
        }
    }

    // =========================================================
    // 6. @LoadBalanced RestTemplate
    // =========================================================

    /**
     * @LoadBalanced RestTemplate:
     * - Integrates with Spring Cloud LoadBalancer (replaces Ribbon)
     * - Use service name (from Eureka) instead of hostname in URL
     * - LoadBalancer resolves "employee-service" to an actual IP:port
     * - Automatically round-robins across multiple instances
     *
     * Must be declared as a @Bean with @LoadBalanced annotation.
     * Without @LoadBalanced, "http://employee-service/..." would fail (can't resolve hostname).
     */
    @Configuration
    static class RestTemplateConfig {

        /**
         * @LoadBalanced enables client-side load balancing.
         * Spring Cloud LoadBalancer intercepts requests and resolves
         * service names to actual instances from the service registry.
         */
        @Bean
        @LoadBalanced
        // In real code: org.springframework.web.client.RestTemplate
        org.springframework.web.client.RestTemplate loadBalancedRestTemplate() {
            return new org.springframework.web.client.RestTemplate();
        }

        /**
         * @LoadBalanced WebClient.Builder - reactive alternative.
         * Use service name in URI: http://employee-service/api/employees
         */
        @Bean
        @LoadBalanced
        WebClient.Builder loadBalancedWebClientBuilder() {
            return WebClient.builder();
        }
    }

    /**
     * Using @LoadBalanced RestTemplate in a service
     */
    @Service
    static class EmployeeClientWithRestTemplate {
        private static final Logger log = LoggerFactory.getLogger(EmployeeClientWithRestTemplate.class);

        // Inject the @LoadBalanced RestTemplate
        private final org.springframework.web.client.RestTemplate restTemplate;
        private final WebClient.Builder webClientBuilder;

        EmployeeClientWithRestTemplate(
                org.springframework.web.client.RestTemplate restTemplate,
                WebClient.Builder webClientBuilder) {
            this.restTemplate = restTemplate;
            this.webClientBuilder = webClientBuilder;
        }

        /**
         * RestTemplate usage - synchronous, blocking.
         * "employee-service" is resolved via Eureka to actual host:port.
         */
        public EmployeeDto getEmployeeByIdRestTemplate(Long id) {
            // Uses service name from Eureka - load balanced
            String url = "http://employee-service/api/employees/{id}";
            ResponseEntity<EmployeeDto> response = restTemplate.getForEntity(url, EmployeeDto.class, id);
            return response.getBody();
        }

        /**
         * WebClient usage - asynchronous, non-blocking.
         * .block() makes it synchronous (avoid in reactive stack).
         */
        public EmployeeDto getEmployeeByIdWebClient(Long id) {
            return webClientBuilder.build()
                    .get()
                    .uri("http://employee-service/api/employees/{id}", id)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Client error: " + body))
                    )
                    .bodyToMono(EmployeeDto.class)
                    .block(); // block() converts Mono to synchronous call
        }

        /**
         * WebClient - fully reactive (return Mono, don't block)
         */
        public reactor.core.publisher.Mono<EmployeeDto> getEmployeeReactive(Long id) {
            return webClientBuilder.build()
                    .get()
                    .uri("http://employee-service/api/employees/{id}", id)
                    .retrieve()
                    .bodyToMono(EmployeeDto.class)
                    .doOnSuccess(emp -> log.info("Found employee: {}", emp))
                    .doOnError(ex -> log.error("Error fetching employee: {}", ex.getMessage()));
        }
    }

    // =========================================================
    // 7. DISTRIBUTED TRACING WITH MDC (Correlation IDs)
    // =========================================================

    /**
     * DISTRIBUTED TRACING:
     * ─────────────────────────────────────────────────────────
     * Problem: A request flows across 5 microservices. How to trace it?
     * Solution: Assign a unique correlation ID at entry point (API Gateway),
     *           propagate it in every HTTP header and log entry.
     *
     * MDC (Mapped Diagnostic Context):
     * - Thread-local key-value store for logging context
     * - Add correlation ID to MDC -> all log statements in that thread include it
     * - Logback pattern: %X{correlationId} to print MDC value in log
     *
     * Spring Cloud Sleuth (auto-instruments):
     * - Adds trace ID and span ID to every request automatically
     * - Propagates via HTTP headers (X-B3-TraceId, X-B3-SpanId)
     * - Integrates with Zipkin for visualization
     *
     * Manual MDC approach (without Sleuth):
     */

    /**
     * Interceptor that extracts or generates correlation ID on every request
     * and puts it in MDC for structured logging.
     */
    @Component
    static class CorrelationIdInterceptor implements HandlerInterceptor {
        private static final Logger log = LoggerFactory.getLogger(CorrelationIdInterceptor.class);
        public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
        public static final String MDC_CORRELATION_KEY  = "correlationId";

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
            // Extract from incoming header (propagated from API Gateway or upstream service)
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);

            // Generate new one if not present (this is the entry point)
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
                log.debug("Generated new correlationId: {}", correlationId);
            }

            // Put in MDC - all subsequent log statements will include this
            MDC.put(MDC_CORRELATION_KEY, correlationId);

            // Also add to response header so client can correlate
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            return true; // continue processing
        }

        @Override
        public void afterCompletion(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler, Exception ex) {
            // IMPORTANT: Always clear MDC to avoid thread pool leakage
            MDC.remove(MDC_CORRELATION_KEY);
        }
    }

    /**
     * Feign interceptor to propagate correlation ID to downstream services
     * Ensures the correlation ID flows through all service calls.
     */
    @Component
    static class FeignCorrelationIdInterceptor implements feign.RequestInterceptor {
        @Override
        public void apply(feign.RequestTemplate template) {
            String correlationId = MDC.get(CorrelationIdInterceptor.MDC_CORRELATION_KEY);
            if (correlationId != null) {
                // Propagate to downstream service via HTTP header
                template.header(CorrelationIdInterceptor.CORRELATION_ID_HEADER, correlationId);
            }
        }
    }

    /**
     * WebClient filter to propagate correlation ID
     */
    static class WebClientCorrelationFilter {
        public static org.springframework.web.reactive.function.client.ExchangeFilterFunction correlationIdFilter() {
            return (request, next) -> {
                String correlationId = MDC.get(CorrelationIdInterceptor.MDC_CORRELATION_KEY);
                if (correlationId != null) {
                    request = org.springframework.web.reactive.function.client.ClientRequest
                            .from(request)
                            .header(CorrelationIdInterceptor.CORRELATION_ID_HEADER, correlationId)
                            .build();
                }
                return next.exchange(request);
            };
        }
    }

    /**
     * MDC usage in a service with correlation ID in logs.
     * logback-spring.xml pattern to include correlationId:
     *   <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [corrId=%X{correlationId}] - %msg%n</pattern>
     */
    @Service
    static class TracedEmployeeService {
        private static final Logger log = LoggerFactory.getLogger(TracedEmployeeService.class);

        public EmployeeDto processEmployee(Long id) {
            // correlationId is already in MDC from the interceptor
            // Every log line will automatically include it via logback pattern
            log.info("Processing employee id={}", id);    // includes correlationId
            log.debug("Fetching from DB for id={}", id);  // includes correlationId

            // When spawning a new thread, MDC must be copied manually:
            String correlationId = MDC.get(CorrelationIdInterceptor.MDC_CORRELATION_KEY);
            Thread asyncThread = new Thread(() -> {
                try {
                    MDC.put(CorrelationIdInterceptor.MDC_CORRELATION_KEY, correlationId);
                    log.info("Async processing for id={}", id); // still has correlationId
                } finally {
                    MDC.clear(); // always clear in spawned threads
                }
            });
            asyncThread.start();

            return new EmployeeDto(id, "John Doe", "john@example.com", 1L);
        }
    }

    // =========================================================
    // 8. CUSTOM HEALTH CHECK ENDPOINT
    // =========================================================

    /**
     * SPRING BOOT ACTUATOR HEALTH:
     * ─────────────────────────────────────────────────────────
     * Actuator exposes: GET /actuator/health
     * Built-in indicators: DB, Redis, Kafka, RabbitMQ, Disk Space, etc.
     *
     * Custom HealthIndicator: implement HealthIndicator interface.
     * Kubernetes uses: /actuator/health/liveness and /actuator/health/readiness
     *
     * application.properties:
     *   management.endpoints.web.exposure.include=health,info,metrics,prometheus
     *   management.endpoint.health.show-details=always
     *   management.health.circuitbreakers.enabled=true
     */

    /**
     * Custom Health Indicator for downstream employee-service dependency.
     * Appears under: GET /actuator/health/employeeServiceHealth
     */
    @Component("employeeServiceHealth") // bean name = health indicator name in /actuator/health
    static class EmployeeServiceHealthIndicator implements HealthIndicator {
        private static final Logger log = LoggerFactory.getLogger(EmployeeServiceHealthIndicator.class);

        private final org.springframework.web.client.RestTemplate restTemplate;

        EmployeeServiceHealthIndicator(org.springframework.web.client.RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @Override
        public Health health() {
            try {
                // Call the downstream service's health endpoint
                ResponseEntity<Map> response = restTemplate.getForEntity(
                        "http://employee-service/actuator/health", Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return Health.up()
                            .withDetail("employee-service", "Available")
                            .withDetail("status-code", response.getStatusCodeValue())
                            .build();
                } else {
                    return Health.down()
                            .withDetail("employee-service", "Returned non-2xx")
                            .withDetail("status-code", response.getStatusCodeValue())
                            .build();
                }
            } catch (Exception ex) {
                log.error("Health check failed for employee-service: {}", ex.getMessage());
                return Health.down()
                        .withDetail("employee-service", "Unreachable")
                        .withDetail("error", ex.getMessage())
                        .build();
            }
        }
    }

    /**
     * Custom Liveness Probe - Is the application alive?
     * Kubernetes kills and restarts pod if liveness check fails.
     * Should be simple - just check if app is running (not dependencies).
     */
    @Component
    static class CustomLivenessIndicator
            implements org.springframework.boot.actuate.availability.LivenessStateHealthIndicator {

        // Spring Boot auto-configures liveness at /actuator/health/liveness
        // This just extends the default - override health() to add custom logic
        CustomLivenessIndicator(
                org.springframework.boot.availability.ApplicationAvailability availability) {
            super(availability);
        }

        @Override
        public Health health() {
            // Custom liveness: check critical internal state (e.g., thread pool not exhausted)
            boolean healthy = checkCriticalInternalState();
            if (healthy) {
                return Health.up().withDetail("reason", "Application is running normally").build();
            }
            return Health.down().withDetail("reason", "Critical internal state failure").build();
        }

        private boolean checkCriticalInternalState() {
            // Example: check if application context is fully initialized
            // In practice: check if critical resources are available
            return true;
        }
    }

    /**
     * Custom Readiness Probe - Is the application ready to serve traffic?
     * Kubernetes stops sending traffic if readiness check fails (but doesn't restart).
     * Should check: DB connection, cache, required upstream services.
     */
    @Component
    static class DatabaseReadinessIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                // In a real app: check DB connection pool, etc.
                boolean dbReachable = checkDatabase();
                if (dbReachable) {
                    return Health.up()
                            .withDetail("database", "Connected")
                            .withDetail("pool-size", "10/10 available")
                            .build();
                }
                return Health.outOfService()
                        .withDetail("database", "Connection pool exhausted")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("database", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }

        private boolean checkDatabase() {
            // In real code: execute a simple query like SELECT 1
            return true;
        }
    }

    // =========================================================
    // FEIGN CLIENT CONFIGURATION CLASS
    // =========================================================

    /**
     * Custom Feign configuration - applied to specific Feign clients.
     * Can also be global by registering as @Bean in @Configuration class.
     * NOTE: Do NOT annotate this with @Configuration if used as FeignClient.configuration=
     *       to avoid it being picked up globally.
     */
    static class FeignClientConfig {

        // Custom error decoder - convert HTTP error responses to exceptions
        @Bean
        feign.codec.ErrorDecoder errorDecoder() {
            return (methodKey, response) -> {
                if (response.status() == 404) {
                    return new EmployeeNotFoundException("Resource not found: " + methodKey);
                }
                if (response.status() == 503) {
                    return new ServiceUnavailableException("Service unavailable: " + methodKey);
                }
                return new Exception("Generic error: status=" + response.status());
            };
        }

        // Custom timeout configuration
        @Bean
        feign.Request.Options requestOptions() {
            return new feign.Request.Options(
                    5000,  // connect timeout ms
                    10000  // read timeout ms
            );
        }

        // Logger level for Feign (useful for debugging)
        @Bean
        feign.Logger.Level feignLoggerLevel() {
            // NONE, BASIC, HEADERS, FULL
            return feign.Logger.Level.FULL; // use BASIC or NONE in production
        }
    }

    // =========================================================
    // SUPPORTING DTOs & EXCEPTIONS
    // =========================================================

    static class EmployeeDto {
        private Long id;
        private String name;
        private String email;
        private Long departmentId;

        public EmployeeDto() {}

        public EmployeeDto(Long id, String name, String email, Long departmentId) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.departmentId = departmentId;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        @Override
        public String toString() {
            return "EmployeeDto{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }

    static class DepartmentDto {
        private Long id;
        private String name;

        public DepartmentDto() {}
        public DepartmentDto(Long id, String name) { this.id = id; this.name = name; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class EmployeeNotFoundException extends RuntimeException {
        public EmployeeNotFoundException(String message) { super(message); }
    }

    static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) { super(message); }
    }
}

/*
 * ================================================================
 * QUICK INTERVIEW RECAP - MICROSERVICES
 * ================================================================
 *
 * Q: What is the difference between @FeignClient and RestTemplate?
 * A: Feign is declarative (interface-based), auto-integrates with Eureka/Resilience4j.
 *    RestTemplate is imperative (manual URL construction), deprecated in Spring 5+.
 *
 * Q: How does Circuit Breaker prevent cascading failures?
 * A: When failure rate exceeds threshold, circuit OPENS and immediately returns fallback
 *    without calling the downstream service. Periodically allows test calls (HALF_OPEN).
 *    Prevents overloading a failing service and frees calling service threads.
 *
 * Q: What is the difference between Retry and Circuit Breaker?
 * A: Retry: transient failures, try again with backoff (assumes service will recover quickly).
 *    Circuit Breaker: persistent failures, fast-fail until service recovers (prevents resource waste).
 *
 * Q: What is service discovery?
 * A: Dynamic lookup of service location (IP:port) from a registry (Eureka/Consul).
 *    Avoids hardcoding URLs. Supports dynamic scaling (new instances register automatically).
 *
 * Q: What is the Saga pattern?
 * A: Distributed transaction management across microservices using compensating transactions.
 *    Choreography: event-driven, no central coordinator.
 *    Orchestration: central saga manager directs each step.
 *
 * Q: What are the 3 pillars of observability?
 * A: Logs (ELK), Metrics (Prometheus+Grafana), Traces (Zipkin/Jaeger/OpenTelemetry).
 *
 * Q: What is CQRS?
 * A: Separate write model (Commands) from read model (Queries).
 *    Enables independent scaling, different storage for reads/writes.
 *
 * Q: How to handle distributed transactions without 2PC?
 * A: SAGA pattern. Each service has a compensating transaction.
 *    If step N fails, steps N-1, N-2... are compensated (rolled back).
 */
