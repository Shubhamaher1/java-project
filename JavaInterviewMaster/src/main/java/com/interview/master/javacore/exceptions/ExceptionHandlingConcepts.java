package com.interview.master.javacore.exceptions;

import java.io.*;
import java.sql.SQLException;

/**
 * =============================================================================
 * EXCEPTION HANDLING IN JAVA - Complete Interview Reference
 * =============================================================================
 *
 * An exception is an unwanted or unexpected event that disrupts the normal
 * flow of program execution at runtime.
 *
 * Java uses a "throw and catch" mechanism:
 *   - Code that detects a problem THROWS an exception object
 *   - Code that handles the problem CATCHES it
 *
 * =============================================================================
 * EXCEPTION HIERARCHY
 * =============================================================================
 *
 *                         Throwable
 *                        /         \
 *                     Error       Exception
 *                    /    \       /        \
 *            OutOfMemory  Stack  IOException  RuntimeException
 *            Error        Overflow  SQLException  /    |    \
 *                         Error  ClassNotFound  NPE  AIOOBE  CCE
 *                                Exception
 *
 * Throwable          - Root of the exception hierarchy
 *   |
 *   +-- Error        - SERIOUS problems, application should NOT try to recover
 *   |     +-- OutOfMemoryError       - heap space exhausted
 *   |     +-- StackOverflowError     - deep/infinite recursion
 *   |     +-- VirtualMachineError    - JVM broken
 *   |
 *   +-- Exception    - Conditions that applications should handle
 *         |
 *         +-- RuntimeException (UNCHECKED) - programmer errors, not forced to handle
 *         |     +-- NullPointerException
 *         |     +-- ArrayIndexOutOfBoundsException
 *         |     +-- ClassCastException
 *         |     +-- NumberFormatException
 *         |     +-- IllegalArgumentException
 *         |     +-- IllegalStateException
 *         |     +-- ArithmeticException (divide by zero)
 *         |     +-- UnsupportedOperationException
 *         |     +-- ConcurrentModificationException
 *         |
 *         +-- Checked Exceptions - compiler FORCES you to handle or declare
 *               +-- IOException
 *               |     +-- FileNotFoundException
 *               |     +-- SocketException
 *               +-- SQLException
 *               +-- ClassNotFoundException
 *               +-- InterruptedException
 *               +-- ParseException
 *
 * =============================================================================
 */
public class ExceptionHandlingConcepts {

    public static void main(String[] args) {
        System.out.println("=== EXCEPTION HANDLING CONCEPTS ===\n");

        demonstrateCheckedExceptions();
        demonstrateUncheckedExceptions();
        demonstrateTryCatchFinally();
        demonstrateTryWithResources();
        demonstrateMultiCatch();
        demonstrateCustomExceptions();
        demonstrateThrowVsThrows();
        demonstrateExceptionChaining();
        demonstrateExceptionPropagation();
        demonstrateBestPractices();
        demonstrateStackOverflowError();
        demonstrateOutOfMemoryError();
        demonstrateInterviewQuestions();
    }


    // =========================================================================
    // 1. CHECKED EXCEPTIONS
    // =========================================================================
    // Checked exceptions are checked at COMPILE TIME.
    // The compiler forces you to either:
    //   a) Handle it: wrap in try-catch
    //   b) Declare it: add "throws ExceptionType" to method signature
    //
    // Philosophy: "Recoverable conditions" - caller can do something useful
    //
    // Common checked exceptions:
    //   - IOException         (file not found, network error)
    //   - SQLException        (database query error)
    //   - ClassNotFoundException (class not found in classpath)
    //   - InterruptedException (thread was interrupted)
    //   - ParseException       (invalid date/number string)
    // =========================================================================

    // Method MUST declare checked exceptions it might throw
    static void readFile(String path) throws IOException {
        // IOException is checked - must declare with throws
        FileReader reader = new FileReader(path);
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        System.out.println("Read: " + line);
        br.close();
    }

    // If you don't want to propagate, handle it locally
    static void readFileSafely(String path) {
        try {
            readFile(path);
        } catch (IOException e) {
            System.out.println("  [Checked] IOException caught: " + e.getMessage());
            // handle gracefully: return default value, retry, log, etc.
        }
    }

    static void demonstrateCheckedExceptions() {
        System.out.println("--- 1. CHECKED EXCEPTIONS ---");

        // Must handle or declare
        try {
            FileReader fr = new FileReader("nonexistent.txt"); // FileNotFoundException
        } catch (FileNotFoundException e) {
            System.out.println("  FileNotFoundException: " + e.getMessage());
        }

        // Simulate IOException
        readFileSafely("missing_file.txt");

        System.out.println("  Note: Checked exceptions must be handled or declared");
        System.out.println();
    }


    // =========================================================================
    // 2. UNCHECKED EXCEPTIONS (RuntimeException)
    // =========================================================================
    // Unchecked exceptions are NOT checked at compile time.
    // They represent PROGRAMMING ERRORS - bugs in logic.
    //
    // Philosophy: "Should not occur in correct code" - fix the code, not catch it
    //
    // You CAN catch them, but you're not forced to.
    //
    // Most common RuntimeExceptions (must memorize for interviews):
    // =========================================================================

