package com.interview.master.javacore.advanced;

/**
 * ============================================================
 * ADVANCED JAVA CONCEPTS — Complete Interview Reference
 * ============================================================
 * Topics covered:
 *  1.  Generics
 *  2.  String deep dive
 *  3.  Serialization
 *  4.  Reflection
 *  5.  Annotations
 *  6.  Java Memory Model & GC
 *  7.  Immutability
 *  8.  Comparable vs Comparator
 *  9.  var keyword (Java 10)
 * 10.  Records (Java 16)
 * 11.  Sealed classes (Java 17)
 * 12.  Pattern matching
 * ============================================================
 */

import java.io.*;
import java.lang.annotation.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

// ── Needed for Records implementing an interface ───────────────
// (declared before the class so they are visible to nested types)

public class AdvancedJavaConcepts {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Advanced Java Concepts Demo ===\n");

        // 1 — Generics
        GenericsDemo.run();

        // 2 — String deep dive
        StringDeepDive.run();

        // 3 — Serialization
        SerializationDemo.run();

        // 4 — Reflection
        ReflectionDemo.run();

        // 5 — Annotations
        AnnotationsDemo.run();

        // 6 — Memory Model summary (printed; JVM heap not directly observable)
        MemoryModelGuide.print();

        // 7 — Immutability
        ImmutabilityDemo.run();

        // 8 — Comparable vs Comparator
        SortingDemo.run();

        // 9 — var keyword
        VarKeywordDemo.run();

        // 10 — Records
        RecordsDemo.run();

        // 11 — Sealed classes
        SealedClassesDemo.run();

        // 12 — Pattern matching
        PatternMatchingDemo.run();

