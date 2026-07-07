package com.interview.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ============================================================
 * JAVA INTERVIEW MASTER - COMPLETE GUIDE
 * ============================================================
 * This project covers ALL Java Core + Spring Boot concepts
 * needed to crack any Java Developer Interview (Fresher to 10+ years)
 *
 * TOPICS COVERED:
 * ► Java Core: OOP, Collections, Generics, Exceptions, Multithreading
 * ► Java 8+: Lambda, Streams, Optional, Functional Interfaces
 * ► Design Patterns: Singleton, Factory, Builder, Observer, Strategy
 * ► Spring Boot: IoC, DI, MVC, JPA, Security, AOP, Caching, Scheduling
 * ► Spring: REST APIs, Validation, Exception Handling, Events
 * ► Testing: JUnit 5, Mockito, Spring Boot Test
 * ============================================================
 *
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * - @Configuration: Marks class as source of Spring bean definitions
 * - @EnableAutoConfiguration: Tells Spring Boot to auto-configure beans based on classpath
 * - @ComponentScan: Scans the package for @Component, @Service, @Repository, @Controller
 *
 * @EnableCaching - Activates Spring's annotation-driven cache management capability
 * @EnableScheduling - Enables Spring's scheduled task execution capability (@Scheduled)
 * @EnableAsync - Enables Spring's asynchronous method execution capability (@Async)
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class JavaInterviewMasterApplication {
    public static void main(String[] args) {
        // SpringApplication.run() - Bootstraps the Spring application
        // Creates ApplicationContext, registers beans, starts embedded server (Tomcat by default)
        SpringApplication.run(JavaInterviewMasterApplication.class, args);
        System.out.println("🚀 Java Interview Master Application Started!");
        System.out.println("📚 Visit: http://localhost:8080/api/employees");
        System.out.println("🔍 H2 Console: http://localhost:8080/h2-console");
        System.out.println("📊 Actuator: http://localhost:8080/actuator");
    }
}