    static void demonstrateUncheckedExceptions() {
        System.out.println("--- 2. UNCHECKED EXCEPTIONS (RuntimeException) ---");

        // --- NullPointerException ---
        // Accessing method/field on null reference
        try {
            String s = null;
            s.length(); // NPE!
        } catch (NullPointerException e) {
            System.out.println("  NullPointerException: accessing method on null");
        }
        // Java 14+ has helpful NullPointerException messages (JEP 358)
        // "Cannot invoke String.length() because 's' is null"

        // --- ArrayIndexOutOfBoundsException ---
        // Accessing array with invalid index
        try {
            int[] arr = {1, 2, 3};
            int x = arr[5]; // index 5 doesn't exist
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("  ArrayIndexOutOfBoundsException: " + e.getMessage());
        }

        // --- ClassCastException ---
        // Invalid type cast
        try {
            Object obj = "Hello";
            Integer num = (Integer) obj; // String cannot be cast to Integer
        } catch (ClassCastException e) {
            System.out.println("  ClassCastException: " + e.getMessage());
        }

        // --- NumberFormatException ---
        // Invalid string -> number conversion
        try {
            int n = Integer.parseInt("abc123"); // not a valid number
        } catch (NumberFormatException e) {
            System.out.println("  NumberFormatException: " + e.getMessage());
        }

        // --- ArithmeticException ---
        // Divide by zero (integers only; doubles return Infinity)
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("  ArithmeticException: " + e.getMessage()); // / by zero
        }
        System.out.println("  Note: double division by zero = " + (10.0 / 0.0)); // Infinity

        // --- IllegalArgumentException ---
        // Method receives an illegal/inappropriate argument
        try {
            validateAge(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("  IllegalArgumentException: " + e.getMessage());
        }

        // --- IllegalStateException ---
        // Method called at inappropriate time / object in wrong state
        try {
            java.util.Iterator<String> it = new java.util.ArrayList<String>().iterator();
            it.remove(); // nothing to remove
        } catch (IllegalStateException e) {
            System.out.println("  IllegalStateException: " + e.getMessage());
        }

        // --- StringIndexOutOfBoundsException (subclass of IndexOutOfBoundsException) ---
        try {
            String s = "Hello";
            char c = s.charAt(10); // index out of range
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("  StringIndexOutOfBoundsException: " + e.getMessage());
        }

        // --- ConcurrentModificationException ---
        try {
            java.util.List<String> list = new java.util.ArrayList<>();
            list.add("a"); list.add("b"); list.add("c");
            for (String item : list) {
                if (item.equals("a")) {
                    list.remove(item); // modifying list while iterating
                }
            }
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("  ConcurrentModificationException: modifying during iteration");
        }

        System.out.println();
    }