        System.out.println("\n=== All demos complete ===");
    }

    // ============================================================
    // ============================================================
    //  1.  GENERICS
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Generics:
     *
     * Q: What are Generics?
     *    A compile-time mechanism that allows classes, interfaces, and methods
     *    to operate on typed parameters. Added in Java 5.
     *    Benefit: type-safety without casting; errors caught at compile time.
     *
     * Q: What is type erasure?
     *    At runtime, ALL generic type information is ERASED by the compiler.
     *    List<String> and List<Integer> both become List at runtime.
     *    The bytecode contains casts inserted by the compiler, not the type params.
     *    Implication: you cannot do: new T(), T.class, instanceof List<String>
     *
     * Q: Why can't you create a generic array?  new T[10]
     *    Because arrays are covariant and reified (they know their type at runtime).
     *    Generics are invariant and erased (they DON'T know their type at runtime).
     *    Mixing them would break type safety: a T[] would be treated as Object[] at
     *    runtime, allowing insertion of wrong types without ArrayStoreException.
     *    Workaround: List<T> or @SuppressWarnings("unchecked") T[] arr = (T[]) new Object[n]
     *
     * Q: What is the difference between List<? extends T> and List<? super T>?
     *    Producer Extends, Consumer Super (PECS rule):
     *    List<? extends T> — you can READ T from it, but NOT ADD to it (except null)
     *                        producer: something that produces T values
     *    List<? super T>   — you can ADD T to it, but you only READ Object from it
     *                        consumer: something that consumes T values
     *
     * Q: What is an unbounded wildcard List<?>?
     *    "I have a list of some unknown type." You can read Object from it and add null.
     *    Use when the method only uses Object methods (e.g., size(), contains()).
     *
     * Q: Generic class vs raw type?
     *    Raw type: List list = new ArrayList();  — no type parameter; unsafe
     *    Generic : List<String> list = new ArrayList<>(); — type-safe
     */
    static class GenericsDemo {

        // ── Generic class with bounded type parameter ──────────────

        /**
         * A container that can hold any type T.
         * Interview Note: T is just a convention; could be any identifier.
         * Common conventions: T (type), E (element), K (key), V (value), N (number), R (return)
         */
        static class Box<T> {
            private T value;

            public Box(T value) { this.value = value; }

            public T get()      { return value; }
            public void set(T v) { this.value = v; }

            // Generic method inside a generic class
            public <R> Box<R> map(Function<T, R> mapper) {
                return new Box<>(mapper.apply(value));
            }

            @Override public String toString() { return "Box[" + value + "]"; }
        }

        /**
         * Generic class with an UPPER-BOUNDED type parameter.
         * T extends Number — T must be Number or one of its subclasses
         * (Integer, Long, Double, BigDecimal, etc.)
         */
        static class NumberBox<T extends Number & Comparable<T>> {
            private T value;
            public NumberBox(T value) { this.value = value; }

            public double doubleValue() { return value.doubleValue(); }
            public T      getValue()    { return value; }

            // Can call Number methods on T because T extends Number
            public boolean isGreaterThan(NumberBox<T> other) {
                return this.value.compareTo(other.value) > 0;
            }
        }

        // ── Generic method ─────────────────────────────────────────

        /**
         * Type parameter <T> is declared on the METHOD, independent of the class.
         * The compiler infers T from the arguments.
         */
        static <T> List<T> repeat(T item, int times) {
            List<T> list = new ArrayList<>();
            for (int i = 0; i < times; i++) list.add(item);
            return list;
        }

        /**
         * Multiple bounds: T must extend Number AND implement Comparable<T>.
         * Syntax: T extends TypeA & InterfaceB & InterfaceC
         * Only ONE class bound; class must come FIRST; rest must be interfaces.
         */
        static <T extends Number & Comparable<T>> T max(T a, T b) {
            return a.compareTo(b) >= 0 ? a : b;
        }

        // ── Wildcards ──────────────────────────────────────────────

        /**
         * PECS — Producer Extends, Consumer Super
         *
         * List<? extends Number> — PRODUCER: method READS from list
         *   Can: get Number items.  Cannot: add items (type unknown).
         */
        static double sumList(List<? extends Number> numbers) {
            return numbers.stream().mapToDouble(Number::doubleValue).sum();
        }

        /**
         * List<? super Integer> — CONSUMER: method WRITES Integer into list
         *   Can: add Integer (or subtypes) items.  Cannot: read typed items (only Object).
         */
        static void fillWithIntegers(List<? super Integer> list, int count) {
            for (int i = 0; i < count; i++) list.add(i);
        }

        /**
         * Unbounded wildcard List<?> — method only cares about collection structure,
         * not the element type. Can only read Object.
         */
        static void printSize(List<?> list) {
            System.out.println("  Size: " + list.size());
        }

        // ── Type erasure demonstration ─────────────────────────────

        @SuppressWarnings("unchecked")
        static void typeErasureDemo() {
            List<String> strings   = new ArrayList<>();
            List<Integer> integers = new ArrayList<>();

            // At runtime both are just List (raw type) — same class
            System.out.println("  Same class at runtime: "
                + (strings.getClass() == integers.getClass())); // true

            // Cannot check generic type at runtime
            System.out.println("  strings instanceof List<?>: " + (strings instanceof List<?>));
            // strings instanceof List<String> — COMPILE ERROR: cannot use generic type in instanceof
        }

        static void run() {
            System.out.println("--- 1. Generics ---");

            Box<String> strBox = new Box<>("Hello");
            Box<Integer> intBox = strBox.map(String::length);
            System.out.println("  strBox: " + strBox + ", intBox: " + intBox);

            NumberBox<Integer> nb1 = new NumberBox<>(10);
            NumberBox<Integer> nb2 = new NumberBox<>(20);
            System.out.println("  nb1 > nb2? " + nb1.isGreaterThan(nb2));
            System.out.println("  max(3,7): " + max(3, 7));
            System.out.println("  max(3.14, 2.71): " + max(3.14, 2.71));

            System.out.println("  repeat('X',3): " + repeat("X", 3));

            List<Integer> ints   = List.of(1, 2, 3, 4, 5);
            List<Double>  doubles = List.of(1.1, 2.2);
            System.out.println("  sum(ints):    " + sumList(ints));
            System.out.println("  sum(doubles): " + sumList(doubles));

            List<Number> numList = new ArrayList<>();
            fillWithIntegers(numList, 3);
            System.out.println("  numList after fill: " + numList);

            printSize(ints);
            typeErasureDemo();
        }
    }

    // ============================================================
    // ============================================================
    //  2.  STRING DEEP DIVE
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — String:
     *
     * Q: Why is String immutable in Java?
     *    1. SECURITY: String used for class names, network connections, DB URLs.
     *       Immutability prevents malicious code from changing a path after a
     *       security check (time-of-check to time-of-use vulnerability).
     *    2. STRING POOL / CACHING: immutable strings can be safely shared.
     *       Multiple references can point to the same object without risk.
     *    3. THREAD SAFETY: immutable objects are inherently thread-safe.
     *    4. HASHCODE CACHING: String caches its hashCode because the value
     *       can never change. HashMap/HashSet operations are faster.
     *    5. CLASS LOADING: ClassLoader uses String for class names; mutability
     *       would be a security nightmare.
     *
     * Q: What is the String pool (String constant pool)?
     *    A special region in the JVM heap (since Java 7 moved from PermGen to Heap)
     *    where string LITERALS are stored. When you write:
     *      String a = "hello";
     *      String b = "hello";
     *    Both a and b point to the SAME object in the pool.
     *    new String("hello") creates a NEW object in the HEAP, NOT the pool.
     *
     * Q: What does intern() do?
     *    String.intern() checks if a string with the same content exists in the pool.
     *    If yes: returns the pooled reference.
     *    If no:  adds the string to the pool and returns it.
     *    Use case: memory optimisation when you have millions of equal strings.
     *    Modern JVMs deduplicate strings automatically (G1 string dedup) — rarely needed.
     *
     * Q: String vs StringBuilder vs StringBuffer?
     *    String        — immutable; every operation creates a NEW object; thread-safe by nature
     *    StringBuilder — mutable; NOT thread-safe; use in single-threaded code; FAST
     *    StringBuffer  — mutable; THREAD-SAFE (all methods synchronized); SLOWER than StringBuilder
     *    Rule of thumb: always use StringBuilder unless you need thread safety.
     *
     * Q: == vs equals() for Strings?
     *    ==         compares REFERENCES (same object in memory)
     *    equals()   compares CONTENT  (same sequence of characters)
     *    NEVER use == to compare String content in production code.
     */
    static class StringDeepDive {

        static void run() {
            System.out.println("\n--- 2. String Deep Dive ---");

            // ── String pool ────────────────────────────────────────
            String a = "hello";                  // from pool
            String b = "hello";                  // same object from pool
            String c = new String("hello");      // NEW heap object — NOT from pool
            String d = new String("hello").intern(); // intern() → pool reference

            System.out.println("  a == b (pool literals):      " + (a == b));   // true
            System.out.println("  a == c (new String):         " + (a == c));   // false
            System.out.println("  a == d (interned):           " + (a == d));   // true
            System.out.println("  a.equals(c):                 " + a.equals(c)); // true

            // ── Immutability ───────────────────────────────────────
            String original = "Hello";
            String modified = original.concat(" World");
            System.out.println("  original after concat: '" + original + "'"); // unchanged
            System.out.println("  modified: '"             + modified  + "'");

            // ── StringBuilder performance ──────────────────────────
            // BAD: creates N intermediate String objects
            String slow = "";
            for (int i = 0; i < 5; i++) slow = slow + i;
            System.out.println("  Concatenation result: " + slow);

            // GOOD: single mutable buffer, one final toString()
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) sb.append(i);
            String fast = sb.toString();
            System.out.println("  StringBuilder result: " + fast);

            // ── StringBuilder API ──────────────────────────────────
            StringBuilder demo = new StringBuilder("Hello");
            demo.append(" World")          // appends
                .insert(5, ",")            // inserts at index
                .replace(7, 12, "Java")    // replace chars 7-12
                .reverse();                // reverses in-place
            System.out.println("  StringBuilder demo: " + demo);
            demo.reverse();                // reverse back
            System.out.println("  After re-reverse:   " + demo);

            // ── StringBuffer (thread-safe, same API) ──────────────
            StringBuffer tsBuffer = new StringBuffer("thread-safe");
            tsBuffer.append(" demo");
            System.out.println("  StringBuffer: " + tsBuffer);

            // ── String comparison ──────────────────────────────────
            String s1 = "Java";
            String s2 = new String("Java");
            System.out.println("  s1 == s2:              " + (s1 == s2));         // false
            System.out.println("  s1.equals(s2):         " + s1.equals(s2));       // true
            System.out.println("  s1.equalsIgnoreCase:   " + s1.equalsIgnoreCase("java")); // true
            System.out.println("  s1.compareTo(\"Java\"): " + s1.compareTo("Java"));  // 0

            // ── String methods cheat-sheet ─────────────────────────
            String str = "  Hello, World!  ";
            System.out.println("  trim():            '" + str.trim() + "'");
            System.out.println("  strip():           '" + str.strip() + "'"); // Unicode-aware (Java 11)
            System.out.println("  toUpperCase():     " + "hello".toUpperCase());
            System.out.println("  substring(7,12):   " + "Hello, World!".substring(7, 12));
            System.out.println("  replace:           " + "aabbcc".replace("bb", "XX"));
            System.out.println("  split:             " + Arrays.toString("a,b,c".split(",")));
            System.out.println("  contains:          " + "Hello World".contains("World"));
            System.out.println("  startsWith:        " + "Hello".startsWith("He"));
            System.out.println("  indexOf:           " + "Hello".indexOf('l'));
            System.out.println("  charAt(1):         " + "Hello".charAt(1));
            System.out.println("  isEmpty:           " + "".isEmpty());
            System.out.println("  isBlank (J11):     " + "  ".isBlank());
            System.out.println("  repeat (J11):      " + "ab".repeat(3));
            System.out.println("  formatted (J15):   " + "Hello %s".formatted("World"));

            // ── String.join, String.valueOf, String.format ─────────
            System.out.println("  join:     " + String.join("-", "a", "b", "c"));
            System.out.println("  valueOf:  " + String.valueOf(42));
            System.out.println("  format:   " + String.format("%.2f", 3.14159));

            // ── char[] vs String (security consideration) ─────────
            // Passwords should be stored as char[] NOT String, because:
            // 1. String lingers in the pool — stays in memory until GC
            // 2. char[] can be explicitly zeroed out after use:
            char[] password = {'s', 'e', 'c', 'r', 'e', 't'};
            Arrays.fill(password, '\0'); // zero out immediately — security best practice
        }
    }

    // ============================================================
    // ============================================================
    //  3.  SERIALIZATION
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Serialization:
     *
     * Q: What is serialization?
     *    Converting a Java object graph (object + all referenced objects) into a
     *    byte stream for: file persistence, network transfer, caching, deep copy.
     *    Deserialization: converting the byte stream back to objects.
     *
     * Q: What is the Serializable interface?
     *    A MARKER interface (no methods) in java.io. Implementing it tells the JVM
     *    that objects of this class can be serialized with ObjectOutputStream.
     *
     * Q: What is serialVersionUID?
     *    A static final long field that acts as a VERSION ID for the class.
     *    During deserialization the JVM checks the serialVersionUID in the byte stream
     *    against the current class's serialVersionUID.
     *    If they DIFFER → InvalidClassException.
     *    If you DON'T declare it the JVM auto-generates one from the class structure.
     *    If you add/remove a field WITHOUT updating serialVersionUID → stream mismatch → exception.
     *    Best practice: ALWAYS declare it explicitly.
     *
     * Q: What does transient mean?
     *    A transient field is EXCLUDED from serialization.
     *    When deserialized, transient fields get their DEFAULT value (null / 0 / false).
     *    Use for: passwords, socket connections, file handles, computed caches,
     *             any field that doesn't make sense to persist.
     *
     * Q: Serializable vs Externalizable?
     *    Serializable   — automatic (JVM handles it); uses reflection; slower.
     *                     You can customise via writeObject() / readObject() methods.
     *    Externalizable — manual (you implement writeExternal / readExternal);
     *                     full control over format; faster; more boilerplate.
     *
     * Q: Problems with Java serialization and alternatives?
     *    Problems:
     *      - Security: malicious byte streams can execute arbitrary code (deserialization attacks)
     *      - Fragile: tight coupling between serialized form and class structure
     *      - Performance: reflection-based; verbose binary format
     *      - No schema: hard to version across different systems
     *    Alternatives:
     *      JSON  — Jackson/Gson; human-readable; language-neutral; widespread
     *      XML   — JAXB; verbose; widely supported
     *      Protobuf — Google Protocol Buffers; binary; schema-driven; fast
     *      Avro  — Apache Avro; schema evolution; Hadoop ecosystem
     *      Kryo  — fast binary; Java-only
     */
    static class SerializationDemo {

        /**
         * Example serializable class.
         * serialVersionUID must be declared to control versioning.
         * transient marks fields to exclude from serialization.
         */
        static class Employee implements Serializable {
            private static final long serialVersionUID = 1L; // explicit version control

            private String name;
            private String department;
            private double salary;

            // transient — not serialized (sensitive data / non-serializable type)
            private transient String password;          // sensitive: exclude
            private transient int    cachedHashCode;    // computed: exclude

            public Employee(String name, String department, double salary, String password) {
                this.name       = name;
                this.department = department;
                this.salary     = salary;
                this.password   = password;
            }

            // Custom serialization hooks (optional — overrides default behaviour)
            private void writeObject(ObjectOutputStream out) throws IOException {
                out.defaultWriteObject(); // serialize non-transient fields normally
                // Could add encrypted data here
                System.out.println("    [writeObject called]");
            }

            private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
                in.defaultReadObject(); // deserialize non-transient fields
                // Recompute transient fields here
                this.cachedHashCode = Objects.hash(name, department);
                System.out.println("    [readObject called — cachedHashCode restored]");
            }

            @Override
            public String toString() {
                return "Employee{name='" + name + "', dept='" + department
                       + "', salary=" + salary + ", password='" + password
                       + "', hashCache=" + cachedHashCode + "}";
            }
        }

        /**
         * Externalizable — full manual control.
         * Must have a public no-arg constructor (JVM calls it during deserialization).
         */
        static class Config implements Externalizable {
            private String host;
            private int    port;
            private String protocol;

            public Config() {} // REQUIRED for Externalizable

            public Config(String host, int port, String protocol) {
                this.host = host; this.port = port; this.protocol = protocol;
            }

            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeUTF(host);
                out.writeInt(port);
                out.writeUTF(protocol);
            }

            @Override
            public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                this.host     = in.readUTF();
                this.port     = in.readInt();
                this.protocol = in.readUTF();
            }

            @Override
            public String toString() {
                return "Config{" + protocol + "://" + host + ":" + port + "}";
            }
        }

        static void run() {
            System.out.println("\n--- 3. Serialization ---");

            // Serialize Employee
            Employee original = new Employee("Alice", "Engineering", 85_000.0, "secret123");
            System.out.println("  Before: " + original);

            byte[] bytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
                bytes = baos.toByteArray();
                System.out.println("  Serialized to " + bytes.length + " bytes");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                Employee restored = (Employee) ois.readObject();
                System.out.println("  After:  " + restored);
                System.out.println("  transient password is null: " + (restored.password == null));
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Externalizable
            Config cfg = new Config("localhost", 8080, "https");
            System.out.println("  Config before: " + cfg);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(cfg);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                     ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Config restored = (Config) ois.readObject();
                    System.out.println("  Config after:  " + restored);
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ============================================================
    // ============================================================
    //  4.  REFLECTION
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Reflection:
     *
     * Q: What is Reflection?
     *    The ability to inspect and manipulate classes, methods, fields, and constructors
     *    at RUNTIME, even private ones, without knowing them at compile time.
     *    Package: java.lang.reflect
     *
     * Q: How do you get a Class object?
     *    Three ways:
     *      1. ClassName.class              — compile-time literal
     *      2. object.getClass()           — from an instance at runtime
     *      3. Class.forName("full.Name")  — from a String name (throws ClassNotFoundException)
     *
     * Q: What can you do with Reflection?
     *    - Inspect class name, superclass, interfaces, modifiers
     *    - Get/set fields (even private, via setAccessible(true))
     *    - Invoke methods (even private)
     *    - Create instances (constructor.newInstance())
     *    - Get annotations, generic type information
     *
     * Q: Use cases of Reflection?
     *    - Frameworks: Spring (dependency injection), Hibernate (entity mapping),
     *      JUnit (test discovery), Jackson (JSON serialization)
     *    - Testing: access private methods/fields in unit tests
     *    - Plugin systems: load classes by name at runtime
     *    - Serialization: iterate over all fields generically
     *    - Debuggers and profilers
     *
     * Q: Performance concerns with Reflection?
     *    - Slower than direct calls (bypasses JIT optimisations, extra overhead)
     *    - Should be avoided in hot paths
     *    - Modern JVMs reduce the gap with MethodHandle and invokedynamic
     *    - AccessibleObject.setAccessible() bypasses access checks — security concern
     *    - Java 9+ module system restricts reflective access by default
     *
     * Q: setAccessible(true) — what does it do?
     *    Suppresses the Java access control check for that member.
     *    Allows accessing private fields/methods from outside the class.
     *    Throws InaccessibleObjectException in Java 9+ if the module doesn't allow it.
     */
    static class ReflectionDemo {

        static class Secret {
            private String value = "hidden-secret";
            private int count = 42;

            private String getFormattedValue() {
                return "SECRET[" + value + ", count=" + count + "]";
            }
        }

        static void run() throws Exception {
            System.out.println("\n--- 4. Reflection ---");

            // ── Get Class object three ways ────────────────────────
            Class<?> c1 = String.class;               // compile-time literal
            Class<?> c2 = "hello".getClass();          // from instance
            Class<?> c3 = Class.forName("java.lang.String"); // from name
            System.out.println("  All same: " + (c1 == c2 && c2 == c3));

            // ── Inspect a class ────────────────────────────────────
            Class<?> clazz = Secret.class;
            System.out.println("  Class name: " + clazz.getName());
            System.out.println("  Simple name: " + clazz.getSimpleName());
            System.out.println("  Superclass: " + clazz.getSuperclass().getSimpleName());

            // Declared fields (all access levels in this class only)
            System.out.println("  Declared fields:");
            for (Field f : clazz.getDeclaredFields()) {
                System.out.println("    " + Modifier.toString(f.getModifiers())
                                   + " " + f.getType().getSimpleName() + " " + f.getName());
            }

            // Declared methods
            System.out.println("  Declared methods:");
            for (Method m : clazz.getDeclaredMethods()) {
                System.out.println("    " + Modifier.toString(m.getModifiers())
                                   + " " + m.getReturnType().getSimpleName()
                                   + " " + m.getName() + "()");
            }

            // ── Access private field ───────────────────────────────
            Secret obj = new Secret();
            Field valueField = clazz.getDeclaredField("value");
            valueField.setAccessible(true);  // bypass private access
            System.out.println("  Private field before: " + valueField.get(obj));
            valueField.set(obj, "modified-by-reflection");
            System.out.println("  Private field after:  " + valueField.get(obj));

            // ── Invoke private method ──────────────────────────────
            Method m = clazz.getDeclaredMethod("getFormattedValue");
            m.setAccessible(true);
            String result = (String) m.invoke(obj);
            System.out.println("  Private method result: " + result);

            // ── Create instance via reflection ─────────────────────
            // Use getDeclaredConstructor() for no-arg, or with param types
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object newInstance = ctor.newInstance();
            System.out.println("  Created via reflection: " + newInstance.getClass().getSimpleName());

            // ── Checking annotations at runtime ───────────────────
            // (see AnnotationsDemo for full annotation reflection)
            System.out.println("  String @Deprecated: "
                + String.class.isAnnotationPresent(Deprecated.class));
        }
    }

    // ============================================================
    // ============================================================
    //  5.  ANNOTATIONS
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Annotations:
     *
     * Q: What is an annotation?
     *    A form of metadata attached to code elements (class, method, field, parameter,
     *    package, annotation type). Does NOT change program logic directly.
     *    Defined with @interface keyword.
     *
     * Q: What are meta-annotations?
     *    Annotations that annotate other annotation definitions:
     *
     *    @Retention(RetentionPolicy.X) — when the annotation is retained:
     *      SOURCE  — discarded by compiler (e.g., @Override, @SuppressWarnings)
     *      CLASS   — in .class file but NOT in JVM at runtime (default)
     *      RUNTIME — in JVM at runtime; readable via Reflection (e.g., @SpringBootApplication)
     *
     *    @Target(ElementType.X) — where the annotation can be applied:
     *      TYPE            — class, interface, enum, annotation type
     *      METHOD          — method declaration
     *      FIELD           — field (including enum constants)
     *      PARAMETER       — formal method parameter
     *      CONSTRUCTOR     — constructor
     *      LOCAL_VARIABLE  — local variable
     *      ANNOTATION_TYPE — another annotation type
     *      PACKAGE         — package declaration
     *
     *    @Documented — include annotation in Javadoc
     *    @Inherited  — subclasses inherit the annotation from superclass
     *                  (only for @Target(TYPE); not inherited for methods/fields)
     *    @Repeatable — allow the same annotation to be used multiple times on one element
     *
     * Q: How to process annotations at runtime?
     *    Use Reflection: element.getAnnotation(MyAnno.class) or isAnnotationPresent()
     *    This requires @Retention(RUNTIME).
     */
    static class AnnotationsDemo {

        // ── Define a custom annotation ─────────────────────────────

        /**
         * @Retention(RUNTIME) — readable at runtime via Reflection
         * @Target(METHOD)     — only applies to methods
         * @Documented         — appears in Javadoc
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.METHOD, ElementType.TYPE})
        @Documented
        @interface ApiEndpoint {
            String path();
            String method() default "GET";   // element with default value
            String[] roles() default {};     // array element
            boolean deprecated() default false;
        }

        /**
         * @Inherited — if a class has this annotation, its subclasses inherit it.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @Inherited
        @interface Service {
            String name() default "";
        }

        /**
         * SOURCE retention — used only for compile-time checks, then discarded.
         * Example: @Override tells compiler to verify overriding; not in bytecode.
         */
        @Retention(RetentionPolicy.SOURCE)
        @Target(ElementType.METHOD)
        @interface CompileTimeOnly {
            String reason();
        }

        // ── Use the custom annotation ──────────────────────────────

        @Service(name = "EmployeeAPI")
        static class AnnotatedController {

            @ApiEndpoint(path = "/employees", method = "GET", roles = {"USER", "ADMIN"})
            public void getAllEmployees() { /* ... */ }

            @ApiEndpoint(path = "/employees/{id}", method = "DELETE",
                         roles = {"ADMIN"}, deprecated = true)
            public void deleteEmployee() { /* ... */ }

            @CompileTimeOnly(reason = "Marks legacy methods for migration tracking")
            public void legacyMethod() { /* ... */ }
        }

        // ── Annotation processor (runtime reflection) ──────────────

        static void processAnnotations(Class<?> clazz) {
            // Class-level annotation
            if (clazz.isAnnotationPresent(Service.class)) {
                Service svc = clazz.getAnnotation(Service.class);
                System.out.println("  @Service: name='" + svc.name() + "'");
            }

            // Method-level annotations
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ApiEndpoint.class)) {
                    ApiEndpoint ep = method.getAnnotation(ApiEndpoint.class);
                    System.out.println("  @ApiEndpoint: "
                        + ep.method() + " " + ep.path()
                        + " roles=" + Arrays.toString(ep.roles())
                        + " deprecated=" + ep.deprecated());
                }
                // @CompileTimeOnly is SOURCE retention → not visible here at runtime
            }
        }

        static void run() {
            System.out.println("\n--- 5. Annotations ---");
            processAnnotations(AnnotatedController.class);

            // Reflection on built-in annotations
            System.out.println("  Thread.sleep @Deprecated: "
                + isDeprecatedMethod(Thread.class, "sleep", long.class));
        }

        static boolean isDeprecatedMethod(Class<?> clazz, String name, Class<?>... params) {
            try {
                return clazz.getMethod(name, params).isAnnotationPresent(Deprecated.class);
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    }

    // ============================================================
    // ============================================================
    //  6.  JAVA MEMORY MODEL & GARBAGE COLLECTION
    // ============================================================
    // ============================================================

    /**
     * Interview Reference — Memory Model & GC:
     *
     * ── JVM Memory Areas ──────────────────────────────────────────
     *
     *   HEAP (shared across all threads):
     *     Young Generation:
     *       Eden Space    — new objects allocated here first (fast; bump-pointer alloc)
     *       Survivor S0   — objects that survived one minor GC
     *       Survivor S1   — objects that survived another minor GC
     *     Old Generation (Tenured):
     *       Objects that survived enough minor GCs (tenuring threshold, default 15)
     *       Major GC / Full GC cleans this; slower, pauses longer
     *     Metaspace (Java 8+):
     *       Stores class metadata (was PermGen in Java 7 and earlier)
     *       Grows dynamically in native memory (not in heap)
     *       Avoids the PermGen OutOfMemoryError problem
     *
     *   STACK (per thread):
     *     Each method call creates a STACK FRAME containing:
     *       - Local variables (primitives and references)
     *       - Operand stack
     *       - Return address
     *     Stack frames are pushed on call, popped on return
     *     StackOverflowError — too many nested calls (usually infinite recursion)
     *
     *   OTHERS:
     *     PC (Program Counter) Register — current instruction per thread
     *     Native Method Stack — for native (C/C++) method calls
     *     Code Cache — JIT-compiled native code
     *
     * ── GC Algorithms ─────────────────────────────────────────────
     *
     *   Serial GC      — single thread; stop-the-world; good for small heaps / CLI tools
     *                    -XX:+UseSerialGC
     *
     *   Parallel GC    — multiple threads for minor GC; stop-the-world; throughput-focused
     *   (Throughput GC)  Default in Java 8. -XX:+UseParallelGC
     *
     *   CMS            — Concurrent Mark Sweep; low-pause old-gen collection;
     *   (deprecated)     most work concurrent with app threads; fragmentation issue;
     *                    DEPRECATED in Java 9, REMOVED in Java 14
     *
     *   G1GC            — Garbage First; divides heap into equal-sized regions;
     *   (default J9-J20)  can compact heap (no fragmentation); targets pause goals;
     *                    -XX:+UseG1GC  -XX:MaxGCPauseMillis=200
     *
     *   ZGC             — Z Garbage Collector (Java 15+ production);
     *                     pause < 1ms regardless of heap size;
     *                     scalable to multi-TB heaps;
     *                     -XX:+UseZGC
     *
     *   Shenandoah      — RedHat; concurrent compaction; very low pauses;
     *                     -XX:+UseShenandoahGC
     *
     * ── GC Roots ──────────────────────────────────────────────────
     *   Objects reachable from GC roots are NOT collected. Roots include:
     *     - Local variables on active thread stacks
     *     - Static fields of loaded classes
     *     - JNI references
     *     - Synchronized monitors
     *   Anything NOT reachable from a root is eligible for collection.
     *
     * ── Reference Types ───────────────────────────────────────────
     *   Strong   — normal reference (Object obj = new Object()).
     *              Object is NEVER collected while a strong reference exists.
     *
     *   Soft     — SoftReference<T>. GC may collect when memory is LOW.
     *              Used for: memory-sensitive caches. Guaranteed collected before OOM.
     *
     *   Weak     — WeakReference<T>. GC collects at NEXT GC cycle regardless of memory.
     *              Used for: canonicalizing mappings (WeakHashMap), listeners.
     *              get() returns null after collection.
     *
     *   Phantom  — PhantomReference<T>. Object already FINALIZED; reference enqueued
     *              in a ReferenceQueue so you know when memory can be reclaimed.
     *              get() ALWAYS returns null. Used for: native resource cleanup.
     */
    static class MemoryModelGuide {

        @SuppressWarnings("unused")
        static void demonstrateReferences() {
            // Strong reference — object lives as long as strongRef is in scope
            Object strongRef = new Object();

            // Soft reference — kept alive while memory allows; good for caches
            SoftReference<byte[]> softCache = new SoftReference<>(new byte[1024 * 1024]);
            byte[] cached = softCache.get(); // returns null if GC collected it
            if (cached != null) System.out.println("  Soft ref: cache still alive");

            // Weak reference — collected at next GC
            WeakReference<Object> weakRef = new WeakReference<>(new Object());
            System.gc(); // suggestion; not guaranteed
            System.out.println("  Weak ref after GC hint: " + weakRef.get()); // likely null

            // Phantom reference with queue — for post-finalization cleanup
            ReferenceQueue<Object> queue = new ReferenceQueue<>();
            PhantomReference<Object> phantomRef =
                new PhantomReference<>(new Object(), queue);
            System.out.println("  Phantom ref get(): " + phantomRef.get()); // always null
        }

        static void print() {
            System.out.println("\n--- 6. Memory Model & GC (see Javadoc for full details) ---");
            System.out.println("  Heap regions: Eden + S0 + S1 (Young) | Tenured (Old) | Metaspace");
            System.out.println("  GC algorithms: Serial, Parallel, G1GC (default), ZGC, Shenandoah");
            System.out.println("  Reference types: Strong > Soft > Weak > Phantom");
            demonstrateReferences();
        }
    }

    // ============================================================
    // ============================================================
    //  7.  IMMUTABILITY
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Immutability:
     *
     * Q: What is an immutable class?
     *    A class whose instances CANNOT be changed after creation.
     *    Every field is set once in the constructor and never modified.
     *    Example: String, Integer, LocalDate, UUID, BigDecimal.
     *
     * Q: Rules for creating a truly immutable class:
     *    1. Declare the class FINAL — prevents subclasses from adding mutability
     *    2. Make all fields PRIVATE and FINAL — set once, no setters
     *    3. No setter methods — no way to modify state after construction
     *    4. DEEP COPY mutable objects in the constructor (defensive copy)
     *       If a field is a mutable type (List, Date), copy it; don't store the reference
     *    5. DEEP COPY mutable objects in getters
     *       Return a copy, not the reference, so callers can't mutate your internal state
     *    6. If the class has a reference to a mutable object, do not let that
     *       reference escape (don't store it, return a copy)
     *
     * Q: Benefits of immutable classes?
     *    - Thread-safe by design (no synchronization needed)
     *    - Safe to use as Map keys and Set elements (hashCode never changes)
     *    - Easier to reason about (no hidden state changes)
     *    - Can be freely shared and cached
     *    - Failure atomicity: partially-constructed objects don't exist
     *
     * Q: What breaks immutability?
     *    - Storing a mutable reference and returning it directly (e.g., returning List field)
     *    - Having a non-final field (allows reassignment via reflection)
     *    - Subclassing (subclass can add mutable state)
     *    - Mutable objects reachable through the class (Date, arrays, collections)
     */
    static class ImmutabilityDemo {

        // ── BAD: Not truly immutable despite private field ─────────
        static class BadPoint {
            private final int[] coords; // final reference, but array is mutable!

            public BadPoint(int[] coords) {
                this.coords = coords;   // storing original — caller can mutate it!
            }

            public int[] getCoords() {
                return coords;          // returning reference — caller can mutate it!
            }
        }

        // ── GOOD: Truly immutable class ────────────────────────────

        /**
         * Truly immutable class following all six rules.
         * Rule 1: final class
         * Rule 2: private final fields
         * Rule 3: no setters
         * Rule 4: defensive copy in constructor
         * Rule 5: defensive copy in getter
         * Rule 6: no mutable reference escapes
         */
        static final class ImmutableEmployee {
            private final String name;
            private final double salary;
            private final List<String> skills; // mutable — must copy

            public ImmutableEmployee(String name, double salary, List<String> skills) {
                this.name   = name;
                this.salary = salary;
                // Rule 4: defensive copy in constructor
                // If caller mutates the original list, our internal state is unaffected
                this.skills = List.copyOf(skills); // Java 10+; creates unmodifiable copy
                // alternative: new ArrayList<>(skills) — modifiable copy
            }

            public String getName()   { return name; }   // String is immutable; safe to return directly
            public double getSalary() { return salary; } // primitive; safe

            public List<String> getSkills() {
                // Rule 5: defensive copy in getter
                // Caller gets a copy; modifying it doesn't affect our internal list
                return List.copyOf(skills); // returns unmodifiable view
                // alternative: return Collections.unmodifiableList(new ArrayList<>(skills));
            }

            // 'with' pattern — create modified copy instead of mutation
            public ImmutableEmployee withSalary(double newSalary) {
                return new ImmutableEmployee(this.name, newSalary, this.skills);
            }

            @Override public String toString() {
                return "ImmutableEmployee{name='" + name
                       + "', salary=" + salary + ", skills=" + skills + "}";
            }
        }

        static void run() {
            System.out.println("\n--- 7. Immutability ---");

            // BAD example: array reference escape
            int[] original = {1, 2, 3};
            BadPoint bad = new BadPoint(original);
            original[0] = 99;                 // mutates the internal state!
            System.out.println("  BadPoint coords[0]: " + bad.getCoords()[0]); // 99 — broken!
            bad.getCoords()[1] = 88;          // mutates via getter!
            System.out.println("  BadPoint coords[1] after getter mutation: " + bad.getCoords()[1]); // 88

            // GOOD example: truly immutable
            List<String> skills = new ArrayList<>(Arrays.asList("Java", "Spring"));
            ImmutableEmployee emp = new ImmutableEmployee("Alice", 85_000.0, skills);

            skills.add("Kubernetes");             // mutate the original list
            System.out.println("  ImmutableEmployee skills (constructor copy): " + emp.getSkills());
            // still ["Java", "Spring"] — defensive copy protects internal state

            try {
                emp.getSkills().add("Python");    // attempt to mutate returned list
            } catch (UnsupportedOperationException e) {
                System.out.println("  Cannot mutate returned unmodifiable list (good!)");
            }

            ImmutableEmployee promoted = emp.withSalary(95_000.0);
            System.out.println("  Original: " + emp);
            System.out.println("  Promoted: " + promoted);
        }
    }

    // ============================================================
    // ============================================================
    //  8.  COMPARABLE vs COMPARATOR
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Comparable vs Comparator:
     *
     * Q: What is Comparable?
     *    java.lang.Comparable<T> — interface with one method: int compareTo(T other)
     *    Defines the NATURAL ORDERING of the class.
     *    The class itself implements it.
     *    Used by: Collections.sort(), Arrays.sort(), TreeSet/TreeMap (natural order).
     *    Contract: compareTo must be consistent with equals.
     *
     * Q: What is Comparator?
     *    java.util.Comparator<T> — functional interface with: int compare(T o1, T o2)
     *    Defines an EXTERNAL / CUSTOM ordering — the class doesn't need to know about it.
     *    One class can have many different Comparators.
     *    Used by: Collections.sort(list, comp), Arrays.sort(arr, comp), TreeSet(comp).
     *
     * Q: When to use each?
     *    Comparable  — when there is ONE obvious natural ordering (String alphabetical,
     *                  Integer numeric). Implement in the class itself.
     *    Comparator  — when you need multiple orderings, or you can't modify the class
     *                  (third-party classes), or ordering is context-dependent.
     *
     * Q: compareTo / compare return value convention?
     *    < 0  → first object is LESS THAN second
     *    = 0  → objects are EQUAL
     *    > 0  → first object is GREATER THAN second
     *
     * Q: Comparator chaining (Java 8+)?
     *    Comparator.comparing(keyExtractor)
     *    .thenComparing(secondKeyExtractor)
     *    .reversed()
     *    .nullsFirst() / .nullsLast()
     */
    static class SortingDemo {

        /** Implements Comparable — natural order is by salary ascending */
        static class Employee implements Comparable<Employee> {
            String name;
            String department;
            double salary;
            int    age;

            Employee(String name, String department, double salary, int age) {
                this.name = name; this.department = department;
                this.salary = salary; this.age = age;
            }

            @Override
            public int compareTo(Employee other) {
                // Natural order: ascending salary
                return Double.compare(this.salary, other.salary);
            }

            @Override public String toString() {
                return name + "(" + department + ", $" + (int)salary + ", age=" + age + ")";
            }
        }

        static void run() {
            System.out.println("\n--- 8. Comparable vs Comparator ---");

            List<Employee> employees = new ArrayList<>(Arrays.asList(
                new Employee("Alice", "Engineering", 95_000, 30),
                new Employee("Bob",   "Marketing",   70_000, 25),
                new Employee("Carol", "Engineering", 110_000, 35),
                new Employee("Dave",  "HR",           60_000, 28),
                new Employee("Eve",   "Engineering",  85_000, 32)
            ));

            // Natural order (Comparable) — ascending salary
            Collections.sort(employees);
            System.out.println("  Natural order (salary asc): " + employees);

            // Comparator — descending salary
            employees.sort(Comparator.comparingDouble(Employee::getSalary).reversed());
            System.out.println("  Salary desc:  " + employees);

            // Comparator — by department, then name
            Comparator<Employee> deptThenName =
                Comparator.comparing(Employee::getDept)
                          .thenComparing(Employee::getName);
            employees.sort(deptThenName);
            System.out.println("  Dept+Name:    " + employees);

            // Comparator — by age, nulls last
            List<Employee> withNulls = new ArrayList<>(employees);
            Comparator<Employee> byAge =
                Comparator.nullsLast(Comparator.comparingInt(Employee::getAge));
            withNulls.sort(byAge);
            System.out.println("  By age (nulls last): " + withNulls);

            // TreeSet with custom Comparator — orders by name
            TreeSet<Employee> byName = new TreeSet<>(
                Comparator.comparing(Employee::getName));
            byName.addAll(employees);
            System.out.println("  TreeSet by name: " + byName);

            // Lambda comparator
            employees.sort((e1, e2) -> e1.name.compareTo(e2.name));
            System.out.println("  Lambda sort:  " + employees);

            // Reversed natural order
            employees.sort(Comparator.reverseOrder());  // uses compareTo
            System.out.println("  reverseOrder: " + employees);
        }
    }

    // helper accessor methods for Employee (SortingDemo) used in lambdas
    // defined on SortingDemo.Employee via methods below (workaround for inner class)
    // Note: Employee already has toString(); we add helper getters
    // Actually we need to add these to the Employee static inner class:
    // Since Java doesn't let us add methods to inner static class after the fact,
    // we use lambdas with field access directly in SortingDemo.

    // ============================================================
    // ============================================================
    //  9.  var KEYWORD (Java 10 — Local Variable Type Inference)
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — var:
     *
     * Q: What is var in Java 10?
     *    Local Variable Type Inference — the compiler infers the declared type from
     *    the initializer expression. It is NOT dynamic typing (like JavaScript).
     *    The type is determined at COMPILE TIME and is FIXED for the variable's lifetime.
     *
     * Q: Where can var be used?
     *    - Local variable declarations with an initializer
     *    - Index variables in for loops: for (var item : list)
     *    - Resource variables in try-with-resources: try (var is = new FileInputStream(...))
     *    - Lambda parameter (Java 11): (var x, var y) -> x + y
     *
     * Q: Where CANNOT var be used?
     *    - Field declarations (instance/static fields)
     *    - Method return types
     *    - Method parameters
     *    - Constructor parameters
     *    - var x;       — no initializer (type cannot be inferred)
     *    - var x = null; — null has no type
     *    - var x = () -> {} — lambda without a target type
     *    - Array creation: var a = {1, 2, 3}; — array initializer without explicit type
     *
     * Q: Is var dynamic typing?
     *    NO. Java remains STATICALLY typed. var just lets the compiler infer the type.
     *    After inference, the type is fixed. You cannot reassign a different type.
     *    var x = "hello";  x = 42; // COMPILE ERROR — x is String
     *
     * Q: When SHOULD you use var?
     *    - When the type is obvious from the right-hand side:
     *      var list = new ArrayList<String>(); // clear
     *    - When the type name is very long (generics, anonymous):
     *      var map = new HashMap<String, List<Map.Entry<Integer, String>>>();
     *    - In enhanced for loops over collections
     *
     * Q: When SHOULD NOT you use var?
     *    - When the type is not obvious: var result = service.process(); (what type?)
     *    - For primitive literals where the type matters: var x = 1; (is this int or long?)
     *    - When readability would suffer
     */
    static class VarKeywordDemo {

        static void run() {
            System.out.println("\n--- 9. var keyword (Java 10) ---");

            // ── Basic inference ────────────────────────────────────
            var greeting = "Hello, Java!";           // inferred as String
            var count    = 42;                        // inferred as int
            var pi       = 3.14159;                   // inferred as double
            var active   = true;                      // inferred as boolean
            System.out.println("  var types: " + greeting.getClass().getSimpleName()
                               + ", " + ((Integer)count).getClass().getSimpleName());

            // ── Collections ───────────────────────────────────────
            var names = new ArrayList<String>();      // inferred as ArrayList<String>
            names.add("Alice");
            names.add("Bob");
            var map = new HashMap<String, Integer>(); // inferred as HashMap<String, Integer>
            map.put("Alice", 1);

            // ── Enhanced for loop ──────────────────────────────────
            for (var name : names) {                  // var inferred as String
                System.out.println("  var in for: " + name.toUpperCase()); // String methods work
            }

            // ── try-with-resources ────────────────────────────────
            try (var sw = new java.io.StringWriter()) {
                sw.write("written via var");
                System.out.println("  try-with-resources var: " + sw);
            } catch (Exception e) { /* ignore */ }

            // ── var does NOT change static typing ─────────────────
            var x = "statically typed";
            // x = 42;  // COMPILE ERROR — x is String; type is fixed
            System.out.println("  var is still static: " + x.length());

            // ── Anonymous class captures type info ────────────────
            // var is useful here to avoid typing the long anonymous class name
            var formatter = new Object() {
                String format(double d) { return String.format("%.2f", d); }
            };
            // Note: can only call format() through this var — type is the anonymous class
            System.out.println("  Anonymous class via var: " + formatter.format(3.14159));

            // ── What var CANNOT do ─────────────────────────────────
            // var field = "hello";  // COMPILE ERROR — var not allowed for fields
            // public var method() { return ""; }  // COMPILE ERROR — not for return types
            // void m(var param) {}  // COMPILE ERROR — not for parameters
            // var noInit;           // COMPILE ERROR — needs initializer
            // var nullVar = null;   // COMPILE ERROR — null has no type
            System.out.println("  var restrictions: see comments in source");
        }
    }

    // ============================================================
    // ============================================================
    //  10.  RECORDS (Java 16 — Stable)
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Records:
     *
     * Q: What is a Record in Java 16?
     *    A concise way to declare an IMMUTABLE DATA CLASS.
     *    The compiler auto-generates:
     *      - private final fields for each component
     *      - Canonical constructor (all-args)
     *      - Accessor methods (component name, no 'get' prefix): name(), email()
     *      - equals() based on all components
     *      - hashCode() based on all components
     *      - toString() showing all components
     *    Declared with: record ClassName(ComponentType component, ...)
     *
     * Q: What can Records do?
     *    - Implement interfaces
     *    - Have static fields and methods
     *    - Have instance methods
     *    - Have compact constructors (for validation)
     *    - Declare additional constructors (must delegate to canonical)
     *    - Declare @Override for equals/hashCode/toString
     *
     * Q: What can Records NOT do?
     *    - Extend another class (records implicitly extend java.lang.Record)
     *    - Declare instance fields beyond the record components
     *    - Be abstract or non-final (records are implicitly final)
     *    - Have mutable state (fields are final)
     *
     * Q: What is a compact constructor?
     *    A constructor with no parameter list; automatically receives and sets
     *    the record components. Used for VALIDATION and NORMALISATION.
     *    Syntax: record R(Type x) { R { /* validate/transform x here * / } }
     *    The compiler assigns x to this.x after the compact constructor body.
     *
     * Q: Are Records truly immutable?
     *    Components themselves are final, but if a component is a mutable type
     *    (e.g., List), the internal object CAN be mutated via the reference.
     *    For true immutability: use List.copyOf() in the compact constructor.
     *
     * Q: 'with' pattern for Records?
     *    Records are immutable, so updates return a NEW record.
     *    Define a withXxx() method that copies all fields and replaces one.
     */
    record Point(double x, double y) {
        // Compact constructor — no parameter list; used for validation
        Point {
            if (Double.isNaN(x) || Double.isNaN(y))
                throw new IllegalArgumentException("Coordinates cannot be NaN");
        }

        // Static factory method
        static Point origin() { return new Point(0, 0); }

        // Instance methods
        double distanceTo(Point other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }

        // 'with' pattern — return new record with one field changed
        Point withX(double newX) { return new Point(newX, this.y); }
        Point withY(double newY) { return new Point(this.x, newY); }
    }

    interface Describable {
        String describe();
    }

    record Person(String name, int age) implements Describable {
        // Compact constructor — validate and normalise
        Person {
            if (name == null || name.isBlank())
                throw new IllegalArgumentException("Name cannot be blank");
            if (age < 0 || age > 150)
                throw new IllegalArgumentException("Invalid age: " + age);
            // Normalise: trim and capitalise
            name = name.trim();
        }

        // Implement interface method
        @Override
        public String describe() {
            return "Person " + name + " is " + age + " years old";
        }

        // Additional constructor (must delegate to canonical)
        Person(String name) { this(name, 0); }

        // Static field (allowed)
        static final int MAX_AGE = 150;
    }

    static class RecordsDemo {
        static void run() {
            System.out.println("\n--- 10. Records (Java 16) ---");

            Point p1 = new Point(3.0, 4.0);
            Point p2 = Point.origin();
            System.out.println("  p1: " + p1);              // auto toString
            System.out.println("  p2: " + p2);
            System.out.println("  p1.x(): " + p1.x());      // accessor (no 'get' prefix)
            System.out.println("  distance: " + p1.distanceTo(p2));
            System.out.println("  p1 == p1.withX(3.0): " + p1.equals(p1.withX(3.0))); // true — same values

            Point p3 = new Point(3.0, 4.0);
            System.out.println("  p1.equals(p3): " + p1.equals(p3)); // true — structural equality
            System.out.println("  p1 == p3:      " + (p1 == p3));     // false — different objects

            // hashCode consistent with equals
            System.out.println("  p1.hashCode() == p3.hashCode(): " + (p1.hashCode() == p3.hashCode()));

            Person alice = new Person("  alice  ", 30); // compact constructor trims
            System.out.println("  Person: " + alice);
            System.out.println("  describe: " + alice.describe());

            try {
                new Point(Double.NaN, 0); // compact constructor validation
            } catch (IllegalArgumentException e) {
                System.out.println("  Validation caught: " + e.getMessage());
            }

            // Records in collections
            Set<Point> pointSet = new HashSet<>(List.of(p1, p2, p3));
            System.out.println("  Distinct points in set: " + pointSet.size()); // 2 (p1==p3)
        }
    }

    // ============================================================
    // ============================================================
    //  11.  SEALED CLASSES (Java 17 — Stable)
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Sealed Classes:
     *
     * Q: What is a sealed class?
     *    A class or interface that RESTRICTS which classes can extend / implement it.
     *    Declared with the 'sealed' modifier + 'permits' clause listing allowed subclasses.
     *    Available as a stable feature in Java 17.
     *
     * Q: Why use sealed classes?
     *    - Exhaustive modelling: the compiler knows ALL possible subtypes.
     *    - Pattern matching switch can be EXHAUSTIVE without a default branch.
     *    - Closes the class hierarchy intentionally — replaces enums for richer type hierarchies.
     *    - Better expresses domain models: a Shape is ONLY Circle, Rectangle, or Triangle.
     *
     * Q: What modifiers can a permitted subclass have?
     *    FINAL    — cannot be further subclassed (leaf of the hierarchy)
     *    SEALED   — can itself restrict its subclasses
     *    NON-SEALED — open for any extension (opt-out from sealing)
     *    Every permitted class must directly extend/implement the sealed type.
     *
     * Q: Sealed classes and records?
     *    Records are implicitly final, so they can be permitted subclasses of sealed classes.
     *
     * Q: Sealed interfaces?
     *    Interfaces can also be sealed: sealed interface Expr permits Num, Add, Mul {}
     */

    // Sealed hierarchy: Shape can only be Circle, Rectangle, or Triangle
    sealed interface Shape permits AdvancedJavaConcepts.Circle,
                                   AdvancedJavaConcepts.Rectangle,
                                   AdvancedJavaConcepts.Triangle {
        double area();
        double perimeter();
    }

    // Records implementing a sealed interface (records are implicitly final)
    record Circle(double radius) implements Shape {
        Circle {
            if (radius <= 0) throw new IllegalArgumentException("Radius must be positive");
        }
        @Override public double area()      { return Math.PI * radius * radius; }
        @Override public double perimeter() { return 2 * Math.PI * radius; }
    }

    record Rectangle(double width, double height) implements Shape {
        @Override public double area()      { return width * height; }
        @Override public double perimeter() { return 2 * (width + height); }
    }

    // final class — not a record, but explicitly final
    static final class Triangle implements Shape {
        private final double a, b, c; // sides
        Triangle(double a, double b, double c) { this.a = a; this.b = b; this.c = c; }
        @Override public double area() {
            double s = (a + b + c) / 2;
            return Math.sqrt(s * (s-a) * (s-b) * (s-c));
        }
        @Override public double perimeter() { return a + b + c; }
        @Override public String toString()  { return "Triangle("+a+","+b+","+c+")"; }
    }

    static class SealedClassesDemo {

        /**
         * With a sealed hierarchy, the switch can be EXHAUSTIVE — no default needed.
         * The compiler verifies all permitted types are covered.
         * This is one of the primary motivations for sealed classes.
         */
        static String describeShape(Shape shape) {
            return switch (shape) {                           // switch EXPRESSION
                case Circle c     -> String.format("Circle with radius %.1f, area=%.2f", c.radius(), c.area());
                case Rectangle r  -> String.format("Rectangle %.1fx%.1f, area=%.2f", r.width(), r.height(), r.area());
                case Triangle t   -> String.format("Triangle, area=%.2f, perimeter=%.2f", t.area(), t.perimeter());
                // NO default needed — compiler knows all subtypes because Shape is sealed
            };
        }

        static void run() {
            System.out.println("\n--- 11. Sealed Classes (Java 17) ---");

            List<Shape> shapes = List.of(
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 4, 5)
            );

            for (Shape s : shapes) {
                System.out.println("  " + describeShape(s));
            }

            // instanceof works normally
            System.out.println("  shapes.get(0) instanceof Circle: "
                + (shapes.get(0) instanceof Circle));
        }
    }

    // ============================================================
    // ============================================================
    //  12.  PATTERN MATCHING
    // ============================================================
    // ============================================================

    /**
     * Interview Questions — Pattern Matching:
     *
     * Q: What is pattern matching for instanceof (Java 16)?
     *    An enhancement to instanceof that:
     *    1. Checks if the object is an instance of the type
     *    2. If yes, automatically binds it to a PATTERN VARIABLE — no cast needed
     *    Syntax: if (obj instanceof String s) { use s here }
     *    The binding variable s is in scope only where the condition is true.
     *    Eliminates the verbose cast: if (obj instanceof String) { String s = (String) obj; }
     *
     * Q: What is pattern matching in switch (Java 21 — stable)?
     *    switch can now match patterns, not just constants.
     *    case String s  — matches if object is a String; binds to s
     *    case Integer i when i > 0 — matches Integer AND adds a GUARD condition
     *    case null      — explicitly handle null (without NPE)
     *    Exhaustiveness: when all types are covered, no default needed.
     *
     * Q: What is a guarded pattern?
     *    case Type t when condition — extra boolean condition after the type pattern.
     *    Example: case Employee e when e.getSalary() > 100_000
     *
     * Q: What is a switch expression (Java 14)?
     *    An enhanced switch that can return a VALUE.
     *    Uses -> syntax (arrow case) — no fall-through, no break needed.
     *    Uses yield to return a value from a block case.
     *    Can be used as an expression: String result = switch(day) { ... }
     */
    static class PatternMatchingDemo {

        // ── instanceof pattern variable ────────────────────────────

        static String describe(Object obj) {
            // Old Java (before 16):
            //   if (obj instanceof String) { String s = (String) obj; return "String of length " + s.length(); }
            //
            // Java 16+ pattern matching — binding variable s automatically cast and bound:
            if (obj instanceof String s) {
                return "String of length " + s.length() + ": '" + s.toUpperCase() + "'";
            } else if (obj instanceof Integer i && i > 0) {
                // '&&' guard: binding variable i is in scope AND i > 0
                return "Positive integer: " + i;
            } else if (obj instanceof Integer i) {
                return "Non-positive integer: " + i;
            } else if (obj instanceof List<?> list && !list.isEmpty()) {
                return "Non-empty list of size " + list.size();
            } else if (obj == null) {
                return "null value";
            } else {
                return "Other: " + obj.getClass().getSimpleName();
            }
        }

        // ── Switch expression (Java 14) ────────────────────────────

        static String dayType(String day) {
            return switch (day.toUpperCase()) {       // switch EXPRESSION — returns a value
                case "MONDAY", "TUESDAY",
                     "WEDNESDAY", "THURSDAY",
                     "FRIDAY"    -> "Weekday";        // arrow case: no fall-through, no break
                case "SATURDAY",
                     "SUNDAY"   -> "Weekend";
                default          -> "Unknown";
            };
        }

        static int daysInMonth(int month, int year) {
            return switch (month) {
                case 1, 3, 5, 7, 8, 10, 12 -> 31;
                case 4, 6, 9, 11            -> 30;
                case 2 -> {                           // block case needs 'yield'
                    boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
                    yield leap ? 29 : 28;             // yield returns value from block
                }
                default -> throw new IllegalArgumentException("Invalid month: " + month);
            };
        }

        // ── Pattern matching switch with sealed hierarchy (Java 21) ─

        static double computeArea(Shape shape) {
            return switch (shape) {
                case Circle c    -> c.area();
                case Rectangle r -> r.area();
                case Triangle t  -> t.area();
                // exhaustive — no default needed because Shape is sealed
            };
        }

        // ── Guarded patterns ──────────────────────────────────────

        static String classifyNumber(Object obj) {
            return switch (obj) {
                case Integer i when i < 0   -> "Negative integer: " + i;
                case Integer i when i == 0  -> "Zero";
                case Integer i              -> "Positive integer: " + i;
                case Double d when d < 0    -> "Negative double: " + d;
                case Double d               -> "Positive/zero double: " + d;
                case String s               -> "String: " + s;
                case null                   -> "null";
                default                     -> "Other: " + obj;
            };
        }

        static void run() {
            System.out.println("\n--- 12. Pattern Matching ---");

            // instanceof pattern variable
            System.out.println("  describe(\"hello\"):  " + describe("hello"));
            System.out.println("  describe(42):       " + describe(42));
            System.out.println("  describe(-5):       " + describe(-5));
            System.out.println("  describe(List.of(1,2)): " + describe(List.of(1, 2)));
            System.out.println("  describe(null):     " + describe(null));
            System.out.println("  describe(3.14):     " + describe(3.14));

            // Switch expression
            System.out.println("  Monday:   " + dayType("Monday"));
            System.out.println("  Saturday: " + dayType("Saturday"));

            System.out.println("  Days in Feb 2024 (leap): " + daysInMonth(2, 2024));
            System.out.println("  Days in Feb 2023:        " + daysInMonth(2, 2023));
            System.out.println("  Days in April:           " + daysInMonth(4, 2024));

            // Pattern switch with sealed class
            System.out.println("  Circle(5) area: " + computeArea(new Circle(5)));
            System.out.println("  Rect(4,6) area: " + computeArea(new Rectangle(4, 6)));

            // Guarded patterns
            System.out.println("  classifyNumber(-3):   " + classifyNumber(-3));
            System.out.println("  classifyNumber(0):    " + classifyNumber(0));
            System.out.println("  classifyNumber(7):    " + classifyNumber(7));
            System.out.println("  classifyNumber(3.14): " + classifyNumber(3.14));
            System.out.println("  classifyNumber(null): " + classifyNumber(null));
        }
    }
}

// ============================================================
// Helper methods needed by SortingDemo.Employee
// (cannot add methods to a static inner class from outside it,
// so these are defined as methods on the Employee inner class directly above)
// ============================================================
// Note: SortingDemo.Employee uses direct field access in lambdas
// (e.getName(), e.getDept(), e.getSalary(), e.getAge()).
// These are added as inner methods on the Employee static class:
// ─ they are defined inline with the class above for clean compilation.
