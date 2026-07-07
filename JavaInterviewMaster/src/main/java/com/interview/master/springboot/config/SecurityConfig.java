package com.interview.master.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ============================================================
 * SPRING SECURITY - Complete Interview Guide
 * ============================================================
 *
 * WHAT IS SPRING SECURITY?
 *   A powerful, customizable authentication and access-control framework.
 *   It is the de-facto standard for securing Spring-based applications.
 *
 * ---------------------------------------------------------------
 * SPRING SECURITY REQUEST PROCESSING ARCHITECTURE:
 * ---------------------------------------------------------------
 *
 *  HTTP Request
 *      |
 *      v
 *  DelegatingFilterProxy  (Servlet Filter registered in web.xml / auto-registered by Spring Boot)
 *      |
 *      v
 *  FilterChainProxy  (Spring Security's main entry point)
 *      |
 *      v
 *  SecurityFilterChain  (ordered list of security filters for this request)
 *      |
 *      +-- SecurityContextPersistenceFilter  (1) loads SecurityContext from session
 *      +-- UsernamePasswordAuthenticationFilter (2) handles form login POST
 *      +-- BasicAuthenticationFilter  (3) handles Authorization: Basic <base64>
 *      +-- BearerTokenAuthenticationFilter (4) handles Authorization: Bearer <jwt>
 *      +-- ExceptionTranslationFilter  (5) catches auth/authz exceptions, redirects to login
 *      +-- FilterSecurityInterceptor   (6) access control decision (is user allowed?)
 *      |
 *      v
 *  DispatcherServlet -> Controller
 *
 * ---------------------------------------------------------------
 * AUTHENTICATION vs AUTHORIZATION:
 * ---------------------------------------------------------------
 *
 * Authentication = WHO are you? (Identity verification)
 *   - User provides credentials (username + password, JWT token, OAuth2 token)
 *   - Spring verifies credentials via AuthenticationManager
 *   - On success: stores Authentication object in SecurityContextHolder
 *   - Authentication object holds: Principal + Credentials + Authorities (roles)
 *
 * Authorization = WHAT can you do? (Permission check)
 *   - After authentication, check if user has required role/permission
 *   - Spring checks authorities in Authentication object
 *   - Configured via: HttpSecurity rules OR @PreAuthorize on methods
 *
 * ---------------------------------------------------------------
 * SecurityContextHolder:
 * ---------------------------------------------------------------
 *   - ThreadLocal storage holding Authentication for current request thread
 *   - SecurityContextHolder.getContext().getAuthentication()
 *   - After request: cleared automatically (prevents info leaking to other requests)
 *   - In async: must propagate manually (DelegatingSecurityContextExecutor)
 *
 * ---------------------------------------------------------------
 * AUTHENTICATION PROVIDERS:
 * ---------------------------------------------------------------
 *
 * ProviderManager implements AuthenticationManager
 *   -> delegates to list of AuthenticationProvider implementations
 *
 * DaoAuthenticationProvider:
 *   - Loads UserDetails via UserDetailsService.loadUserByUsername(username)
 *   - Compares provided password with stored password using PasswordEncoder
 *   - Most common provider for username/password authentication
 *
 * UserDetailsService interface:
 *   UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
 *   - Implement this to load user from DB, LDAP, etc.
 *
 * UserDetails interface:
 *   getUsername(), getPassword(), getAuthorities(), isEnabled(), isAccountNonLocked(), ...
 *
 * ---------------------------------------------------------------
 * PASSWORD ENCODING:
 * ---------------------------------------------------------------
 *
 * NEVER store plain-text passwords!
 *
 * BCryptPasswordEncoder:
 *   - BCrypt hashing algorithm with work factor (cost factor, default 10)
 *   - Salt is built into the hash (different hash every time, even same password)
 *   - Verify: encoder.matches(rawPassword, hashedPassword)
 *   - Work factor: higher = slower = more secure (10 = ~100ms per hash)
 *
 * Q: Why is BCrypt preferred over MD5/SHA?
 *   BCrypt is intentionally slow (adjustable work factor). MD5/SHA are fast,
 *   making brute-force attacks easy. BCrypt's slowness defeats brute-force.
 *
 * ---------------------------------------------------------------
 * JWT (JSON Web Token):
 * ---------------------------------------------------------------
 *
 * Structure: Header.Payload.Signature (each part base64url encoded)
 *
 * Header:  { "alg": "HS256", "typ": "JWT" }
 * Payload: { "sub": "alice", "roles": ["ADMIN"], "exp": 1700000000, "iat": 1699996400 }
 * Signature: HMACSHA256(base64(header) + "." + base64(payload), secretKey)
 *
 * JWT Flow:
 *   1. Client: POST /login { username, password }
 *   2. Server: validates credentials, creates JWT, returns it
 *   3. Client: stores JWT (localStorage or httpOnly cookie)
 *   4. Client: sends JWT in every request: Authorization: Bearer <token>
 *   5. Server: validates JWT signature, extracts claims, sets SecurityContext
 *
 * Stateless: server stores NO session. Any server in cluster can validate token.
 *
 * Q: Session-based vs JWT-based auth?
 *   Session:
 *     - Server stores session (stateful, memory/Redis)
 *     - Sticky session or centralized session store needed for scaling
 *     - Easy to invalidate (delete session from store)
 *     - Safer (token never sent to client, only session ID)
 *   JWT:
 *     - Server stores nothing (stateless, scalable)
 *     - No invalidation until expiry (mitigate with short expiry + refresh tokens)
 *     - Token exposed in client (XSS risk) or cookie (CSRF risk)
 *     - Larger payload than session cookie
 *
 * ---------------------------------------------------------------
 * CSRF (Cross-Site Request Forgery):
 * ---------------------------------------------------------------
 *
 * CSRF attack: malicious site tricks browser into making authenticated request
 *   to victim site (browser automatically sends cookies/session with request).
 *
 * CSRF protection: server generates CSRF token, embeds in forms/headers.
 *   Attacker can't read the CSRF token (same-origin policy), so they can't forge.
 *
 * Q: Why disable CSRF for REST APIs?
 *   REST APIs are stateless (no session/cookie by default).
 *   CSRF attacks rely on cookies being sent automatically by the browser.
 *   If API uses JWT in Authorization header (not cookie), CSRF isn't applicable.
 *   Mobile apps, SPAs using local storage: no CSRF risk.
 *   Enable CSRF if: you use cookie-based auth for your API.
 *
 * ---------------------------------------------------------------
 * CORS (Cross-Origin Resource Sharing):
 * ---------------------------------------------------------------
 *
 * Browser Same-Origin Policy: scripts on origin A can't call origin B.
 * CORS: server tells browser "it's OK for origin A to call me".
 *
 * Preflight request: browser sends OPTIONS request before actual request
 *   to check if CORS is allowed.
 *
 * Headers:
 *   Access-Control-Allow-Origin: http://localhost:3000
 *   Access-Control-Allow-Methods: GET, POST, PUT, DELETE
 *   Access-Control-Allow-Headers: Content-Type, Authorization
 *
 * Q: Why configure CORS in Spring Security AND not just Spring MVC?
 *   Security filters run BEFORE DispatcherServlet.
 *   If CORS preflight (OPTIONS) request is blocked by security, it never reaches
 *   Spring MVC's CORS config. Configure CORS in Security to allow preflight.
 *
 * ---------------------------------------------------------------
 * METHOD SECURITY ANNOTATIONS:
 * ---------------------------------------------------------------
 *
 * @PreAuthorize  - evaluated BEFORE method runs (blocks call if false)
 *   @PreAuthorize("hasRole('ADMIN')")
 *   @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
 *   @PreAuthorize("hasAuthority('EMPLOYEE_WRITE')")
 *   @PreAuthorize("authentication.name == #username")  // SpEL with method args
 *   @PreAuthorize("@customService.isAllowed(#id)")     // call a Spring bean
 *
 * @PostAuthorize - evaluated AFTER method runs (can check return value)
 *   @PostAuthorize("returnObject.owner == authentication.name")
 *
 * @Secured - simpler, only supports role names (no SpEL)
 *   @Secured({"ROLE_ADMIN", "ROLE_USER"})
 *
 * @RolesAllowed - JSR-250 standard annotation
 *   @RolesAllowed("ADMIN")
 *
 * hasRole('ADMIN')      -> checks for authority "ROLE_ADMIN" (adds prefix automatically)
 * hasAuthority('ADMIN') -> checks for exact authority "ADMIN" (no prefix added)
 *
 * ============================================================
 */
@Configuration
@EnableWebSecurity      // activates Spring Security's web security support
@EnableMethodSecurity(  // enables @PreAuthorize, @PostAuthorize, @Secured
        prePostEnabled = true,   // enables @PreAuthorize and @PostAuthorize (default true)
        securedEnabled = true,   // enables @Secured
        jsr250Enabled  = true    // enables @RolesAllowed (JSR-250)
)
public class SecurityConfig {

    // ================================================================
    // SECURITY FILTER CHAIN
    // ================================================================
    /**
     * SecurityFilterChain bean defines the HTTP security rules.
     *
     * In Spring Security 5.7+ / Spring Boot 3.x:
     *   The WebSecurityConfigurerAdapter was DEPRECATED and removed.
     *   New approach: declare a SecurityFilterChain @Bean.
     *
     * Multiple SecurityFilterChain beans can coexist, with @Order to prioritize.
     * Example: one chain for /api/** (JWT), another for /admin/** (form login).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ----------------------------------------------------------
            // CSRF configuration
            // ----------------------------------------------------------
            // Disabled for REST API: stateless, uses JWT/Bearer (not cookies)
            // If you had a browser-facing MVC app with sessions, KEEP csrf enabled.
            .csrf(AbstractHttpConfigurer::disable)

            // ----------------------------------------------------------
            // CORS configuration
            // ----------------------------------------------------------
            // Wire in our CORS config bean defined below
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ----------------------------------------------------------
            // URL-based authorization rules
            // ----------------------------------------------------------
            .authorizeHttpRequests(auth -> auth

                // Public endpoints - no authentication required
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/error").permitAll()

                // Public GET on employees (read-only access for everyone)
                .requestMatchers(HttpMethod.GET, "/api/v1/employees/**").permitAll()

                // Require authentication for all write operations
                .requestMatchers(HttpMethod.POST,   "/api/v1/employees/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/v1/employees/**").authenticated()
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/employees/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasRole("ADMIN")

                // Actuator management endpoints - ADMIN only
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // All other requests: require authentication
                .anyRequest().authenticated()
            )

            // ----------------------------------------------------------
            // HTTP Basic Authentication
            // ----------------------------------------------------------
            // Enables: Authorization: Basic base64(username:password)
            // In production REST APIs: prefer JWT Bearer tokens instead.
            // HTTP Basic sends credentials on EVERY request (no session).
            .httpBasic(Customizer.withDefaults())

            // ----------------------------------------------------------
            // Form Login (disabled for pure REST API)
            // ----------------------------------------------------------
            // .formLogin(Customizer.withDefaults())  // enables /login page
            .formLogin(AbstractHttpConfigurer::disable)

            // ----------------------------------------------------------
            // Session Management
            // ----------------------------------------------------------
            // REST APIs are STATELESS - no server-side session needed.
            // STATELESS means: SecurityContext is NOT saved between requests.
            // Each request must authenticate independently.
            // Interview Q: What does STATELESS session management mean?
            //   Server creates no HttpSession for security purposes.
            //   Token (Basic/Bearer) must be sent with every request.
            // .sessionManagement(session ->
            //     session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ----------------------------------------------------------
            // H2 Console frame options
            // ----------------------------------------------------------
            // H2 web console uses HTML frames (<frame>/<iframe>).
            // By default Spring Security adds X-Frame-Options: DENY header.
            // This causes H2 console to show blank page.
            // Disable frame options protection for H2 console only.
            .headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );

        return http.build();
    }

    // ================================================================
    // PASSWORD ENCODER
    // ================================================================
    /**
     * BCryptPasswordEncoder bean.
     *
     * Spring Security requires a PasswordEncoder for storing/verifying passwords.
     * BCrypt is the recommended choice:
     *   - Adaptive: work factor can be increased as hardware gets faster
     *   - Salt: automatically generates and embeds random salt in hash
     *   - Slow: intentional slowness thwarts brute-force attacks
     *
     * Strength parameter (default 10):
     *   - Determines number of bcrypt rounds: 2^strength iterations
     *   - 10 = ~100ms per hash on modern hardware (good balance)
     *   - 12 = ~400ms, 14 = ~1.5s (use higher for high-security systems)
     *
     * Usage:
     *   passwordEncoder.encode("rawPassword")         -> "$2a$10$..."
     *   passwordEncoder.matches("rawPassword", hash)  -> true/false
     *
     * NEVER compare passwords with == or .equals()! Always use matches().
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // ================================================================
    // IN-MEMORY USER DETAILS SERVICE
    // ================================================================
    /**
     * InMemoryUserDetailsManager - stores users in memory.
     * For DEVELOPMENT and TESTING only. Never use in production.
     *
     * Production alternatives:
     *   - JdbcUserDetailsManager: loads from relational database
     *   - Custom UserDetailsService: implement loadUserByUsername() to query your DB
     *   - LDAP: LdapAuthenticationProvider
     *   - OAuth2: use Spring Security OAuth2 Resource Server
     *
     * UserDetails interface:
     *   getUsername()        - login name
     *   getPassword()        - encoded password
     *   getAuthorities()     - collection of GrantedAuthority (roles/permissions)
     *   isEnabled()          - false = account disabled (403)
     *   isAccountNonExpired()- false = account expired (403)
     *   isAccountNonLocked() - false = account locked (403)
     *   isCredentialsNonExpired() - false = password expired (403)
     *
     * Role naming conventions:
     *   hasRole('ADMIN')       -> looks for GrantedAuthority "ROLE_ADMIN"
     *   hasAuthority('ADMIN')  -> looks for GrantedAuthority "ADMIN" (exact match)
     *   User.withUsername().roles("ADMIN") -> stores as "ROLE_ADMIN" automatically
     *
     * Interview Q: What's the difference between Role and Authority?
     *   Conceptually: Role = coarse-grained ("ADMIN"), Authority = fine-grained ("READ_EMPLOYEES")
     *   In Spring: Roles are authorities prefixed with "ROLE_"
     *   hasRole('X') checks for authority 'ROLE_X'
     *   hasAuthority('X') checks for exact authority 'X' (no prefix)
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Admin user: can read, write, delete
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")     // stored as ROLE_ADMIN, ROLE_USER
                // equivalent: .authorities("ROLE_ADMIN", "ROLE_USER")
                .build();

        // Regular user: can read and write, but not delete
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")              // stored as ROLE_USER
                .build();

        // Read-only viewer
        UserDetails viewer = User.builder()
                .username("viewer")
                .password(passwordEncoder().encode("viewer123"))
                .roles("VIEWER")
                // Could also set fine-grained authorities:
                // .authorities("ROLE_VIEWER", "EMPLOYEE_READ", "REPORT_READ")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        return new InMemoryUserDetailsManager(admin, user, viewer);
    }

    // ================================================================
    // CORS CONFIGURATION
    // ================================================================
    /**
     * CorsConfigurationSource bean.
     *
     * CORS = Cross-Origin Resource Sharing
     * Browser security: JS on origin A (http://localhost:3000) cannot
     * call origin B (http://localhost:8080) without CORS headers from B.
     *
     * allowedOrigins: which origins can call this API
     *   - In production: only list your actual frontend domains
     *   - "*" allows all origins (OK for public APIs, avoid for authenticated APIs)
     *
     * allowedMethods: which HTTP methods are allowed cross-origin
     *
     * allowedHeaders: which request headers are allowed
     *   - "Authorization" must be included if you send JWT tokens
     *
     * allowCredentials(true): allows cookies and Authorization header
     *   - CANNOT be combined with allowedOrigins("*") (security restriction)
     *   - Must use specific origins when allowCredentials = true
     *
     * maxAge: how long browser caches the preflight response (seconds)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins (frontend applications)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",    // React dev server
                "http://localhost:4200",    // Angular dev server
                "http://localhost:5173"     // Vite dev server
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // Allowed headers
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "X-Correlation-ID"
        ));

        // Headers exposed to browser JavaScript
        config.setExposedHeaders(List.of(
                "Location",
                "X-Correlation-ID"
        ));

        config.setAllowCredentials(true);  // allow Authorization header & cookies
        config.setMaxAge(3600L);           // cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // apply to all paths
        return source;
    }
}

// ================================================================
// EXAMPLE SERVICE SHOWING @PreAuthorize USAGE
// ================================================================

/**
 * EmployeeSecurityService - demonstrates method-level security.
 *
 * @PreAuthorize and @PostAuthorize use Spring Expression Language (SpEL):
 *
 * Common SpEL security expressions:
 *   hasRole('ADMIN')                     - user has ROLE_ADMIN
 *   hasAnyRole('ADMIN', 'MANAGER')       - user has any of these roles
 *   hasAuthority('EMPLOYEE_WRITE')       - exact authority match
 *   hasAnyAuthority('READ', 'WRITE')
 *   isAuthenticated()                    - user is logged in
 *   isAnonymous()                        - user is NOT logged in
 *   isFullyAuthenticated()               - not anonymous, not remember-me
 *   principal                            - the Authentication.getPrincipal() object
 *   authentication                       - full Authentication object
 *   authentication.name                  - logged-in username
 *   #paramName                           - method parameter by name (needs -parameters compile flag)
 *   returnObject                         - return value (in @PostAuthorize)
 *   @beanName.method()                   - call a Spring bean's method
 */
class EmployeeSecurityService {

    /**
     * Only ADMIN users can delete employees.
     * If not ADMIN: AccessDeniedException is thrown -> 403 Forbidden.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        System.out.println("Deleting employee " + id);
    }

    /**
     * ADMIN or USER can create employees.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void createEmployee(Object request) {
        System.out.println("Creating employee");
    }

    /**
     * Any authenticated user can read, but we check ownership.
     * The #id refers to the method parameter named 'id'.
     *
     * This checks: "is the logged-in user's name equal to the requested ID?"
     * Real use: "is this user requesting their own data?"
     *
     * Note: requires -parameters compiler flag or @Param annotation for parameter binding.
     */
    @PreAuthorize("isAuthenticated()")
    public Object getEmployee(Long id) {
        return null; // placeholder
    }

    /**
     * @PostAuthorize: runs AFTER method, checks return value.
     * If condition is false, throws AccessDeniedException (discards the return value).
     *
     * Use case: row-level security - user can only see records they own.
     * returnObject refers to the actual returned value.
     *
     * IMPORTANT: the method ALREADY ran (and may have had side effects).
     * @PostAuthorize only prevents returning the value, not the execution.
     * Use @PreAuthorize for preventing execution entirely.
     */
    @PreAuthorize("isAuthenticated()")
    // @PostAuthorize("returnObject.owner == authentication.name")
    public Object getOwnEmployee(Long id) {
        return null; // placeholder - real impl would fetch from DB
    }
}
