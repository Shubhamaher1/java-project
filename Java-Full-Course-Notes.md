# Java Full Course — Complete Topic Reference
Source: [Java-Full-Course by adityatandon15](https://github.com/adityatandon15/Java-Full-Course) (Coder Army YouTube playlist), 57 lectures.
Every concept below has a **Definition** followed by a **code snippet**.

---

## 1. Java Fundamentals & JVM Architecture (Lectures 1–2)

**Why Java — Definition:** Java was created after C/C++ to solve three problems: **platform dependence** (C/C++ compiles straight to machine code for one CPU+OS), **complexity** (manual pointers, multiple inheritance), and **security**. Java compiles to **bytecode**, an intermediate, platform-independent format that any JVM can run.

**Compiler vs Interpreter — Definition:** A **compiler** converts the entire source to machine code at once (fast execution, but platform-specific output — used by C/C++). An **interpreter** converts and executes code line by line (slower, but portable — used by Python). **Java uses both**: `javac` compiles `.java` → bytecode (`.class`); the JVM then *interprets* bytecode, and a **JIT (Just-In-Time) compiler** compiles "hot" (frequently executed) bytecode paths into native machine code for speed.
```java
// javac Demo.java   -> produces Demo.class (bytecode, NOT machine code)
// java Demo          -> JVM interprets bytecode, JIT-compiles hot paths
```

**JVM / JRE / JDK — Definition:**
- **JVM (Java Virtual Machine)**: executes bytecode (interpreter + JIT), platform-specific implementation, provides the sandboxed, garbage-collected runtime.
- **JRE (Java Runtime Environment)**: JVM + standard class libraries (needed to *run* Java apps).
- **JDK (Java Development Kit)**: JRE + compiler (`javac`), debugger, `javadoc` (needed to *develop* Java apps).
```
JDK
 └── JRE
      └── JVM + class libraries
```

**WORA (Write Once, Run Anywhere) — Definition:** Because bytecode is not tied to any CPU architecture, the same `.class` file runs unmodified on any machine that has a compatible JVM — only the JVM implementation itself is platform-specific.
```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, World!");   // this .class runs on Windows/Linux/Mac identically
    }
}
```

**JDK editions — Definition:** `JSE` (Java Standard Edition) = core Java; `JEE`/Jakarta EE = enterprise/transactional APIs (servlets, etc.); `JME` (Java Micro Edition) = lightweight, historically used for early Android/embedded devices.

---

## 2. Variables, Data Types, Type Conversion (Lecture 3–5)

**Primitive Data Types — Definition:** Java has 8 primitive types, stored directly (by value) on the stack, not as objects.
| Type | Size | Example |
|---|---|---|
| byte | 1 byte | `byte b = 0x0A;` |
| short | 2 bytes | `short s = 10;` |
| int | 4 bytes | `int i = 4000;` |
| long | 8 bytes | `long l = 341256789L;` |
| float | 4 bytes | `float f = 10.5f;` |
| double | 8 bytes | `double d = 6.022e23;` |
| char | 2 bytes | `char c = 'a';` |
| boolean | JVM-impl defined (~1 byte) | `boolean b = false;` |
```java
public class Demo {
    public static void main(String[] args) {
        byte b = 0xA;              // hexadecimal literal
        short s = 10;
        int i = 4000;
        long l = 341256789L;
        float f = 10.54f;          // single precision
        double d = 6.022e23;       // double precision
        char c = 'a';              // stored internally as an integer (Unicode code point)
        boolean bool = false;
        System.out.println(b + " " + s + " " + i + " " + l + " " + f + " " + d + " " + c + " " + bool);
    }
}
```

**Implicit (Widening) Conversion — Definition:** Automatic conversion from a smaller type to a larger, compatible type — no data loss, no cast needed.
```java
byte b = 24;
int i = b;         // implicit: byte -> int
char ch = 'a';
int code = ch;      // implicit: char -> int (97)
```

**Explicit (Narrowing) Conversion / Casting — Definition:** Converting a larger type to a smaller one requires an explicit `(type)` cast and can lose data (values wrap around modulo the target type's range).
```java
int i = 300;
byte b = (byte) i;      // 300 % 256 = 44 (overflow wraps around)
```

**Truncating Conversion — Definition:** Converting a floating-point type to an integer type via cast drops the fractional part (no rounding).
```java
float f = 15.678f;
int truncated = (int) f;  // 15, NOT 16
```
Note: `boolean` cannot be converted to or from any other type in Java.

**Expression Type Promotion — Definition:** In a mixed-type arithmetic expression, Java automatically promotes all operands to the "widest" type present, following: `byte/short/char → int → long → float → double`.
```java
byte b = 42; char c = 'a'; int i = 50000; float f = 5.67f; double d = .1234;
double result = (f * b) + (i / c) - (d * s); // f*b -> float, i/c -> int, d*s -> double, whole expr -> double
```

---

## 3. Operators (Lecture 6)

**Arithmetic Operators — Definition:** `+ - * / % += -= *= /= %= ++ --` perform basic math and compound assignment/increment.
```java
int a = 5, b = 10;
int sum = a + b, diff = a - b, quotient = b / a, mod = b % a;
int j = 7;
j++;          // postfix: use current value, then increment
++j;          // prefix: increment, then use new value
int k = j++;  // k gets the OLD value of j; j increments afterward
```

**Relational Operators — Definition:** `== != < > <= >=` compare two values and produce a `boolean`.
```java
boolean equal = (10 == 10); // true
```

**Bitwise & Shift Operators — Definition:** `& | ^ ~` operate on individual bits; `<< >> >>>` shift bits left/right (`>>>` is the unsigned right shift, always fills with 0).
```java
int a = 2, b = 3;              // 10, 11 in binary
int and = a & b;               // 2  (10)
int or  = a | b;               // 3  (11)
int xor = a ^ b;                // 1  (01)
int not = ~a;                  // -3 (two's complement flips all bits)
int leftShift  = 1 << 3;       // 8
int rightShift = 8 >> 2;       // 2 (sign-preserving)
int unsignedRightShift = -8 >>> 28; // fills with 0 regardless of sign
```

**Logical Operators — Definition:** `&& || !` combine boolean expressions; `&&`/`||` are **short-circuiting** — the right operand is skipped if the result is already determined by the left.
```java
boolean r = (a < b) && (b < c); // right side not evaluated if (a < b) is false
```

---

## 4. Control Flow (Lecture 7–8)

**Selection Statements (if / if-else / nested-if / if-else-if ladder) — Definition:** Execute a block conditionally based on a boolean expression.
```java
int age = 50;
if (age > 80) { System.out.println("very old"); }
else if (age > 40) { System.out.println("becoming old"); }
else { System.out.println("young"); }
```

**switch Statement — Definition:** Selects one of many code blocks to execute based on the value of an expression that must evaluate to `byte, short, int, char, String (JDK7+), or an enum`. No duplicate `case` labels are allowed; `break` prevents fall-through to the next case.
```java
int i = 1;
switch (i) {
    case 1: System.out.println("one"); break;
    case 2: System.out.println("two"); break;
    default: System.out.println("other"); break;
}
```

**Loops (for / while / do-while) — Definition:** Repeat a block of code. `for` is best for known iteration counts; `while` checks the condition *before* each run; `do-while` checks *after*, guaranteeing at least one execution.
```java
for (int i = 1; i <= 10; i++) { System.out.println(i); }

int i = 0;
while (i < 10) { System.out.println(i); i++; }        // condition checked first

int j = 0;
do { System.out.println(j); j++; } while (j < 10);     // body runs at least once
```

**Jump Statements (break / continue / labeled loops) — Definition:** `break` exits a loop entirely; `continue` skips to the next iteration; a **label** lets `break`/`continue` target an outer loop from inside a nested one.
```java
outer:
for (int i = 1; i <= 10; i++) {
    for (int j = 1; j <= i; j++) {
        if (j >= 5) break outer;   // breaks the OUTER loop, not just the inner one
        System.out.print("* ");
    }
}
```

---

## 5. Arrays & Strings Basics (Lecture 9–10)

**1-D Array — Definition:** A fixed-size, indexed collection of same-type elements. Arrays are a **reference type** — the variable holds a pointer to heap memory, not the data itself.
```java
int[] rollNums = new int[3];      // default values: 0
rollNums[0] = 101;
int[] literal = {101, 102, 103};  // array literal shorthand
System.out.println(rollNums.length); // a PROPERTY, not a method (no parentheses)
```

**Array Memory Model — Definition:** The array reference lives on the stack; the actual elements are stored contiguously on the heap. Element access is computed directly by address arithmetic, which is why it's **O(1)**.
```
int[] arr = new int[5];
Stack:  arr ---> (reference, points to heap)
Heap:   [10][20][30][40][50]   <- contiguous block

arr[i] = baseAddress + (dataTypeSize * i)   // e.g. int (4 bytes): arr[3] = base + 4*3
```
Out-of-bounds access throws `ArrayIndexOutOfBoundsException`.

**2-D Arrays / Array of Arrays (Jagged Arrays) — Definition:** A 2-D array in Java is really an array of array-references — each row can be independently sized ("jagged").
```java
int[][] marks = new int[3][4];        // rectangular: 3 rows x 4 cols, contiguous per row
int[][] jagged = new int[3][];        // each row can have a DIFFERENT length
jagged[0] = new int[1];
jagged[1] = new int[2];
// arr[i][j] -> arr[i] is a reference to a 1-D array on the heap; arr[i][j] indexes into THAT array
```

**Strings intro — Definition:** A `String` is a sequence of characters; internally it wraps a `char[]`. String literals are cached in the **String Pool** (details in §17).
```java
String firstName = "Aditya";
String lastName = "Tandon";
String fullName = firstName + " " + lastName; // concatenation -> "Aditya Tandon"
char[] name = "ADITYA".toCharArray();          // conversion to underlying char array
```

---

## 6. Functions / Methods (Lecture 11)

**Method — Definition:** A named, reusable block of code. The 4 categories are based on whether it accepts input (parameters) and/or produces output (return value).
```java
public class Demo {
    public static void main(String[] args) {
        greet();                       // no input, no output
        sayHello("Rohit");             // input, no output
        System.out.println(getNumber());       // no input, output
        System.out.println(multiply(2, 4));    // input, output
    }
    static void greet() { System.out.println("Hello"); }
    static void sayHello(String name) { System.out.println("Hello " + name); }
    static int getNumber() { return 10; }
    static int multiply(int a, int b) { return a * b; }
}
```

**Method Overloading — Definition:** Defining multiple methods with the same name but a different parameter list (count, type, or order). Resolved at **compile time** based on the arguments passed.
```java
static int sum(int a, int b) { return a + b; }
static int sum(int a, int b, int c) { return a + b + c; }   // different parameter COUNT
static int sum(double a, double b) { return (int)(a + b); } // different parameter TYPE
static void greet(String name, int age) { }
static void greet(int age, String name) { }                  // different parameter ORDER
```

**Function Chaining — Definition:** One method calling another, which calls another, forming a call chain (visible on the call stack).
```java
static void fun1() { fun2(); System.out.println("Hi"); }
static void fun2() { fun3(); System.out.println("Hello"); }
static void fun3() { System.out.println("How are you"); }
```

**Variable Scope — Definition:** The region of code where a variable is visible/accessible. Class-level (`static`) fields are visible everywhere in the class; local variables exist only within their enclosing block.
```java
static String name = "Aditya"; // class/global scope — visible everywhere in the class
public static void main(String[] args) {
    int x = 4;   // local scope — only visible inside main()
    if (x == 4) { int j = 7; } // j only visible inside this if-block
}
```

---

## 7. OOP: Classes, Objects, Constructors (Lecture 12–13)

**Class & Object — Definition:** A **class** is a blueprint describing fields (data) and methods (behavior). An **object** is a runtime instance of a class, created with `new` and living on the heap.
```java
class Student {
    String name; int age; int rollNumber; String college; // instance variables
    void markAttendance() { System.out.println(name + " marked"); } // instance method
}
public class Demo {
    public static void main(String[] args) {
        Student s1 = new Student();
        s1.name = "Aditya"; s1.age = 28;
        s1.markAttendance();
    }
}
```
Default field values without a constructor: numeric → `0`, boolean → `false`, object references → `null`. Local variables have **no** default value and must be initialized before use.

**Constructor — Definition:** A special method that runs when `new` is called, sharing the class's name and having no return type — used to initialize an object's state. If none is defined, Java supplies a no-arg **default constructor**.
```java
class Student {
    String name; int age;
    Student() { }                                  // default (no-arg) constructor
    Student(String n, int a) { name = n; age = a; } // parameterized constructor
}
```

**Constructor Chaining (`this(...)`) — Definition:** One constructor invoking another constructor of the same class to avoid duplicating initialization logic. `this(...)` must be the first statement.
```java
class Student {
    String name; int age;
    Student() { this("Unknown"); }               // calls the 1-arg constructor below
    Student(String name) { this(name, 0); }
    Student(String name, int age) { this.name = name; this.age = age; }
}
```

---

## 8. Call by Value, static, final (Lecture 14–15)

**Call by Value — Definition:** Java always passes a *copy* of the value to a method. For primitives, that's a copy of the number. For objects, it's a copy of the **reference** — the callee can mutate the referenced object's fields, but reassigning the parameter itself never affects the caller's variable. Java has **no** call-by-reference.
```java
static void addTen(int x) { x = x + 10; }     // caller's original int is UNCHANGED

class Box { int x; }
static void mutate(Box b) { b.x = b.x + 10; } // caller's object IS mutated (same heap object, shared via copied reference)
```

**`static` keyword — Definition:** Marks a member as belonging to the **class itself**, not to individual objects — a single shared copy in memory across all instances.
```java
class Student {
    static String college = "IIT G";  // shared by ALL Student objects
    static int grade;
    static { grade = 8; }             // static initializer block — runs once, at class load time
}
System.out.println(Student.college);  // accessed via class name, not an instance
```

**`final` keyword — Definition:** Prevents modification. On a **variable**: value can be assigned only once (constant). On a **method**: cannot be overridden by subclasses. On a **class**: cannot be extended/subclassed.
```java
final int x;
x = 4;              // OK — assigned exactly once
// x = 5;            // compile error: cannot reassign a final variable

class Random {
    static final double PI;
    static { PI = 3.14; }  // a "blank final" can be set once, in a declaration, constructor, or static block
}
```

**`String[] args` — Definition:** Command-line arguments passed to `main()` when the program is launched, available as a `String` array.
```java
public static void main(String[] args) {
    System.out.println("Args: " + args.length);
    for (String a : args) System.out.println(a);
}
// run:  java Demo input.txt output.txt   -> args = {"input.txt", "output.txt"}
```

---

## 9. Encapsulation, Packages, Inheritance (Lecture 16)

**Encapsulation — Definition:** Bundling data (`private` fields) with behavior (`public` getters/setters) to hide internal state and control access/validation from outside the class.
```java
class BankAccount {
    private double balance;
    public void deposit(int amount) { balance += amount; }
    public void withdraw(int amount) { balance -= amount; } // could validate amount <= balance here
    public double getBalance() { return balance; }
}
```

**Packages — Definition:** A namespace mechanism used to organize related classes and avoid naming collisions between classes with the same simple name.
```java
// file: college/Student.java
package college;
public class Student { public void print() { System.out.println("College Student"); } }
```
```java
import college.Student;   // single-class import, or: import college.*;
college.Student s1 = new college.Student(); // fully-qualified name if two same-named classes collide
```

**Inheritance (`extends`) — Definition:** A mechanism where a subclass acquires the fields/methods of a superclass, modeling an **is-a** relationship.
```java
class Student { String name; void markAttendance() { /* ... */ } }
class EngineeringStudent extends Student { void attendLab() { /* ... */ } }
```
**Types of inheritance:** simple (A→B), multi-level (A→B→C), hierarchical (A→B, A→C). **Multiple inheritance of classes is NOT supported** in Java (avoids the diamond problem) — achieved instead through interfaces (§11.5).

**`super` keyword — Definition:** Refers to the immediate parent class — used to call the parent's constructor or an overridden parent method.
```java
class EngineeringStudent extends Student {
    String college;
    EngineeringStudent(String name, int age, int rollNo, String college) {
        super(name, age, rollNo);   // must be the FIRST statement in the constructor
        this.college = college;
    }
    void print() { super.print(); System.out.println(college); } // extend, don't just replace, parent behavior
}
```

---

## 10. Wrapper Classes: Autoboxing, Unboxing & Integer Caching (Lecture 18)

**Wrapper classes — Definition:** Every primitive type has a corresponding object "wrapper" class (`int`→`Integer`, `char`→`Character`, `boolean`→`Boolean`, etc.) so primitives can be used where an `Object` is required (e.g., inside generic collections like `List<Integer>`).

**Autoboxing / Unboxing — Definition:** **Autoboxing** is the compiler automatically wrapping a primitive into its wrapper object; **unboxing** is the reverse (wrapper → primitive). This happens implicitly during assignments, method calls, and arithmetic operations.
```java
int x = 10;
Integer y = x;      // autoboxing: int -> Integer
int z = y;            // unboxing: Integer -> int

Integer a = 10, b = 20;
int sum = a.intValue() + b.intValue(); // equivalent explicit unboxing
```
**Danger — unboxing `null`:**
```java
Integer x = null;
int y = x;   // throws NullPointerException at runtime — unboxing null has no primitive equivalent
```

**Integer Caching — Definition:** The JVM caches (reuses) `Integer` wrapper objects for values in the range **-128 to 127** (per the official spec — implementations like HotSpot follow this). Autoboxed values inside this range share the same object (`==` is true); outside it, each autobox typically creates a new object (`==` is unreliable) — always use `.equals()` to compare wrapper values.
```java
Integer a = 100, b = 100;
System.out.println(a == b);        // true — both come from the cached pool (-128..127)

Integer c = 200, d = 200;
System.out.println(c == d);        // false (or unreliable) — outside the cached range, different objects
System.out.println(c.equals(d));   // true — ALWAYS use equals() for wrapper value comparison
```

---

## 11. Abstraction, Polymorphism, Interfaces (Lecture 17–18, 24)

**Method Overriding / Runtime Polymorphism — Definition:** A subclass provides a specific implementation of a method already defined in its parent — resolved at **runtime** (dynamic dispatch) based on the actual object type, not the reference type.
```java
class Animal { void makeSound() { System.out.println("..."); } }
class Dog extends Animal {
    @Override void makeSound() { System.out.println("Bark"); }
}
Animal a = new Dog();
a.makeSound(); // "Bark" — decided at runtime by the ACTUAL object type, not the declared type
```
Rules: `static`, `private`, and `final` methods **cannot** be overridden (no runtime polymorphism for them — they're resolved statically). Fields are **not** polymorphic (resolved by declared/reference type, not object type).

**Abstract Classes — Definition:** A class that cannot be instantiated directly; it may mix abstract methods (no body — must be implemented by subclasses) with concrete methods, and is meant to be extended.
```java
abstract class Car {
    void start() { System.out.println("Car started"); } // concrete method
    abstract void accelerate();                          // abstract — subclass MUST implement
}
class FuelCar extends Car {
    @Override void accelerate() { System.out.println("accelerating"); }
}
```
Abstract classes **can** have constructors, static members, and `final`/`private` (non-abstract) methods, and may even declare zero abstract methods. They **cannot** be `final` (that would contradict "meant to be extended").

**Interfaces — Definition:** A pure contract of behavior that a class agrees to implement (originally 100% abstract methods).
```java
interface Car {
    void start();
    void accelerate();
}
class FuelCar implements Car {
    @Override public void start() { System.out.println("started"); }
    @Override public void accelerate() { System.out.println("accelerating"); }
}
```

**Interface variables, default & static methods, private methods — Definition:** Interface fields are implicitly `public static final` (constants). Since Java 8, interfaces can have `default` methods (with a body, overridable) and `static` methods (called via `Interface.method()`). Since Java 9, `private` methods are allowed as helpers for default methods.
```java
interface Vehicle {
    double PI_VALUE = 3.14;               // implicitly public static final
    default void drive() { System.out.println("driving"); accelerate(); } // has a default body
    static void brake() { System.out.println("braking"); }                // Vehicle.brake()
    private void accelerate() { System.out.println("accelerating"); }     // helper for drive()
}
```

**Multiple Inheritance via Interfaces & the Diamond Problem — Definition:** A class can `implement` multiple interfaces (achieving a form of multiple inheritance), but if two interfaces provide conflicting `default` methods, the implementing class **must** override the method to resolve the ambiguity.
```java
interface A { void fun(); }
interface B extends A { default void fun() { System.out.println("B"); } }
interface C extends A { default void fun() { System.out.println("C"); } }
class D implements B, C {
    @Override public void fun() { System.out.println("D"); } // MUST override — compile error otherwise
}
```
**Resolution priority rule:** a concrete class method always wins over an interface default method, even if inherited from a superclass higher up.
```java
class B { public void fun() { System.out.println("Inside B class"); } }
interface A { default void fun() { System.out.println("Inside A interface"); } }
class C extends B implements A { } // fun() resolves to B's version — class wins over interface default
```

**Abstract class vs Interface — Definition:** Abstract class = shared state + partial implementation, models an "is-a" relationship with common code to reuse. Interface = a pure capability contract, supports being implemented by unrelated classes ("can-do"), and supports multiple implementation.

---

## 12. Nested / Inner / Local / Anonymous Classes (Lecture 19)

**Static Nested Class — Definition:** A class declared `static` inside another class; it does not need an outer instance to exist and cannot directly access the outer class's instance (non-static) members.
```java
class Outer {
    private static int x = 4;
    static class Inner {
        void fun() { System.out.println(x); } // can access outer's STATIC members only
    }
}
Outer.Inner inner = new Outer.Inner();  // no outer instance required
```
Use cases: helper classes, the Builder design pattern, request/response DTOs.

**Inner (Non-Static) Class — Definition:** A class tied to a specific outer instance; it can access the outer instance's members (including non-static ones).
```java
class Outer {
    int x = 10;
    class Inner { void fun() { System.out.println(x); } }
}
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner(); // requires an existing outer instance
```

**Local Class — Definition:** A class defined inside a method body; it can capture local variables from the enclosing method as long as they are **effectively final** (never reassigned after initialization).
```java
void greet() {
    int y = 5;
    class Local { void sayHello() { System.out.println(y); } } // captures y
    new Local().sayHello();
}
```

**Anonymous Class — Definition:** A one-off, unnamed class that both declares and instantiates a subclass or interface implementation inline, typically for a single use.
```java
Person p2 = new Person() {
    @Override void introduce() { System.out.println("Hi, I am anonymous"); }
};
```

---

## 13. Basic I/O (Lecture 20)

**`System.out` / `System.err` — Definition:** `System.out` is the standard output stream (a `PrintStream`); `System.err` is the standard error stream — kept separate so error messages can be redirected independently of normal output.
```java
System.out.println("Hello");   // normal output
System.err.println("Error!");  // error output
```

**Reading raw input — Definition:** `System.in` is a raw `InputStream` of bytes; wrapping it lets you read characters or parsed values conveniently.
```java
int x = System.in.read();          // reads ONE raw byte
System.out.println((char) x);
```

**`BufferedReader` — Definition:** An efficient character-stream reader that buffers input and supports reading a full line at once.
```java
// System.in (bytes) -> InputStreamReader (bytes -> chars) -> BufferedReader.readLine() (chars -> String)
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
String name = br.readLine();
```

**`Scanner` — Definition:** A convenience class that parses primitive types and strings from an input stream (like `System.in`) using configurable delimiters.
```java
Scanner sc = new Scanner(System.in);
int age = sc.nextInt();
String line = sc.nextLine();
```

---

## 14. Immutability (Lecture 21)

**Immutable Object — Definition:** An object whose state cannot change after construction. Achieved with a `final class`, `private final` fields, no setters, and **defensive copies** for any mutable field types (so external code can't reach in and mutate the "immutable" object through a shared reference).
```java
final class Student {
    private final int age;
    private final String name;
    private final College college;         // College itself is mutable!

    Student(int age, String name, College college) {
        this.age = age; this.name = name;
        this.college = new College(college.name, college.address); // defensive copy IN
    }
    public College getCollege() {
        return new College(college.name, college.address);          // defensive copy OUT
    }
}
```
Without defensive copies on both sides, a caller holding a reference to the mutable `College` object could change it after construction, silently breaking the "immutable" `Student`'s state.

---

## 15. Object Class Methods (Lecture 22)

**`Object` base class — Definition:** Every class implicitly extends `Object`, which supplies default `toString()`, `equals()`, `hashCode()`, `clone()`, and more — usually overridden for meaningful class-specific behavior.
```java
class Student implements Cloneable {
    String name; int age;

    @Override public String toString() { return name + ", " + age; } // human-readable representation

    @Override public boolean equals(Object obj) {   // value-based equality instead of reference equality
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Student s = (Student) obj;
        return this.age == s.age && Objects.equals(this.name, s.name);
    }

    @Override public int hashCode() { return Objects.hash(name, age); } // MUST be consistent with equals()

    @Override protected Object clone() throws CloneNotSupportedException { return super.clone(); } // shallow copy
}
```

**`instanceof` operator — Definition:** Checks whether an object is an instance of a given class or any of its subclasses, returning a `boolean`.
```java
Animal d = new Dog();
System.out.println(d instanceof Animal);  // true
```

**equals/hashCode contract — Definition:** If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` **must** also be true — this is required for correct behavior in hash-based collections (`HashMap`, `HashSet`).

---

## 16. Enums (Lecture 23)

**Enum — Definition:** A type-safe, fixed set of named constants — solves the type-safety and readability problems of using raw `int`/`String` constants (no compile-time checking, no grouping).
```java
enum PaymentStatus { SUCCESS, FAILED, PENDING; }
PaymentStatus status = PaymentStatus.FAILED;
System.out.println(status.name());
```

**Enum with fields & constructor — Definition:** Enums can carry data and behavior just like a class — each constant is a singleton instance of the enum type.
```java
enum Direction {
    NORTH(0), SOUTH(180), EAST(90), WEST(270);
    private final int degree;
    Direction(int degree) { this.degree = degree; }   // enum constructors are implicitly private
    public int getDegree() { return degree; }
}
```

**Constant-specific method bodies — Definition:** Each enum constant may override an abstract method with its own implementation.
```java
enum Direction {
    NORTH { public void move() { System.out.println("Move up (Y+1)"); } },
    SOUTH { public void move() { System.out.println("Move down (Y-1)"); } };
    public abstract void move();
}
```

**Built-in enum methods — Definition:** `values()` returns an array of all constants (for iteration); `valueOf(String)` converts a name back to its enum constant; `ordinal()` returns the declaration-order index (0-based); `name()`/`toString()`/`equals()`/`hashCode()` are also inherited.
```java
for (Direction d : Direction.values()) { System.out.println(d.name()); }
Direction d = Direction.valueOf("EAST");
System.out.println(d.ordinal());  // position in the declaration list
```

---

## 17. Strings Deep Dive (Lecture 25–26)

**String Pool & Immutability — Definition:** String literals are cached in a special pool region of the heap; using `new String(...)` forces a distinct, fresh heap object outside the pool. Strings are **immutable** — every "modification" (`+`, `substring`, etc.) creates a brand-new `String` object rather than changing the original.
```java
String s1 = "Hello"; String s2 = "Hello";
System.out.println(s1 == s2);          // true — both reference the SAME pooled literal

String s3 = new String("Aditya"); String s4 = new String("Aditya");
System.out.println(s3 == s4);          // false — two distinct heap objects, use .equals() instead
```
Because of immutability, building a string with `+=` inside a loop is inefficient (creates a new object each time, `O(n^2)` overall) — prefer `StringBuilder` for repeated mutation.

**String construction & common methods — Definition:** Strings can be built from literals, char arrays, byte arrays, or other `CharSequence`s, and support a rich set of query/transform methods.
```java
String s = new String("Aditya");
s.length(); s.isEmpty(); s.isBlank();
s.charAt(2); s.toCharArray();
s.equals("abc"); s.equalsIgnoreCase("ABC"); s.compareTo("abc"); // lexicographic comparison
s.contains("ity"); s.indexOf("ity"); s.startsWith("Ad");
s.substring(1); s.toUpperCase(); s.trim(); s.strip(); s.repeat(3); // strip() is unicode-aware
s.replace("ity", "abc"); String[] parts = s.split("-"); String.join("-", "a", "b", "c");
String.valueOf(10); s.getBytes();
String.format("Hello %s, your age is %s", name, age);
```

**StringBuilder / StringBuffer — Definition:** A **mutable** character sequence that avoids creating a new object on every modification. `StringBuffer` is the synchronized (thread-safe, slower) version; `StringBuilder` is unsynchronized and faster — preferred in single-threaded code.
```java
StringBuilder sb = new StringBuilder();
sb.append("Aditya"); sb.insert(2, 'o'); sb.delete(0, 2);
sb.deleteCharAt(1); sb.replace(1, 3, "XY"); sb.reverse();
sb.setCharAt(3, 'r');
sb.capacity(); sb.trimToSize(); // internal buffer size management
```

---

## 18. Casting, Generics (Lecture 27–28)

**Upcasting / Downcasting — Definition:** **Upcasting** converts a subtype reference to a supertype reference — implicit and always safe. **Downcasting** converts a supertype reference back to a subtype — explicit and can throw `ClassCastException` if the object isn't actually that subtype.
```java
String s = "Hello"; Object obj = s;                 // upcasting — implicit, always safe
Object obj2 = "Aditya"; String s2 = (String) obj2;   // downcasting — explicit cast, risky
```

**Why Generics — Definition:** Before generics, a general-purpose container had to use `Object`, which loses type information and forces unsafe manual casts on retrieval.
```java
class Box { private Object value; /* getValue() returns Object — caller must cast, unsafe */ }
```

**Generic classes & methods — Definition:** A generic type/method is parameterized over one or more types, specified with angle-bracket **type parameters** (`<T>`), preserving type safety without casting.
```java
class Box<T> {                 // T = type parameter
    private T value;
    Box(T value) { this.value = value; }
    public T getValue() { return value; }
}
Box<Integer> b1 = new Box<>(10);   // type argument specified at instantiation

class Pair<T, U> { T first; U second; }   // multiple type parameters

public static <T> T identity(T x) { return x; }             // generic method
public static <T, U> void printPair(T a, U b) { System.out.println(a + " , " + b); }
```

**Bounded Type Parameters — Definition:** Restrict a type parameter to be a subtype of a given class/interface (or multiple, with `&`), enabling use of that bound's methods.
```java
class Box<T extends Number> { T value; public void printDouble() { System.out.println(value.doubleValue()); } }
class Box2<T extends Animal & Swimmable> { T value; }   // multiple bounds
```

**Wildcards: `?`, `? extends`, `? super` — Definition:** A wildcard represents an unknown type argument, useful for method parameters that need flexibility. `? extends T` (upper bound) allows safe *reading* as `T`; `? super T` (lower bound) allows safe *writing* of `T` and its subtypes.
```java
static void fun(List<?> values) { }                     // unknown type — treat elements as Object, read-only
static void fun(List<? extends Animal> values) { }       // upper bound — safe to READ as Animal, can't add()
static void fun(List<? super Animal> values) {           // lower bound — safe to WRITE Animal/subtypes
    values.add(new Dog());
}
```
Mnemonic: **PECS** — **P**roducer `extends`, **C**onsumer `super`.

**Array Covariance vs Generic Invariance — Definition:** Java arrays are **covariant** (`Dog[]` can be assigned to `Animal[]`, but this risks a runtime `ArrayStoreException`); Java generics are **invariant** by design — `List<Dog>` is NOT a `List<Animal>`, caught safely at compile time instead.
```java
Dog[] dogs = new Dog[10];
Animal[] animals = dogs;          // arrays ARE covariant (allowed, but can throw ArrayStoreException later)

List<Dog> dogs2 = new ArrayList<>();
// List<Animal> animals2 = dogs2; // COMPILE ERROR — generics are invariant (safer, caught early)
```

---

## 19. Collections Framework Overview (Lecture 29)

**Why not plain arrays — Definition:** Arrays are fixed-size, and querying an unsorted array for a value is `O(N)`. The Collections Framework provides dynamic-size, richer data structures with better guarantees.

**The Collection hierarchy — Definition:** A set of interfaces/implementations for grouping objects: `List` (ordered, duplicates allowed, indexed), `Set` (no duplicates, optimized existence checks), `Queue` (FIFO / priority processing), and separately `Map` (unique key → value pairs).
```
        <<interface>> Collection
        /        |         \
      List      Set        Queue
   (ArrayList  (HashSet   (ArrayDeque,
   LinkedList) TreeSet)   PriorityQueue)

        <<interface>> Map   (a SEPARATE hierarchy — not a Collection)
     (HashMap, TreeMap, LinkedHashMap)
```

**Dynamic Array (concept behind `ArrayList`) — Definition:** An array-backed structure that automatically reallocates a bigger underlying array and copies elements over when it becomes full, giving the illusion of unbounded size.
```java
class DynamicArray {
    private int[] arr;
    DynamicArray(int size) { arr = new int[size]; }
    void add(int el) { /* if full: allocate new bigger array, copy old elements, then insert */ }
}
```

**LinkedList (concept) — Definition:** A sequence of `Node` objects, each holding data and a reference to the next node — insertion/deletion at the head is `O(1)` (no shifting, unlike arrays), but random access is `O(N)`.
```java
class Node { int data; Node next; }
// head -> [2] -> [3] -> [4] -> [5] -> null
```

**Stack (LIFO) & Queue (FIFO) — Definition:** A **Stack** processes elements Last-In-First-Out (like a stack of plates); a **Queue** processes elements First-In-First-Out (like a line of people).
```java
class Stack { int[] arr; void add(int el){ /* push */ } void remove(){ /* pop from top */ } } // LIFO
// Queue removes from the FRONT, adds at the BACK — FIFO
```

---

## 20. Iterator & Iterable (Lecture 30)

**`Iterator` — Definition:** An object that provides sequential, one-directional traversal over a collection, exposing `hasNext()` and `next()`.
```java
Collection<Integer> c = new TreeSet<>(List.of(10, 20, 30));
Iterator<Integer> it = c.iterator();
while (it.hasNext()) { System.out.println(it.next()); }
```

**`Iterable` — Definition:** An interface implemented by any class that wants to support the enhanced `for` loop; it must supply an `iterator()`.
```java
class NameContainer implements Iterable<String> {
    private String[] names;
    @Override public Iterator<String> iterator() {
        return new Iterator<String>() {
            private int pos = 0;
            @Override public boolean hasNext() { return pos < names.length; }
            @Override public String next() { return names[pos++]; }
        };
    }
}
for (String name : container) { System.out.println(name); } // enhanced for-loop calls iterator() internally
```

**`ConcurrentModificationException` — Definition:** A **fail-fast** exception thrown when a collection is structurally modified (e.g., `list.remove(x)`) while being iterated by a live `Iterator`, other than through `Iterator.remove()` itself.

---

## 21. Collection Interface & List (Lecture 31–32)

**Common `Collection` methods — Definition:** Methods shared by all collection types (`List`, `Set`, `Queue`) for size, membership, and bulk operations.
```java
Collection<Integer> c = new ArrayList<>();
c.size(); c.isEmpty(); c.contains(2); c.iterator();
c.toArray(); c.add(3); c.remove(3);
c.addAll(List.of(5,6,7)); c.containsAll(List.of(1,2,3));
c.removeAll(other); c.retainAll(List.of(1,2)); // retainAll() keeps only the INTERSECTION
c.clear();
```

**`List` — Definition:** An ordered `Collection` that allows duplicate elements and supports positional (indexed) access; `ArrayList` is array-backed (fast random access), `LinkedList` is node-backed (fast head insertion).
```java
List<Integer> list = new ArrayList<>(List.of(1,2,3));
list.get(1); list.set(1, 5); list.addAll(0, List.of(9,8,7));
list.remove(0); list.indexOf(2); list.lastIndexOf(5);
ListIterator<Integer> it = list.listIterator(3); // supports BIDIRECTIONAL traversal
while (it.hasPrevious()) { it.previous(); }

List<Integer> immutable = List.of(1,2,3);      // an immutable list — add() throws UnsupportedOperationException
List<Integer> copy = List.copyOf(immutable);   // also immutable
```

---

## 22. Set & Map (Lecture 33–34)

**`HashSet` / `LinkedHashSet` / `TreeSet` — Definition:** A `Set` stores unique elements (no duplicates). `HashSet` gives no order guarantee but `O(1)` average add/contains (hash-based); `LinkedHashSet` preserves insertion order; `TreeSet` keeps elements sorted, backed by a balanced BST, giving `O(log N)` operations.
```java
Set<String> set = new HashSet<>();
set.add("Aditya");

Set<Integer> linkedSet = new LinkedHashSet<>(100, 0.8f); // (initial capacity, load factor), preserves insertion order

TreeSet<Integer> tree = new TreeSet<>();
tree.add(80); tree.add(23); tree.add(10); tree.add(90); tree.add(50);
tree.first(); tree.last();                    // smallest / largest
tree.headSet(80); tree.tailSet(80);            // elements strictly < 80 / >= 80
tree.subSet(23, 80);                            // range [23, 80) — from inclusive, to exclusive
tree.headSet(80, true);                          // overload with an explicit "inclusive" boolean flag
tree.subSet(10, false, 80, true);                 // (from, fromInclusive, to, toInclusive)
```
**NavigableSet extras — Definition:** `TreeSet` also implements `NavigableSet`, exposing closest-match lookups and reverse iteration.
```java
tree.lower(10);   // largest element STRICTLY LESS than 10
tree.floor(10);    // largest element <= 10
tree.higher(80);    // smallest element STRICTLY GREATER than 80
tree.ceiling(80);    // smallest element >= 80
tree.pollFirst(); tree.pollLast();       // remove & return smallest/largest
tree.descendingSet(); tree.descendingIterator(); // reverse-order view
```

**`HashMap` / `LinkedHashMap` / `TreeMap` — Definition:** A `Map` stores unique key → value pairs. `HashMap` is unordered, `O(1)` average operations; `LinkedHashMap` preserves insertion order; `TreeMap` keeps keys sorted, `O(log N)` operations (backed by a BST).
```java
Map<Integer, String> map = new HashMap<>();
map.put(101, "Aditya");             // put() ALWAYS overwrites an existing key's value
map.putIfAbsent(103, "Abhay");      // does NOT overwrite if the key already exists
map.get(102); map.getOrDefault(105, "Unknown");
map.containsKey(101); map.containsValue("Aditya");
map.remove(101); map.replace(101, "Sonu");
Set<Integer> keys = map.keySet();
Collection<String> values = map.values();
for (Map.Entry<Integer, String> e : map.entrySet()) { e.getKey(); e.getValue(); }

Map<Integer, String> immutableMap = Map.of(101, "Aditya", 102, "Rohit"); // immutable map literal

TreeMap<Integer, String> treeMap = new TreeMap<>(); // sorted by key
treeMap.put(101, "Aditya"); treeMap.put(102, "Rohit");
treeMap.lastEntry();                 // entry with the largest key
treeMap.subMap(101, 103);             // key range [101, 103)
treeMap.higherEntry(102);              // entry with the smallest key STRICTLY greater than 102
```

---

## 23. Queue, Deque, PriorityQueue (Lecture 35)

**`Queue` (FIFO) — Definition:** A collection designed for holding elements prior to processing, in First-In-First-Out order. `ArrayDeque` is a common, efficient implementation.
```java
Queue<Integer> queue = new ArrayDeque<>();
queue.offer(1);       // enqueue — returns false instead of throwing if capacity-bounded and full
queue.peek();          // look at the front element, returns null if empty
queue.poll();           // dequeue — returns null if empty (SAFER than remove())
queue.remove();          // dequeue — THROWS NoSuchElementException if empty
```

**`PriorityQueue` — Definition:** A queue that always dequeues the "highest priority" element first, based on natural ordering or a supplied `Comparator` — implemented internally as a binary heap.
```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();               // natural ordering -> min-heap (smallest first)
PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a); // reversed comparator -> max-heap
```

---

## 24. Comparable & Comparator (Lecture 36–37)

**`Comparable` — Definition:** An interface implemented **inside** a class to define its single, natural sort order via `compareTo()`.
```java
class Student implements Comparable<Student> {
    String name; int marks;
    @Override public int compareTo(Student other) {
        if (this.marks != other.marks) return this.marks - other.marks;
        return this.name.compareTo(other.name);   // tie-breaker
    }
}
Collections.sort(list); // uses compareTo()
```

**`Comparator` — Definition:** An **external**, swappable strategy object for ordering, useful when you need multiple different orderings or can't modify the class itself.
```java
class SortByMarks implements Comparator<Student> {
    @Override public int compare(Student s1, Student s2) { return s1.marks - s2.marks; }
}
Collections.sort(list, new SortByMarks());
Collections.sort(list, (s1, s2) -> s1.marks - s2.marks); // equivalent lambda form
```
Rule for `compareTo`/`compare`: negative → first argument sorts before the second; `0` → considered equal; positive → first argument sorts after.

---

## 25. Lambda Expressions & Functional Interfaces (Lecture 37–38)

**Functional Interface — Definition:** An interface with **exactly one** abstract method, which allows it to be implemented concisely with a lambda expression. Marked (optionally, for compiler checking) with `@FunctionalInterface`.
```java
@FunctionalInterface
interface Calculator { int calculate(int a, int b); }

Calculator add = (a, b) -> a + b;   // lambda implements the single abstract method
System.out.println(add.calculate(5, 4));
```

**Built-in functional interfaces (`java.util.function`) — Definition:** The standard library provides common-shape functional interfaces so custom ones don't always need to be declared.
```java
Function<Integer, Integer> square = x -> x * x;      // T -> R
Consumer<Integer> print = x -> System.out.println(x); // T -> void
Supplier<Double> randomValue = () -> Math.random();    // () -> T
Predicate<Integer> isEven = x -> x % 2 == 0;            // T -> boolean

list.forEach(System.out::println); // method reference syntax (shorthand for a lambda)
```

**Composing functions — Definition:** `Function`, `Predicate`, and `Consumer` provide default methods to combine multiple functions into one pipeline.
```java
Function<Integer, Integer> add2 = x -> x + 2;
Function<Integer, Integer> multiply3 = x -> x * 3;
Function<Integer, Integer> combined = add2.andThen(multiply3); // apply add2, THEN multiply3

Predicate<Integer> isOdd = isEven.negate();               // negate() -> !
Predicate<Student> eligible = passed.or(isAdult);           // and() -> &&, or() -> ||
```

---

## 26. Streams API (Lecture 39–42)

**Stream — Definition:** A sequence of elements supporting a functional pipeline of operations: a **source**, zero or more lazy **intermediate operations**, and exactly one **terminal operation** that triggers execution.
```java
List<Integer> list = new ArrayList<>(List.of(5, 12, 7, 14));
list.stream()                       // source
    .filter(x -> x > 10)             // intermediate — lazy, returns a new Stream
    .map(x -> x * 2)                  // intermediate
    .forEach(System.out::println);     // terminal — triggers the whole pipeline
```

**Stream creation — Definition:** Streams can be created from collections, arrays, or generator functions.
```java
list.stream();                    // from a Collection
Arrays.stream(new int[]{1,2,3});   // from an array
Stream.of(1, 2, 3);                 // from varargs
Stream.iterate(1, x -> x + 1);        // infinite generator — MUST be combined with limit()
```

**Intermediate operations — Definition:** Transform or filter the stream lazily, without producing a final result.
```java
.filter(predicate)      // keep only matching elements
.map(function)           // transform each element
.flatMap(x -> x.stream()) // flatten nested streams (e.g., Stream<List<T>> -> Stream<T>)
.sorted()                 // stateful — must see all elements before emitting any
.distinct()                // stateful — removes duplicates via hashing
.limit(10); .skip(5);      // truncate / skip a number of elements
.peek(System.out::println) // side-effect for debugging, does not consume the stream
```

**Terminal operations — Definition:** Consume the stream and produce a final result or side effect; after a terminal op, the stream cannot be reused.
```java
.forEach(...); .forEachOrdered(...);
.collect(Collectors.toList());     // or toSet()
.reduce(1, (a, b) -> a + b);         // combine ALL elements into a single value
.count(); .findFirst(); .findAny();  // findFirst/Any are short-circuiting (stop early)
.anyMatch(p); .allMatch(p); .noneMatch(p);
.mapToInt(x -> x).sum(); .average(); .max(); .min(); // primitive streams (IntStream, etc.)
```

**Collectors — Definition:** Reusable reduction strategies passed to `.collect()`, for building collections, grouping, partitioning, or joining strings.
```java
list.stream().collect(Collectors.joining("-"));                     // concatenate into a String
list.stream().collect(Collectors.toMap(x -> x, x -> x.length()));   // build a Map<T, Integer>
list.stream().collect(Collectors.groupingBy(x -> x.length(),
                       Collectors.mapping(x -> x.toLowerCase(), Collectors.toList()))); // group + sub-transform
list.stream().collect(Collectors.partitioningBy(x -> x % 2 == 0));  // Map<Boolean, List<T>> — true/false buckets
```

**`Optional<T>` — Definition:** A container object that may or may not hold a non-null value, used to avoid explicit `null` checks and `NullPointerException`s.
```java
Optional<String> name = Optional.ofNullable(getName());
name.ifPresent(System.out::println);
name.orElse("Unknown"); name.orElseGet(() -> "Unknown"); name.orElseThrow();
name.map(String::length).filter(len -> len > 4).ifPresent(System.out::println);
name.ifPresentOrElse(System.out::println, () -> System.out.println("Unknown"));
```

**Parallel Streams — Definition:** A stream that splits its work across multiple threads (backed by the common `ForkJoinPool`), best for CPU-bound, stateless, large-data transformations.
```java
list.parallelStream().map(x -> x * 2).forEachOrdered(System.out::println); // forEachOrdered preserves order
```

---

## 27. Exception Handling (Lecture 43–44)

**Exception — Definition:** An event that disrupts the normal flow of a program's instructions; an uncaught exception propagates up the call stack and crashes the program.
```java
static void methodA(int a, int b) { methodB(a, b); }
static void methodB(int a, int b) { System.out.println(a / b); } // uncaught -> propagates up through methodA -> main -> crash
```

**try / catch / finally — Definition:** `try` wraps risky code; `catch` handles a specific exception type if thrown; `finally` **always** runs (cleanup, resource closing), whether or not an exception occurred.
```java
try {
    int a = 5, b = 0;
    System.out.println(a / b);           // throws ArithmeticException
} catch (ArithmeticException e) {
    System.out.println("Divide by zero not allowed");
} finally {
    System.out.println("Always runs — cleanup code goes here");
}
```

**Nested try-catch — Definition:** A `try/catch` block placed inside another; the inner `catch` handles matching exceptions locally, but if the inner block doesn't have a matching `catch`, the exception propagates to the enclosing (outer) `catch`.
```java
try {                                        // outer try
    try {                                     // inner try
        System.out.println(5 / 0);            // throws ArithmeticException
    } catch (NullPointerException e) {          // inner catch DOESN'T match -> exception escapes to outer
        System.out.println("Nulls are not allowed");
    }
} catch (ArithmeticException e) {               // outer catch handles it instead
    System.out.println("Divide by zero not allowed : Outer");
}
```

**Multi-catch & catch ordering — Definition:** A single `catch` can handle several unrelated exception types with `|`; when using multiple `catch` blocks, more specific exception types must come **before** more general ones (like `Exception`).
```java
try { /* ... */ }
catch (ArithmeticException | NullPointerException e) { }  // multi-catch (use only when handling is IDENTICAL)
catch (RuntimeException e) { }   // broader — must come AFTER more specific catches
catch (Exception e) { }          // most generic — must be LAST
```

**Custom (checked) exceptions — Definition:** A user-defined exception class, extending `Exception` (checked) or `RuntimeException` (unchecked), used to represent domain-specific error conditions with extra context.
```java
class InvalidAgeException extends Exception {          // extends Exception = CHECKED
    private int age;
    public InvalidAgeException(String message, int age) { super(message); this.age = age; }
    public int getAge() { return age; }
}
static void checkEligibility(int age) throws InvalidAgeException {
    if (age <= 0) throw new InvalidAgeException("Age cannot be negative", age); // "throw" raises an instance
}
```
`throw` raises an exception instance; `throws` (on a method signature) declares that a checked exception may propagate out of that method.

**Checked vs Unchecked Exceptions — Definition:** **Checked** exceptions (extend `Exception`, not `RuntimeException`) must be declared with `throws` or caught — enforced by the compiler (e.g., `FileNotFoundException`). **Unchecked** exceptions (extend `RuntimeException`) are not compiler-enforced (e.g., `ArithmeticException`, `NullPointerException`).
```java
private static void readFile() throws FileNotFoundException {   // checked — MUST declare or catch
    FileReader fr = new FileReader("abc.txt");
}
```

---

## 28. Memory Management & Garbage Collection (Lecture 45–46)

**JVM Runtime Memory Areas — Definition:** The regions the JVM divides its managed memory into, each serving a distinct purpose.
```
Java Process
 └── JVM Runtime
      ├── Heap             (objects, arrays — SHARED across all threads)
      ├── Method Area/Metaspace (class metadata, static vars, method bytecode)
      ├── Java Stack        (PER-THREAD: local vars, method call frames — LIFO)
      ├── PC Register        (PER-THREAD: address of the current instruction)
      └── Native Method Stack (support for native/JNI code)
```

**Heap Generations — Definition:** The heap is split based on the "generational hypothesis" (most objects die young), so garbage collection can be cheaper for short-lived objects.
```
Heap
 ├── Young Generation
 │     ├── Eden       (new objects allocated here first — fastest allocation region)
 │     └── Survivor (S0, S1)  (objects that survived at least one minor GC)
 └── Old Generation   (long-lived objects, promoted after surviving several minor GCs)
```
- **Minor GC**: runs on the Young Generation only — cheap and frequent.
- **Major/Full GC**: runs on the Old Generation — expensive, causes a longer "stop-the-world" pause.

**Garbage Collection algorithms — Definition:** Strategies the JVM uses to reclaim memory occupied by objects no longer reachable from any live reference.
| Algorithm | How it works | Trade-off |
|---|---|---|
| Mark & Sweep | Mark reachable objects, sweep (free) unmarked ones | Leaves memory fragmentation |
| Mark & Compact | Mark, then slide surviving objects together | No fragmentation, but moving is costly |
| Copying | Copy live objects from one region (e.g., Eden) to another (Survivor) | Used for the Young Gen (minor GC) |

**Eligibility for GC — Definition:** An object becomes eligible for garbage collection once no reachable reference points to it anymore.
```java
Student s1 = new Student();
s1 = null;   // the Student object is now unreachable -> eligible for GC
```

**`OutOfMemoryError` / `StackOverflowError` — Definition:** `OutOfMemoryError` occurs when the heap is exhausted and the GC can't free enough space; `StackOverflowError` occurs when a thread's call stack exceeds its limit (typically unbounded recursion).
```java
List<int[]> list = new ArrayList<>();
while (true) { list.add(new int[250000]); } // keeps allocating on the HEAP -> eventually OutOfMemoryError

static void f1() { f1(); } // unbounded recursion -> STACK overflows -> StackOverflowError
```

---

## 29. Program, Process, Thread & Concurrency Fundamentals (Lecture 47)

**Program — Definition:** A passive set of stored instructions (e.g., a `.java` source file or a compiled `.class` bytecode file) — not currently executing.
```java
// Demo.java (source, human-readable) and Demo.class (bytecode) are both "programs" — stored, not running
```

**Process — Definition:** A **running instance** of a program, given its own memory (heap, stacks, loaded libraries) and resources (files, sockets) by the operating system. Running `java Demo` starts an OS process containing a JVM runtime executing the Demo application.
```
Operating-system process
 └── JVM runtime
      └── Java application (bytecode being executed)
```
Processes do **not** share memory with each other by default; communicating between processes (IPC) requires pipes, sockets, shared memory, or files.

**Thread — Definition:** An independent path of execution *inside* a process. A process must contain at least one thread; when a Java app starts, the JVM creates a **main thread** that invokes `main()`.
```java
Thread worker = new Thread(() -> System.out.println("Worker running"));
worker.start();
// Now the process contains: main thread + worker thread
```
Threads within the same process **share** heap objects, static variables, and loaded classes, but each thread has its **own** private Java stack and program-counter (PC) register.
```
Private execution stacks (per thread)
       +
Shared process memory (heap, static fields)
```

**Context Switching — Definition:** The mechanism where a CPU core stops executing one thread and starts executing another, saving/restoring each thread's execution state — not free (has real overhead from state saving, cache disruption, etc.).

**Concurrency vs Parallelism — Definition:** **Concurrency** means multiple tasks make progress during *overlapping* time periods (can happen even on a single core, via interleaving/context switching). **Parallelism** means multiple tasks execute at the *exact same instant* on different CPU cores.
| Concurrency | Parallelism |
|---|---|
| Overlapping progress | Simultaneous execution |
| Can happen on 1 core | Needs multiple cores |
| Task coordination | Actual hardware execution |

**Multitasking vs Multithreading — Definition:** **Multitasking** = the OS running multiple separate *processes*/applications concurrently (e.g., browser + editor + music player). **Multithreading** = a single *process* running multiple *threads* that share its memory.
```
One Java process
 ├── Main thread
 ├── Request-processing thread
 └── Background monitoring thread
```

---

## 30. Multithreading Basics (Lecture 48–49)

**Creating threads: extending `Thread` vs implementing `Runnable` — Definition:** Two standard ways to define a unit of concurrent work in Java. Implementing `Runnable` is generally preferred (frees the class to extend something else, since Java has no multiple inheritance of classes).
```java
class MyThread extends Thread {
    @Override public void run() { System.out.println("Running"); }
}
new MyThread().start();                                  // Approach 1

Thread t = new Thread(() -> System.out.println("Running")); // Approach 2 (Runnable via lambda — preferred)
t.start();
```
Always call `.start()` — it spawns a new OS thread and calls `run()` on it. Calling `.run()` directly just executes synchronously on the *current* thread (no new thread created). `.start()` can only be called **once** per thread.

**Thread identity & naming — Definition:** Every thread has a name and numeric ID, useful for logging/debugging.
```java
Thread.currentThread().getName();
Thread.currentThread().getId();
thread.setName("worker-1");
```

**Thread lifecycle — Definition:** The set of states a thread passes through from creation to termination.
```
NEW -> RUNNABLE -> (BLOCKED / WAITING / TIMED_WAITING) -> TERMINATED
```
```java
Thread t1 = new Thread(() -> {});
t1.getState();          // NEW
t1.start();
t1.getState();           // RUNNABLE
Thread.sleep(2000);
t1.getState();            // TERMINATED (once finished)
```

**Key thread methods — Definition:** Utility methods for coordinating and controlling thread execution.
```java
Thread.sleep(2000);        // pause the CURRENT thread -> TIMED_WAITING (throws InterruptedException)
t1.join();                   // caller thread WAITS for t1 to finish -> WAITING
t1.join(1000);                 // wait at most 1000ms, then continue regardless
Thread.yield();                  // suggestion to let same-priority threads run (OS may ignore it)
t1.interrupt();                    // signal a thread it should stop; check via isInterrupted()/interrupted()
t1.isAlive();                        // true between start() and termination
t1.setPriority(10);                    // hint only (MIN=1, NORM=5, MAX=10); OS may not respect it
t1.setDaemon(true);                      // daemon threads auto-die when all user threads finish (e.g. GC thread)
```

---

## 31. Synchronization & Thread Safety (Lecture 50–51)

**Race Condition — Definition:** A bug that occurs when multiple threads access and modify shared data concurrently without coordination, and the final result depends on unpredictable timing/interleaving.
```java
class Counter {
    int count = 0;
    void increment() { count++; } // NOT atomic: read -> add 1 -> write (3 separate steps) — data race with 2+ threads
}
```

**`synchronized` keyword — Definition:** Ensures only **one thread at a time** can execute a critical section guarded by a given lock object — either a `synchronized` block (explicit lock object) or a `synchronized` method (implicit lock = `this`).
```java
class Counter {
    int count = 0;
    void increment() {
        synchronized (this) { count++; } // critical section — only 1 thread at a time can hold "this" lock
    }
}
synchronized void show() { /* ... */ }   // shorthand for: void show() { synchronized(this) { ... } }
```
Using a *different* lock object per call (e.g. `synchronized(new Object())`) provides no real mutual exclusion — always synchronize on a shared, stable lock object.

**Static Synchronization (class-level lock) — Definition:** Locks on the `Class` object itself (`Counter.class`) rather than an instance — needed to protect shared `static` state across all instances.
```java
static synchronized void increment() { /* locks on Counter.class */ }
synchronized (Counter.class) { /* equivalent explicit form */ }
```
Instance-level `synchronized` and static-level `synchronized` use **different locks** — they do not block each other.

**`volatile` keyword — Definition:** Guarantees that reads/writes to a variable are always visible across threads immediately (prevents each CPU/thread from caching a stale local copy) — but does **NOT** provide atomicity for compound operations like `count++`.
```java
static volatile boolean flag = false; // ensures other threads immediately see updates to "flag"
```

---

## 32. wait/notify — Producer-Consumer (Lecture 52)

**`wait()` / `notify()` / `notifyAll()` — Definition:** Low-level thread coordination primitives on any object's intrinsic monitor. `wait()` releases the lock and pauses the current thread until notified; `notify()` wakes one waiting thread; `notifyAll()` wakes all of them. Must be called from inside a `synchronized` block on that same object.
```java
class Box {
    volatile Integer item;
    volatile Boolean flag = false;

    synchronized void producer(int value) throws InterruptedException {
        while (flag == true) wait();     // release the lock, sleep until notified
        item = value; flag = true;
        System.out.println("Produced " + item);
        notify();                          // wake up a waiting thread (e.g. the consumer)
    }

    synchronized void consumer() throws InterruptedException {
        while (flag == false) wait();
        System.out.println("Consumed " + item);
        item = null; flag = false;
        notify();
    }
}
```
Unlike a busy-spin `while` loop (which wastes CPU polling a `volatile` flag), `wait()` truly suspends the thread until it's notified — far more efficient.

---

## 33. Explicit Locks (Lecture 53)

**`ReentrantLock` — Definition:** An explicit, more flexible alternative to `synchronized` — supports try-lock (non-blocking attempt), timed lock acquisition, and interruptible waiting.
```java
Lock lock = new ReentrantLock();
void f1() {
    lock.lock();
    try { /* critical section */ }
    finally { lock.unlock(); }   // ALWAYS unlock in a finally block
}
```

**`ReadWriteLock` — Definition:** Splits locking into a **shared** read lock (multiple readers allowed simultaneously) and an **exclusive** write lock (only one writer, no concurrent readers) — improves throughput for read-heavy workloads.
```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock = rwLock.readLock();    // shared among readers
Lock writeLock = rwLock.writeLock();   // exclusive — blocks readers and other writers
```

**`StampedLock` — Definition:** Adds an **optimistic read** mode (reads with NO locking at all, validated afterward) on top of read/write locking — even faster than `ReadWriteLock` when writes are rare, since readers never block.
```java
StampedLock lock = new StampedLock();
long stamp = lock.tryOptimisticRead();
int value = sharedValue;                   // read WITHOUT locking
if (!lock.validate(stamp)) {               // check whether a write happened meanwhile
    stamp = lock.readLock();                // fallback: acquire a real (pessimistic) read lock
    try { value = sharedValue; } finally { lock.unlockRead(stamp); }
}
```

---

## 34. Atomic Variables (Lecture 54–55)

**Atomic Variables & CAS — Definition:** Classes like `AtomicInteger`/`AtomicReference` provide lock-free thread safety using the CPU-level **Compare-And-Set (CAS)** instruction — often faster than locks for simple counters/references since there's no thread blocking.
```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();      // atomic equivalent of ++count, safe under concurrent access

AtomicReference<String> seat = new AtomicReference<>("EMPTY");
seat.compareAndSet("EMPTY", "Aditya");   // atomically: IF current value == "EMPTY", THEN set to "Aditya"
```

**Manual CAS retry-loop pattern — Definition:** The general algorithm `AtomicInteger`/`AtomicReference` implement internally — read the current value, compute the new value, then try to atomically swap; retry on conflict.
```java
while (true) {
    int current = counter.get();
    int next = current + 1;
    if (counter.compareAndSet(current, next)) break; // success — swap happened atomically
    // else: someone else updated it first — retry from the top
}
```

---

## 35. Executor Framework (Lecture 56)

**`ExecutorService` — Definition:** A higher-level abstraction over manual thread creation — manages a **pool** of reusable worker threads to which many tasks can be submitted, avoiding the overhead of creating a new thread per task.
```java
ExecutorService executor = Executors.newFixedThreadPool(2);
executor.execute(() -> System.out.println("Task on " + Thread.currentThread().getName()));

Future<Integer> future = executor.submit(() -> { Thread.sleep(3000); return 10; }); // submit() returns a value-bearing Future
future.get();   // blocks until the result is ready; wraps task failures in ExecutionException

executor.shutdown(); // stop accepting NEW tasks; lets already-submitted ones finish
```

**Custom `ThreadPoolExecutor` — Definition:** The lower-level, fully configurable thread pool implementation behind the `Executors` factory methods — lets you tune core/max pool size, idle timeout, and the task queue.
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,                          // core pool size (always kept alive)
    5,                          // max pool size (grows up to this under load)
    10, TimeUnit.SECONDS,        // idle-thread keep-alive time beyond core size
    new ArrayBlockingQueue<>(2)   // bounded task queue holding pending tasks
);
```

---

## 36. Advanced Concurrency (Lecture 57)

**`Future<T>` — Definition:** A placeholder representing the eventual result of an asynchronous task submitted to an executor; lets the caller wait, check status, or cancel.
```java
Future<Integer> future = executor.submit(() -> 50);
future.get();                        // blocks until the result is ready
future.get(2, TimeUnit.SECONDS);       // blocks with a timeout, throws TimeoutException if exceeded
future.isDone();                        // non-blocking status check (true even if it FAILED or was CANCELLED!)
future.cancel(true);                      // request cancellation; true = may interrupt if already running
future.isCancelled();
```

**`CompletableFuture<T>` — Definition:** An enhanced `Future` that also implements `CompletionStage`, allowing asynchronous operations to be **chained, transformed, and combined** declaratively instead of blocking between every step.
```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 10); // async task that returns a value
CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> System.out.println("run")); // async task, no value

future.thenApply(v -> v * 2)          // transform the result -> new CompletableFuture
      .thenAccept(v -> System.out.println(v))  // consume the result, produces no new value
      .thenRun(() -> System.out.println("done")); // run after completion, ignores the result entirely

CompletableFuture<Integer> combined = first.thenCombine(second, (a, b) -> a + b); // merge 2 INDEPENDENT futures

future.exceptionally(ex -> 0);         // recover from a failure with a fallback value
future.whenComplete((result, ex) -> { /* observe success OR failure, doesn't change the result */ });
future.handle((result, ex) -> ex != null ? "fallback" : "ok"); // transform in BOTH success AND failure cases
```
`thenApply()` may run on whichever thread completes the previous stage; `thenApplyAsync()` explicitly schedules the continuation on the common `ForkJoinPool` (or a custom executor, if passed as a second argument).

**`ForkJoinPool` — Definition:** A specialized thread pool for **divide-and-conquer** parallelism — recursively splits a big problem into small subtasks, executes them across worker threads, and combines partial results; idle workers **steal** tasks from busy workers' queues to balance uneven workloads.
```java
class SumTask extends RecursiveTask<Integer> {           // returns a result (use RecursiveAction if no result needed)
    int[] numbers; int start, end;
    protected Integer compute() {
        if (end - start <= THRESHOLD) return calculateDirectly(); // base case — small enough, solve directly
        int mid = start + (end - start) / 2;
        SumTask left = new SumTask(numbers, start, mid);
        SumTask right = new SumTask(numbers, mid, end);
        left.fork();                       // schedule left subtask ASYNCHRONOUSLY
        int rightResult = right.compute();  // compute right subtask directly on the CURRENT thread
        int leftResult = left.join();        // wait for the forked left subtask's result
        return leftResult + rightResult;      // combine
    }
}
ForkJoinPool pool = new ForkJoinPool();
int result = pool.invoke(new SumTask(numbers, 0, numbers.length));
```

**`ThreadLocal<T>` — Definition:** A container that gives each thread its **own independent copy** of a variable, so threads sharing the same `ThreadLocal` object never see each other's values.
```java
private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();
USER_NAME.set("Aditya");        // only visible to the CURRENT thread
USER_NAME.get();
USER_NAME.remove();              // ALWAYS clean up in a finally block, especially with pooled/reused threads
```

**Virtual Threads (Java 21+) — Definition:** Lightweight threads scheduled by the JVM (not directly by the OS), enabling huge numbers of concurrent blocking tasks without the cost of one OS thread per task. A **carrier thread** (a real platform thread) temporarily runs a virtual thread; when it blocks on I/O, the JVM can *unmount* it, freeing the carrier for other virtual threads.
```java
Thread t = Thread.startVirtualThread(() -> System.out.println("Running in " + Thread.currentThread()));

ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // ONE new virtual thread PER task, not pooled
executor.submit(() -> { Thread.sleep(1000); System.out.println("done"); });
```
Best for high-concurrency blocking I/O (web requests, DB calls); *not* beneficial for CPU-bound work (use `ForkJoinPool` there instead). Never pool virtual threads — create one per task, since they're cheap to spin up.

**Choosing the right concurrency tool — Definition:** A quick decision guide based on what the workload actually needs.
| Need | Tool |
|---|---|
| Track/cancel one async task | `Future` |
| Chain/transform/combine async results | `CompletableFuture` |
| Parallelize a divisible CPU-bound computation | `ForkJoinPool` |
| Handle massive concurrent blocking I/O | Virtual Threads |
| Per-thread context data | `ThreadLocal` |

---

## Quick Index (all 57 lectures)
1–2: Java intro, JVM/JRE/JDK · 3–5: data types, casting · 6: operators · 7–8: control flow, loops · 9–10: arrays, memory model · 11: functions · 12–13: classes, constructors · 14–15: call-by-value, static/final · 16: encapsulation, packages, inheritance · 17–18: abstract classes, interfaces, overriding, **autoboxing/unboxing, Integer caching** · 19: nested/inner/local/anonymous classes · 20: I/O basics · 21: immutability · 22: equals/hashCode/toString/clone/instanceof · 23: enums · 24: polymorphism, interface default/static methods · 25–26: Strings, StringBuilder · 27–28: casting, generics · 29: collections overview · 30: Iterator/Iterable · 31–32: Collection, List · 33–34: Set, Map (incl. NavigableSet/Map) · 35: Queue/Deque/PriorityQueue · 36–37: Comparable/Comparator, lambdas · 38: functional interfaces · 39–42: Streams API, parallel streams · 43–44: exception handling, nested try-catch · 45–46: memory management, GC · **47: program/process/thread theory, concurrency vs parallelism** · 48–49: thread basics · 50–51: synchronization · 52: wait/notify · 53: locks (Reentrant/ReadWrite/Stamped) · 54–55: atomic variables · 56: Executor framework · 57: Future/CompletableFuture/ForkJoinPool/ThreadLocal/Virtual Threads