    static void validateAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age: " + age + ". Must be 0-150.");
        }
    }


    // =========================================================================
    // 3. TRY-CATCH-FINALLY
    // =========================================================================
    // try    - code that might throw an exception
    // catch  - handle specific exceptions (child before parent!)
    // finally - ALWAYS executes (cleanup: close connections, release resources)
    //
    // IMPORTANT RULES:
    //   1. finally ALWAYS executes, even if:
    //      - No exception is thrown
    //      - Exception IS thrown and caught
    //      - Exception IS thrown and NOT caught
    //      - return statement in try or catch
    //   2. finally does NOT execute ONLY if: System.exit() is called, or JVM crashes
    //   3. Catch blocks: more SPECIFIC exceptions must come BEFORE general ones
    //      (child class before parent class) - else compile error!
    //   4. You can have multiple catch blocks
    //   5. try-finally (without catch) is valid
    // =========================================================================

    static void demonstrateTryCatchFinally() {
        System.out.println("--- 3. TRY-CATCH-FINALLY ---");

        // Basic try-catch-finally
        System.out.println("  [Basic] Result: " + divide(10, 2));
        System.out.println("  [Basic] Result: " + divide(10, 0));

        // Order of catch: child before parent (IMPORTANT!)
        System.out.println("  [Order] " + catchOrder());

        // finally with return
        System.out.println("  [Finally+Return] " + finallyWithReturn());

        // try-finally (no catch): valid syntax
        System.out.println("  [Try-Finally] executing...");
        tryWithoutCatch();

        System.out.println();
    }

    static int divide(int a, int b) {
        try {
            return a / b;
        } catch (ArithmeticException e) {
            System.out.println("  [Catch] ArithmeticException: " + e.getMessage());
            return -1; // default value on error
        } finally {
            // ALWAYS runs - even when return is in try or catch
            System.out.println("  [Finally] This always executes for divide(" + a + "," + b + ")");
        }
    }

    static String catchOrder() {
        try {
            throw new FileNotFoundException("file.txt not found");
        } catch (FileNotFoundException e) {    // MORE specific - must come first
            return "Caught FileNotFoundException (child)";
        } catch (IOException e) {             // LESS specific - must come after
            return "Caught IOException (parent)";
        } catch (Exception e) {               // MOST general - must come last
            return "Caught Exception (root)";
        }
        // If we switched FileNotFoundException and IOException, it would be a COMPILE ERROR:
        // "Exception FileNotFoundException has already been caught"
    }

    // Interview Q: Can finally override a return value?
    // Answer: YES! If finally has a return, it OVERRIDES the try/catch return.
    static String finallyWithReturn() {
        try {
            System.out.println("  [Try] returning 'from try'");
            return "from try";   // this return is prepared...
        } finally {
            System.out.println("  [Finally] returning 'from finally' - overrides try return!");
            return "from finally"; // ...but THIS return wins!
        }
    }

    // try-finally is valid (no catch block required)
    static void tryWithoutCatch() {
        try {
            System.out.println("  [Try-Finally] inside try block");
        } finally {
            System.out.println("  [Try-Finally] inside finally block");
        }
    }


    // =========================================================================
    // 4. TRY-WITH-RESOURCES (Java 7+)
    // =========================================================================
    // Automatically closes resources after the try block completes.
    // Eliminates the need for manually closing in finally blocks.
    //
    // Requirements:
    //   - Resource must implement java.lang.AutoCloseable (or Closeable)
    //   - close() is called automatically, even if exception is thrown
    //   - Multiple resources: closed in REVERSE order of declaration
    //
    // AutoCloseable vs Closeable:
    //   - Closeable (java.io): close() throws IOException, extends AutoCloseable
    //   - AutoCloseable (java.lang): close() throws Exception
    //
    // Suppressed exceptions:
    //   - If both try AND close() throw exceptions, the close() exception is
    //     SUPPRESSED (attached to primary exception, not lost)
    //   - Access via: e.getSuppressed()
    // =========================================================================

    // Custom AutoCloseable resource for demonstration
    static class DatabaseConnection implements AutoCloseable {
        private String url;
        private boolean throwOnClose;

        DatabaseConnection(String url) {
            this(url, false);
        }

        DatabaseConnection(String url, boolean throwOnClose) {
            this.url           = url;
            this.throwOnClose  = throwOnClose;
            System.out.println("  [DB] Opened connection to: " + url);
        }

        public String executeQuery(String sql) {
            System.out.println("  [DB] Executing: " + sql);
            return "ResultSet{url=" + url + "}";
        }

        @Override
        public void close() throws Exception {
            System.out.println("  [DB] Closing connection to: " + url);
            if (throwOnClose) {
                throw new Exception("Error closing connection to " + url);
            }
        }
    }

    static class FileResource implements AutoCloseable {
        private String fileName;

        FileResource(String fileName) {
            this.fileName = fileName;
            System.out.println("  [File] Opened: " + fileName);
        }

        public void write(String data) {
            System.out.println("  [File] Writing to " + fileName + ": " + data);
        }

        @Override
        public void close() {
            System.out.println("  [File] Closed: " + fileName);
        }
    }

    static void demonstrateTryWithResources() {
        System.out.println("--- 4. TRY-WITH-RESOURCES (Java 7+) ---");

        // Single resource - automatically closed after try block
        System.out.println("  [Single Resource]:");
        try (DatabaseConnection conn = new DatabaseConnection("jdbc:mysql://localhost/db")) {
            String result = conn.executeQuery("SELECT * FROM users");
            System.out.println("  Result: " + result);
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        // conn.close() called automatically here!

        // Multiple resources - closed in REVERSE order (file first, then conn)
        System.out.println("  [Multiple Resources]:");
        try (DatabaseConnection conn = new DatabaseConnection("jdbc:oracle://db:1521/prod");
             FileResource file = new FileResource("output.txt")) {
            String data = conn.executeQuery("SELECT name FROM employees");
            file.write(data);
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        // file.close() called FIRST, then conn.close() (reverse order)

        // Suppressed exceptions demo
        System.out.println("  [Suppressed Exceptions]:");
        try (DatabaseConnection conn = new DatabaseConnection("bad-db", true)) {
            // This executes normally, but close() will throw
            conn.executeQuery("SELECT 1");
            throw new RuntimeException("Primary exception from try block");
        } catch (Exception e) {
            System.out.println("  Primary exception: " + e.getMessage());
            // The close() exception is SUPPRESSED (not lost)
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("  Suppressed exception: " + suppressed.getMessage());
            }
        }

        // Before Java 7: verbose and error-prone
        System.out.println("  [Pre-Java7 - verbose way]:");
        DatabaseConnection conn = null;
        try {
            conn = new DatabaseConnection("jdbc:mysql://legacy/db");
            conn.executeQuery("SELECT 1");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // close() itself might throw!
                } catch (Exception e) {
                    System.out.println("  Error closing: " + e.getMessage());
                }
            }
        }

        System.out.println();
    }


    // =========================================================================
    // 5. MULTI-CATCH (Java 7+)
    // =========================================================================
    // Catch multiple exception types in a single catch block.
    // Reduces code duplication when different exceptions need same handling.
    //
    // Rules:
    //   - Separate exception types with pipe: catch (A | B | C e)
    //   - The caught variable is EFFECTIVELY FINAL (cannot reassign)
    //   - Cannot catch exceptions that are in a parent-child relationship
    //     in the same multi-catch (e.g., catch (IOException | FileNotFoundException)
    //     is a compile error since FileNotFoundException IS-A IOException)
    // =========================================================================

    static void demonstrateMultiCatch() {
        System.out.println("--- 5. MULTI-CATCH (Java 7+) ---");

        // Before Java 7: repetitive catch blocks
        System.out.println("  [Pre-Java7 - repetitive]:");
        try {
            riskyOperation("pre7");
        } catch (IOException e) {
            System.out.println("  Handling: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("  Handling: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  Handling: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        // Java 7+: multi-catch with pipe operator
        System.out.println("  [Java 7 Multi-Catch]:");
        try {
            riskyOperation("multi");
        } catch (IOException | SQLException e) {
            // 'e' is effectively final here - cannot do e = new IOException()
            System.out.println("  Caught IO or SQL: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  Caught NFE: " + e.getMessage());
        }

        // INVALID: Cannot use parent-child in same multi-catch (compile error)
        // catch (IOException | FileNotFoundException e) {} // COMPILE ERROR!

        System.out.println();
    }

    static void riskyOperation(String mode) throws IOException, SQLException {
        // Simulate throwing different exceptions
        if (mode.equals("pre7")) {
            throw new IOException("File not readable");
        } else {
            throw new SQLException("Connection timeout");
        }
    }


    // =========================================================================
    // 6. CUSTOM EXCEPTIONS
    // =========================================================================
    // Create your own exceptions to represent domain-specific error conditions.
    //
    // Two types of custom exceptions:
    //   a) Custom Checked Exception:   extend Exception
    //   b) Custom Unchecked Exception: extend RuntimeException
    //
    // When to use which:
    //   Checked:   caller CAN reasonably recover (file not found, invalid config)
    //   Unchecked: programming error, caller probably CANNOT recover
    //              (most business logic exceptions in modern code)
    //
    // Best practices:
    //   1. Provide meaningful message
    //   2. Include cause (exception chaining) where applicable
    //   3. Add domain-specific fields (error code, user info, etc.)
    //   4. Follow naming convention: end with "Exception"
    //   5. Provide both (String message) and (String message, Throwable cause) constructors
    // =========================================================================

    // Custom CHECKED exception (extend Exception)
    static class InsufficientFundsException extends Exception {
        private double balance;
        private double amount;
        private String accountId;

        // Best practice: multiple constructors
        public InsufficientFundsException(String message) {
            super(message);
        }

        public InsufficientFundsException(String accountId, double balance, double amount) {
            super(String.format("Account %s: insufficient funds. Balance: %.2f, Required: %.2f",
                    accountId, balance, amount));
            this.accountId = accountId;
            this.balance   = balance;
            this.amount    = amount;
        }

        // Constructor with cause (for exception chaining)
        public InsufficientFundsException(String message, Throwable cause) {
            super(message, cause);
        }

        // Domain-specific getters
        public double getBalance()   { return balance; }
        public double getAmount()    { return amount; }
        public String getAccountId() { return accountId; }
    }

    // Custom UNCHECKED exception (extend RuntimeException)
    static class InvalidOrderException extends RuntimeException {
        private String orderId;
        private int errorCode;

        public InvalidOrderException(String message) {
            super(message);
        }

        public InvalidOrderException(String orderId, int errorCode, String message) {
            super("Order " + orderId + " [" + errorCode + "]: " + message);
            this.orderId   = orderId;
            this.errorCode = errorCode;
        }

        // Constructor with cause (always provide this!)
        public InvalidOrderException(String message, Throwable cause) {
            super(message, cause);
        }

        public String getOrderId()  { return orderId; }
        public int getErrorCode()   { return errorCode; }
    }

    // Service that uses custom exceptions
    static class BankAccount {
        private String id;
        private double balance;

        BankAccount(String id, double initialBalance) {
            this.id      = id;
            this.balance = initialBalance;
        }

        // Checked exception: caller must decide how to handle insufficient funds
        public void withdraw(double amount) throws InsufficientFundsException {
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive: " + amount);
            }
            if (amount > balance) {
                throw new InsufficientFundsException(id, balance, amount);
            }
            balance -= amount;
            System.out.println("  [Bank] Withdrew $" + amount + ". New balance: $" + balance);
        }

        public double getBalance() { return balance; }
    }

    static void demonstrateCustomExceptions() {
        System.out.println("--- 6. CUSTOM EXCEPTIONS ---");

        BankAccount account = new BankAccount("ACC-001", 100.0);

        // Successful withdrawal
        try {
            account.withdraw(50.0);
        } catch (InsufficientFundsException e) {
            System.out.println("  Error: " + e.getMessage());
        }

        // Failed withdrawal - insufficient funds
        try {
            account.withdraw(200.0); // only has $50 now
        } catch (InsufficientFundsException e) {
            System.out.println("  InsufficientFundsException: " + e.getMessage());
            System.out.println("  Balance was: $" + e.getBalance() + ", needed: $" + e.getAmount());
        }

        // Unchecked exception
        try {
            processOrder(null, -5);
        } catch (InvalidOrderException e) {
            System.out.println("  InvalidOrderException: " + e.getMessage());
        }

        System.out.println();
    }

    static void processOrder(String orderId, int quantity) {
        if (orderId == null || orderId.isEmpty()) {
            throw new InvalidOrderException("Order ID cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new InvalidOrderException(orderId, 1001, "Quantity must be positive, got: " + quantity);
        }
    }


    // =========================================================================
    // 7. THROW vs THROWS
    // =========================================================================
    //
    // throw  (verb) - used to THROW an exception object
    //   - Used inside method body
    //   - Followed by an exception instance: throw new SomeException()
    //   - Transfers control to nearest matching catch block
    //   - Only ONE exception can be thrown at a time
    //
    // throws (declarator) - declares exceptions a method MAY throw
    //   - Used in method SIGNATURE
    //   - For checked exceptions that are not handled inside the method
    //   - Can declare MULTIPLE exceptions: throws IOException, SQLException
    //   - Informs the caller to handle or propagate them
    //
    // Key differences:
    //   +------------------------------------------------------------+
    //   | throw                     | throws                         |
    //   +---------------------------+--------------------------------+
    //   | Used inside method        | Used in method signature       |
    //   | Followed by instance      | Followed by class names        |
    //   | Throws one at a time      | Can declare multiple           |
    //   | Used for checked+unchecked| Primarily for checked          |
    //   +------------------------------------------------------------+
    // =========================================================================

    // throws: declares what this method might throw (caller must handle)
    static void readConfig(String path) throws IOException, ClassNotFoundException {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null"); // throw unchecked
        }
        if (!path.endsWith(".xml")) {
            throw new IOException("Only XML config files supported: " + path); // throw checked
        }
        System.out.println("  [Config] Reading: " + path);
    }

    // Re-throwing: catch, do something, then rethrow
    static void processConfig(String path) throws IOException {
        try {
            readConfig(path);
        } catch (ClassNotFoundException e) {
            // wrap in IOException (exception chaining)
            throw new IOException("Config class not found: " + path, e);
        }
    }

    static void demonstrateThrowVsThrows() {
        System.out.println("--- 7. THROW vs THROWS ---");

        System.out.println("  throw  = used inside a method to throw an exception instance");
        System.out.println("  throws = used in method signature to declare possible exceptions");

        // Demonstrate throw
        try {
            readConfig("config.json"); // wrong extension
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("  Caught: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        try {
            readConfig(null); // IllegalArgumentException (unchecked - not in throws)
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("  Caught: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught IAE: " + e.getMessage());
        }

        System.out.println();
    }


    // =========================================================================
    // 8. EXCEPTION CHAINING (Cause)
    // =========================================================================
    // When catching a low-level exception and rethrowing a high-level one,
    // ALWAYS preserve the original cause. This creates a chain of exceptions.
    //
    // Why? Without chaining, the root cause is lost, making debugging very hard.
    //
    // How to chain:
    //   throw new HighLevelException("message", originalException);
    //
    // How to access cause:
    //   e.getCause()           - immediate cause
    //   e.getMessage()         - message of this exception
    //   e.printStackTrace()    - prints full chain
    //   e.initCause(cause)     - set cause after creation (rarely used)
    // =========================================================================

    // Low-level exception (e.g., from a library/framework)
    static String fetchFromDatabase(String query) throws SQLException {
        throw new SQLException("Connection refused: db.prod.example.com:5432");
    }

    // Mid-level: catches low-level, wraps in domain exception
    static String getUserData(int userId) throws Exception {
        try {
            return fetchFromDatabase("SELECT * FROM users WHERE id=" + userId);
        } catch (SQLException e) {
            // Chain: preserve original cause!
            throw new Exception("Failed to fetch user data for id=" + userId, e);
        }
    }

    // High-level: catches mid-level, wraps in service exception
    static String processUserRequest(int userId) {
        try {
            return getUserData(userId);
        } catch (Exception e) {
            // Chain again
            throw new RuntimeException("User request processing failed for userId=" + userId, e);
        }
    }

    static void demonstrateExceptionChaining() {
        System.out.println("--- 8. EXCEPTION CHAINING ---");

        try {
            processUserRequest(42);
        } catch (RuntimeException e) {
            System.out.println("  Top-level exception: " + e.getMessage());
            System.out.println("  Caused by: " + e.getCause().getMessage());
            System.out.println("  Root cause: " + e.getCause().getCause().getMessage());

            // Full chain traversal
            System.out.println("  --- Full exception chain ---");
            Throwable cause = e;
            int depth = 0;
            while (cause != null) {
                System.out.println("  [" + depth + "] " + cause.getClass().getSimpleName()
                        + ": " + cause.getMessage());
                cause = cause.getCause();
                depth++;
            }
        }

        System.out.println();
    }


    // =========================================================================
    // 9. EXCEPTION PROPAGATION
    // =========================================================================
    // When an exception is NOT caught in the current method, it PROPAGATES
    // (bubbles up) to the calling method. This continues up the call stack
    // until either caught or it reaches the JVM (program terminates).
    //
    // Propagation rules:
    //   - Unchecked exceptions: propagate automatically without declaring
    //   - Checked exceptions: must be declared with "throws" at each level
    //
    // Interview Q: What is exception propagation?
    // A: The process by which an unhandled exception travels up the call stack
    //    from the method where it occurred to the method that called it.
    //    This continues until a matching catch block is found or the JVM terminates.
    // =========================================================================

    // Exception originates here
    static void methodC() {
        System.out.println("  [methodC] executing...");
        throw new RuntimeException("Exception originated in methodC");
    }

    // Exception propagates through here (not caught)
    static void methodB() {
        System.out.println("  [methodB] calling methodC...");
        methodC(); // propagates up to methodA
        System.out.println("  [methodB] this line NEVER executes");
    }

    // Exception propagates through here (not caught)
    static void methodA() {
        System.out.println("  [methodA] calling methodB...");
        methodB(); // propagates up to caller
        System.out.println("  [methodA] this line NEVER executes");
    }

    static void demonstrateExceptionPropagation() {
        System.out.println("--- 9. EXCEPTION PROPAGATION ---");
        System.out.println("  Call stack: main -> methodA -> methodB -> methodC");

        try {
            methodA(); // exception propagates all the way up to here
        } catch (RuntimeException e) {
            System.out.println("  [main] Caught exception: " + e.getMessage());
            System.out.println("  Exception propagated through: methodC -> methodB -> methodA -> main");
        }

        System.out.println();
    }


    // =========================================================================
    // 10. BEST PRACTICES
    // =========================================================================
    // Common mistakes and best practices for exception handling in production code
    // =========================================================================

    // Custom exception for demonstrating best practices
    static class ServiceException extends RuntimeException {
        public ServiceException(String message)                 { super(message); }
        public ServiceException(String message, Throwable cause){ super(message, cause); }
    }

    static void demonstrateBestPractices() {
        System.out.println("--- 10. BEST PRACTICES ---");

        System.out.println("  Best practices for exception handling:");
        System.out.println("  1. NEVER catch Throwable or Error (JVM-level issues)");
        System.out.println("  2. NEVER swallow exceptions (empty catch blocks)");
        System.out.println("  3. Use specific exception types, not just Exception");
        System.out.println("  4. Always include a meaningful message");
        System.out.println("  5. Always chain causes when wrapping exceptions");
        System.out.println("  6. Clean up resources in finally or use try-with-resources");
        System.out.println("  7. Don't use exceptions for flow control");
        System.out.println("  8. Log before rethrowing (don't double-log)");
        System.out.println("  9. Fail fast: validate inputs early");
        System.out.println("  10. Prefer unchecked exceptions for unrecoverable errors");

        // BAD: swallowing exception (NEVER do this!)
        try {
            String s = null;
            s.length();
        } catch (Exception e) {
            // BAD: silently ignoring the exception!
            // e.printStackTrace() at minimum, or log it properly
        }
        System.out.println("  [BAD PRACTICE] Swallowed exception above - don't do this!");

        // BAD: catching Exception or Throwable too broadly
        // catch (Exception e) { ... }  // catches everything - too broad
        // catch (Throwable e) { ... }  // NEVER do this - catches Errors too!

        // GOOD: specific exception, log and rethrow
        try {
            specificOperation();
        } catch (NullPointerException e) {
            // Log the exception (in real code: log.error("...", e))
            System.out.println("  [GOOD] Caught specific NPE: " + e.getMessage());
            // If you can't recover, wrap and rethrow with context
            throw new ServiceException("Null value encountered in operation", e);
        } catch (ServiceException e) {
            // Rethrow without double-logging
            throw e;
        }
    }

    static void specificOperation() {
        String config = null;
        config.length(); // deliberate NPE
    }


    // =========================================================================
    // 11. STACKOVERFLOW ERROR
    // =========================================================================
    // Occurs when the call stack exceeds its maximum depth.
    // Most common cause: infinite recursion (missing base case).
    //
    // StackOverflowError extends VirtualMachineError extends Error
    //
    // Can you catch it? YES, but you generally should NOT.
    //   - It's an Error, not Exception
    //   - The stack is already exhausted when caught, unreliable state
    //
    // Solutions:
    //   - Add base case to recursive method
    //   - Convert recursion to iteration
    //   - Increase stack size: java -Xss4m MyApp (rarely the right fix)
    // =========================================================================

    // Example: recursive method WITHOUT base case
    static int badFactorial(int n) {
        return n * badFactorial(n - 1); // MISSING base case -> infinite recursion
    }

    // Example: correct recursive method WITH base case
    static long goodFactorial(int n) {
        if (n <= 1) return 1;       // BASE CASE: stops recursion
        return n * goodFactorial(n - 1); // recursive case
    }

    // Example: mutual recursion (A calls B, B calls A)
    static boolean isEven(int n) {
        if (n == 0) return true;
        return isOdd(n - 1); // mutual recursion with isOdd
    }

    static boolean isOdd(int n) {
        if (n == 0) return false;
        return isEven(n - 1); // mutual recursion with isEven
    }

    static void demonstrateStackOverflowError() {
        System.out.println("--- 11. STACK OVERFLOW ERROR ---");

        // Demonstrate StackOverflowError from infinite recursion
        try {
            badFactorial(10000); // will overflow
        } catch (StackOverflowError e) {
            System.out.println("  StackOverflowError caught! (infinite recursion)");
            System.out.println("  Type: " + e.getClass().getSimpleName());
            System.out.println("  This is an Error, NOT an Exception - do not catch in production!");
        }

        // Correct recursive approach
        System.out.println("  Good recursive factorial(10) = " + goodFactorial(10)); // 3628800
        System.out.println("  isEven(4) = " + isEven(4));
        System.out.println("  isOdd(7)  = " + isOdd(7));

        System.out.println();
    }


    // =========================================================================
    // 12. OUT OF MEMORY ERROR
    // =========================================================================
    // Occurs when JVM cannot allocate memory for a new object because
    // the heap is exhausted and garbage collection cannot free enough memory.
    //
    // OutOfMemoryError extends VirtualMachineError extends Error
    //
    // Common causes:
    //   1. Creating too many objects (memory leak)
    //   2. Large arrays or collections
    //   3. Memory leaks (objects referenced but not needed)
    //   4. Too many class definitions (PermGen/Metaspace)
    //   5. Finalizers blocking GC
    //
    // Common variants:
    //   - "Java heap space"           - heap exhausted
    //   - "GC overhead limit exceeded" - GC running too much, gaining too little
    //   - "Metaspace"                 - too many class definitions (Java 8+)
    //   - "unable to create native thread" - OS can't create more threads
    //
    // Solutions:
    //   - Increase heap: java -Xmx2g MyApp
    //   - Fix memory leaks (use profiler: VisualVM, JProfiler, YourKit)
    //   - Use streaming for large data instead of loading all into memory
    //   - Use weak references for caches (WeakHashMap)
    // =========================================================================

    static void demonstrateOutOfMemoryError() {
        System.out.println("--- 12. OUT OF MEMORY ERROR ---");

        // NOTE: We do NOT actually trigger OOM here (would crash the demo)
        // Instead, we show the pattern and what causes it

        System.out.println("  OOM causes:");
        System.out.println("  1. java.util.List<byte[]> list = new ArrayList<>();");
        System.out.println("     while(true) { list.add(new byte[1024 * 1024]); } // memory leak");
        System.out.println("  2. int[] bigArray = new int[Integer.MAX_VALUE]; // too large");
        System.out.println("  3. Circular references preventing GC (in some cases)");
        System.out.println();
        System.out.println("  OOM JVM flags:");
        System.out.println("  -Xms256m  : initial heap size");
        System.out.println("  -Xmx2g    : maximum heap size");
        System.out.println("  -XX:+HeapDumpOnOutOfMemoryError : dump heap on OOM");
        System.out.println("  -XX:HeapDumpPath=/tmp/dump.hprof");
        System.out.println();
        System.out.println("  Detecting OOM: Use VisualVM, JConsole, or -verbose:gc flag");
        System.out.println();
    }


    // =========================================================================
    // INTERVIEW QUESTIONS
    // =========================================================================

    static void demonstrateInterviewQuestions() {
        System.out.println("--- INTERVIEW QUESTIONS ---");

        // Q1: Can finally block return a value that overrides try/catch?
        System.out.println("Q1: Can finally override try/catch return?");
        System.out.println("  Answer: YES. finally return overrides try/catch return.");
        System.out.println("  Result: '" + q1FinallyReturn() + "'");  // prints "from finally"

        // Q2: Can we have try without catch?
        System.out.println("\nQ2: Can we have try without catch?");
        System.out.println("  Answer: YES. try-finally is valid (no catch required).");
        q2TryWithoutCatch();

        // Q3: What happens when exception occurs in finally?
        System.out.println("\nQ3: What happens when exception is thrown in finally?");
        System.out.println("  Answer: finally exception REPLACES the original exception (original is LOST).");
        System.out.println("  This is why try-with-resources uses suppressed exceptions instead.");
        try {
            q3ExceptionInFinally();
        } catch (RuntimeException e) {
            System.out.println("  Caught: " + e.getMessage() + " (original exception was lost!)");
        }

        // Q4: Can we catch Error?
        System.out.println("\nQ4: Can we catch Error (StackOverflowError, OutOfMemoryError)?");
        System.out.println("  Answer: Syntactically YES, but it's BAD PRACTICE.");
        System.out.println("  Errors indicate serious JVM problems - the JVM may be in an unreliable state.");
        System.out.println("  Never catch Error in production code unless you have a very specific reason.");

        // Q5: Difference between final, finally, finalize
        System.out.println("\nQ5: Difference between final, finally, and finalize?");
        System.out.println("  final     - keyword for constants, prevent override/extend/reassign");
        System.out.println("  finally   - block in try-catch that ALWAYS executes (cleanup)");
        System.out.println("  finalize  - deprecated Object method called by GC before collection");
        System.out.println("              (removed in Java 18, unreliable, do not use)");

        // Q6: What is a checked exception?
        System.out.println("\nQ6: Why does Java have checked exceptions?");
        System.out.println("  Design philosophy: force callers to think about error handling");
        System.out.println("  for recoverable conditions (file not found, network timeout).");
        System.out.println("  Controversy: many modern frameworks prefer unchecked exceptions");
        System.out.println("  because checked exceptions pollute method signatures.");

        // Q7: Exception in static initializer
        System.out.println("\nQ7: What happens if exception occurs in static initializer?");
        System.out.println("  Answer: The class fails to load, and an ExceptionInInitializerError");
        System.out.println("  is thrown. All subsequent attempts to use the class throw NoClassDefFoundError.");

        // Q8: Multi-catch and re-throw
        System.out.println("\nQ8: Can you rethrow an exception from a multi-catch block?");
        System.out.println("  Answer: YES. In Java 7+, if you rethrow from a multi-catch,");
        System.out.println("  the compiler is smart enough to know the actual specific type");
        System.out.println("  being rethrown (more precise rethrow).");

        System.out.println();
    }

    static String q1FinallyReturn() {
        try {
            System.out.println("  [try] executing, will return 'from try'");
            return "from try";     // prepared but overridden
        } catch (Exception e) {
            return "from catch";
        } finally {
            System.out.println("  [finally] executing, will return 'from finally'");
            return "from finally"; // THIS wins - overrides try/catch return
        }
    }

    static void q2TryWithoutCatch() {
        System.out.println("  try-finally (no catch):");
        try {
            System.out.println("  [try] doing cleanup-critical work");
        } finally {
            System.out.println("  [finally] cleanup executed (no catch needed)");
        }
    }

    static void q3ExceptionInFinally() {
        try {
            throw new RuntimeException("Original exception from try");
        } finally {
            // This exception REPLACES the try block exception!
            // The original exception is LOST (not suppressed, just gone)
            throw new RuntimeException("Exception from finally - REPLACES original!");
        }
    }

}

/*
 * =============================================================================
 * COMPLETE SUMMARY: EXCEPTION HANDLING QUICK REFERENCE
 * =============================================================================
 *
 * HIERARCHY:
 *   Throwable -> Error (JVM errors, don't catch)
 *   Throwable -> Exception -> RuntimeException (unchecked, programming bugs)
 *   Throwable -> Exception -> CheckedException (IO, SQL, etc., must handle)
 *
 * KEY CONCEPTS TABLE:
 * +-------------------------------+-------------------------------------------+
 * | Concept                       | Key Points                                |
 * +-------------------------------+-------------------------------------------+
 * | Checked Exception             | Extends Exception, compile-time check,    |
 * |                               | must catch or declare with throws          |
 * | Unchecked Exception           | Extends RuntimeException, no forced catch  |
 * | Error                         | Extends Error, JVM issues, don't catch     |
 * | finally                       | Always executes (except System.exit())     |
 * | try-with-resources            | AutoCloseable, auto close, Java 7+         |
 * | multi-catch                   | catch(A | B e), Java 7+                   |
 * | throw                         | Throws instance, used in method body       |
 * | throws                        | Declares possible exceptions in signature  |
 * | Exception chaining            | new Ex("msg", cause)                       |
 * | Suppressed exceptions         | Try-with-resources, e.getSuppressed()      |
 * | StackOverflowError            | Infinite recursion, is an Error            |
 * | OutOfMemoryError              | Heap exhausted, is an Error                |
 * +-------------------------------+-------------------------------------------+
 *
 * COMMON INTERVIEW QUESTIONS:
 *
 * Q: Can we have try without catch?
 * A: YES - try-finally is valid. catch is optional.
 *
 * Q: Can finally return a value?
 * A: YES - and it overrides the try/catch return value.
 *
 * Q: When does finally NOT execute?
 * A: When System.exit() is called, or the JVM crashes/is killed (SIGKILL).
 *
 * Q: What are suppressed exceptions?
 * A: When try-with-resources close() throws while try block also has an
 *    exception, the close() exception is suppressed (attached to main exception).
 *    Access via e.getSuppressed().
 *
 * Q: What is exception propagation?
 * A: Uncaught exceptions bubble up the call stack until caught or JVM terminates.
 *
 * Q: final vs finally vs finalize?
 * A: final=keyword(const/no-override), finally=cleanup block, finalize=deprecated GC method
 *
 * Q: Can StackOverflowError be caught?
 * A: Yes syntactically, but DON'T. It's an Error, JVM state is unreliable.
 *
 * Q: Why prefer unchecked exceptions in modern Java?
 * A: Clean API signatures, Spring/Hibernate use them, callers can't always recover.
 *    Checked exceptions lead to empty catch blocks or "throws Exception" pollution.
 *
 * Q: What is the difference between throw and throws?
 * A: throw (verb): used inside method body to throw an exception instance.
 *    throws (declarator): used in method signature to declare checked exceptions.
 *
 * Q: Order of catch blocks - why does it matter?
 * A: More specific (child) exceptions MUST come before general (parent) ones.
 *    Otherwise, the specific catch is unreachable -> compile error.
 *
 * BEST PRACTICES CHECKLIST:
 *   [ ] Never catch Throwable or Error
 *   [ ] Never swallow exceptions (empty catch)
 *   [ ] Always include meaningful message
 *   [ ] Always chain causes when wrapping
 *   [ ] Use try-with-resources for AutoCloseable resources
 *   [ ] Catch specific exceptions, not broad Exception
 *   [ ] Log at appropriate level (don't double-log)
 *   [ ] Fail fast with early validation (IllegalArgumentException)
 *   [ ] Prefer unchecked for business logic exceptions
 *   [ ] Add domain-specific fields to custom exceptions
 * =============================================================================
 */
