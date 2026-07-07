package com.interview.master.springboot.service;

import com.interview.master.springboot.dto.EmployeeDTO.*;
import com.interview.master.springboot.entity.Employee;
import com.interview.master.springboot.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ============================================================
 * SERVICE INTERFACE - EmployeeService
 * ============================================================
 *
 * Q: Why define a separate interface for the service?
 *
 *   1. ABSTRACTION / DECOUPLING:
 *      Controller depends on the interface, not the implementation.
 *      You can swap implementations (e.g., EmployeeServiceImpl -> MockEmployeeService for tests)
 *      without changing the controller.
 *
 *   2. PROXY-BASED AOP (critical for Spring):
 *      Spring's @Transactional, @Cacheable, @Async work via AOP proxies.
 *      When you inject EmployeeService (interface), Spring injects a JDK dynamic proxy
 *      that wraps EmployeeServiceImpl. The proxy intercepts method calls to apply
 *      transaction management, caching, async execution, etc.
 *
 *      JDK Proxy  : requires an interface (works when you inject by interface type).
 *      CGLIB Proxy: subclasses the concrete class (works even without interface).
 *      Spring Boot uses CGLIB by default (proxyTargetClass=true), but the interface
 *      is still good practice for testability and clean architecture.
 *
 *   3. TESTABILITY:
 *      In unit tests: @MockBean EmployeeService service; — Mockito mocks the interface.
 *      No real DB calls needed in controller unit tests.
 *
 *   4. MULTIPLE IMPLEMENTATIONS:
 *      e.g., EmployeeServiceImpl (real), CachedEmployeeService, RemoteEmployeeService.
 *
 *   5. CLEAN ARCHITECTURE:
 *      Interface = contract (stable). Implementation = details (can change freely).
 *
 * Q: When is a service interface optional?
 *   For simple CRUD-only services, some teams skip the interface.
 *   Spring Boot works fine with just the @Service class (CGLIB proxy is used).
 *   However, interfaces are REQUIRED when using JDK-proxy-based mocking in tests.
 *
 * ============================================================
 */
public interface EmployeeService {

    // --- CRUD Operations ---
    EmployeeResponse createEmployee(EmployeeCreateRequest request);
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse getEmployeeByEmail(String email);
    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);
    void deleteEmployee(Long id);

    // --- Query Operations ---
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);
    Page<EmployeeResponse> getEmployeesByDepartment(String department, Pageable pageable);
    List<EmployeeResponse> getEmployeesBySalaryRange(Double min, Double max);
    List<DepartmentStats> getDepartmentStatistics();

    // --- Business Operations ---
    EmployeeResponse updateStatus(Long id, EmployeeStatus status);
    int giveDepartmentRaise(String department, Double percentage);

    // --- Async Operation ---
    CompletableFuture<List<EmployeeResponse>> getAllEmployeesAsync();
}
