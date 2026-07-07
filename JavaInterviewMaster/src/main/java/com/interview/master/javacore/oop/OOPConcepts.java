package com.interview.master.javacore.oop;

import java.util.Objects;

/**
 * ============================================================
 * OOP CONCEPTS - Complete Interview Guide
 * ============================================================
 *
 * Four Pillars of OOP:
 * 1. ENCAPSULATION  - Hiding internal state, exposing via methods (getters/setters)
 * 2. ABSTRACTION    - Hiding implementation details, showing only the interface/contract
 * 3. INHERITANCE    - Child class acquires parent class properties and behaviors
 * 4. POLYMORPHISM   - One name, many forms (compile-time: overloading, runtime: overriding)
 *
 * INTERVIEW TIP: Java is NOT 100% OOP because:
 *   - It has primitive types (int, char, etc.) that are not objects
 *   - Static methods belong to class, not objects
 *
 * ============================================================
 * TABLE OF CONTENTS:
 * 1.  Interfaces: Flyable, Swimmable
 * 2.  Abstract class: Animal
 * 3.  Concrete classes: Dog, Duck
 * 4.  Encapsulation: Person class
 * 5.  Polymorphism demo: method overloading, overriding
 * 6.  'this' and 'super' keywords
 * 7.  final keyword (class, method, variable)
 * 8.  static keyword (variable, method, block)
 * 9.  instanceof operator
 * 10. Object class methods: toString, equals, hashCode, clone
 * 11. Inner classes: static nested, inner, anonymous, local
 * 12. Enum with constructor, fields, methods
 * 13. Covariant return type
 * 14. Constructor chaining: this() and super()
 * 15. Main demo class: OOPConcepts
 * ============================================================
 */

// =============================================================================
// SECTION 1: INTERFACES
// =============================================================================

/**
 * INTERFACE - Pure abstraction contract (Java 8+ allows default and static methods)
 *
 * INTERVIEW Q: What is an interface?
 * A: An interface is a reference type that defines a contract (method signatures).
 *    All fields are implicitly public static final.
 *    All methods (pre-Java 8) are implicitly public abstract.
 *    Java 8 added: default methods (with body), static methods.
 *    Java 9 added: private methods (for helper logic inside interface).
 *
 * INTERVIEW Q: Interface vs Abstract class?
 * A: Interface - multiple inheritance, no constructor, all fields are constants,
 *               used when unrelated classes share behavior (e.g., Serializable, Comparable)
 *    Abstract  - single inheritance, has constructor, can have instance fields,
 *               used when related classes share a common base
 *
 * INTERVIEW Q: Can an interface extend another interface?
 * A: YES. An interface can extend multiple interfaces.
 *    e.g., interface C extends A, B {}
 *
 * INTERVIEW Q: Can we instantiate an interface?
 * A: NO directly. But YES via anonymous class or lambda (functional interface).
 */
interface Flyable {

    // Constant - implicitly public static final
    // INTERNAL: stored in the interface's class area, not per-object
    double MAX_ALTITUDE = 10000.0; // same as: public static final double MAX_ALTITUDE = 10000.0;

    // Abstract method - implicitly public abstract
    // Every non-abstract implementing class MUST provide this
    void fly();

    // DEFAULT METHOD (Java 8+) - has a body, can be overridden
    // WHY: Allows adding new methods to interfaces without breaking existing implementations
    // INTERVIEW Q: What problem do default methods solve?
    // A: Backward compatibility - existing classes implementing this interface
    //    don't break when a new method is added to the interface.
    default void glide() {
        System.out.println("Gliding through the air at altitude: " + MAX_ALTITUDE);
    }

    // STATIC METHOD (Java 8+) - belongs to the interface itself, NOT inherited
    // Call as: Flyable.checkWeather()
    // INTERVIEW Q: Can static methods of interfaces be overridden?
    // A: NO. They are not inherited by implementing classes.
    static void checkWeather() {
        System.out.println("Checking weather before flight... Clear skies!");
    }
}

/**
 * Another interface - Java allows MULTIPLE INTERFACE IMPLEMENTATION
 * This is how Java achieves "multiple inheritance of type"
 */
interface Swimmable {

    void swim();

    // Default method
    default void float_() { // 'float' is reserved, so using float_
        System.out.println("Floating on water surface...");
    }
}

// =============================================================================
// SECTION 2: ABSTRACT CLASS
// =============================================================================

/**
 * ABSTRACT CLASS - Partial abstraction (0% to 100% abstract)
 *
 * INTERVIEW Q: Can abstract class have constructor?
 * A: YES! The constructor is called when a concrete subclass is instantiated
 *    via super(). Abstract classes cannot be instantiated directly.
 *
 * INTERVIEW Q: Can abstract class have non-abstract methods?
 * A: YES! Unlike interfaces (pre-Java 8), abstract classes can have fully
 *    implemented methods. This is the key reason to use abstract class.
 *
 * INTERVIEW Q: Can abstract class have abstract methods?
 * A: YES, but it's not required. An abstract class can have 0 abstract methods.
 *    (Useful to prevent instantiation while providing common functionality)
 *
 * INTERVIEW Q: What if subclass doesn't implement all abstract methods?
 * A: The subclass itself must be declared abstract.
 */
abstract class Animal {

    // Instance variables - encapsulated (private)
    private String name;
    private int age;
    protected String species; // protected: accessible in subclasses

    // Static variable - ONE COPY shared across ALL Animal instances
    // INTERNAL: stored in Method Area (Metaspace in Java 8+), not on heap per object
    private static int totalAnimals = 0;

    // CONSTRUCTOR - called when subclass is instantiated via super()
    // INTERVIEW Q: Why does abstract class have constructor?
    // A: For initialization logic that all subclasses share.
    //    Subclasses call super() to set common fields.
    public Animal(String name, int age, String species) {
        this.name = name;       // 'this' refers to current instance
        this.age = age;
        this.species = species;
        totalAnimals++;         // Increment shared counter
        System.out.println("Animal constructor called for: " + name);
    }

    // ABSTRACT METHOD - no body, MUST be overridden in concrete subclasses
    // WHY abstract: Every animal makes sound differently, no default makes sense
    public abstract void makeSound();

    // ABSTRACT METHOD - subclass decides how it eats
    public abstract void eat();

    // CONCRETE METHOD - common behavior, inherited by all subclasses
    // Subclasses can override this if needed (not forced to, unlike abstract)
    public void breathe() {
        System.out.println(name + " is breathing oxygen.");
    }

    // CONCRETE METHOD with logic that subclasses share
    public void sleep(int hours) {
        System.out.println(name + " is sleeping for " + hours + " hours.");
    }

    // toString() override - explained in detail in Section 10
    @Override
    public String toString() {
        return "Animal{name='" + name + "', age=" + age + ", species='" + species + "'}";
    }

    // GETTERS and SETTERS (Encapsulation)
    public String getName() { return name; }
    public int getAge() { return age; }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public void setAge(int age) {
        if (age >= 0) {  // Validation logic hidden from caller
            this.age = age;
        }
    }

