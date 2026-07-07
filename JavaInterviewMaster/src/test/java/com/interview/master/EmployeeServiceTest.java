package com.interview.master;

/**
 * ============================================================
 * JUNIT 5 + MOCKITO + SPRING BOOT TEST — Complete Interview Guide
 * ============================================================
 *
 * JUnit 5 Architecture (three sub-projects):
 *
 *   JUnit Platform  — foundation layer; discovers and launches test frameworks on the JVM.
 *                     Defines the TestEngine SPI that any framework can implement.
 *
 *   JUnit Jupiter   — the new JUnit 5 API: @Test, @BeforeEach, Assertions, Extensions, etc.
 *
 *   JUnit Vintage   — backward-compat engine; runs JUnit 3 and JUnit 4 tests unchanged.
 *
 * ── JUnit 5 Annotations ────────────────────────────────────────
 *   @Test              — marks a test method
 *   @BeforeEach        — before EACH test  (was @Before in JUnit 4)
 *   @AfterEach         — after  EACH test  (was @After in JUnit 4)
 *   @BeforeAll         — once before ALL tests; must be static  (was @BeforeClass)
 *   @AfterAll          — once after  ALL tests; must be static  (was @AfterClass)
 *   @DisplayName       — human-readable name in test reports
 *   @Disabled          — skip test or class  (was @Ignore)
 *   @ParameterizedTest — run same test body with multiple inputs
 *   @ValueSource       — supply a single-type array of values
 *   @CsvSource         — CSV rows: one row = one invocation
 *   @MethodSource      — static factory method returning Stream<Arguments>
 *   @EnumSource        — all or selected enum constants
 *   @NullSource        — pass null
 *   @EmptySource       — pass empty String / Collection / array
 *   @NullAndEmptySource — @NullSource + @EmptySource combined
 *   @RepeatedTest(n)   — run same test n times
 *   @Timeout(n)        — fail if test exceeds n seconds
 *   @Tag               — categorise tests for CI filtering
 *   @Nested            — non-static inner class as a nested test class
 *   @TestMethodOrder   — control execution order
 *   @TestInstance      — PER_METHOD (default) or PER_CLASS lifecycle
 *   @ExtendWith        — register JUnit 5 extensions
 *
 * ── JUnit 5 Assertions ─────────────────────────────────────────
 *   assertEquals / assertNotEquals      — value equality (.equals())
 *   assertTrue / assertFalse            — boolean conditions
 *   assertNull / assertNotNull          — null checks
 *   assertSame / assertNotSame          — reference identity (==)
 *   assertThrows(type, executable)      — must throw that exception; returns it
 *   assertDoesNotThrow(executable)      — must not throw
 *   assertAll(executables…)             — run ALL assertions; report all failures
 *   assertTimeout(duration, executable) — must finish within duration
 *   assertArrayEquals                   — array element equality
 *   assertIterableEquals                — iterable element equality
 *   fail(message)                       — unconditional failure
 *
 * ── Mockito Concepts ───────────────────────────────────────────
 *   Mock  — fully fake; every method returns null/0/false unless stubbed
 *   Stub  — mock configured to return values: when().thenReturn()
 *   Spy   — wraps a REAL object; real methods run unless stubbed
 *
 *   @Mock        — creates a mock
 *   @Spy         — creates a spy around a real instance
 *   @InjectMocks — instantiates SUT and injects @Mock / @Spy fields
 *   @Captor      — shorthand for ArgumentCaptor.forClass(T.class)
 *   @ExtendWith(MockitoExtension.class) — enables strict-stubbing Mockito in JUnit 5
 *
 *   Stubbing:
 *     when(mock.m()).thenReturn(v)           — return value
 *     when(mock.m()).thenThrow(ex)           — throw exception
 *     when(mock.m()).thenAnswer(inv -> …)    — dynamic / context-aware response
 *     when(mock.m()).thenReturn(v1).thenReturn(v2) — chain: different per call
 *     doReturn(v).when(spy).m()             — spy stub without calling real method
 *     doThrow(ex).when(mock).voidMethod()   — void method throw
 *     doNothing().when(mock).voidMethod()   — explicit no-op
 *     doAnswer(inv->…).when(mock).m()       — dynamic for void / spy
 *
 *   Verification:
 *     verify(mock).m()                      — called exactly once
 *     verify(mock, times(n)).m()            — exactly n times
 *     verify(mock, never()).m()             — never called
 *     verify(mock, atLeast(n)).m()          — at least n times
 *     verify(mock, timeout(ms)).m()         — async: passes once called within timeout
 *     verifyNoMoreInteractions(mock)        — no unexpected calls after verify()
 *     InOrder order = inOrder(m1,m2);       — verify call order
 *
 *   Matchers (ArgumentMatchers.*):
 *     any(), any(Type.class), anyString(), anyLong()
 *     eq(value), isNull(), isNotNull(), argThat(predicate)
 *     RULE: if one arg uses a matcher, ALL args must use matchers
 *
 *   ArgumentCaptor:
 *     captor.capture()      — inside verify() to record the argument
 *     captor.getValue()     — last captured value
 *     captor.getAllValues() — all captured values across multiple calls
 *
 * ── Spring Boot Test Annotations ──────────────────────────────
 *   @SpringBootTest      — full ApplicationContext; slowest
 *     webEnvironment:
 *       MOCK (default)   — mock servlet; use MockMvc
 *       RANDOM_PORT      — real Tomcat on random port; use TestRestTemplate
 *   @WebMvcTest(Ctrl)    — only web layer; no @Service/@Repository
 *   @DataJpaTest         — only JPA layer; H2 replaces DataSource; rolls back per test
 *   @MockBean            — Mockito mock as a Spring bean
 *   @SpyBean             — Mockito spy wrapping a Spring bean
 *   MockMvc              — fake HTTP through DispatcherServlet; no real server
 *   TestRestTemplate     — real HTTP for RANDOM_PORT tests
 *   @WithMockUser        — (spring-security-test) mock authenticated user
 * ============================================================
 */

// ── All imports in ONE place — shared by every nested class ────
import com.interview.master.springboot.controller.EmployeeController;
import com.interview.master.springboot.controller.EmployeeController.EmployeeDTO;
import com.interview.master.springboot.controller.EmployeeController.CreateEmployeeRequest;
import com.interview.master.springboot.exception.EmployeeNotFoundException;
import com.interview.master.springboot.exception.DuplicateEmailException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

