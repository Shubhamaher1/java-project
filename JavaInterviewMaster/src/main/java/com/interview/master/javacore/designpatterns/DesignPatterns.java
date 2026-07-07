package com.interview.master.javacore.designpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * DESIGN PATTERNS - Complete Interview Reference
 * =============================================================================
 *
 * Design patterns are reusable solutions to commonly occurring problems in
 * software design. They are NOT finished code, but templates/blueprints.
 *
 * Categorized by the Gang of Four (GoF) book into three groups:
 *   1. Creational  - How objects are created
 *   2. Structural  - How classes/objects are composed
 *   3. Behavioral  - How objects communicate/interact
 *
 * =============================================================================
 * SPRING FRAMEWORK AND DESIGN PATTERNS (Very common interview question!)
 * =============================================================================
 *
 * Q: "Which design patterns does Spring use?"
 *
 *   1. SINGLETON   - Spring beans are Singleton by default (@Scope("singleton"))
 *                    BeanFactory/ApplicationContext manages single instances
 *
 *   2. FACTORY     - BeanFactory, ApplicationContext are factory classes
 *                    They create and manage bean instances
 *
 *   3. PROXY       - AOP (Aspect-Oriented Programming) uses dynamic proxies
 *                    @Transactional, @Cacheable wrap beans in proxy objects
 *                    Spring uses JDK dynamic proxy or CGLIB proxy
 *
 *   4. TEMPLATE    - JdbcTemplate, RestTemplate, HibernateTemplate
 *                    Define skeleton of an algorithm; subclasses fill steps
 *
 *   5. OBSERVER    - ApplicationEvent + ApplicationListener / @EventListener
 *                    Spring's event publishing mechanism
 *
 *   6. DECORATOR   - BeanPostProcessor wraps/enhances beans
 *                    HttpServletRequestWrapper, HttpServletResponseWrapper
 *
 *   7. ADAPTER     - HandlerAdapter in Spring MVC adapts different controllers
 *
 *   8. FRONT CONTROLLER - DispatcherServlet is a Front Controller pattern
 *
 *   9. DEPENDENCY INJECTION - Core of Spring IoC container
 *
 * =============================================================================
 */
public class DesignPatterns {

    public static void main(String[] args) {
        System.out.println("=== DESIGN PATTERNS DEMO ===\n");

        // ---- CREATIONAL ----
        demonstrateSingleton();
        demonstrateFactory();
        demonstrateAbstractFactory();
        demonstrateBuilder();
        demonstratePrototype();

        // ---- STRUCTURAL ----
        demonstrateAdapter();
        demonstrateDecorator();
        demonstrateFacade();
        demonstrateProxy();
        demonstrateComposite();

        // ---- BEHAVIORAL ----
        demonstrateStrategy();
        demonstrateObserver();
        demonstrateTemplateMethod();
        demonstrateCommand();
        demonstrateChainOfResponsibility();
        demonstrateState();
    }


    // =========================================================================
    // CREATIONAL PATTERNS
    // =========================================================================


    // -------------------------------------------------------------------------
    // 1. SINGLETON PATTERN
    // -------------------------------------------------------------------------
    // Intent: Ensure a class has only ONE instance and provide a global point
    //         of access to it.
    //
    // Use cases: Logger, Configuration manager, Connection pool, Cache
    //
    // Key Interview Points:
    //   - Must prevent instantiation from outside (private constructor)
    //   - Must handle multi-threading (thread-safe singleton)
    //   - Enum Singleton is the BEST practice (handles serialization + reflection)
    //   - Bill Pugh (static inner class) is lazy + thread-safe without sync overhead
    // -------------------------------------------------------------------------

    // --- Approach 1: Eager Initialization ---
    // Created at class loading time. Thread-safe. Simple but wastes memory if never used.
    static class EagerSingleton {
        // Instance created eagerly when class is loaded
        private static final EagerSingleton INSTANCE = new EagerSingleton();

        private EagerSingleton() {
            // private constructor prevents external instantiation
        }

        public static EagerSingleton getInstance() {
            return INSTANCE;
        }

        public void show() { System.out.println("EagerSingleton instance: " + hashCode()); }
    }

    // --- Approach 2: Lazy Initialization (NOT thread-safe) ---
    // Created only when first needed. But NOT safe for multi-threaded environments.
    static class LazySingleton {
        private static LazySingleton instance; // not created yet

        private LazySingleton() {}

        public static LazySingleton getInstance() {
            if (instance == null) { // PROBLEM: two threads can pass this check simultaneously
                instance = new LazySingleton();
            }
            return instance;
        }
    }

    // --- Approach 3: Thread-Safe with synchronized method ---
    // Simple fix but EXPENSIVE: every call acquires the lock even after instance is created.
    static class SynchronizedSingleton {
        private static SynchronizedSingleton instance;

        private SynchronizedSingleton() {}

        // synchronized: only one thread can execute this at a time
        public static synchronized SynchronizedSingleton getInstance() {
            if (instance == null) {
                instance = new SynchronizedSingleton();
            }
            return instance;
        }
    }

    // --- Approach 4: Double-Checked Locking (DCL) ---
    // Best of both: lazy init + thread-safe + avoids lock after first creation.
    // CRITICAL: volatile keyword is MANDATORY to prevent instruction reordering.
    static class DCLSingleton {
        // volatile: ensures visibility across threads AND prevents partial initialization
        // Without volatile, another thread may see a partially constructed object
        private static volatile DCLSingleton instance;

        private DCLSingleton() {}

        public static DCLSingleton getInstance() {
            if (instance == null) {                    // First check (no locking)
                synchronized (DCLSingleton.class) {   // Lock only when null
                    if (instance == null) {            // Second check (with lock)
                        instance = new DCLSingleton(); // Safe creation
                    }
                }
            }
            return instance;
        }
    }

    // --- Approach 5: Bill Pugh Singleton (Static Inner Class) --- RECOMMENDED ---
    // Lazy initialization WITHOUT synchronization overhead.
    // The inner class is loaded only when getInstance() is first called.
    // JVM guarantees class loading is thread-safe.
    static class BillPughSingleton {
        private BillPughSingleton() {}

        // Inner class is not loaded until getInstance() is called
        private static class SingletonHelper {
            // JVM handles thread safety during class initialization
            private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        }

        public static BillPughSingleton getInstance() {
            return SingletonHelper.INSTANCE; // triggers inner class loading
        }

        public void show() { System.out.println("BillPugh Singleton: " + hashCode()); }
    }

    // --- Approach 6: Enum Singleton --- BEST PRACTICE (Josh Bloch recommendation) ---
    // Handles: thread safety, serialization, reflection attacks, clone attacks
    // Java guarantees enums are instantiated exactly once.
    enum EnumSingleton {
        INSTANCE; // The single instance

        private int value = 0;

        public void setValue(int v) { this.value = v; }
        public int getValue() { return value; }
        public void show() { System.out.println("EnumSingleton: " + hashCode()); }
    }