    // Static method - called as Animal.getTotalAnimals()
    public static int getTotalAnimals() {
        return totalAnimals;
    }
}

// =============================================================================
// SECTION 3: CONCRETE CLASSES - INHERITANCE + POLYMORPHISM
// =============================================================================

/**
 * DOG - demonstrates SINGLE INHERITANCE + implementing one interface
 *
 * INTERVIEW Q: What does Java support - multiple inheritance?
 * A: Java supports:
 *    - Single class inheritance (extends ONE class only)
 *    - Multiple interface implementation (implements A, B, C...)
 *    - Multilevel inheritance (A extends B extends C)
 *    Java does NOT support multiple class inheritance to avoid:
 *    "Diamond Problem" - ambiguity when two parents have same method
 *
 * INTERVIEW Q: What is the Diamond Problem?
 * A: If class C extends A and B, and both A,B have method foo(),
 *    which foo() does C inherit? Java avoids this by not allowing
 *    multiple class inheritance.
 *    NOTE: With default methods in interfaces, Java 8 introduced a limited
 *    diamond problem - compiler forces you to override the conflicting method.
 */
class Dog extends Animal implements Swimmable {

    private String breed;
    private boolean isVaccinated;

    // CONSTRUCTOR CHAINING with super()
    // super() MUST be the first statement in constructor
    public Dog(String name, int age, String breed) {
        super(name, age, "Canis lupus familiaris"); // calls Animal constructor
        this.breed = breed;
        this.isVaccinated = false;
        System.out.println("Dog constructor called for breed: " + breed);
    }

    // CONSTRUCTOR OVERLOADING - same name, different parameters
    public Dog(String name, int age, String breed, boolean isVaccinated) {
        this(name, age, breed);          // calls the 3-param constructor above
        this.isVaccinated = isVaccinated; // additional initialization
    }

    // METHOD OVERRIDING - @Override annotation is optional but highly recommended
    // INTERVIEW Q: What does @Override do?
    // A: It tells the compiler "I intend to override a method from parent/interface."
    //    The compiler then verifies the signature matches. Without it, a typo in
    //    method name would silently create a new method instead of overriding.
    @Override
    public void makeSound() {
        // super.makeSound() would cause error - abstract method has no body
        System.out.println(getName() + " says: Woof! Woof!");
    }

    @Override
    public void eat() {
        System.out.println(getName() + " is eating dog food.");
    }

    // Overriding Swimmable interface method
    @Override
    public void swim() {
        System.out.println(getName() + " the " + breed + " is swimming (dog paddle style)!");
    }

    // Overriding default method from Swimmable
    @Override
    public void float_() {
        System.out.println(getName() + " is floating effortlessly!");
    }

    // Dog-specific method - NOT in Animal, NOT in any interface
    public void fetch(String item) {
        System.out.println(getName() + " fetches the " + item + "!");
    }

    // COVARIANT RETURN TYPE - covered in Section 13
    // Method that returns a more specific type than parent
    public Dog copy() {
        return new Dog(getName(), getAge(), breed, isVaccinated);
    }

    // Overriding toString() from Object (via Animal)
    // INTERVIEW Q: What is method overriding?
    // A: Subclass provides specific implementation of method already defined in parent.
    //    Runtime decides which version to call based on actual object type (dynamic dispatch).
    @Override
    public String toString() {
        return "Dog{name='" + getName() + "', breed='" + breed +
               "', vaccinated=" + isVaccinated + "}";
    }

    public String getBreed() { return breed; }
    public boolean isVaccinated() { return isVaccinated; }
    public void setVaccinated(boolean vaccinated) { isVaccinated = vaccinated; }
}

/**
 * DUCK - demonstrates MULTIPLE INTERFACE IMPLEMENTATION
 *
 * INTERVIEW Q: What is multiple interface implementation?
 * A: A class can implement multiple interfaces, inheriting the contracts of all.
 *    This gives Java a form of multiple inheritance (for behavior/type).
 *
 * INTERVIEW Q: What happens if two interfaces have the same default method?
 * A: Compilation error! The implementing class MUST override the conflicting
 *    default method to resolve the ambiguity.
 */
class Duck extends Animal implements Flyable, Swimmable {

    private String color;

    public Duck(String name, int age, String color) {
        super(name, age, "Anas platyrhynchos");
        this.color = color;
    }

    @Override
    public void makeSound() {
        System.out.println(getName() + " says: Quack! Quack!");
    }

    @Override
    public void eat() {
        System.out.println(getName() + " is eating fish and weeds.");
    }

    // Implementing Flyable interface
    @Override
    public void fly() {
        System.out.println(getName() + " the duck is flying at max altitude: " + MAX_ALTITUDE);
        // MAX_ALTITUDE is inherited from Flyable interface (public static final)
    }

    // Implementing Swimmable interface
    @Override
    public void swim() {
        System.out.println(getName() + " the " + color + " duck is swimming gracefully.");
    }

    // RESOLVING DEFAULT METHOD CONFLICT:
    // Both Flyable.glide() and Swimmable.float_() are inherited without conflict here
    // because they have different names. If both had 'glide()' as default, we'd need:
    @Override
    public void glide() {
        // Calling specific interface's default method using: InterfaceName.super.method()
        Flyable.super.glide(); // Explicitly calling Flyable's default glide()
        System.out.println(getName() + " transitions to swimming after gliding.");
    }

    // Uses interface's static constant directly
    public double getMaxAltitude() {
        return MAX_ALTITUDE; // Inherited constant from Flyable
    }

    @Override
    public String toString() {
        return "Duck{name='" + getName() + "', color='" + color + "'}";
    }
}

// =============================================================================
// SECTION 4: ENCAPSULATION
// =============================================================================

/**
 * ENCAPSULATION - "Data Hiding" principle
 *
 * INTERVIEW Q: What is encapsulation?
 * A: Bundling data (fields) and methods that operate on data into a single unit (class),
 *    AND restricting direct access to internal state using access modifiers.
 *    The external world interacts ONLY through public methods (API).
 *
 * INTERVIEW Q: Benefits of encapsulation?
 * A: 1. Data validation in setters (e.g., age can't be negative)
 *    2. Read-only fields (only getter, no setter)
 *    3. Write-only fields (only setter, no getter) - rare
 *    4. Internal implementation can change without affecting callers
 *    5. Loose coupling between classes
 *
 * INTERVIEW Q: What is a JavaBean?
 * A: A class that follows: private fields, public no-arg constructor,
 *    getters/setters following naming convention (getFieldName/setFieldName),
 *    implements Serializable.
 *
 * ACCESS MODIFIERS (least to most restrictive):
 * public    - accessible everywhere
 * protected - accessible within package + subclasses (even outside package)
 * (default) - accessible within package only (package-private)
 * private   - accessible within class only
 */
class Person implements Cloneable {

    // private fields - ENCAPSULATED, not directly accessible from outside
    private String firstName;
    private String lastName;
    private int age;
    private String email;

    // private static field - shared across all Person instances
    private static int personCount = 0;

    // FINAL instance field - must be initialized in constructor, cannot change
    // INTERVIEW Q: What is a blank final field?
    // A: A final field declared without initialization. Must be set in constructor.
    private final long id; // Each person gets a unique immutable ID