// Static imports — apply to ALL nested classes inside EmployeeServiceTest
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ============================================================
// OUTER SHELL
// All sections live as @Nested inner classes so they share
// the imports above — solving the "cannot find symbol" errors
// that appear when multiple top-level classes share one file.
// ============================================================

/**
 * Master test class for interview preparation.
 * Each @Nested inner class is a self-contained section.
 */
class EmployeeServiceTest {

    // ============================================================
    // SHARED HELPER TYPES (package-private, visible to all sections)
    // ============================================================

    /**
     * Service contract — what the unit tests mock.
     * In a real layered project this lives in the springboot.service package.
     */
    interface EmployeeService {
        List<EmployeeDTO>  getAllEmployees();
        EmployeeDTO        getEmployeeById(Long id);
        EmployeeDTO        createEmployee(CreateEmployeeRequest req);
        EmployeeDTO        updateEmployee(Long id, CreateEmployeeRequest req);
        void               deleteEmployee(Long id);
        List<EmployeeDTO>  getEmployeesByDepartment(String dept);
    }

    /**
     * In-memory EmployeeService implementation — used by Spy tests.
     * Mirrors the in-memory store inside EmployeeController.
     */
    static class EmployeeServiceImpl implements EmployeeService {

        private final Map<Long, EmployeeDTO> store = new ConcurrentHashMap<>();
        private final AtomicLong idSeq = new AtomicLong(1);

        private EmployeeDTO findOrThrow(Long id) {
            EmployeeDTO e = store.get(id);
            if (e == null)
                throw new EmployeeNotFoundException("Employee not found with id: " + id);
            return e;
        }

        @Override public List<EmployeeDTO> getAllEmployees() { return new ArrayList<>(store.values()); }
        @Override public EmployeeDTO getEmployeeById(Long id) { return findOrThrow(id); }

        @Override
        public EmployeeDTO createEmployee(CreateEmployeeRequest req) {
            store.values().stream()
                 .filter(e -> e.getEmail().equalsIgnoreCase(req.getEmail()))
                 .findFirst()
                 .ifPresent(e -> { throw new DuplicateEmailException(
                         "Email already exists: " + req.getEmail()); });
            long newId = idSeq.getAndIncrement();
            EmployeeDTO dto = new EmployeeDTO(newId, req.getName(), req.getEmail(),
                    req.getDepartment(), req.getSalary(), req.getLevel(), LocalDateTime.now());
            store.put(newId, dto);
            return dto;
        }

        @Override
        public EmployeeDTO updateEmployee(Long id, CreateEmployeeRequest req) {
            findOrThrow(id);
            EmployeeDTO updated = new EmployeeDTO(id, req.getName(), req.getEmail(),
                    req.getDepartment(), req.getSalary(), req.getLevel(), LocalDateTime.now());
            store.put(id, updated);
            return updated;
        }

        @Override
        public void deleteEmployee(Long id) { findOrThrow(id); store.remove(id); }

        @Override
        public List<EmployeeDTO> getEmployeesByDepartment(String dept) {
            List<EmployeeDTO> result = new ArrayList<>();
            for (EmployeeDTO e : store.values())
                if (e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(dept))
                    result.add(e);
            return result;
        }

        void seed(EmployeeDTO dto) { store.put(dto.getId(), dto); idSeq.set(dto.getId() + 1); }
        void clear()               { store.clear(); idSeq.set(1); }
    }

    /** Factory helpers — avoid repeating the verbose EmployeeDTO / CreateEmployeeRequest constructors. */
    static EmployeeDTO dto(Long id, String name, String email,
                           String dept, double salary, String level) {
        return new EmployeeDTO(id, name, email, dept, salary, level, LocalDateTime.now());
    }

    static CreateEmployeeRequest req(String name, String email,
                                     String dept, double salary, String level) {
        CreateEmployeeRequest r = new CreateEmployeeRequest();
        r.setName(name); r.setEmail(email);
        r.setDepartment(dept); r.setSalary(salary); r.setLevel(level);
        return r;
    }

    // ============================================================
    // ============================================================
    //  SECTION 1 — UNIT TESTS WITH MOCKITO
    //
    //  @ExtendWith(MockitoExtension.class) is the JUnit 5 way to enable
    //  Mockito. It replaces the old MockitoAnnotations.openMocks(this) call.
    //  The extension processes @Mock / @Spy / @InjectMocks / @Captor before
    //  each test and enforces STRICT STUBBING (unused stubs throw).
    // ============================================================
    // ============================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("1. EmployeeService — Pure Unit Tests")
    @Tag("unit")
    class EmployeeServiceUnitTest {

        /**
         * @Mock creates a Mockito mock.
         * Every method returns null / 0 / false / empty unless explicitly stubbed.
         */
        @Mock EmployeeService employeeService;

        /**
         * @Captor is shorthand for ArgumentCaptor.forClass(CreateEmployeeRequest.class).
         * The type parameter is inferred from the field declaration.
         */
        @Captor ArgumentCaptor<CreateEmployeeRequest> requestCaptor;

        EmployeeDTO emp1, emp2;
        CreateEmployeeRequest createReq;

        /**
         * @BeforeEach — before EVERY test. Rebuild fixtures so no test pollutes another.
         * Interview Note: was @Before in JUnit 4.
         */
        @BeforeEach
        void setUp() {
            emp1      = dto(1L, "Alice Smith", "alice@co.com", "Engineering", 85_000.0, "SENIOR");
            emp2      = dto(2L, "Bob Johnson", "bob@co.com",   "Marketing",   70_000.0, "MID");
            createReq = req("Charlie Brown", "charlie@co.com", "Engineering", 90_000.0, "JUNIOR");
        }

        /** @AfterEach — after EVERY test. Interview Note: was @After in JUnit 4. */
        @AfterEach
        void tearDown() { System.out.println("[afterEach] done"); }

        /** @BeforeAll — once before any test; must be static. Interview Note: was @BeforeClass. */
        @BeforeAll
        static void initSuite() { System.out.println("=== Unit suite starting ==="); }

        /** @AfterAll — once after all tests; must be static. Interview Note: was @AfterClass. */
        @AfterAll
        static void cleanUpSuite() { System.out.println("=== Unit suite done ==="); }

        // ── 1.1  getAllEmployees ──────────────────────────────