    static void demonstrateSingleton() {
        System.out.println("--- SINGLETON PATTERN ---");

        // Eager Singleton
        EagerSingleton e1 = EagerSingleton.getInstance();
        EagerSingleton e2 = EagerSingleton.getInstance();
        System.out.println("Eager: same instance? " + (e1 == e2)); // true

        // Bill Pugh Singleton
        BillPughSingleton b1 = BillPughSingleton.getInstance();
        BillPughSingleton b2 = BillPughSingleton.getInstance();
        System.out.println("BillPugh: same instance? " + (b1 == b2)); // true

        // Enum Singleton
        EnumSingleton.INSTANCE.setValue(42);
        System.out.println("Enum Singleton value: " + EnumSingleton.INSTANCE.getValue()); // 42
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 2. FACTORY METHOD PATTERN
    // -------------------------------------------------------------------------
    // Intent: Define an interface for creating an object, but let subclasses
    //         decide which class to instantiate.
    //
    // "Define an interface for creating an object, but let SUBCLASSES decide"
    //
    // Use cases: logging frameworks (different log writers), UI toolkits,
    //            creating different shapes, different payment methods
    //
    // Difference from Abstract Factory:
    //   - Factory Method: ONE product family, subclass decides
    //   - Abstract Factory: MULTIPLE product families, factory decides
    // -------------------------------------------------------------------------

    // Product interface
    interface Shape {
        void draw();
        double area();
    }

    // Concrete products
    static class Circle implements Shape {
        private double radius;
        Circle(double radius) { this.radius = radius; }

        @Override
        public void draw() { System.out.println("Drawing Circle with radius: " + radius); }

        @Override
        public double area() { return Math.PI * radius * radius; }
    }

    static class Rectangle implements Shape {
        private double width, height;
        Rectangle(double w, double h) { this.width = w; this.height = h; }

        @Override
        public void draw() { System.out.println("Drawing Rectangle " + width + "x" + height); }

        @Override
        public double area() { return width * height; }
    }

    static class Triangle implements Shape {
        private double base, height;
        Triangle(double b, double h) { this.base = b; this.height = h; }

        @Override
        public void draw() { System.out.println("Drawing Triangle base=" + base + " height=" + height); }

        @Override
        public double area() { return 0.5 * base * height; }
    }

    // Factory class - centralized object creation
    static class ShapeFactory {
        // Static factory method - client doesn't need to know concrete classes
        public static Shape createShape(String type) {
            switch (type.toLowerCase()) {
                case "circle":    return new Circle(5.0);
                case "rectangle": return new Rectangle(4.0, 6.0);
                case "triangle":  return new Triangle(3.0, 4.0);
                default:
                    throw new IllegalArgumentException("Unknown shape: " + type);
            }
        }

        // Overloaded factory with parameters
        public static Shape createCircle(double radius) { return new Circle(radius); }
        public static Shape createRectangle(double w, double h) { return new Rectangle(w, h); }
    }

    static void demonstrateFactory() {
        System.out.println("--- FACTORY METHOD PATTERN ---");

        Shape s1 = ShapeFactory.createShape("circle");
        Shape s2 = ShapeFactory.createShape("rectangle");
        Shape s3 = ShapeFactory.createShape("triangle");

        s1.draw();
        System.out.println("Circle area: " + String.format("%.2f", s1.area()));
        s2.draw();
        s3.draw();
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 3. ABSTRACT FACTORY PATTERN
    // -------------------------------------------------------------------------
    // Intent: Provide an interface for creating FAMILIES of related or dependent
    //         objects without specifying their concrete classes.
    //
    // Think of it as: "factory of factories"
    //
    // Example: UI toolkit that needs to create Buttons + TextFields for
    //          different OS themes (Windows, Mac, Linux)
    //
    // Use cases: Cross-platform UI, Database drivers (MySQL vs Oracle),
    //            Skinnable applications
    // -------------------------------------------------------------------------

    // Abstract product interfaces
    interface Button {
        void render();
        void onClick();
    }

    interface TextField {
        void render();
        String getText();
    }

    // Windows family
    static class WindowsButton implements Button {
        @Override public void render() { System.out.println("[Windows] Rendering Windows Button"); }
        @Override public void onClick() { System.out.println("[Windows] Button clicked!"); }
    }

    static class WindowsTextField implements TextField {
        @Override public void render() { System.out.println("[Windows] Rendering Windows TextField"); }
        @Override public String getText() { return "Windows input text"; }
    }

    // Mac family
    static class MacButton implements Button {
        @Override public void render() { System.out.println("[Mac] Rendering Mac Button"); }
        @Override public void onClick() { System.out.println("[Mac] Button clicked!"); }
    }

    static class MacTextField implements TextField {
        @Override public void render() { System.out.println("[Mac] Rendering Mac TextField"); }
        @Override public String getText() { return "Mac input text"; }
    }

    // Abstract Factory interface
    interface UIFactory {
        Button createButton();
        TextField createTextField();
    }

    // Concrete factories
    static class WindowsUIFactory implements UIFactory {
        @Override public Button createButton()       { return new WindowsButton(); }
        @Override public TextField createTextField() { return new WindowsTextField(); }
    }

    static class MacUIFactory implements UIFactory {
        @Override public Button createButton()       { return new MacButton(); }
        @Override public TextField createTextField() { return new MacTextField(); }
    }

    // Client code - works with any factory (Open/Closed Principle)
    static class Application {
        private Button button;
        private TextField textField;

        Application(UIFactory factory) {
            // Client doesn't know which concrete objects are created
            this.button    = factory.createButton();
            this.textField = factory.createTextField();
        }

        void render() {
            button.render();
            textField.render();
        }
    }

    static void demonstrateAbstractFactory() {
        System.out.println("--- ABSTRACT FACTORY PATTERN ---");

        System.out.println("Running on Windows:");
        Application winApp = new Application(new WindowsUIFactory());
        winApp.render();

        System.out.println("Running on Mac:");
        Application macApp = new Application(new MacUIFactory());
        macApp.render();
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 4. BUILDER PATTERN
    // -------------------------------------------------------------------------
    // Intent: Separate the construction of a complex object from its
    //         representation, so the same process can create different reps.
    //
    // Problem it solves:
    //   - "Telescoping constructor anti-pattern": too many constructor overloads
    //   - Immutable objects with many optional fields
    //   - Readable and maintainable object creation
    //
    // Use cases: Creating complex objects like HTTP requests, SQL queries,
    //            StringBuilder, Lombok @Builder, Person with many fields
    //
    // Key points:
    //   - Builder is often a static inner class
    //   - Fluent API: method chaining (returns this)
    //   - build() creates the final immutable object
    // -------------------------------------------------------------------------

    static class Person {
        // Immutable fields - set only through builder
        private final String firstName;   // required
        private final String lastName;    // required
        private final int age;            // required
        private final String email;       // optional
        private final String phone;       // optional
        private final String address;     // optional
        private final String occupation;  // optional

        // Private constructor - only Builder can create Person
        private Person(Builder builder) {
            this.firstName  = builder.firstName;
            this.lastName   = builder.lastName;
            this.age        = builder.age;
            this.email      = builder.email;
            this.phone      = builder.phone;
            this.address    = builder.address;
            this.occupation = builder.occupation;
        }

        // Static factory method to get a Builder
        public static Builder builder(String firstName, String lastName, int age) {
            return new Builder(firstName, lastName, age);
        }

        @Override
        public String toString() {
            return "Person{name='" + firstName + " " + lastName + "', age=" + age
                    + (email != null ? ", email='" + email + "'" : "")
                    + (phone != null ? ", phone='" + phone + "'" : "")
                    + (address != null ? ", address='" + address + "'" : "")
                    + (occupation != null ? ", occupation='" + occupation + "'" : "")
                    + "}";
        }

        // Static nested Builder class
        static class Builder {
            // Required parameters
            private final String firstName;
            private final String lastName;
            private final int age;

            // Optional parameters (with defaults)
            private String email;
            private String phone;
            private String address;
            private String occupation;

            private Builder(String firstName, String lastName, int age) {
                this.firstName = firstName;
                this.lastName  = lastName;
                this.age       = age;
            }

            // Fluent setter methods (return Builder for chaining)
            public Builder email(String email) {
                this.email = email;
                return this; // enables method chaining
            }

            public Builder phone(String phone) {
                this.phone = phone;
                return this;
            }

            public Builder address(String address) {
                this.address = address;
                return this;
            }

            public Builder occupation(String occupation) {
                this.occupation = occupation;
                return this;
            }

            // Terminal method: validate and create the object
            public Person build() {
                // Validation before building
                if (firstName == null || firstName.isEmpty()) {
                    throw new IllegalStateException("First name cannot be empty");
                }
                if (age < 0 || age > 150) {
                    throw new IllegalStateException("Invalid age: " + age);
                }
                return new Person(this);
            }
        }
    }

    static void demonstrateBuilder() {
        System.out.println("--- BUILDER PATTERN ---");

        // Fluent API: only set what you need
        Person person1 = Person.builder("John", "Doe", 30)
                .email("john@example.com")
                .phone("555-1234")
                .occupation("Engineer")
                .build();

        // Minimal creation: only required fields
        Person person2 = Person.builder("Jane", "Smith", 25)
                .build();

        System.out.println(person1);
        System.out.println(person2);
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 5. PROTOTYPE PATTERN
    // -------------------------------------------------------------------------
    // Intent: Create new objects by COPYING (cloning) an existing object
    //         rather than creating from scratch.
    //
    // When to use:
    //   - Object creation is expensive (DB query, network call)
    //   - Need multiple similar objects with minor differences
    //   - Independent copies that can be modified without affecting original
    //
    // Two types:
    //   - Shallow Copy: copies references (not the objects they point to)
    //   - Deep Copy: copies everything, including nested objects
    //
    // Java's clone() mechanism:
    //   - implement Cloneable interface (marker interface)
    //   - override clone() method from Object
    //   - Default Object.clone() does SHALLOW copy
    // -------------------------------------------------------------------------

    // Shallow copy example
    static class ShallowCopyExample implements Cloneable {
        private String name;
        private List<String> hobbies; // reference type - will be shared in shallow copy

        ShallowCopyExample(String name, List<String> hobbies) {
            this.name    = name;
            this.hobbies = hobbies;
        }

        // Shallow clone: name is copied, but hobbies list is SHARED
        @Override
        public ShallowCopyExample clone() {
            try {
                return (ShallowCopyExample) super.clone(); // shallow copy
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public void addHobby(String h) { hobbies.add(h); }
        public String toString() { return "ShallowCopy{name='" + name + "', hobbies=" + hobbies + "}"; }
    }

    // Deep copy example
    static class Address implements Cloneable {
        String city;
        String country;

        Address(String city, String country) {
            this.city    = city;
            this.country = country;
        }

        @Override
        public Address clone() {
            try {
                return (Address) super.clone(); // primitives/strings are safe
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public String toString() { return city + ", " + country; }
    }

    static class DeepCopyExample implements Cloneable {
        private String name;
        private Address address; // nested object - needs deep copy

        DeepCopyExample(String name, Address address) {
            this.name    = name;
            this.address = address;
        }

        // Deep clone: clone the nested Address object too
        @Override
        public DeepCopyExample clone() {
            try {
                DeepCopyExample copy = (DeepCopyExample) super.clone();
                copy.address = this.address.clone(); // manually deep copy nested object
                return copy;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public String toString() { return "DeepCopy{name='" + name + "', address=" + address + "}"; }
    }

    static void demonstratePrototype() {
        System.out.println("--- PROTOTYPE PATTERN ---");

        // Shallow copy demo
        List<String> hobbies = new ArrayList<>();
        hobbies.add("Reading");
        ShallowCopyExample original = new ShallowCopyExample("Alice", hobbies);
        ShallowCopyExample shallowClone = original.clone();

        shallowClone.addHobby("Swimming"); // modifies SHARED list - affects original too!
        System.out.println("Original (affected by shallow clone): " + original);
        System.out.println("Shallow clone: " + shallowClone);

        // Deep copy demo
        Address addr = new Address("New York", "USA");
        DeepCopyExample orig2 = new DeepCopyExample("Bob", addr);
        DeepCopyExample deepClone = orig2.clone();

        deepClone.address.city = "Los Angeles"; // modifies CLONED object - original unaffected
        System.out.println("Original (unaffected): " + orig2);
        System.out.println("Deep clone: " + deepClone);
        System.out.println();
    }


    // =========================================================================
    // STRUCTURAL PATTERNS
    // =========================================================================


    // -------------------------------------------------------------------------
    // 6. ADAPTER PATTERN
    // -------------------------------------------------------------------------
    // Intent: Convert the interface of a class into another interface that
    //         clients expect. Makes incompatible interfaces work together.
    //
    // Real-world analogy: Power adapter (US plug -> European socket)
    //
    // Use cases:
    //   - Integrating legacy code with new systems
    //   - Third-party library with different interface
    //   - Java: Arrays.asList(), InputStreamReader (adapts InputStream to Reader)
    //
    // Two types:
    //   - Class Adapter: uses inheritance (extends legacy class)
    //   - Object Adapter: uses composition (wraps legacy class) -- PREFERRED
    // -------------------------------------------------------------------------

    // Target interface (what our code expects)
    interface ModernPrinter {
        void printDocument(String content);
        void printPDF(String filePath);
    }

    // Adaptee: Legacy class with incompatible interface
    static class LegacyPrinter {
        public void print(String text) {
            System.out.println("[LegacyPrinter] Printing: " + text);
        }

        public void printFile(String path) {
            System.out.println("[LegacyPrinter] Printing file from: " + path);
        }
    }

    // Adapter: wraps LegacyPrinter and implements ModernPrinter interface
    static class PrinterAdapter implements ModernPrinter {
        private LegacyPrinter legacyPrinter; // composition (Object Adapter)

        PrinterAdapter(LegacyPrinter legacyPrinter) {
            this.legacyPrinter = legacyPrinter;
        }

        @Override
        public void printDocument(String content) {
            // Translate new interface call to legacy method call
            legacyPrinter.print(content);
        }

        @Override
        public void printPDF(String filePath) {
            legacyPrinter.printFile(filePath);
        }
    }

    static void demonstrateAdapter() {
        System.out.println("--- ADAPTER PATTERN ---");

        // Client works with ModernPrinter interface
        LegacyPrinter legacy = new LegacyPrinter();
        ModernPrinter adapter = new PrinterAdapter(legacy);

        // Client uses modern interface, adapter translates to legacy calls
        adapter.printDocument("Hello World document");
        adapter.printPDF("/docs/report.pdf");
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 7. DECORATOR PATTERN
    // -------------------------------------------------------------------------
    // Intent: Attach additional responsibilities to an object DYNAMICALLY.
    //         An alternative to subclassing for extending functionality.
    //
    // Key insight: Wraps the original object, adds behavior, delegates core work
    //
    // Classic example: Coffee shop (Coffee + Milk + Sugar + Whip)
    // Java I/O is a classic Decorator:
    //   new BufferedReader(new InputStreamReader(new FileInputStream("file.txt")))
    //
    // Difference from inheritance:
    //   - Inheritance: static (compile-time), rigid hierarchy
    //   - Decorator: dynamic (runtime), flexible, composable
    // -------------------------------------------------------------------------

    // Component interface
    interface Coffee {
        String getDescription();
        double getCost();
    }

    // Concrete component (base object)
    static class SimpleCoffee implements Coffee {
        @Override
        public String getDescription() { return "Simple Coffee"; }

        @Override
        public double getCost() { return 1.00; }
    }

    // Abstract decorator - implements same interface AND wraps a Coffee object
    static abstract class CoffeeDecorator implements Coffee {
        protected Coffee coffee; // wrapped component

        CoffeeDecorator(Coffee coffee) { this.coffee = coffee; }

        // Delegate to wrapped component
        @Override
        public String getDescription() { return coffee.getDescription(); }

        @Override
        public double getCost() { return coffee.getCost(); }
    }

    // Concrete decorators - add specific behaviors
    static class MilkDecorator extends CoffeeDecorator {
        MilkDecorator(Coffee coffee) { super(coffee); }

        @Override
        public String getDescription() {
            return coffee.getDescription() + ", Milk"; // add to description
        }

        @Override
        public double getCost() {
            return coffee.getCost() + 0.25; // add to cost
        }
    }

    static class SugarDecorator extends CoffeeDecorator {
        SugarDecorator(Coffee coffee) { super(coffee); }

        @Override
        public String getDescription() { return coffee.getDescription() + ", Sugar"; }

        @Override
        public double getCost()        { return coffee.getCost() + 0.10; }
    }

    static class WhipDecorator extends CoffeeDecorator {
        WhipDecorator(Coffee coffee) { super(coffee); }

        @Override
        public String getDescription() { return coffee.getDescription() + ", Whip"; }

        @Override
        public double getCost()        { return coffee.getCost() + 0.50; }
    }

    static void demonstrateDecorator() {
        System.out.println("--- DECORATOR PATTERN ---");

        Coffee coffee = new SimpleCoffee();
        System.out.println(coffee.getDescription() + " -> $" + coffee.getCost());

        // Dynamically wrap with decorators (order doesn't matter functionally)
        coffee = new MilkDecorator(coffee);
        System.out.println(coffee.getDescription() + " -> $" + coffee.getCost());

        coffee = new SugarDecorator(coffee);
        System.out.println(coffee.getDescription() + " -> $" + coffee.getCost());

        coffee = new WhipDecorator(coffee);
        System.out.println(coffee.getDescription() + " -> $" + coffee.getCost());

        // Can add multiple decorators of the same type
        coffee = new MilkDecorator(coffee); // double milk
        System.out.println(coffee.getDescription() + " -> $" + coffee.getCost());
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 8. FACADE PATTERN
    // -------------------------------------------------------------------------
    // Intent: Provide a SIMPLIFIED interface to a complex subsystem.
    //         Hides complexity behind a simple facade.
    //
    // Real-world analogy: Car ignition key (hides engine, fuel, electronics)
    //
    // Use cases:
    //   - Simplifying library/framework usage
    //   - Layered architecture (Service layer as facade over Repository/DAO)
    //   - Home theater system (one "watch movie" button)
    //
    // Difference from Adapter: Adapter changes interface, Facade simplifies it
    // -------------------------------------------------------------------------

    // Complex subsystem classes
    static class DVDPlayer {
        public void on()          { System.out.println("  [DVD] Player ON"); }
        public void play(String m){ System.out.println("  [DVD] Playing: " + m); }
        public void off()         { System.out.println("  [DVD] Player OFF"); }
    }

    static class Projector {
        public void on()          { System.out.println("  [Projector] ON"); }
        public void widescreen()  { System.out.println("  [Projector] Set widescreen mode"); }
        public void off()         { System.out.println("  [Projector] OFF"); }
    }

    static class SurroundSound {
        public void on()          { System.out.println("  [Sound] System ON"); }
        public void setVolume(int v) { System.out.println("  [Sound] Volume set to " + v); }
        public void off()         { System.out.println("  [Sound] System OFF"); }
    }

    static class Lights {
        public void dim(int level) { System.out.println("  [Lights] Dimmed to " + level + "%"); }
        public void on()           { System.out.println("  [Lights] Lights ON"); }
    }

    // FACADE: Simple interface over complex subsystem
    static class HomeTheaterFacade {
        private DVDPlayer    dvd;
        private Projector    projector;
        private SurroundSound sound;
        private Lights       lights;

        HomeTheaterFacade(DVDPlayer d, Projector p, SurroundSound s, Lights l) {
            this.dvd = d; this.projector = p; this.sound = s; this.lights = l;
        }

        // One simple method hides all the complexity
        public void watchMovie(String movie) {
            System.out.println("[Facade] Getting ready to watch: " + movie);
            lights.dim(20);
            projector.on();
            projector.widescreen();
            sound.on();
            sound.setVolume(8);
            dvd.on();
            dvd.play(movie);
        }

        public void endMovie() {
            System.out.println("[Facade] Shutting down theater...");
            dvd.off();
            sound.off();
            projector.off();
            lights.on();
        }
    }

    static void demonstrateFacade() {
        System.out.println("--- FACADE PATTERN ---");

        HomeTheaterFacade theater = new HomeTheaterFacade(
                new DVDPlayer(), new Projector(), new SurroundSound(), new Lights());

        theater.watchMovie("Inception");
        System.out.println("... watching movie ...");
        theater.endMovie();
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 9. PROXY PATTERN
    // -------------------------------------------------------------------------
    // Intent: Provide a surrogate/placeholder for another object to control
    //         access to it.
    //
    // Types of Proxy:
    //   - Virtual Proxy: Lazy initialization (expensive object created on demand)
    //   - Protection Proxy: Access control
    //   - Remote Proxy: Object in different address space (RMI, web service)
    //   - Logging Proxy: Log method calls
    //   - Caching Proxy: Cache results
    //
    // Java usage: Spring AOP uses JDK Dynamic Proxy or CGLIB proxy
    //   - JDK Proxy: for interface-based proxies (java.lang.reflect.Proxy)
    //   - CGLIB: for class-based proxies (bytecode generation)
    // -------------------------------------------------------------------------

    // Subject interface
    interface DatabaseService {
        String query(String sql);
        void execute(String sql);
    }

    // Real subject (expensive to create)
    static class RealDatabaseService implements DatabaseService {
        public RealDatabaseService() {
            // Simulate expensive initialization (DB connection)
            System.out.println("  [DB] Connecting to database...");
        }

        @Override
        public String query(String sql) {
            System.out.println("  [DB] Executing query: " + sql);
            return "ResultSet{rows=42}";
        }

        @Override
        public void execute(String sql) {
            System.out.println("  [DB] Executing statement: " + sql);
        }
    }

    // Proxy: controls access + adds logging + lazy initialization
    static class DatabaseProxy implements DatabaseService {
        private RealDatabaseService realService; // lazily initialized
        private String user;

        DatabaseProxy(String user) {
            this.user = user;
            // NOTE: realService is NOT created yet (lazy)
        }

        private RealDatabaseService getService() {
            if (realService == null) {
                realService = new RealDatabaseService(); // lazy init
            }
            return realService;
        }

        @Override
        public String query(String sql) {
            System.out.println("  [Proxy] User '" + user + "' requesting query");
            System.out.println("  [Proxy] Logging: query called with: " + sql);
            String result = getService().query(sql); // delegate to real service
            System.out.println("  [Proxy] Logging: query result obtained");
            return result;
        }

        @Override
        public void execute(String sql) {
            // Access control: only admin can execute write operations
            if (!user.equals("admin")) {
                throw new SecurityException("User '" + user + "' is not authorized to execute!");
            }
            System.out.println("  [Proxy] Authorization check passed for: " + user);
            getService().execute(sql);
        }
    }

    static void demonstrateProxy() {
        System.out.println("--- PROXY PATTERN ---");

        // Admin proxy
        DatabaseService adminProxy = new DatabaseProxy("admin");
        String result = adminProxy.query("SELECT * FROM users");
        System.out.println("  Query result: " + result);
        adminProxy.execute("INSERT INTO logs VALUES(...)");

        // Non-admin proxy
        DatabaseService userProxy = new DatabaseProxy("bob");
        userProxy.query("SELECT name FROM products");
        try {
            userProxy.execute("DROP TABLE users"); // should throw
        } catch (SecurityException e) {
            System.out.println("  Access denied: " + e.getMessage());
        }
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 10. COMPOSITE PATTERN
    // -------------------------------------------------------------------------
    // Intent: Compose objects into tree structures to represent part-whole
    //         hierarchies. Lets clients treat individual objects and compositions
    //         uniformly.
    //
    // Classic example: File system (File and Folder both implement FileSystem)
    // Other examples: HTML DOM, org charts, GUI component trees, XML parsing
    //
    // Key insight: Both leaf and composite implement the SAME interface
    // -------------------------------------------------------------------------

    // Component interface - both File and Folder implement this
    interface FileSystemComponent {
        String getName();
        long getSize();
        void display(String indent);
    }

    // Leaf: File has no children
    static class File implements FileSystemComponent {
        private String name;
        private long size;

        File(String name, long size) {
            this.name = name;
            this.size = size;
        }

        @Override public String getName()          { return name; }
        @Override public long getSize()            { return size; }
        @Override public void display(String indent) {
            System.out.println(indent + "- File: " + name + " (" + size + " bytes)");
        }
    }

    // Composite: Folder can contain files and other folders
    static class Folder implements FileSystemComponent {
        private String name;
        private List<FileSystemComponent> children = new ArrayList<>();

        Folder(String name) { this.name = name; }

        public void add(FileSystemComponent component)    { children.add(component); }
        public void remove(FileSystemComponent component) { children.remove(component); }

        @Override public String getName() { return name; }

        @Override
        public long getSize() {
            // Recursively sum sizes of all children
            return children.stream().mapToLong(FileSystemComponent::getSize).sum();
        }

        @Override
        public void display(String indent) {
            System.out.println(indent + "+ Folder: " + name + " (" + getSize() + " bytes)");
            // Recursively display children
            for (FileSystemComponent child : children) {
                child.display(indent + "  "); // increase indent for children
            }
        }
    }

    static void demonstrateComposite() {
        System.out.println("--- COMPOSITE PATTERN ---");

        // Build file system tree
        Folder root = new Folder("root");
        root.add(new File("readme.txt", 1024));
        root.add(new File("config.yaml", 512));

        Folder src = new Folder("src");
        src.add(new File("Main.java", 4096));
        src.add(new File("Helper.java", 2048));

        Folder test = new Folder("test");
        test.add(new File("MainTest.java", 3072));

        src.add(test); // nested folder
        root.add(src);

        Folder docs = new Folder("docs");
        docs.add(new File("design.pdf", 102400));
        root.add(docs);

        // Client treats File and Folder uniformly
        root.display("");
        System.out.println("Total size: " + root.getSize() + " bytes");
        System.out.println();
    }


    // =========================================================================
    // BEHAVIORAL PATTERNS
    // =========================================================================


    // -------------------------------------------------------------------------
    // 11. STRATEGY PATTERN
    // -------------------------------------------------------------------------
    // Intent: Define a family of algorithms, encapsulate each one, and make
    //         them interchangeable. Strategy lets the algorithm vary
    //         independently from the clients that use it.
    //
    // Use cases: Sorting algorithms, payment methods, compression algorithms,
    //            authentication strategies, tax calculation
    //
    // Key insight: "Favor composition over inheritance"
    //   - Instead of subclassing for behavior, inject the behavior
    //
    // Java examples: Comparator is a Strategy pattern!
    //   list.sort((a, b) -> a.compareTo(b)) -- you're injecting a sorting strategy
    // -------------------------------------------------------------------------

    // Strategy interface
    interface SortingStrategy {
        void sort(int[] arr);
        String getName();
    }

    // Concrete strategies
    static class BubbleSortStrategy implements SortingStrategy {
        @Override
        public void sort(int[] arr) {
            // O(n^2) - simple but slow for large datasets
            int n = arr.length;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (arr[j] > arr[j + 1]) {
                        int tmp = arr[j]; arr[j] = arr[j+1]; arr[j+1] = tmp;
                    }
                }
            }
        }

        @Override public String getName() { return "Bubble Sort O(n^2)"; }
    }

    static class QuickSortStrategy implements SortingStrategy {
        @Override
        public void sort(int[] arr) {
            quickSort(arr, 0, arr.length - 1);
        }

        private void quickSort(int[] arr, int low, int high) {
            if (low < high) {
                int pivot = arr[high];
                int i = low - 1;
                for (int j = low; j < high; j++) {
                    if (arr[j] <= pivot) {
                        i++;
                        int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
                    }
                }
                int tmp = arr[i+1]; arr[i+1] = arr[high]; arr[high] = tmp;
                int pi = i + 1;
                quickSort(arr, low, pi - 1);
                quickSort(arr, pi + 1, high);
            }
        }

        @Override public String getName() { return "Quick Sort O(n log n) avg"; }
    }

    // Context class that uses a strategy
    static class Sorter {
        private SortingStrategy strategy;

        Sorter(SortingStrategy strategy) { this.strategy = strategy; }

        // Strategy can be changed at runtime!
        public void setStrategy(SortingStrategy strategy) { this.strategy = strategy; }

        public void sort(int[] arr) {
            System.out.println("  Using: " + strategy.getName());
            strategy.sort(arr);
        }
    }

    static void demonstrateStrategy() {
        System.out.println("--- STRATEGY PATTERN ---");

        int[] arr1 = {64, 34, 25, 12, 22, 11, 90};
        int[] arr2 = {64, 34, 25, 12, 22, 11, 90};

        Sorter sorter = new Sorter(new BubbleSortStrategy());
        sorter.sort(arr1);
        System.out.print("  Sorted: ");
        for (int n : arr1) System.out.print(n + " ");
        System.out.println();

        // Switch strategy at runtime
        sorter.setStrategy(new QuickSortStrategy());
        sorter.sort(arr2);
        System.out.print("  Sorted: ");
        for (int n : arr2) System.out.print(n + " ");
        System.out.println();
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 12. OBSERVER PATTERN
    // -------------------------------------------------------------------------
    // Intent: Define a one-to-many dependency between objects so that when
    //         one object changes state, all its dependents are notified
    //         automatically.
    //
    // Also known as: Publish-Subscribe (Pub-Sub), Event Listener
    //
    // Use cases:
    //   - Event handling systems (GUI buttons)
    //   - News feeds / social media notifications
    //   - Stock price updates
    //   - Spring ApplicationEvent/EventListener
    //   - Java's java.util.Observer (deprecated in Java 9)
    //
    // Java built-in: PropertyChangeListener, Observer/Observable (deprecated)
    // -------------------------------------------------------------------------

    // Observer interface
    interface EventObserver {
        void update(String eventType, Object data);
    }

    // Subject (Observable)
    static class EventManager {
        private List<EventObserver> observers = new ArrayList<>();
        private String name;

        EventManager(String name) { this.name = name; }

        public void subscribe(EventObserver observer)    { observers.add(observer); }
        public void unsubscribe(EventObserver observer)  { observers.remove(observer); }

        // Notify all observers
        public void notifyObservers(String eventType, Object data) {
            System.out.println("  [" + name + "] Publishing event: " + eventType);
            for (EventObserver observer : observers) {
                observer.update(eventType, data);
            }
        }
    }

    // Concrete observers
    static class EmailNotifier implements EventObserver {
        private String email;
        EmailNotifier(String email) { this.email = email; }

        @Override
        public void update(String eventType, Object data) {
            System.out.println("  [Email -> " + email + "] Event: " + eventType + ", Data: " + data);
        }
    }

    static class SMSNotifier implements EventObserver {
        private String phone;
        SMSNotifier(String phone) { this.phone = phone; }

        @Override
        public void update(String eventType, Object data) {
            System.out.println("  [SMS -> " + phone + "] Event: " + eventType + ", Data: " + data);
        }
    }

    static class LogObserver implements EventObserver {
        @Override
        public void update(String eventType, Object data) {
            System.out.println("  [LOG] " + java.time.LocalTime.now() + " - " + eventType + ": " + data);
        }
    }

    // Publisher that uses EventManager
    static class OrderService {
        private EventManager events = new EventManager("OrderService");

        public EventManager getEvents() { return events; }

        public void placeOrder(String orderId, double amount) {
            System.out.println("  [OrderService] Placing order: " + orderId);
            // Business logic here...
            events.notifyObservers("ORDER_PLACED", "Order#" + orderId + " for $" + amount);
        }

        public void cancelOrder(String orderId) {
            System.out.println("  [OrderService] Cancelling order: " + orderId);
            events.notifyObservers("ORDER_CANCELLED", "Order#" + orderId);
        }
    }

    static void demonstrateObserver() {
        System.out.println("--- OBSERVER PATTERN ---");

        OrderService orderService = new OrderService();

        // Subscribe observers
        orderService.getEvents().subscribe(new EmailNotifier("customer@example.com"));
        orderService.getEvents().subscribe(new SMSNotifier("+1-555-9999"));
        orderService.getEvents().subscribe(new LogObserver());

        // Trigger events
        orderService.placeOrder("ORD-001", 99.99);
        orderService.cancelOrder("ORD-001");
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 13. TEMPLATE METHOD PATTERN
    // -------------------------------------------------------------------------
    // Intent: Define the SKELETON of an algorithm in a base class, deferring
    //         some steps to subclasses. Subclasses can redefine certain steps
    //         without changing the algorithm's structure.
    //
    // "Hollywood Principle": Don't call us, we'll call you.
    //   The base class calls the subclass methods (inversion of control)
    //
    // Use cases:
    //   - Data mining (read -> process -> analyze -> report)
    //   - Game frameworks (initialize -> start -> play -> end)
    //   - Spring: JdbcTemplate, AbstractController
    //   - Servlet lifecycle (init -> service -> destroy)
    //
    // Difference from Strategy:
    //   - Template Method: uses INHERITANCE, algorithm defined once in base class
    //   - Strategy: uses COMPOSITION, algorithm swapped at runtime
    // -------------------------------------------------------------------------

    // Abstract class defines the template
    static abstract class DataExporter {
        // TEMPLATE METHOD: defines the algorithm skeleton
        // final: subclasses cannot change the overall algorithm
        public final void export(String destination) {
            System.out.println("  [Exporter] Starting export to: " + destination);
            readData();          // step 1: read
            processData();       // step 2: process (can be overridden)
            formatData();        // step 3: format (must be implemented by subclass)
            writeToDestination(destination); // step 4: write
            System.out.println("  [Exporter] Export complete!");
        }

        protected void readData() {
            System.out.println("  [Exporter] Reading data from source (default impl)");
        }

        // Hook method: optional override (has default behavior)
        protected void processData() {
            System.out.println("  [Exporter] Processing data (default: no-op)");
        }

        // Abstract methods: MUST be implemented by subclasses
        protected abstract void formatData();
        protected abstract void writeToDestination(String destination);
    }

    // Concrete subclass: CSV exporter
    static class CsvExporter extends DataExporter {
        @Override
        protected void formatData() {
            System.out.println("  [CSV] Formatting data as CSV");
        }

        @Override
        protected void writeToDestination(String destination) {
            System.out.println("  [CSV] Writing CSV to: " + destination);
        }
    }

    // Concrete subclass: JSON exporter
    static class JsonExporter extends DataExporter {
        @Override
        protected void processData() {
            // Override hook to add custom processing
            System.out.println("  [JSON] Custom processing: sanitizing special characters");
        }

        @Override
        protected void formatData() {
            System.out.println("  [JSON] Formatting data as JSON");
        }

        @Override
        protected void writeToDestination(String destination) {
            System.out.println("  [JSON] Writing JSON to: " + destination);
        }
    }

    static void demonstrateTemplateMethod() {
        System.out.println("--- TEMPLATE METHOD PATTERN ---");

        DataExporter csvExporter = new CsvExporter();
        csvExporter.export("output.csv");

        System.out.println();

        DataExporter jsonExporter = new JsonExporter();
        jsonExporter.export("output.json");
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 14. COMMAND PATTERN
    // -------------------------------------------------------------------------
    // Intent: Encapsulate a REQUEST as an object, thereby letting you
    //         parameterize clients with different requests, queue or log
    //         requests, and support undoable operations.
    //
    // Key components:
    //   - Command interface: execute() and undo()
    //   - ConcreteCommand: implements the command
    //   - Receiver: the actual business logic object
    //   - Invoker: stores and calls commands
    //   - Client: creates commands
    //
    // Use cases:
    //   - Undo/Redo in text editors, IDEs
    //   - Task queues, job scheduling
    //   - Transaction logging
    //   - Macro recording
    // -------------------------------------------------------------------------

    // Command interface
    interface TextCommand {
        void execute();
        void undo();
        String getDescription();
    }

    // Receiver: the actual text editor
    static class TextEditor {
        private StringBuilder text = new StringBuilder();

        public void insertText(String str, int position) {
            text.insert(position, str);
        }

        public void deleteText(int start, int length) {
            text.delete(start, start + length);
        }

        public String getText() { return text.toString(); }
    }

    // Concrete commands
    static class InsertTextCommand implements TextCommand {
        private TextEditor editor;
        private String text;
        private int position;

        InsertTextCommand(TextEditor editor, String text, int position) {
            this.editor   = editor;
            this.text     = text;
            this.position = position;
        }

        @Override
        public void execute() {
            editor.insertText(text, position);
        }

        @Override
        public void undo() {
            editor.deleteText(position, text.length()); // reverse the insert
        }

        @Override
        public String getDescription() { return "Insert '" + text + "' at " + position; }
    }

    static class DeleteTextCommand implements TextCommand {
        private TextEditor editor;
        private int start;
        private int length;
        private String deletedText; // store for undo

        DeleteTextCommand(TextEditor editor, int start, int length) {
            this.editor = editor;
            this.start  = start;
            this.length = length;
        }

        @Override
        public void execute() {
            deletedText = editor.getText().substring(start, start + length);
            editor.deleteText(start, length);
        }

        @Override
        public void undo() {
            editor.insertText(deletedText, start); // restore deleted text
        }

        @Override
        public String getDescription() { return "Delete " + length + " chars at " + start; }
    }

    // Invoker: manages command history for undo/redo
    static class CommandHistory {
        private List<TextCommand> history = new ArrayList<>();
        private int currentIndex = -1;

        public void execute(TextCommand command) {
            // Remove any redo history if we branch
            while (history.size() > currentIndex + 1) {
                history.remove(history.size() - 1);
            }
            command.execute();
            history.add(command);
            currentIndex++;
            System.out.println("  [History] Executed: " + command.getDescription());
        }

        public void undo() {
            if (currentIndex >= 0) {
                TextCommand command = history.get(currentIndex);
                command.undo();
                currentIndex--;
                System.out.println("  [History] Undone: " + command.getDescription());
            } else {
                System.out.println("  [History] Nothing to undo");
            }
        }

        public void redo() {
            if (currentIndex < history.size() - 1) {
                currentIndex++;
                TextCommand command = history.get(currentIndex);
                command.execute();
                System.out.println("  [History] Redone: " + command.getDescription());
            } else {
                System.out.println("  [History] Nothing to redo");
            }
        }
    }

    static void demonstrateCommand() {
        System.out.println("--- COMMAND PATTERN (with Undo/Redo) ---");

        TextEditor editor   = new TextEditor();
        CommandHistory hist = new CommandHistory();

        hist.execute(new InsertTextCommand(editor, "Hello", 0));
        System.out.println("  Text: '" + editor.getText() + "'");

        hist.execute(new InsertTextCommand(editor, " World", 5));
        System.out.println("  Text: '" + editor.getText() + "'");

        hist.execute(new InsertTextCommand(editor, "!", 11));
        System.out.println("  Text: '" + editor.getText() + "'");

        hist.undo(); // undo insert "!"
        System.out.println("  Text after undo: '" + editor.getText() + "'");

        hist.undo(); // undo insert " World"
        System.out.println("  Text after undo: '" + editor.getText() + "'");

        hist.redo(); // redo insert " World"
        System.out.println("  Text after redo: '" + editor.getText() + "'");
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 15. CHAIN OF RESPONSIBILITY PATTERN
    // -------------------------------------------------------------------------
    // Intent: Avoid coupling the sender of a request to its receiver by giving
    //         more than one object a chance to handle it. Chain the objects and
    //         pass the request along the chain until handled.
    //
    // Use cases:
    //   - Servlet Filters (each filter processes request then passes to next)
    //   - Spring Security filter chain
    //   - Middleware pipelines
    //   - Exception handling hierarchies
    //   - Approval workflows (employee -> manager -> director)
    //   - Logging levels (DEBUG -> INFO -> WARN -> ERROR)
    // -------------------------------------------------------------------------

    // Request class
    static class SupportRequest {
        enum Level { BASIC, INTERMEDIATE, EXPERT }
        String description;
        Level level;

        SupportRequest(String description, Level level) {
            this.description = description;
            this.level       = level;
        }
    }

    // Abstract handler
    static abstract class SupportHandler {
        protected SupportHandler nextHandler; // next in chain

        public SupportHandler setNext(SupportHandler next) {
            this.nextHandler = next;
            return next; // allows chaining: h1.setNext(h2).setNext(h3)
        }

        public final void handle(SupportRequest request) {
            if (canHandle(request)) {
                process(request);
            } else if (nextHandler != null) {
                System.out.println("  [" + getLevel() + "] Cannot handle, passing to next...");
                nextHandler.handle(request); // pass along the chain
            } else {
                System.out.println("  No handler available for: " + request.description);
            }
        }

        protected abstract boolean canHandle(SupportRequest request);
        protected abstract void process(SupportRequest request);
        protected abstract String getLevel();
    }

    // Concrete handlers
    static class Level1Support extends SupportHandler {
        @Override
        protected boolean canHandle(SupportRequest r) {
            return r.level == SupportRequest.Level.BASIC;
        }

        @Override
        protected void process(SupportRequest r) {
            System.out.println("  [L1-Support] Handling basic request: " + r.description);
        }

        @Override protected String getLevel() { return "L1"; }
    }

    static class Level2Support extends SupportHandler {
        @Override
        protected boolean canHandle(SupportRequest r) {
            return r.level == SupportRequest.Level.INTERMEDIATE;
        }

        @Override
        protected void process(SupportRequest r) {
            System.out.println("  [L2-Support] Handling intermediate request: " + r.description);
        }

        @Override protected String getLevel() { return "L2"; }
    }

    static class Level3Support extends SupportHandler {
        @Override
        protected boolean canHandle(SupportRequest r) {
            return r.level == SupportRequest.Level.EXPERT;
        }

        @Override
        protected void process(SupportRequest r) {
            System.out.println("  [L3-Support/Expert] Handling expert request: " + r.description);
        }

        @Override protected String getLevel() { return "L3"; }
    }

    static void demonstrateChainOfResponsibility() {
        System.out.println("--- CHAIN OF RESPONSIBILITY PATTERN ---");

        // Build the chain: L1 -> L2 -> L3
        Level1Support l1 = new Level1Support();
        Level2Support l2 = new Level2Support();
        Level3Support l3 = new Level3Support();
        l1.setNext(l2).setNext(l3); // fluent chain setup

        // Different requests get handled by appropriate level
        l1.handle(new SupportRequest("Password reset",          SupportRequest.Level.BASIC));
        l1.handle(new SupportRequest("Software configuration",  SupportRequest.Level.INTERMEDIATE));
        l1.handle(new SupportRequest("Database corruption bug", SupportRequest.Level.EXPERT));
        System.out.println();
    }


    // -------------------------------------------------------------------------
    // 16. STATE PATTERN
    // -------------------------------------------------------------------------
    // Intent: Allow an object to alter its behavior when its internal state
    //         changes. The object will appear to change its class.
    //
    // Key insight: Instead of large if/else or switch on state, each state
    //              is a separate class with its own behavior.
    //
    // Use cases:
    //   - Traffic light controller
    //   - Vending machine states
    //   - Order states (PENDING -> CONFIRMED -> SHIPPED -> DELIVERED)
    //   - TCP connection states
    //   - Workflow engines
    //
    // Difference from Strategy:
    //   - Strategy: algorithm selected by client, usually stays same
    //   - State: state transitions managed internally, driven by events
    // -------------------------------------------------------------------------

    // State interface
    interface TrafficLightState {
        void handle(TrafficLight context);
        String getColor();
    }

    // Context class
    static class TrafficLight {
        private TrafficLightState currentState;
        private int cycleCount = 0;

        TrafficLight() {
            this.currentState = new RedState(); // initial state
        }

        public void setState(TrafficLightState state) {
            System.out.println("  [TrafficLight] Transitioning from "
                    + currentState.getColor() + " to " + state.getColor());
            this.currentState = state;
            cycleCount++;
        }

        public void trigger() {
            currentState.handle(this); // delegate to current state
        }

        public String getCurrentColor() { return currentState.getColor(); }
        public int getCycleCount()      { return cycleCount; }
    }

    // Concrete states - each knows what state comes next
    static class RedState implements TrafficLightState {
        @Override
        public void handle(TrafficLight context) {
            System.out.println("  [RED] Stop! Transitioning to GREEN...");
            context.setState(new GreenState()); // transition to next state
        }

        @Override public String getColor() { return "RED"; }
    }

    static class GreenState implements TrafficLightState {
        @Override
        public void handle(TrafficLight context) {
            System.out.println("  [GREEN] Go! Transitioning to YELLOW...");
            context.setState(new YellowState());
        }

        @Override public String getColor() { return "GREEN"; }
    }

    static class YellowState implements TrafficLightState {
        @Override
        public void handle(TrafficLight context) {
            System.out.println("  [YELLOW] Caution! Transitioning to RED...");
            context.setState(new RedState());
        }

        @Override public String getColor() { return "YELLOW"; }
    }

    static void demonstrateState() {
        System.out.println("--- STATE PATTERN ---");

        TrafficLight light = new TrafficLight();
        System.out.println("  Initial state: " + light.getCurrentColor());

        // Cycle through states
        for (int i = 0; i < 6; i++) {
            light.trigger();
        }

        System.out.println("  Total transitions: " + light.getCycleCount());
        System.out.println();
    }

}

/*
 * =============================================================================
 * QUICK REFERENCE: PATTERN SUMMARY TABLE
 * =============================================================================
 *
 * CREATIONAL PATTERNS:
 * +--------------------------------------------------------------------------+
 * | Pattern          | Intent                          | Key Java Example     |
 * +------------------+---------------------------------+---------------------|
 * | Singleton        | One instance globally           | Runtime.getRuntime() |
 * | Factory Method   | Subclass decides object type    | Calendar.getInstance |
 * | Abstract Factory | Family of related objects       | DocumentBuilderFactory|
 * | Builder          | Build complex objects step-by-step | StringBuilder      |
 * | Prototype        | Clone existing object           | Object.clone()       |
 * +--------------------------------------------------------------------------+
 *
 * STRUCTURAL PATTERNS:
 * +--------------------------------------------------------------------------+
 * | Pattern     | Intent                              | Key Java Example      |
 * +-------------+-------------------------------------+----------------------|
 * | Adapter     | Convert incompatible interfaces     | InputStreamReader     |
 * | Decorator   | Add behavior dynamically            | BufferedInputStream   |
 * | Facade      | Simplify complex subsystem          | JdbcTemplate          |
 * | Proxy       | Control access to object            | Spring AOP proxy      |
 * | Composite   | Tree structure, uniform treatment   | java.awt.Component    |
 * +--------------------------------------------------------------------------+
 *
 * BEHAVIORAL PATTERNS:
 * +--------------------------------------------------------------------------+
 * | Pattern             | Intent                        | Key Java Example    |
 * +---------------------+-------------------------------+--------------------|
 * | Strategy            | Swap algorithms at runtime    | Comparator          |
 * | Observer            | Notify dependents of changes  | EventListener       |
 * | Template Method     | Algorithm skeleton in base    | AbstractList        |
 * | Command             | Encapsulate requests          | Runnable, Callable  |
 * | Chain of Resp.      | Pass request along chain      | Servlet Filters     |
 * | State               | Behavior changes with state   | TCP connection      |
 * +--------------------------------------------------------------------------+
 *
 * =============================================================================
 * KEY INTERVIEW QUESTIONS:
 * =============================================================================
 *
 * Q1: What is the difference between Factory and Abstract Factory?
 * A:  Factory Method creates ONE product; Abstract Factory creates FAMILIES
 *     of related products. Abstract Factory uses multiple factory methods.
 *
 * Q2: When would you use Builder over Constructor?
 * A:  When you have 4+ parameters, many of which are optional. Builder avoids
 *     the "telescoping constructor" anti-pattern and creates immutable objects.
 *
 * Q3: What is the difference between Decorator and Inheritance?
 * A:  Inheritance is static (compile-time). Decorator is dynamic (runtime).
 *     You can stack multiple decorators. Decorator follows OCP (Open/Closed).
 *
 * Q4: Proxy vs Decorator - what's the difference?
 * A:  Decorator ADDS new behavior; Proxy CONTROLS access to existing behavior.
 *     Proxy often manages the lifecycle of the subject; Decorator does not.
 *
 * Q5: Strategy vs Template Method?
 * A:  Template Method uses inheritance (base class calls subclass methods).
 *     Strategy uses composition (inject the algorithm as an object). Strategy
 *     is more flexible (change at runtime); Template is simpler.
 *
 * Q6: Which design patterns does Spring Framework use?
 * A:  Singleton, Factory (BeanFactory), Proxy (AOP), Template (JdbcTemplate),
 *     Observer (ApplicationEvent), Decorator (BeanPostProcessor),
 *     Adapter (HandlerAdapter), Front Controller (DispatcherServlet)
 *
 * Q7: What is volatile and why is it needed in DCL Singleton?
 * A:  volatile ensures: (1) visibility - changes visible to all threads,
 *     (2) ordering - prevents instruction reordering by JIT/CPU.
 *     Without volatile, a thread may see a partially constructed object.
 *
 * Q8: Why is Enum Singleton the best implementation?
 * A:  1. Thread-safe (JVM handles it)
 *     2. Serialization safe (enum deserialization doesn't create new instances)
 *     3. Reflection safe (cannot use reflection to create new enum instances)
 *     4. Clone safe (enums cannot be cloned)
 *     Recommended by Joshua Bloch in Effective Java.
 * =============================================================================
 */