    // Static initializer block - runs once when class is first loaded
    // INTERVIEW Q: Difference between static block and instance block?
    // A: Static block runs once at class loading, before any object is created.
    //    Instance initializer block runs each time an object is created, before constructor body.
    static {
        System.out.println("Person class loaded into JVM. Static block executed.");
        // Could initialize static resources, load config, etc.
    }

    // Instance initializer block - runs before constructor body, after super()
    {
        personCount++;
        System.out.println("Instance initializer block: Person #" + personCount + " being created.");
        // Note: 'id' is set here for demo, but blank finals are normally set in constructor
    }

    // Constructor
    public Person(String firstName, String lastName, int age, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
        this.id = System.nanoTime(); // Unique ID assigned at creation time
    }

    // GETTERS - read access to private fields
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public long getId() { return id; }

    // SETTERS - write access with VALIDATION (key benefit of encapsulation)
    public void setFirstName(String firstName) {
        if (firstName != null && firstName.trim().length() > 0) {
            this.firstName = firstName.trim();
        } else {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
    }

    public void setLastName(String lastName) {
        if (lastName != null && lastName.trim().length() > 0) {
            this.lastName = lastName.trim();
        }
    }

    public void setAge(int age) {
        if (age >= 0 && age <= 150) { // Business validation
            this.age = age;
        } else {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@")) { // Basic email validation
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // id has NO setter - effectively immutable (read-only field)

    // Static getter
    public static int getPersonCount() { return personCount; }

    // Derived property - computed from existing fields, no stored field needed
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ==========================================================================
    // SECTION 10: Object class methods - toString, equals, hashCode, clone
    // ==========================================================================

    /**
     * toString() - String representation of object
     *
     * INTERVIEW Q: What does the default toString() return?
     * A: ClassName@hexHashCode (e.g., "Person@1b6d3586")
     *    This is barely useful. We override it to show meaningful state.
     *
     * INTERVIEW Q: When is toString() automatically called?
     * A: 1. String concatenation: "Object: " + person  -> person.toString() called
     *    2. System.out.println(person) -> person.toString() called
     *    3. Logging frameworks
     */
    @Override
    public String toString() {
        return "Person{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", age=" + age +
               ", email='" + email + '\'' +
               '}';
    }

    /**
     * equals() - logical equality comparison
     *
     * INTERVIEW Q: Difference between == and equals()?
     * A: == compares REFERENCES (memory addresses) for objects, VALUES for primitives.
     *    equals() compares LOGICAL EQUALITY (what we define it to mean).
     *    Default equals() in Object uses == (reference equality).
     *    We override equals() to define what makes two objects "equal".
     *
     * CONTRACT for equals() (must satisfy all):
     * 1. Reflexive:  x.equals(x) must be true
     * 2. Symmetric:  x.equals(y) must equal y.equals(x)
     * 3. Transitive: if x.equals(y) and y.equals(z), then x.equals(z)
     * 4. Consistent: multiple calls return same result (no side effects)
     * 5. Non-null:   x.equals(null) must return false
     *
     * INTERVIEW Q: If you override equals(), must you override hashCode()?
     * A: YES! This is the equals-hashCode CONTRACT:
     *    If a.equals(b) is true, then a.hashCode() == b.hashCode() MUST also be true.
     *    Failing this breaks HashMap, HashSet, Hashtable, etc.
     */
    @Override
    public boolean equals(Object obj) {
        // 1. Same reference? Optimization shortcut
        if (this == obj) return true;

        // 2. Null check - equals(null) must return false
        if (obj == null) return false;

        // 3. Same type? Use getClass() for strict type check
        //    (instanceof would return true for subclasses too)
        if (getClass() != obj.getClass()) return false;

        // 4. Cast and compare fields
        Person other = (Person) obj;
        return age == other.age &&
               Objects.equals(firstName, other.firstName) &&
               Objects.equals(lastName, other.lastName) &&
               Objects.equals(email, other.email);
        // Note: We use ID for actual unique identification, but for 'equality'
        // we compare meaningful fields. Adjust based on business logic.
    }

    /**
     * hashCode() - integer representation of object for hash-based collections
     *
     * INTERVIEW Q: How does hashCode() work with HashMap?
     * A: HashMap calls key.hashCode() to find the bucket index.
     *    If two keys have same hashCode (collision), equals() is used to
     *    find the exact entry in that bucket's linked list/tree.
     *
     * INTERVIEW Q: What is a good hashCode implementation?
     * A: Uses all fields involved in equals(), distributes evenly across int range.
     *    Objects.hash() uses prime numbers (31) internally for good distribution.
     *    31 is chosen because: it's prime, odd, and 31 * x = (x << 5) - x (fast bit ops).
     *
     * INTERVIEW Q: Can two objects have same hashCode but not be equal?
     * A: YES! This is called a hash collision. hashCode contract only requires
     *    equal objects to have equal hash codes, NOT the reverse.
     */
    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, age, email);
        // Objects.hash() internally: result = 31 * result + (element == null ? 0 : element.hashCode())
    }

    /**
     * clone() - creates a copy of the object
     *
     * INTERVIEW Q: What is the difference between shallow copy and deep copy?
     * A: Shallow copy: copies primitive fields by value, object fields by reference.
     *               Both original and copy point to SAME nested objects.
     * A: Deep copy: copies everything recursively, new nested objects created.
     *              Original and copy are completely independent.
     *
     * INTERVIEW Q: What is Cloneable interface?
     * A: It's a MARKER INTERFACE (no methods). Just signals to JVM that
     *    this class allows cloning via clone(). Without implementing Cloneable,
     *    calling clone() throws CloneNotSupportedException.
     *
     * INTERVIEW Q: Issues with clone()?
     * A: 1. Shallow copy issues with mutable objects
     *    2. Constructors are bypassed (clone uses native memory copy)
     *    3. CloneNotSupportedException is checked exception
     *    4. final fields cannot be cloned properly
     *    ALTERNATIVE: Use copy constructors or factory methods instead.
     */
    @Override
    public Person clone() {
        try {
            return (Person) super.clone(); // Object.clone() does shallow copy
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}

// =============================================================================
// SECTION 5: METHOD OVERLOADING (Compile-time / Static Polymorphism)
// =============================================================================

/**
 * POLYMORPHISM - "Many forms"
 *
 * INTERVIEW Q: What are the two types of polymorphism in Java?
 * A: 1. COMPILE-TIME (Static) Polymorphism - Method Overloading
 *       Resolved at compile time. Same method name, different parameters.
 *       Also called: Ad-hoc polymorphism, early binding.
 *
 *    2. RUNTIME (Dynamic) Polymorphism - Method Overriding
 *       Resolved at runtime. Subclass overrides parent method.
 *       Also called: Subtype polymorphism, late binding, dynamic dispatch.
 *
 * INTERVIEW Q: Is operator overloading supported in Java?
 * A: NO, except for + operator which works for String concatenation.
 *    Java doesn't allow programmers to overload operators (unlike C++).
 */
class MathCalculator {

    /**
     * METHOD OVERLOADING - same name, different parameter list
     * Overloading is determined by: number of params, types of params, order of params
     * RETURN TYPE ALONE cannot differentiate overloaded methods (compiler error)
     *
     * INTERVIEW Q: Can we overload methods with just different return types?
     * A: NO! Return type is NOT part of the method signature for overloading.
     *    public int add(int a) and public double add(int a) -> COMPILE ERROR.
     *
     * INTERVIEW Q: What is method signature?
     * A: Method name + parameter types (and count, order). Return type is NOT included.
     */

    // Version 1: two integers
    public int add(int a, int b) {
        System.out.println("add(int, int) called");
        return a + b;
    }

    // Version 2: three integers
    public int add(int a, int b, int c) {
        System.out.println("add(int, int, int) called");
        return a + b + c;
    }

    // Version 3: two doubles
    public double add(double a, double b) {
        System.out.println("add(double, double) called");
        return a + b;
    }

    // Version 4: String concatenation - different type
    public String add(String a, String b) {
        System.out.println("add(String, String) called");
        return a + b;
    }

    // Version 5: varargs - variable number of arguments
    // INTERVIEW Q: What is varargs?
    // A: Allows passing variable number of arguments. Internally treated as array.
    //    Must be last parameter. Only one varargs per method.
    public int add(int... numbers) {
        System.out.println("add(int...) varargs called with " + numbers.length + " args");
        int sum = 0;
        for (int n : numbers) sum += n;
        return sum;
    }

    /**
     * WIDENING vs NARROWING in overloading:
     * If exact match not found, Java auto-widens (byte -> short -> int -> long -> float -> double)
     * e.g., calling add(5, 3L) would call add(long, long) if add(int, long) doesn't exist.
     * Autoboxing happens AFTER widening. Varargs has lowest priority.
     *
     * Priority: Exact match > Widening > Autoboxing > Varargs
     */
}

// =============================================================================
// SECTION 7: 'this' and 'super' KEYWORDS
// =============================================================================

/**
 * INTERVIEW Q: What is the 'this' keyword?
 * A: 'this' is a reference to the CURRENT OBJECT (the instance the method is called on).
 *    Uses:
 *    1. this.field - distinguish between field and local variable with same name
 *    2. this() - call another constructor in the same class (constructor chaining)
 *    3. this as argument - pass current object to a method
 *    4. this as return value - return current object (Builder pattern)
 *
 * INTERVIEW Q: What is the 'super' keyword?
 * A: 'super' refers to the PARENT CLASS.
 *    Uses:
 *    1. super.field - access parent's field (if same name in child)
 *    2. super.method() - call parent's overridden method
 *    3. super() - call parent's constructor (must be first statement)
 *
 * INTERVIEW Q: Can we use 'this' and 'super' in static methods?
 * A: NO! Static methods don't have 'this' or 'super' because they
 *    belong to the class, not any instance.
 */
class Vehicle {
    protected String brand;
    protected int year;
    protected int speed;

    public Vehicle(String brand, int year) {
        this.brand = brand; // 'this.brand' (field) vs 'brand' (parameter)
        this.year = year;
        this.speed = 0;
    }

    public void accelerate(int amount) {
        this.speed += amount;
        System.out.println(brand + " accelerating. Speed: " + speed);
    }

    public String describe() {
        return brand + " (" + year + ")";
    }
}

class Car extends Vehicle {
    private int numDoors;
    private String model;

    // 'super()' - calling parent constructor
    public Car(String brand, String model, int year, int numDoors) {
        super(brand, year); // MUST be first statement - calls Vehicle(brand, year)
        this.model = model; // then we set Car-specific fields
        this.numDoors = numDoors;
    }

    // Constructor chaining with this()
    // INTERVIEW Q: Can we call both this() and super() in same constructor?
    // A: NO! Only one can be the first statement. Calling this() will eventually
    //    chain to a constructor that calls super().
    public Car(String brand, String model, int year) {
        this(brand, model, year, 4); // calls the 4-param constructor above
        // super() is NOT called here directly, but the 4-param constructor calls it
    }

    // Using 'super' to call parent's method
    @Override
    public void accelerate(int amount) {
        System.out.println("Car gear-shift before acceleration...");
        super.accelerate(amount); // calling parent's accelerate
        // After super call, we can add more Car-specific behavior
        System.out.println("Car model " + model + " at speed: " + speed);
    }

    // Accessing parent field via 'super' (when child has same field name)
    // Here brand is in parent. We'd use super.brand if we had a brand field here too.
    @Override
    public String describe() {
        return super.describe() + " - Model: " + model + ", Doors: " + numDoors;
    }

    // Builder-pattern style: return 'this' for method chaining
    public Car setModel(String model) {
        this.model = model;
        return this; // returning 'this' allows: car.setModel("X").setNumDoors(2)
    }

    public Car setNumDoors(int numDoors) {
        this.numDoors = numDoors;
        return this; // method chaining
    }

    // Pass 'this' to another method
    public void registerCar(VehicleRegistry registry) {
        registry.register(this); // passing current Car object
    }

    public String getModel() { return model; }
    public int getNumDoors() { return numDoors; }
}

// Helper class for the 'this as argument' demo
class VehicleRegistry {
    public void register(Car car) {
        System.out.println("Registering car: " + car.describe());
    }
}

// =============================================================================
// SECTION 8: FINAL KEYWORD
// =============================================================================

/**
 * FINAL CLASS - cannot be extended (subclassed)
 *
 * INTERVIEW Q: Why make a class final?
 * A: 1. Security - prevent malicious subclassing (e.g., String, Integer are final)
 *    2. Immutability - ensures object state cannot be altered via subclass
 *    3. Performance - JVM can inline final methods (devirtualization)
 *    Examples: String, Integer, Double, System, Math are all final classes
 *
 * INTERVIEW Q: Can a final class implement interfaces?
 * A: YES! final only prevents extension, not implementation.
 */
final class ImmutablePoint {

    // FINAL FIELD (instance) - must be initialized at declaration OR in constructor
    // Cannot be changed after initialization
    private final double x;
    private final double y;

    // FINAL FIELD (static) = CONSTANT - by convention use UPPER_SNAKE_CASE
    public static final ImmutablePoint ORIGIN = new ImmutablePoint(0, 0);

    public ImmutablePoint(double x, double y) {
        this.x = x;
        this.y = y;
        // After constructor completes, x and y are permanently set
    }

    // FINAL METHOD - cannot be overridden (but class can still be subclassed if not final)
    // Even though ImmutablePoint is final (can't extend), showing the concept:
    // If this method were in a non-final class, 'final' on the method prevents overriding.
    public final double distanceTo(ImmutablePoint other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Immutable "modifier" - returns NEW object instead of modifying this
    public ImmutablePoint translate(double dx, double dy) {
        return new ImmutablePoint(this.x + dx, this.y + dy); // 'this' unchanged
    }

    public double getX() { return x; }
    public double getY() { return y; }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

// =============================================================================
// SECTION 8 (CONT): STATIC KEYWORD
// =============================================================================

/**
 * STATIC KEYWORD - belongs to the CLASS, not to any instance
 *
 * INTERVIEW Q: What can be static in Java?
 * A: Fields, methods, blocks (initializers), nested classes, imports (static import)
 *
 * INTERVIEW Q: Can static method access instance (non-static) members?
 * A: NO! Static methods don't have 'this' reference. They can only access
 *    other static members directly. To access instance members, they need
 *    an object reference.
 *
 * INTERVIEW Q: Can we override static methods?
 * A: NO! Static methods are resolved at compile time based on reference type
 *    (static binding). This is called METHOD HIDING, not overriding.
 *    If parent and child both have static method with same signature,
 *    calling via parent reference invokes parent's version, child reference
 *    invokes child's version.
 *
 * INTERVIEW Q: What is a static import?
 * A: import static java.lang.Math.PI; allows using PI directly without Math.PI
 */
class Counter {

    // STATIC VARIABLE - shared by ALL Counter instances
    // INTERNAL: Lives in Metaspace (method area), ONE copy regardless of object count
    private static int count = 0;

    // STATIC CONSTANT
    public static final int MAX_COUNT = 1000;

    // Instance variable - each Counter object has its OWN copy
    private int instanceId;

    // STATIC BLOCK - runs ONCE when class is first loaded by ClassLoader
    // INTERVIEW Q: Order of execution in a class?
    // A: 1. Static fields/blocks (in order of appearance)
    //    2. Instance fields/blocks (in order of appearance)
    //    3. Constructor
    //    Parent class statics run before child class statics.
    static {
        System.out.println("Counter class being loaded. Static block runs ONCE.");
        // Useful for: JDBC driver registration, loading config, initializing static resources
        count = 0; // Initialize static state
    }

    // INSTANCE INITIALIZER BLOCK - runs before every constructor
    {
        instanceId = ++count;
        System.out.println("Counter instance block: creating instance #" + instanceId);
    }

    public Counter() {
        System.out.println("Counter constructor called. instanceId=" + instanceId);
    }

    // STATIC METHOD - can only access static members
    public static int getCount() {
        return count; // OK - accessing static field
        // return instanceId; // ERROR! Cannot access instance field from static method
    }

    public static void resetCount() {
        count = 0;
        System.out.println("Count reset to 0");
    }

    public int getInstanceId() { return instanceId; }
}

// =============================================================================
// SECTION 11: INNER CLASSES
// =============================================================================

/**
 * INNER CLASSES - classes defined within another class
 *
 * INTERVIEW Q: What are the four types of inner/nested classes?
 * A: 1. Static Nested Class  - static class inside class, no access to outer instance
 *    2. Inner Class          - non-static class inside class, has access to outer instance
 *    3. Local Class          - class inside a method
 *    4. Anonymous Class      - unnamed class, instantiated inline
 *
 * INTERVIEW Q: Why use inner classes?
 * A: 1. Logical grouping - if a class is only useful to one class, keep them together
 *    2. Encapsulation - inner class can access outer class's private members
 *    3. Readability - related code stays together
 */
class OuterClass {

    private String outerField = "I am outer field";
    private static String outerStatic = "I am outer static field";

    // -------------------------------------------------------------------------
    // STATIC NESTED CLASS
    // INTERVIEW Q: How is static nested class different from inner class?
    // A: Static nested class:
    //    - Declared with 'static' keyword
    //    - Does NOT hold a reference to outer class instance
    //    - Can only access STATIC members of outer class directly
    //    - Instantiate as: OuterClass.StaticNestedClass obj = new OuterClass.StaticNestedClass()
    //    Inner class:
    //    - No 'static' keyword
    //    - Holds implicit reference to outer class instance (can cause memory leaks!)
    //    - Can access ALL members (static and instance) of outer class
    //    - Instantiate as: OuterClass outer = new OuterClass(); outer.new InnerClass()
    // -------------------------------------------------------------------------
    static class StaticNestedClass {
        public void display() {
            System.out.println("Static Nested Class accessing outer static: " + outerStatic);
            // System.out.println(outerField); // ERROR! Cannot access instance field
            // No reference to OuterClass instance
        }
    }

    // -------------------------------------------------------------------------
    // INNER CLASS (Non-static)
    // Has hidden reference to OuterClass.this
    // -------------------------------------------------------------------------
    class InnerClass {
        private String innerField = "I am inner field";

        public void display() {
            // Can access both outer instance field AND static field
            System.out.println("Inner class accessing outer field: " + outerField);
            System.out.println("Inner class accessing outer static: " + outerStatic);
            System.out.println("Inner class field: " + innerField);

            // Access outer 'this' explicitly if there's name conflict
            // OuterClass.this.outerField  - refers to outer class instance
        }
    }

    public void methodWithLocalAndAnonymous() {

        // -------------------------------------------------------------------------
        // LOCAL CLASS - defined inside a method
        // Can access: all outer class members + local variables (must be effectively final)
        // INTERVIEW Q: What is "effectively final"?
        // A: A variable that is never reassigned after initialization.
        //    Java 8+ - doesn't need 'final' keyword, just must not be reassigned.
        // -------------------------------------------------------------------------
        final String localVar = "I am effectively final";

        class LocalClass {
            public void greet() {
                System.out.println("Local class says: " + localVar); // accessing local var
                System.out.println("Also has access to: " + outerField); // and outer field
            }
        }

        LocalClass local = new LocalClass();
        local.greet();

        // -------------------------------------------------------------------------
        // ANONYMOUS CLASS - no name, instantiated inline, extends a class or implements an interface
        // INTERVIEW Q: When would you use anonymous class vs lambda?
        // A: Anonymous class: when implementing interface with multiple methods,
        //    or when you need to maintain state (fields).
        //    Lambda: only for functional interfaces (single abstract method).
        //    Lambda is more concise. Anonymous class is more powerful.
        // -------------------------------------------------------------------------
        Flyable anonymousFlyable = new Flyable() {
            // Implementing abstract method from interface
            @Override
            public void fly() {
                System.out.println("Anonymous class flying! Has access to outer: " + outerField);
            }

            // Can also override default methods or add additional methods
            // (but additional methods aren't accessible via Flyable reference)
            @Override
            public void glide() {
                System.out.println("Anonymous class custom glide!");
            }
        };
        anonymousFlyable.fly();
        anonymousFlyable.glide();

        // Anonymous class extending abstract class
        Animal anonymousAnimal = new Animal("AnonymousAnimal", 1, "Unknown") {
            @Override
            public void makeSound() {
                System.out.println("Anonymous animal makes a mysterious sound...");
            }

            @Override
            public void eat() {
                System.out.println("Anonymous animal eats anything.");
            }
        };
        anonymousAnimal.makeSound();
    }
}

// =============================================================================
// SECTION 12: ENUM
// =============================================================================

/**
 * ENUM - enumeration type (Java 5+)
 *
 * INTERVIEW Q: What is an enum in Java?
 * A: A special class that represents a group of named constants.
 *    Enums are implicitly: public static final and extend java.lang.Enum
 *    They cannot be instantiated with 'new'.
 *
 * INTERVIEW Q: Can enum have constructor, fields, methods?
 * A: YES! That's what makes Java enums powerful.
 *    Constructor is implicitly private (or package-private).
 *    Each constant calls the constructor.
 *
 * INTERVIEW Q: Can enum implement interfaces?
 * A: YES! Enum can implement interfaces but cannot extend classes
 *    (it already implicitly extends java.lang.Enum).
 *
 * INTERVIEW Q: Thread safety of enum?
 * A: Enum instances are created by JVM at class loading time and are
 *    inherently thread-safe. That's why enum is the BEST way to implement Singleton.
 *
 * INTERVIEW Q: Can we use enum in switch statement?
 * A: YES! One of the primary use cases of enums.
 */
enum Planet {

    // Each constant calls the constructor Planet(mass, radius)
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6),
    MARS(6.421e+23, 3.3972e6),
    JUPITER(1.9e+27, 7.1492e7),
    SATURN(5.688e+26, 6.0268e7),
    URANUS(8.686e+25, 2.5559e7),
    NEPTUNE(1.024e+26, 2.4746e7); // semicolon required before methods/fields

    // FIELDS - each enum constant has its own values
    private final double mass;   // in kilograms
    private final double radius; // in meters

    // STATIC constant
    static final double G = 6.67300E-11; // gravitational constant

    // CONSTRUCTOR - implicitly private
    // INTERVIEW Q: Why can't enum constructor be public?
    // A: Enums are pre-defined constants; we don't want new instances created.
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    // METHODS - each enum constant can call these
    public double surfaceGravity() {
        return G * mass / (radius * radius);
    }

    public double surfaceWeight(double otherMass) {
        return otherMass * surfaceGravity();
    }

    // BUILT-IN enum methods (from java.lang.Enum):
    // name()     - returns constant name as String (e.g., "EARTH")
    // ordinal()  - returns 0-based position (e.g., EARTH.ordinal() = 2)
    // valueOf(String) - returns enum constant by name
    // values()   - returns array of all constants

    // GETTERS
    public double getMass() { return mass; }
    public double getRadius() { return radius; }
}

/**
 * Enum with ABSTRACT METHOD - each constant provides its own implementation
 * This is a powerful pattern for Strategy/Command via enum.
 */
enum Operation {
    ADD {
        @Override
        public double apply(double x, double y) { return x + y; }
    },
    SUBTRACT {
        @Override
        public double apply(double x, double y) { return x - y; }
    },
    MULTIPLY {
        @Override
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE {
        @Override
        public double apply(double x, double y) {
            if (y == 0) throw new ArithmeticException("Division by zero");
            return x / y;
        }
    };

    // Abstract method - each constant MUST implement it
    public abstract double apply(double x, double y);
}

// =============================================================================
// SECTION 13: COVARIANT RETURN TYPE
// =============================================================================

/**
 * COVARIANT RETURN TYPE (Java 5+)
 *
 * INTERVIEW Q: What is covariant return type?
 * A: When overriding a method, the return type of the overriding method
 *    can be a SUBTYPE of the return type in the parent class.
 *    This is called covariant return type.
 *
 * Before Java 5: overriding methods had to have IDENTICAL return types.
 * After Java 5: overriding methods can return a more specific (sub) type.
 *
 * WHY it's useful: Avoids unnecessary casting for callers.
 *   Without covariant return: Animal a = (Animal) dog.copy() // caller must cast
 *   With covariant return: Dog d = dog.copy() // no cast needed, compile-time safe
 */
class CovariantDemo {

    // BASE class
    static class Shape {
        public Shape copy() { // returns Shape
            return new Shape();
        }
        public String type() { return "Shape"; }
    }

    // SUBCLASS - overrides copy() with more specific return type (Circle, not Shape)
    static class Circle extends Shape {
        private double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public Circle copy() { // COVARIANT - returns Circle (subtype of Shape)
            return new Circle(this.radius);
        }

        @Override
        public String type() { return "Circle with radius " + radius; }
    }
}

// =============================================================================
// SECTION 9: instanceof OPERATOR
// =============================================================================

/**
 * instanceof OPERATOR - runtime type check
 *
 * INTERVIEW Q: What is instanceof?
 * A: Binary operator that returns true if the object on the left is an
 *    instance of the class/interface on the right.
 *    Returns false if the object is null (no NullPointerException).
 *
 * INTERVIEW Q: instanceof vs getClass()?
 * A: instanceof returns true for subclasses too (checks IS-A relationship)
 *    getClass() == checks EXACT class (no subclass tolerance)
 *    Use instanceof when polymorphism matters, getClass() for strict equality.
 *
 * JAVA 16+: Pattern matching for instanceof
 * if (obj instanceof String s) { ... use s directly, no cast needed ... }
 */

// =============================================================================
// MAIN CLASS - DEMO AND INTERVIEW Q&A
// =============================================================================

/**
 * MAIN CLASS - Demonstrates all OOP concepts with running examples
 *
 * INTERVIEW Q: Summarize all OOP concepts in one line each:
 * A:
 * ENCAPSULATION  : private fields + public getters/setters = data hiding + validation
 * INHERITANCE    : extends keyword = IS-A relationship = code reuse
 * POLYMORPHISM   : overloading (compile-time) + overriding (runtime) = flexibility
 * ABSTRACTION    : abstract class + interface = contract without implementation details
 */
public class OOPConcepts {

    public static void main(String[] args) {

        System.out.println("=".repeat(60));
        System.out.println("OOP CONCEPTS DEMONSTRATION");
        System.out.println("=".repeat(60));

        // ----------------------------------------------------------------
        // 1. ABSTRACTION & INHERITANCE: Abstract class + Interface
        // ----------------------------------------------------------------
        System.out.println("\n--- 1. ABSTRACTION & INHERITANCE ---");

        Dog dog = new Dog("Buddy", 3, "Golden Retriever", true);
        Duck duck = new Duck("Donald", 2, "White");

        // Calling abstract methods (implemented by each concrete class)
        dog.makeSound();   // Dog-specific implementation
        duck.makeSound();  // Duck-specific implementation

        // Calling concrete methods from abstract parent
        dog.breathe();
        duck.sleep(8);

        // Multiple interface methods
        dog.swim();
        duck.fly();
        duck.swim();

        System.out.println("Total animals created: " + Animal.getTotalAnimals());

        // ----------------------------------------------------------------
        // 2. POLYMORPHISM: Runtime polymorphism via parent reference
        // ----------------------------------------------------------------
        System.out.println("\n--- 2. RUNTIME POLYMORPHISM ---");

        // UPCASTING - implicit, safe
        // Reference type: Animal, Object type: Dog
        // INTERVIEW Q: What is upcasting and downcasting?
        // A: Upcasting: child object stored in parent reference (automatic, safe)
        //    Downcasting: parent reference cast to child type (manual, may throw ClassCastException)
        Animal animalRef = dog; // Upcasting: no cast operator needed
        animalRef.makeSound(); // Dog's makeSound() is called! (RUNTIME decision)
        // animalRef.fetch("ball"); // ERROR! Animal reference doesn't know about fetch()

        // Polymorphic array - different objects, same reference type
        Animal[] animals = { dog, duck, new Dog("Rex", 5, "German Shepherd") };
        System.out.println("\nPolymorphic loop:");
        for (Animal a : animals) {
            a.makeSound(); // Each calls its own version - runtime polymorphism
        }

        // DOWNCASTING - requires explicit cast, can throw ClassCastException
        if (animalRef instanceof Dog) {
            Dog specificDog = (Dog) animalRef; // Downcasting
            specificDog.fetch("frisbee");       // Now we can call Dog-specific method
        }

        // Java 16+ pattern matching (if supported):
        // if (animalRef instanceof Dog d) { d.fetch("ball"); }

        // ----------------------------------------------------------------
        // 3. ENCAPSULATION: Person class with private fields
        // ----------------------------------------------------------------
        System.out.println("\n--- 3. ENCAPSULATION ---");

        Person p1 = new Person("Alice", "Smith", 30, "alice@example.com");
        System.out.println("Person: " + p1.getFullName() + ", Age: " + p1.getAge());

        // Validation in setter prevents bad state
        try {
            p1.setAge(-5); // Throws IllegalArgumentException
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }
        p1.setAge(31); // Valid
        System.out.println("Updated age: " + p1.getAge());

        // ----------------------------------------------------------------
        // 4. equals(), hashCode(), toString()
        // ----------------------------------------------------------------
        System.out.println("\n--- 4. Object Methods: equals, hashCode, toString ---");

        Person p2 = new Person("Alice", "Smith", 31, "alice@example.com");
        Person p3 = new Person("Bob", "Jones", 25, "bob@example.com");

        System.out.println("p1.toString(): " + p1);
        System.out.println("p1 == p2: " + (p1 == p2));           // false: different references
        System.out.println("p1.equals(p2): " + p1.equals(p2));   // true: same logical content
        System.out.println("p1.equals(p3): " + p1.equals(p3));   // false
        System.out.println("p1.hashCode(): " + p1.hashCode());
        System.out.println("p2.hashCode(): " + p2.hashCode());   // same as p1 since equals() is true

        // Clone
        Person p4 = p1.clone();
        System.out.println("Clone: " + p4);
        System.out.println("p1 == p4 (reference): " + (p1 == p4));        // false: different objects
        System.out.println("p1.equals(p4) (content): " + p1.equals(p4));  // true: same content

        // ----------------------------------------------------------------
        // 5. METHOD OVERLOADING (Compile-time polymorphism)
        // ----------------------------------------------------------------
        System.out.println("\n--- 5. METHOD OVERLOADING ---");

        MathCalculator calc = new MathCalculator();
        System.out.println(calc.add(5, 3));           // add(int, int)
        System.out.println(calc.add(5, 3, 2));        // add(int, int, int)
        System.out.println(calc.add(5.5, 3.3));       // add(double, double)
        System.out.println(calc.add("Hello ", "World")); // add(String, String)
        System.out.println(calc.add(1, 2, 3, 4, 5)); // add(int...) varargs

        // ----------------------------------------------------------------
        // 6. 'this' and 'super' keywords via Car/Vehicle
        // ----------------------------------------------------------------
        System.out.println("\n--- 6. THIS and SUPER Keywords ---");

        // Method chaining with 'this' as return value (Builder pattern)
        Car car = new Car("Toyota", "Camry", 2022);
        car.setModel("Camry LE").setNumDoors(4); // method chaining via 'this'
        car.accelerate(60); // calls parent's accelerate via super
        System.out.println(car.describe()); // uses super.describe() internally

        // ----------------------------------------------------------------
        // 7. FINAL KEYWORD
        // ----------------------------------------------------------------
        System.out.println("\n--- 7. FINAL Keyword ---");

        ImmutablePoint p = new ImmutablePoint(3.0, 4.0);
        ImmutablePoint q = new ImmutablePoint(0.0, 0.0);
        System.out.println("Point p: " + p);
        System.out.println("Distance p to q: " + p.distanceTo(q));

        // p.x = 5.0; // COMPILE ERROR - final field cannot be reassigned

        ImmutablePoint translated = p.translate(1.0, 1.0); // returns new point
        System.out.println("Original (unchanged): " + p);
        System.out.println("Translated (new object): " + translated);

        // final local variable
        final int MULTIPLIER = 10;
        // MULTIPLIER = 20; // COMPILE ERROR - cannot reassign final local variable
        System.out.println("Final local var: " + (5 * MULTIPLIER));

        // ----------------------------------------------------------------
        // 8. STATIC KEYWORD
        // ----------------------------------------------------------------
        System.out.println("\n--- 8. STATIC Keyword ---");

        Counter c1 = new Counter();
        Counter c2 = new Counter();
        Counter c3 = new Counter();

        // Access static method via class name (preferred) OR via reference (not recommended)
        System.out.println("Total counters created: " + Counter.getCount());
        Counter.resetCount();
        System.out.println("After reset: " + Counter.getCount());

        // Accessing interface static method
        Flyable.checkWeather();

        // ----------------------------------------------------------------
        // 9. instanceof OPERATOR
        // ----------------------------------------------------------------
        System.out.println("\n--- 9. instanceof Operator ---");

        Object[] objects = { dog, duck, "Hello", 42, null };
        for (Object obj : objects) {
            System.out.println(obj + " instanceof Animal: " + (obj instanceof Animal));
            System.out.println(obj + " instanceof Flyable: " + (obj instanceof Flyable));
            System.out.println(obj + " instanceof Swimmable: " + (obj instanceof Swimmable));
            System.out.println(obj + " instanceof String: " + (obj instanceof String));
            System.out.println("---");
        }

        // null check: instanceof returns false for null (no NPE)
        System.out.println("null instanceof Object: " + (null instanceof Object)); // false

        // getClass() strict check
        Animal animalDog = new Dog("Spot", 2, "Dalmatian");
        System.out.println("getClass() == Dog.class: " + (animalDog.getClass() == Dog.class)); // true
        System.out.println("getClass() == Animal.class: " + (animalDog.getClass() == Animal.class)); // false!
        System.out.println("instanceof Animal: " + (animalDog instanceof Animal)); // true (IS-A)

        // ----------------------------------------------------------------
        // 10. INNER CLASSES
        // ----------------------------------------------------------------
        System.out.println("\n--- 10. Inner Classes ---");

        // Static nested class - no outer instance needed
        OuterClass.StaticNestedClass staticNested = new OuterClass.StaticNestedClass();
        staticNested.display();

        // Inner class - requires outer class instance first
        OuterClass outer = new OuterClass();
        OuterClass.InnerClass innerObj = outer.new InnerClass();
        innerObj.display();

        // Local and Anonymous classes are created inside the method
        outer.methodWithLocalAndAnonymous();

        // ----------------------------------------------------------------
        // 11. ENUM
        // ----------------------------------------------------------------
        System.out.println("\n--- 11. ENUM ---");

        double earthWeight = 75.0; // kg
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        System.out.println("Mass: " + mass + " kg");

        for (Planet p5 : Planet.values()) {
            System.out.printf("Weight on %s: %.2f N%n", p5, p5.surfaceWeight(mass));
        }

        // Enum in switch
        Planet myPlanet = Planet.MARS;
        switch (myPlanet) {
            case EARTH:
                System.out.println("Home planet!");
                break;
            case MARS:
                System.out.println("The Red Planet!");
                break;
            default:
                System.out.println("Other planet: " + myPlanet.name());
        }

        // Enum with abstract method (Operation)
        System.out.println("\nOperation enum:");
        for (Operation op : Operation.values()) {
            System.out.printf("%.1f %s %.1f = %.1f%n", 10.0, op, 5.0, op.apply(10.0, 5.0));
        }

        // Enum methods from java.lang.Enum
        System.out.println("Planet.EARTH.name(): " + Planet.EARTH.name());         // "EARTH"
        System.out.println("Planet.EARTH.ordinal(): " + Planet.EARTH.ordinal());   // 2
        System.out.println("Planet.valueOf(\"MARS\"): " + Planet.valueOf("MARS"));   // MARS

        // ----------------------------------------------------------------
        // 12. COVARIANT RETURN TYPE
        // ----------------------------------------------------------------
        System.out.println("\n--- 12. Covariant Return Type ---");

        CovariantDemo.Circle circle = new CovariantDemo.Circle(5.0);
        CovariantDemo.Circle circleCopy = circle.copy(); // No cast needed!
        System.out.println("Original: " + circle.type());
        System.out.println("Covariant copy: " + circleCopy.type());

        // If copy() returned Shape (without covariant), we'd need: (Circle) circle.copy()
        CovariantDemo.Shape shapeRef = circle; // upcasting
        CovariantDemo.Shape shapeCopy = shapeRef.copy(); // returns Circle at runtime
        System.out.println("Shape ref copy type: " + shapeCopy.type()); // still "Circle with radius 5.0"!

        // ----------------------------------------------------------------
        // INTERVIEW: Common OOP questions summary
        // ----------------------------------------------------------------
        System.out.println("\n--- KEY INTERVIEW CONCEPTS SUMMARY ---");
        System.out.println("1. Four pillars: Encapsulation, Abstraction, Inheritance, Polymorphism");
        System.out.println("2. Abstract class vs Interface: use abstract for IS-A (base class), interface for CAN-DO (capability)");
        System.out.println("3. Method overloading = compile-time polymorphism (same name, diff params)");
        System.out.println("4. Method overriding = runtime polymorphism (@Override in subclass)");
        System.out.println("5. Always override hashCode() when overriding equals()");
        System.out.println("6. Static members belong to class, instance members belong to objects");
        System.out.println("7. final class=no extend, final method=no override, final field=no reassign");
        System.out.println("8. super() must be first in constructor; this() must be first in constructor");
        System.out.println("9. instanceof returns false for null, no NPE");
        System.out.println("10. Anonymous class > lambda when multiple methods or state needed");
    }
}

/*
 * ============================================================
 * ADDITIONAL INTERVIEW Q&A (Advanced)
 * ============================================================
 *
 * Q: What is the order of initialization in Java?
 * A: 1. Parent static fields & static blocks (in order of declaration)
 *    2. Child static fields & static blocks
 *    3. Parent instance fields & instance blocks
 *    4. Parent constructor
 *    5. Child instance fields & instance blocks
 *    6. Child constructor
 *
 * Q: Can a constructor be private?
 * A: YES! Used in:
 *    - Singleton pattern (prevents external instantiation)
 *    - Utility classes (e.g., Math, Collections - only static members)
 *    - Factory method pattern (object creation via static factory methods)
 *    - Builder pattern inner class
 *
 * Q: What is a marker interface?
 * A: An interface with NO methods. Used to "mark" a class as having some capability.
 *    Examples: Serializable, Cloneable, RandomAccess
 *    JVM/frameworks use instanceof to check if an object is "marked".
 *    Modern alternative: annotations (e.g., @Serializable)
 *
 * Q: What is a functional interface?
 * A: Interface with exactly ONE abstract method (can have multiple default/static methods).
 *    Can be implemented with a lambda expression.
 *    @FunctionalInterface annotation is optional but recommended.
 *    Examples: Runnable, Callable, Comparator, Predicate, Function, Consumer, Supplier
 *
 * Q: What is difference between abstract class and interface?
 * A: Abstract Class:                     Interface:
 *    - Can have constructor               - No constructor
 *    - Can have instance fields           - Only constants (public static final)
 *    - Methods can be any access level    - Methods public (or private in Java 9+)
 *    - Single inheritance only            - Multiple implementation allowed
 *    - IS-A relationship                  - CAN-DO / HAS-A-CAPABILITY relationship
 *    - Use when: shared code among        - Use when: unrelated classes share behavior
 *      closely related classes            (e.g., Flyable, Serializable, Comparable)
 *
 * Q: What is tight coupling vs loose coupling?
 * A: Tight coupling: classes directly depend on each other (hard to change/test)
 *    Loose coupling: classes depend on interfaces/abstractions (flexible, testable)
 *    OOP achieves loose coupling via abstraction + polymorphism + dependency injection.
 *
 * Q: What is the Liskov Substitution Principle (LSP)?
 * A: Objects of a subclass should be substitutable for objects of the parent class
 *    without breaking the program. (One of SOLID principles)
 *    If Dog extends Animal, anywhere you use Animal, you should be able to use Dog.
 *
 * Q: What is encapsulation vs abstraction?
 * A: Encapsulation = HOW (implementation detail hiding via access modifiers)
 *    Abstraction = WHAT (hiding complexity, showing only necessary interface)
 *    Encapsulation is a technique to achieve abstraction.
 *    Capsule analogy: Encapsulation puts medicine (data+methods) in a capsule (class).
 *    Abstraction is the capsule itself - you just swallow it without knowing the chemistry.
 *
 * Q: What is method hiding?
 * A: When a subclass has a STATIC method with same signature as parent's static method.
 *    Not true overriding - resolved at compile time based on reference type.
 *    class Parent { static void foo() {...} }
 *    class Child extends Parent { static void foo() {...} } // METHOD HIDING
 *    Parent p = new Child(); p.foo(); // calls Parent.foo() - not Child.foo()!
 *
 * Q: Can we override private methods?
 * A: NO! Private methods are not inherited, so they cannot be overridden.
 *    If child defines same-signature private method, it's a NEW method, not override.
 *
 * Q: What is the difference between method overloading and method hiding?
 * A: Overloading: same class or hierarchy, same name, DIFFERENT signature
 *    Hiding: subclass defines STATIC method with SAME signature as parent's static method
 *
 * Q: What is Object class? What methods does it have?
 * A: Root of Java class hierarchy. Every class implicitly extends Object.
 *    Key methods:
 *    - toString()    : string representation
 *    - equals()      : logical equality
 *    - hashCode()    : hash code for collections
 *    - clone()       : shallow copy (protected)
 *    - finalize()    : called before GC (deprecated Java 9+)
 *    - getClass()    : returns runtime Class object
 *    - wait()        : thread coordination (releases monitor lock)
 *    - notify()      : wakes one waiting thread
 *    - notifyAll()   : wakes all waiting threads
 * ============================================================
 */