        @Test
        @DisplayName("getAllEmployees — returns all employees")
        void testGetAllEmployees_Success() {
            // ARRANGE: when().thenReturn() is the core Mockito stubbing pattern
            when(employeeService.getAllEmployees()).thenReturn(List.of(emp1, emp2));

            // ACT
            List<EmployeeDTO> result = employeeService.getAllEmployees();

            // ASSERT
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Alice Smith", result.get(0).getName());

            // VERIFY: check the interaction actually happened
            verify(employeeService, times(1)).getAllEmployees();
            verifyNoMoreInteractions(employeeService);
        }

        @Test
        @DisplayName("getAllEmployees — returns empty list when no employees exist")
        void testGetAllEmployees_Empty() {
            when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
            List<EmployeeDTO> result = employeeService.getAllEmployees();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        // ── 1.2  getEmployeeById ──────────────────────────────

        @Test
        @DisplayName("getEmployeeById — returns employee when found")
        void testGetEmployeeById_Found() {
            when(employeeService.getEmployeeById(1L)).thenReturn(emp1);
            EmployeeDTO result = employeeService.getEmployeeById(1L);

            // assertAll: every assertion runs even if one fails — complete failure report
            assertAll("Employee fields",
                () -> assertNotNull(result),
                () -> assertEquals(1L,             result.getId()),
                () -> assertEquals("Alice Smith",  result.getName()),
                () -> assertEquals("alice@co.com", result.getEmail()),
                () -> assertEquals("Engineering",  result.getDepartment()),
                () -> assertEquals(85_000.0,       result.getSalary())
            );
            verify(employeeService).getEmployeeById(1L);
        }

        @Test
        @DisplayName("getEmployeeById — throws EmployeeNotFoundException when not found")
        void testGetEmployeeById_NotFound_ThrowsException() {
            when(employeeService.getEmployeeById(999L))
                .thenThrow(new EmployeeNotFoundException("Employee not found with id: 999"));

            // assertThrows returns the exception so you can inspect its message
            EmployeeNotFoundException ex = assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(999L)
            );
            assertTrue(ex.getMessage().contains("999"));
            verify(employeeService).getEmployeeById(999L);
        }

        // ── 1.3  createEmployee ──────────────────────────────

        @Test
        @DisplayName("createEmployee — saves and returns new employee")
        void testCreateEmployee_Success() {
            EmployeeDTO saved = dto(3L, "Charlie Brown", "charlie@co.com", "Engineering", 90_000.0, "JUNIOR");
            when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(saved);

            EmployeeDTO result = employeeService.createEmployee(createReq);

            assertNotNull(result);
            assertEquals(3L, result.getId());
            assertEquals("Charlie Brown", result.getName());

            // ArgumentCaptor — capture and assert the exact argument passed to the mock
            verify(employeeService).createEmployee(requestCaptor.capture());
            CreateEmployeeRequest captured = requestCaptor.getValue();
            assertEquals("charlie@co.com", captured.getEmail());
            assertEquals("Engineering",    captured.getDepartment());
        }

        @Test
        @DisplayName("createEmployee — throws DuplicateEmailException for existing email")
        void testCreateEmployee_DuplicateEmail_ThrowsException() {
            when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new DuplicateEmailException("Email already exists: charlie@co.com"));

            DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
                () -> employeeService.createEmployee(createReq));
            assertTrue(ex.getMessage().contains("charlie@co.com"));
        }

        // ── 1.4  updateEmployee ──────────────────────────────

        @Test
        @DisplayName("updateEmployee — returns updated employee")
        void testUpdateEmployee_Success() {
            EmployeeDTO updated = dto(1L, "Alice Updated", "alice.new@co.com", "Engineering", 95_000.0, "LEAD");
            when(employeeService.updateEmployee(eq(1L), any(CreateEmployeeRequest.class)))
                .thenReturn(updated);

            EmployeeDTO result = employeeService.updateEmployee(1L, createReq);

            assertEquals("Alice Updated",    result.getName());
            assertEquals("alice.new@co.com", result.getEmail());
            assertEquals(95_000.0,           result.getSalary());
        }

        // ── 1.5  deleteEmployee ──────────────────────────────

        @Test
        @DisplayName("deleteEmployee — completes without exception for existing employee")
        void testDeleteEmployee_Success() {
            doNothing().when(employeeService).deleteEmployee(1L);
            assertDoesNotThrow(() -> employeeService.deleteEmployee(1L));
            verify(employeeService).deleteEmployee(1L);
        }

        @Test
        @DisplayName("deleteEmployee — throws EmployeeNotFoundException for missing employee")
        void testDeleteEmployee_NotFound() {
            doThrow(new EmployeeNotFoundException("Employee not found with id: 99"))
                .when(employeeService).deleteEmployee(99L);
            assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(99L));
        }

        // ── 1.6  getEmployeesByDepartment ────────────────────

        @Test
        @DisplayName("getEmployeesByDepartment — returns filtered list")
        void testGetEmployeesByDepartment() {
            when(employeeService.getEmployeesByDepartment("Engineering")).thenReturn(List.of(emp1));
            List<EmployeeDTO> result = employeeService.getEmployeesByDepartment("Engineering");
            assertEquals(1, result.size());
            assertEquals("Alice Smith", result.get(0).getName());
            verify(employeeService).getEmployeesByDepartment("Engineering");
        }

        // ── 1.7  @Timeout ─────────────────────────────────────

        @Test
        @Timeout(2)
        @DisplayName("getAllEmployees — must complete within 2 seconds")
        void testGetAllEmployees_Timeout() {
            when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
            assertNotNull(employeeService.getAllEmployees());
        }

        // ── 1.8  @RepeatedTest ────────────────────────────────

        /**
         * @RepeatedTest(n) runs the same test n times.
         * RepetitionInfo is injected automatically by JUnit 5.
         * Use for: consistency checks, flaky-test detection, perf baselines.
         */
        @RepeatedTest(3)
        @DisplayName("getAllEmployees — consistent across repeated runs")
        void testGetAllEmployees_Repeated(RepetitionInfo info) {
            when(employeeService.getAllEmployees()).thenReturn(List.of(emp1));
            List<EmployeeDTO> result = employeeService.getAllEmployees();
            assertEquals(1, result.size(),
                "Repetition " + info.getCurrentRepetition() + "/" + info.getTotalRepetitions());
        }

        // ── 1.9  @Disabled ───────────────────────────────────

        @Test
        @Disabled("Bulk import feature pending — JIRA-1234")
        @DisplayName("testBulkImport — not yet implemented")
        void testBulkImport() { fail("Not yet implemented"); }

        // ── 1.10  thenAnswer — dynamic stubbing ──────────────

        /**
         * thenAnswer gives access to InvocationOnMock:
         *   invocation.getArgument(0) — first arg
         *   invocation.callRealMethod() — call through to real object (spy)
         * Use when return value must be DERIVED from the input argument.
         */
        @Test
        @DisplayName("createEmployee — thenAnswer derives return value from input")
        void testCreateEmployee_WithThenAnswer() {
            when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenAnswer(inv -> {
                    CreateEmployeeRequest r = inv.getArgument(0);
                    return dto(42L, r.getName(), r.getEmail(),
                               r.getDepartment(), r.getSalary(), r.getLevel());
                });

            EmployeeDTO result = employeeService.createEmployee(createReq);
            assertEquals(42L,             result.getId());
            assertEquals("Charlie Brown", result.getName());
        }

        // ── 1.11  thenReturn chaining ─────────────────────────

        /**
         * Chained thenReturn() returns different values on successive calls.
         * After all chained values are used, the LAST one repeats.
         */
        @Test
        @DisplayName("getEmployeeById — first call returns emp; second throws")
        void testGetEmployeeById_ThenReturn_Chaining() {
            when(employeeService.getEmployeeById(1L))
                .thenReturn(emp1)
                .thenThrow(new EmployeeNotFoundException("Already deleted"));

            assertEquals("Alice Smith", employeeService.getEmployeeById(1L).getName());
            assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(1L));
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 2 — PARAMETERIZED TESTS
    //
    //  Parameterized tests avoid copy-paste by running one test body
    //  with many different inputs.
    //
    //  Source annotations:
    //    @ValueSource     — single-type values
    //    @CsvSource       — multiple typed params, inline CSV
    //    @MethodSource    — complex objects from a static Stream<Arguments>
    //    @EnumSource      — enum constants
    //    @NullAndEmptySource — null + empty edge cases
    // ============================================================
    // ============================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("2. Parameterized Tests")
    @Tag("unit")
    class EmployeeServiceParameterizedTest {

        @Mock EmployeeService employeeService;

        // ── 2.1  @ValueSource ────────────────────────────────

        /**
         * @ValueSource: one value per invocation.
         * Supported types: ints, longs, doubles, floats, bytes, shorts,
         *                  chars, booleans, strings, classes.
         */
        @ParameterizedTest(name = "[{index}] dept=''{0}'' returns empty list")
        @ValueSource(strings = {"Engineering", "Marketing", "HR", "Finance", "Legal"})
        @DisplayName("getEmployeesByDepartment — empty result for various departments")
        void testGetEmployeesByDepartment_EmptyResult(String department) {
            when(employeeService.getEmployeesByDepartment(department))
                .thenReturn(Collections.emptyList());

            List<EmployeeDTO> result = employeeService.getEmployeesByDepartment(department);

            assertNotNull(result);
            assertTrue(result.isEmpty(), "Expected empty for: " + department);
        }

        // ── 2.2  @CsvSource ──────────────────────────────────

        /**
         * @CsvSource: CSV row per invocation.
         * Single quotes wrap values containing commas/spaces.
         */
        @ParameterizedTest(name = "[{index}] id={0} name={1}")
        @CsvSource({
            "1, 'Alice Smith',   85000.0, Engineering, SENIOR",
            "2, 'Bob Johnson',   70000.0, Marketing,   MID",
            "3, 'Charlie Brown', 90000.0, Engineering, JUNIOR"
        })
        @DisplayName("getEmployeeById — correct fields for various employees")
        void testGetEmployeeById_CsvSource(Long id, String name,
                                           Double salary, String dept, String level) {
            when(employeeService.getEmployeeById(id))
                .thenReturn(dto(id, name, name.replace(" ", ".").toLowerCase() + "@co.com",
                               dept, salary, level));

            EmployeeDTO result = employeeService.getEmployeeById(id);

            assertAll(
                () -> assertEquals(id,    result.getId()),
                () -> assertEquals(name,  result.getName()),
                () -> assertEquals(salary, result.getSalary()),
                () -> assertEquals(dept,  result.getDepartment()),
                () -> assertEquals(level, result.getLevel())
            );
        }

        // ── 2.3  @MethodSource ───────────────────────────────

        /**
         * @MethodSource: points to a static factory returning Stream<Arguments>.
         * Best for: complex objects, many parameters, runtime-computed data.
         */
        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.interview.master.EmployeeServiceTest#provideCreateRequests")
        @DisplayName("createEmployee — succeeds for various valid inputs")
        void testCreateEmployee_MethodSource(String description,
                                             CreateEmployeeRequest request,
                                             Long expectedId) {
            when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(dto(expectedId, request.getName(), request.getEmail(),
                               request.getDepartment(), request.getSalary(), request.getLevel()));

            EmployeeDTO result = employeeService.createEmployee(request);

            assertNotNull(result);
            assertEquals(expectedId,        result.getId());
            assertEquals(request.getName(), result.getName());
        }

        // ── 2.4  @NullAndEmptySource ─────────────────────────

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("getEmployeesByDepartment — handles null and empty input")
        void testGetEmployeesByDepartment_NullAndEmpty(String department) {
            when(employeeService.getEmployeesByDepartment(department))
                .thenReturn(Collections.emptyList());

            List<EmployeeDTO> result = employeeService.getEmployeesByDepartment(department);
            assertNotNull(result);
        }
    }

    /**
     * Factory method for @MethodSource in Section 2.
     * Must be accessible from the ParameterizedTest annotation's fully-qualified path.
     * Declared at the outer class level so it can be static.
     */
    static Stream<Arguments> provideCreateRequests() {
        return Stream.of(
            Arguments.of("Engineer",
                req("Dave",  "dave@co.com",  "Engineering", 95_000.0, "SENIOR"), 10L),
            Arguments.of("Marketer",
                req("Eve",   "eve@co.com",   "Marketing",   75_000.0, "MID"),    11L),
            Arguments.of("HR Specialist",
                req("Frank", "frank@co.com", "HR",          65_000.0, "JUNIOR"), 12L)
        );
    }

    // ============================================================
    // ============================================================
    //  SECTION 3 — @Nested TESTS
    //
    //  @Nested inner classes group related tests under one context.
    //  Benefits:
    //    - Readability: reads like BDD (Given/When/Then)
    //    - Each @Nested class has its own @BeforeEach / @AfterEach
    //    - Outer @BeforeEach runs FIRST, then inner @BeforeEach
    //
    //  Restriction: @Nested classes must be non-static (inner classes).
    //  @BeforeAll in @Nested requires @TestInstance(PER_CLASS).
    // ============================================================
    // ============================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("3. createEmployee — Nested Test Groups")
    @Tag("unit")
    class CreateEmployeeNestedTest {

        @Mock EmployeeService employeeService;
        @Captor ArgumentCaptor<CreateEmployeeRequest> captor;

        CreateEmployeeRequest validRequest;

        @BeforeEach
        void outerSetUp() {
            validRequest = req("Nested Tester", "nested@test.com", "QA", 80_000.0, "MID");
        }

        @Nested
        @DisplayName("Given a valid create request")
        class WhenRequestIsValid {

            @BeforeEach
            void stubHappyPath() {
                // Runs AFTER outer outerSetUp()
                when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                    .thenAnswer(inv -> {
                        CreateEmployeeRequest r = inv.getArgument(0);
                        return dto(100L, r.getName(), r.getEmail(),
                                   r.getDepartment(), r.getSalary(), r.getLevel());
                    });
            }

            @Test
            @DisplayName("should return employee with generated ID")
            void shouldReturnEmployeeWithId() {
                EmployeeDTO result = employeeService.createEmployee(validRequest);
                assertNotNull(result.getId());
                assertEquals(100L, result.getId());
            }

            @Test
            @DisplayName("should persist correct name, email and department")
            void shouldPersistCorrectFields() {
                employeeService.createEmployee(validRequest);
                verify(employeeService).createEmployee(captor.capture());
                CreateEmployeeRequest saved = captor.getValue();
                assertEquals("Nested Tester",  saved.getName());
                assertEquals("nested@test.com", saved.getEmail());
                assertEquals("QA",              saved.getDepartment());
            }

            @Test
            @DisplayName("should return employee matching all request fields")
            void shouldReturnAllFields() {
                EmployeeDTO result = employeeService.createEmployee(validRequest);
                assertAll(
                    () -> assertEquals("Nested Tester",  result.getName()),
                    () -> assertEquals("nested@test.com", result.getEmail()),
                    () -> assertEquals("QA",              result.getDepartment()),
                    () -> assertEquals(80_000.0,          result.getSalary()),
                    () -> assertEquals("MID",             result.getLevel())
                );
            }
        }

        @Nested
        @DisplayName("Given the email address already exists")
        class WhenEmailAlreadyExists {

            @BeforeEach
            void stubDuplicate() {
                when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                    .thenThrow(new DuplicateEmailException(
                        "Email already exists: nested@test.com"));
            }

            @Test
            @DisplayName("should throw DuplicateEmailException")
            void shouldThrowDuplicateEmailException() {
                assertThrows(DuplicateEmailException.class,
                    () -> employeeService.createEmployee(validRequest));
            }

            @Test
            @DisplayName("exception message should contain the duplicate email")
            void exceptionMessageContainsEmail() {
                DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
                    () -> employeeService.createEmployee(validRequest));
                assertTrue(ex.getMessage().contains("nested@test.com"));
            }
        }

        @Nested
        @DisplayName("Boundary conditions")
        class BoundaryConditions {

            @Test
            @DisplayName("minimum non-zero salary (0.01) is accepted")
            void minimumSalaryIsAccepted() {
                CreateEmployeeRequest minReq = req("Min Wage", "min@co.com", "Ops", 0.01, "INTERN");
                when(employeeService.createEmployee(any()))
                    .thenReturn(dto(200L, "Min Wage", "min@co.com", "Ops", 0.01, "INTERN"));
                assertEquals(0.01, employeeService.createEmployee(minReq).getSalary());
            }

            @Test
            @DisplayName("maximum-length name (255 chars) is accepted")
            void maximumLengthNameIsAccepted() {
                String longName = "A".repeat(255);
                CreateEmployeeRequest r = req(longName, "long@co.com", "Ops", 50_000.0, "MID");
                when(employeeService.createEmployee(any()))
                    .thenReturn(dto(201L, longName, "long@co.com", "Ops", 50_000.0, "MID"));
                assertEquals(255, employeeService.createEmployee(r).getName().length());
            }
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 4 — ArgumentCaptor DEEP DIVE
    //
    //  ArgumentCaptor captures the exact argument passed to a mock.
    //
    //  Key difference from matchers:
    //    Matchers — control WHICH calls are matched during stubbing/verify
    //    Captor   — captures the ACTUAL value AFTER verify() for assertion
    //
    //  API:
    //    captor.capture()      — inside verify() to record the argument
    //    captor.getValue()     — last captured value
    //    captor.getAllValues() — all captured values across multiple calls
    // ============================================================
    // ============================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("4. ArgumentCaptor Deep Dive")
    @Tag("unit")
    class ArgumentCaptorDeepDiveTest {

        @Mock EmployeeService employeeService;
        @Captor ArgumentCaptor<CreateEmployeeRequest> requestCaptor;
        @Captor ArgumentCaptor<Long> longCaptor;

        @Test
        @DisplayName("Capture single request and assert all fields")
        void captureSingleRequest() {
            when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(dto(42L, "Grace Hopper", "grace@navy.mil", "Engineering", 120_000.0, "LEAD"));

            employeeService.createEmployee(
                req("Grace Hopper", "grace@navy.mil", "Engineering", 120_000.0, "LEAD"));

            verify(employeeService).createEmployee(requestCaptor.capture());
            CreateEmployeeRequest captured = requestCaptor.getValue();

            assertAll("Captured fields",
                () -> assertEquals("Grace Hopper",  captured.getName()),
                () -> assertEquals("grace@navy.mil", captured.getEmail()),
                () -> assertEquals("Engineering",    captured.getDepartment()),
                () -> assertEquals(120_000.0,        captured.getSalary()),
                () -> assertEquals("LEAD",           captured.getLevel())
            );
        }

        @Test
        @DisplayName("Capture multiple calls — getAllValues()")
        void captureMultipleCalls() {
            when(employeeService.createEmployee(any())).thenAnswer(inv -> {
                CreateEmployeeRequest r = inv.getArgument(0);
                return dto(1L, r.getName(), r.getEmail(), r.getDepartment(), r.getSalary(), r.getLevel());
            });

            employeeService.createEmployee(req("Alpha", "a@co.com", "Eng", 50_000.0, "MID"));
            employeeService.createEmployee(req("Beta",  "b@co.com", "HR",  60_000.0, "SENIOR"));

            verify(employeeService, times(2)).createEmployee(requestCaptor.capture());
            List<CreateEmployeeRequest> all = requestCaptor.getAllValues();

            assertEquals(2,       all.size());
            assertEquals("Alpha", all.get(0).getName());
            assertEquals("Beta",  all.get(1).getName());
        }

        @Test
        @DisplayName("Capture Long argument passed to deleteEmployee")
        void captureLongArgument() {
            doNothing().when(employeeService).deleteEmployee(anyLong());
            employeeService.deleteEmployee(5L);
            verify(employeeService).deleteEmployee(longCaptor.capture());
            assertEquals(5L, longCaptor.getValue());
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 5 — ADVANCED MOCKITO PATTERNS
    // ============================================================
    // ============================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("5. Advanced Mockito Patterns")
    @Tag("unit")
    class AdvancedMockitoTest {

        @Mock EmployeeService employeeService;

        // ── 5.1  doThrow / doAnswer for void methods ──────────

        /**
         * when(mock.voidMethod()) does NOT work for void methods.
         * Use doThrow() / doNothing() / doAnswer() instead.
         */
        @Test
        @DisplayName("doThrow — stub void method to throw exception")
        void testDoThrow_VoidMethod() {
            doThrow(new RuntimeException("DB error")).when(employeeService).deleteEmployee(7L);
            assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee(7L));
        }

        @Test
        @DisplayName("doAnswer — stub void method with side effect")
        void testDoAnswer_VoidMethod() {
            doAnswer(inv -> {
                System.out.println("Mock: deleting id=" + inv.getArgument(0));
                return null;   // void stubs must return null
            }).when(employeeService).deleteEmployee(8L);

            assertDoesNotThrow(() -> employeeService.deleteEmployee(8L));
            verify(employeeService).deleteEmployee(8L);
        }

        // ── 5.2  InOrder verification ────────────────────────

        /**
         * InOrder asserts methods were called in a specific SEQUENCE.
         */
        @Test
        @DisplayName("InOrder — getById called before getEmployeesByDepartment")
        void testInOrder_Verification() {
            when(employeeService.getEmployeeById(1L))
                .thenReturn(dto(1L, "A", "a@co.com", "Eng", 80_000.0, "MID"));
            when(employeeService.getEmployeesByDepartment("Eng"))
                .thenReturn(Collections.emptyList());

            employeeService.getEmployeeById(1L);
            employeeService.getEmployeesByDepartment("Eng");

            InOrder inOrder = inOrder(employeeService);
            inOrder.verify(employeeService).getEmployeeById(1L);
            inOrder.verify(employeeService).getEmployeesByDepartment("Eng");
        }

        // ── 5.3  Argument matchers ───────────────────────────

        /**
         * RULE: if ANY arg uses a matcher, ALL args must use matchers.
         *   WRONG: when(m.update(1L, any()))         ← mixing literal + matcher
         *   RIGHT: when(m.update(eq(1L), any()))     ← all matchers
         */
        @Test
        @DisplayName("Argument matchers — eq(), any(), argThat()")
        void testArgumentMatchers() {
            when(employeeService.getEmployeesByDepartment(eq("Engineering")))
                .thenReturn(List.of(dto(1L, "Dev", "d@co.com", "Engineering", 90_000.0, "SENIOR")));
            when(employeeService.getEmployeesByDepartment(argThat(d -> d != null && d.length() > 10)))
                .thenReturn(Collections.emptyList());

            assertEquals(1,
                employeeService.getEmployeesByDepartment("Engineering").size());
            assertTrue(
                employeeService.getEmployeesByDepartment("VeryLongDeptName").isEmpty());
        }

        // ── 5.4  Spy — partial mock of a real object ─────────

        /**
         * A Spy wraps a REAL object.
         * Real methods run unless you stub them with doReturn() / doThrow().
         *
         * Use doReturn() instead of when().thenReturn() on spies — the latter
         * calls the real method BEFORE the stub is applied.
         */
        @Test
        @DisplayName("Spy — real getAllEmployees() runs; getEmployeesByDepartment() is stubbed")
        void testSpy_Example() {
            EmployeeServiceImpl realService = new EmployeeServiceImpl();
            realService.seed(dto(1L, "Real", "real@co.com", "IT", 70_000.0, "MID"));

            EmployeeServiceImpl spy = Mockito.spy(realService);

            // Real method — returns seeded data
            List<EmployeeDTO> all = spy.getAllEmployees();
            assertEquals(1, all.size());
            assertEquals("Real", all.get(0).getName());

            // Stub one method
            doReturn(Collections.<EmployeeDTO>emptyList())
                .when(spy).getEmployeesByDepartment("IT");

            assertTrue(spy.getEmployeesByDepartment("IT").isEmpty());
            verify(spy).getAllEmployees();
            verify(spy).getEmployeesByDepartment("IT");
        }

        // ── 5.5  assertTimeout ───────────────────────────────

        @Test
        @DisplayName("assertTimeout — must finish within 500 ms")
        void testAssertTimeout() {
            when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
            assertTimeout(Duration.ofMillis(500),
                () -> employeeService.getAllEmployees());
        }

        // ── 5.6  verify with timeout (async) ─────────────────

        @Test
        @DisplayName("verify timeout — waits until condition met (async scenario)")
        void testVerifyWithTimeout() {
            when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
            employeeService.getAllEmployees();
            verify(employeeService, timeout(1000)).getAllEmployees();
        }

        // ── 5.7  lenient() — opt out of strict stubbing ──────

        /**
         * Strict mode (default) throws for stubs never called in the test.
         * lenient() silences this for specific stubs — use in @BeforeEach
         * when only SOME tests use a stub.
         */
        @Test
        @DisplayName("lenient() — optional stub not called in this test")
        void testLenient() {
            lenient().when(employeeService.getEmployeeById(999L))
                     .thenReturn(dto(999L, "Ghost", "g@co.com", "IT", 0.0, "MID"));

            when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
            assertTrue(employeeService.getAllEmployees().isEmpty());
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 6 — @SpringBootTest (Full Integration)
    //
    //  Loads the FULL ApplicationContext including Spring Security.
    //  RANDOM_PORT starts a real embedded Tomcat.
    //  TestRestTemplate makes real HTTP requests.
    // ============================================================
    // ============================================================

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DisplayName("6. @SpringBootTest Integration Tests")
    @Tag("integration")
    class EmployeeControllerIntegrationTest {

        @Autowired TestRestTemplate restTemplate;

        @Test
        @DisplayName("GET /api/v1/employees — 401 without credentials")
        void getAllEmployees_WithoutAuth_Returns401() {
            ResponseEntity<String> res =
                restTemplate.getForEntity("/api/v1/employees", String.class);
            assertEquals(401, res.getStatusCode().value());
        }

        @Test
        @DisplayName("GET /api/v1/employees — 200 with Basic Auth")
        void getAllEmployees_WithAuth_Returns200() {
            ResponseEntity<String> res =
                restTemplate.withBasicAuth("user", "password")
                            .getForEntity("/api/v1/employees", String.class);
            assertEquals(200, res.getStatusCode().value());
        }

        @Test
        @DisplayName("GET /api/v1/employees/999 — 404 for missing employee")
        void getEmployeeById_NotFound_Returns404() {
            ResponseEntity<String> res =
                restTemplate.withBasicAuth("user", "password")
                            .getForEntity("/api/v1/employees/999", String.class);
            assertEquals(404, res.getStatusCode().value());
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 7 — @WebMvcTest (Controller Slice)
    //
    //  Loads ONLY the web layer: controllers, ControllerAdvice, filters,
    //  Spring Security, Jackson. No @Service / @Repository beans.
    //
    //  MockMvc dispatches through the full DispatcherServlet pipeline
    //  WITHOUT starting a real HTTP server — fast + rich assertions.
    //
    //  MockMvc API:
    //    mockMvc.perform(get("/path"))
    //           .andExpect(status().isOk())
    //           .andExpect(content().contentType(APPLICATION_JSON))
    //           .andExpect(jsonPath("$.name").value("Alice"))
    //           .andDo(print());   ← prints full request+response (debug)
    //
    //  jsonPath (Jayway JsonPath):
    //    "$"           root
    //    "$.name"      field at root
    //    "$[0].name"   name of first array element
    //    "$.length()"  array size
    //
    //  Security:
    //    @WithMockUser     — injects mock SecurityContext; simplest
    //    .with(csrf())     — adds CSRF token; required for POST/PUT/DELETE
    // ============================================================
    // ============================================================

    @Nested
    @WebMvcTest(EmployeeController.class)
    @DisplayName("7. @WebMvcTest — EmployeeController")
    @Tag("web")
    class EmployeeControllerWebMvcTest {

        @Autowired MockMvc mockMvc;
        @Autowired ObjectMapper objectMapper;

        // ── 7.1  GET all — 200 OK ────────────────────────────

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("GET /api/v1/employees — 200 OK with JSON array")
        void getAllEmployees_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/employees").accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                   .andExpect(jsonPath("$").isArray())
                   .andDo(print());
        }

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("GET /api/v1/employees?department=Engineering — 200 filtered")
        void getAllEmployees_FilterByDept_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/employees").param("department", "Engineering")
                       .accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$").isArray());
        }

        // ── 7.2  GET by ID ────────────────────────────────────

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("GET /api/v1/employees/1 — 200 OK")
        void getEmployeeById_Existing_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/employees/1").accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.id").value(1))
                   .andExpect(jsonPath("$.name").exists());
        }

        // ── 7.3  GET non-existent — 404 ──────────────────────

        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        @DisplayName("GET /api/v1/employees/999 — 404 Not Found")
        void getEmployeeById_NotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/employees/999").accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isNotFound());
        }

        // ── 7.4  POST valid body — 201 Created ───────────────

        /**
         * csrf() adds the CSRF token required by Spring Security for mutating requests.
         * Without it: 403 Forbidden.
         */
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
        @DisplayName("POST /api/v1/employees with valid body — 201 Created + Location")
        void createEmployee_ValidBody_Returns201() throws Exception {
            mockMvc.perform(post("/api/v1/employees")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {
                                 "name": "New Employee",
                                 "email": "brand.new.unique.456@test.com",
                                 "department": "Engineering",
                                 "salary": 80000.0,
                                 "level": "MID"
                               }
                               """)
                       .with(csrf()))
                   .andExpect(status().isCreated())
                   .andExpect(header().exists("Location"))
                   .andExpect(jsonPath("$.id").exists())
                   .andExpect(jsonPath("$.name").value("New Employee"))
                   .andDo(print());
        }

        // ── 7.5  POST invalid body — 400 ─────────────────────

        /**
         * {} fails @NotBlank (name, email, dept) and @NotNull (salary).
         * GlobalExceptionHandler handles MethodArgumentNotValidException → 400.
         */
        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
        @DisplayName("POST with empty body — 400 Bad Request")
        void createEmployee_EmptyBody_Returns400() throws Exception {
            mockMvc.perform(post("/api/v1/employees")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{}")
                       .with(csrf()))
                   .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
        @DisplayName("POST with invalid email — 400 Bad Request")
        void createEmployee_InvalidEmail_Returns400() throws Exception {
            mockMvc.perform(post("/api/v1/employees")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {
                                 "name": "Valid Name",
                                 "email": "not-a-valid-email",
                                 "department": "Engineering",
                                 "salary": 80000.0,
                                 "level": "MID"
                               }
                               """)
                       .with(csrf()))
                   .andExpect(status().isBadRequest());
        }

        // ── 7.6  DELETE — 204 / 404 ──────────────────────────

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("DELETE /api/v1/employees/3 — 204 No Content")
        void deleteEmployee_Existing_Returns204() throws Exception {
            mockMvc.perform(delete("/api/v1/employees/3").with(csrf()))
                   .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("DELETE /api/v1/employees/999 — 404 Not Found")
        void deleteEmployee_NotFound_Returns404() throws Exception {
            mockMvc.perform(delete("/api/v1/employees/999").with(csrf()))
                   .andExpect(status().isNotFound());
        }

        // ── 7.7  PATCH salary ────────────────────────────────

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("PATCH /api/v1/employees/4/salary — 200 with updated salary")
        void updateSalary_Returns200() throws Exception {
            mockMvc.perform(patch("/api/v1/employees/4/salary")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"newSalary\": 120000.0}")
                       .with(csrf()))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.salary").value(120000.0));
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 8 — @DataJpaTest (Repository Slice)
    //
    //  @DataJpaTest configures:
    //    - Embedded H2 in-memory DB (replaces configured DataSource)
    //    - Spring Data JPA repositories
    //    - Hibernate / JPA
    //    - @Transactional per test — each test rolls back automatically
    //
    //  NOT loaded: @Service, @Controller, @Component, full context.
    //
    //  TestEntityManager:
    //    persistAndFlush(entity) — save + flush immediately to H2
    //    find(Class, id)         — bypass L1 cache; hit H2 directly
    //    clear()                 — clear L1 cache (persistence context)
    //
    //  Why flush before querying?
    //    JPA batches writes until commit. persistAndFlush() ensures the row
    //    is in H2 so a SELECT in the same transaction finds it.
    // ============================================================
    // ============================================================

    @Nested
    @DataJpaTest
    @DisplayName("8. @DataJpaTest — Repository Layer")
    @Tag("repository")
    class EmployeeRepositoryDataJpaTest {

        @Autowired EntityManager entityManager;

        @Test
        @DisplayName("EntityManager injected — JPA context loaded correctly")
        void entityManagerIsAvailable() {
            assertNotNull(entityManager);
        }

        /**
         * Full @DataJpaTest patterns.
         * Copy the code below into a real test that has
         * EmployeeJpaRepository (extends JpaRepository<Employee, Long>).
         *
         * <pre>
         * {@code
         * @DataJpaTest
         * class EmployeeJpaRepositoryTest {
         *
         *   @Autowired TestEntityManager em;
         *   @Autowired EmployeeJpaRepository repo;
         *   Employee seeded;
         *
         *   @BeforeEach
         *   void seed() {
         *     seeded = em.persistAndFlush(
         *       Employee.builder()
         *         .firstName("JPA").lastName("Test")
         *         .email("jpa@test.com").salary(60000.0)
         *         .department("IT").build());
         *   }
         *
         *   @Test void testFindByEmail_Found() {
         *     Optional<Employee> r = repo.findByEmail("jpa@test.com");
         *     assertTrue(r.isPresent());
         *     assertEquals("JPA", r.get().getFirstName());
         *   }
         *
         *   @Test void testFindByEmail_NotFound() {
         *     assertFalse(repo.findByEmail("nobody@test.com").isPresent());
         *   }
         *
         *   @Test void testFindByDepartment() {
         *     em.persistAndFlush(Employee.builder().firstName("B").lastName("B")
         *       .email("b@test.com").salary(55000.0).department("IT").build());
         *     assertEquals(2, repo.findByDepartment("IT").size());
         *   }
         *
         *   @Test void testCustomQuery_SalaryGreaterThan() {
         *     em.persistAndFlush(Employee.builder().firstName("Senior").lastName("Dev")
         *       .email("s@test.com").salary(150000.0).department("Eng").build());
         *     List<Employee> r = repo.findBySalaryGreaterThan(100000.0);
         *     assertEquals(1, r.size());
         *     assertEquals("Senior", r.get(0).getFirstName());
         *   }
         *
         *   @Test void testSave_AutoGeneratesId() {
         *     Employee e = Employee.builder().firstName("New").lastName("Emp")
         *       .email("new@test.com").salary(40000.0).department("HR").build();
         *     assertNotNull(repo.save(e).getId());
         *   }
         *
         *   @Test void testDeleteById() {
         *     Long id = seeded.getId();
         *     repo.deleteById(id);
         *     em.flush(); em.clear();
         *     assertNull(em.find(Employee.class, id));
         *   }
         * }
         * }
         * </pre>
         */
        @Test
        @Disabled("Pattern reference — copy into a real test with EmployeeJpaRepository injected")
        void dataJpaPatternReference() {
            assertTrue(true, "See Javadoc above for full @DataJpaTest patterns");
        }
    }

    // ============================================================
    // ============================================================
    //  SECTION 9 — INTERVIEW Q&A QUICK REFERENCE
    // ============================================================
    // ============================================================

    /**
     * Q01: @Mock vs @MockBean?
     *      @Mock     — pure Mockito; no Spring; fast unit tests
     *      @MockBean — Spring Boot; replaces Spring bean; resets context cache; slower
     *
     * Q02: @Spy vs @SpyBean?
     *      @Spy     — pure Mockito; wraps a manually-created real object
     *      @SpyBean — Spring Boot; wraps the Spring-managed bean
     *
     * Q03: When does @InjectMocks fail silently?
     *      When no matching constructor/setter/field exists for a mock.
     *      Always assert injected mocks are not null.
     *
     * Q04: Stubbing vs Verification?
     *      Stubbing     — what should the mock RETURN: when().thenReturn()
     *      Verification — was the mock CALLED correctly: verify()
     *
     * Q05: What is strict stubbing?
     *      Default with MockitoExtension. Throws UnnecessaryStubbingException
     *      for stubs never invoked. Opt out with lenient().
     *
     * Q06: What does @WebMvcTest load?
     *      Web layer only: controllers, ControllerAdvice, filters,
     *      Spring Security, Jackson. No @Service/@Repository.
     *
     * Q07: Why does @DataJpaTest use H2?
     *      Self-contained, reproducible, fast tests without external DB.
     *      Each test rolls back — no test pollution.
     *
     * Q08: @BeforeEach vs @BeforeAll?
     *      @BeforeEach — before every test; fresh state
     *      @BeforeAll  — once; expensive one-time setup; must be static
     *
     * Q09: @TestInstance(PER_CLASS)?
     *      One instance per class. @BeforeAll can be non-static.
     *      Risk: shared mutable state between tests.
     *
     * Q10: @ParameterizedTest vs @RepeatedTest?
     *      @ParameterizedTest — different inputs; tests multiple scenarios
     *      @RepeatedTest      — same input N times; consistency / flakiness
     *
     * Q11: MockMvc vs TestRestTemplate?
     *      MockMvc          — fake HTTP; no server; rich assertions; fast
     *      TestRestTemplate — real HTTP; use with RANDOM_PORT
     *
     * Q12: Spring Security in @WebMvcTest?
     *      @WithMockUser    — mock SecurityContext; simplest
     *      .with(csrf())    — CSRF token for POST/PUT/DELETE
     *      .with(httpBasic) — Basic Auth header
     *
     * Q13: ArgumentCaptor vs matchers?
     *      Matchers — control WHICH calls match (during stubbing/verify)
     *      Captor   — inspect ACTUAL argument value AFTER verify()
     *
     * Q14: @DataJpaTest vs @SpringBootTest for repository tests?
     *      @DataJpaTest  — JPA slice only; H2; fast; preferred
     *      @SpringBootTest — everything; can use real DB; slower
     */
    @Nested
    @DisplayName("9. Interview Q&A Reference")
    class InterviewQnAReference {

        @Test
        @Disabled("Documentation only — see Javadoc above")
        void seeJavadocForAllQnA() {
            assertTrue(true);
        }
    }
}
