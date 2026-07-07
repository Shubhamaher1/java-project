package com.interview.master.javacore.collections;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================
 * JAVA COLLECTIONS FRAMEWORK - Complete Interview Guide
 * ============================================================
 *
 * COLLECTION HIERARCHY DIAGRAM:
 *
 *   java.lang.Iterable
 *       └── java.util.Collection
 *               ├── List (ordered, index-based, duplicates allowed)
 *               │     ├── ArrayList
 *               │     ├── LinkedList
 *               │     ├── Vector
 *               │     │     └── Stack
 *               │     └── CopyOnWriteArrayList (concurrent)
 *               │
 *               ├── Set (no duplicates)
 *               │     ├── HashSet
 *               │     │     └── LinkedHashSet
 *               │     ├── TreeSet (SortedSet → NavigableSet)
 *               │     └── CopyOnWriteArraySet (concurrent)
 *               │
 *               └── Queue (FIFO ordering)
 *                     ├── PriorityQueue (heap-based, not FIFO)
 *                     ├── LinkedList (also implements Deque)
 *                     ├── ArrayDeque (Deque = double-ended queue)
 *                     └── BlockingQueue (concurrent)
 *                           ├── ArrayBlockingQueue
 *                           ├── LinkedBlockingQueue
 *                           └── PriorityBlockingQueue
 *
 *   java.util.Map (key-value pairs, SEPARATE hierarchy - does NOT extend Collection)
 *         ├── HashMap
 *         │     └── LinkedHashMap
 *         ├── TreeMap (SortedMap → NavigableMap)
 *         ├── Hashtable (legacy, synchronized)
 *         │     └── Properties
 *         ├── WeakHashMap
 *         ├── IdentityHashMap
 *         └── ConcurrentHashMap (concurrent)
 *
 * INTERVIEW Q: Why doesn't Map extend Collection?
 * A: Map stores key-VALUE pairs. Collection stores single elements.
 *    Their behaviors are fundamentally different (put vs add, get vs contains, etc.).
 *    Forcing Map into Collection hierarchy would be awkward and misleading.
 *
 * ============================================================
 * TABLE OF CONTENTS:
 *  1.  ArrayList - dynamic array implementation
 *  2.  LinkedList - doubly-linked list + Deque
 *  3.  ArrayList vs LinkedList - time complexity comparison
 *  4.  Vector and Stack - legacy thread-safe list
 *  5.  HashSet - hash-table based set
 *  6.  LinkedHashSet - insertion-ordered set
 *  7.  TreeSet - sorted set (Red-Black Tree)
 *  8.  HashMap - core internals: hash, buckets, load factor, rehash
 *  9.  HashMap collision handling - chaining -> treeification (Java 8)
 * 10.  LinkedHashMap - insertion/access ordered map
 * 11.  TreeMap - sorted map (Red-Black Tree)
 * 12.  Hashtable vs HashMap - legacy comparison
 * 13.  ConcurrentHashMap - segment locking / CAS
 * 14.  PriorityQueue - min-heap
 * 15.  ArrayDeque - double-ended queue
 * 16.  Collections utility class
 * 17.  Arrays utility class
 * 18.  Iterator, ListIterator, for-each
 * 19.  Fail-fast vs Fail-safe iterators
 * 20.  Comparable vs Comparator
 * 21.  Thread-safe collections: CopyOnWriteArrayList, BlockingQueue
 * 22.  Main demo class
 * ============================================================
 */
public class CollectionsConcepts {

    // =========================================================================
    // HELPER CLASS used across multiple sections
    // =========================================================================

    /**
     * Employee class implementing Comparable (natural order by salary).
     * Used to demonstrate Comparable, Comparator, TreeSet, TreeMap, PriorityQueue.
     *
     * INTERVIEW Q: What is Comparable?
     * A: java.lang.Comparable<T> has one method: int compareTo(T o)
     *    Return negative if this < o, 0 if equal, positive if this > o.
     *    Defines the NATURAL ORDERING of the class.
     *    e.g., String's natural order is lexicographic; Integer's is numeric.
     *
     * INTERVIEW Q: Where is Comparable used automatically?
     * A: Collections.sort(list) and Arrays.sort(arr) use natural order.
     *    TreeSet and TreeMap use natural order for elements/keys.
     *    Elements must implement Comparable or a Comparator must be provided.
     */
    static class Employee implements Comparable<Employee> {
        private int id;
        private String name;
        private double salary;
        private String department;

        public Employee(int id, String name, double salary, String department) {
            this.id = id;
            this.name = name;
            this.salary = salary;
            this.department = department;
        }

        // NATURAL ORDERING: by salary (ascending)
        @Override
        public int compareTo(Employee other) {
            // Double.compare handles NaN, Infinity correctly (preferred over subtraction)
            return Double.compare(this.salary, other.salary);
        }

        // Needed for correct behavior in HashMap/HashSet
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Employee)) return false;
            Employee e = (Employee) obj;
            return id == e.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id); // ID is unique identifier
        }

        @Override
        public String toString() {
            return String.format("Employee{id=%d, name='%s', salary=%.2f, dept='%s'}",
                                 id, name, salary, department);
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public double getSalary() { return salary; }
        public String getDepartment() { return department; }
    }

    // =========================================================================
    // SECTION 1: ARRAYLIST
    // =========================================================================

    /**
     * ARRAYLIST - dynamic resizable array
     *
     * INTERNAL WORKING:
     * - Backed by Object[] array (default capacity 10)
     * - When full: new array = oldCapacity * 1.5 + 1 (roughly 50% growth)
     * - Elements are copied to the new array (System.arraycopy)
     * - This is why add() is amortized O(1) but occasionally O(n) during resize
     *
     * TIME COMPLEXITY:
     * - get(index)           : O(1) - direct array access via index
     * - add(element)         : O(1) amortized (O(n) when resize happens)
     * - add(index, element)  : O(n) - shift right of elements right
     * - remove(index)        : O(n) - shift elements left
     * - remove(object)       : O(n) - linear scan + shift
     * - contains(object)     : O(n) - linear scan
     * - indexOf(object)      : O(n) - linear scan
     * - size()               : O(1)
     *
     * INTERVIEW Q: When to use ArrayList?
     * A: - Frequent random access (get by index)
     *    - Mostly at-end additions/removals
     *    - Iterating through entire list (cache-friendly contiguous memory)
     *    - Less frequent insertions/deletions in middle
     */
    static void demonstrateArrayList() {
        System.out.println("\n===== ARRAYLIST =====");

        // Creating ArrayList - three common ways
        ArrayList<String> list1 = new ArrayList<>();              // empty, default capacity 10
        ArrayList<String> list2 = new ArrayList<>(50);           // empty, initial capacity 50
        ArrayList<String> list3 = new ArrayList<>(Arrays.asList("A", "B", "C")); // from collection

        // Basic operations
        List<String> fruits = new ArrayList<>();
        fruits.add("Apple");           // add to end - O(1) amortized
        fruits.add("Banana");
        fruits.add("Cherry");
        fruits.add(1, "Avocado");     // add at index 1 - O(n) due to shift

        System.out.println("Fruits: " + fruits);
        System.out.println("Get index 2: " + fruits.get(2));      // O(1)
        System.out.println("Size: " + fruits.size());
        System.out.println("Contains 'Banana': " + fruits.contains("Banana")); // O(n)
        System.out.println("Index of 'Cherry': " + fruits.indexOf("Cherry"));  // O(n)

        fruits.remove("Avocado");          // remove by object - O(n)
        fruits.remove(0);                  // remove by index - O(n)
        System.out.println("After removes: " + fruits);

        // Iteration (multiple ways)
        System.out.print("For-each: ");
        for (String fruit : fruits) { System.out.print(fruit + " "); }
        System.out.println();

        // subList - returns a VIEW (backed by original list)
        // Modifications to subList affect the original!
        List<String> moreList = new ArrayList<>(Arrays.asList("A","B","C","D","E"));
        List<String> sub = moreList.subList(1, 4); // indices 1,2,3 -> B,C,D
        System.out.println("subList(1,4): " + sub);

        // Sort using natural order (Comparable)
        List<Integer> nums = new ArrayList<>(Arrays.asList(5, 1, 8, 3, 2));
        Collections.sort(nums);
        System.out.println("Sorted: " + nums);

        // Converting array <-> ArrayList
        String[] arr = { "X", "Y", "Z" };
        List<String> fromArray = new ArrayList<>(Arrays.asList(arr)); // array to list
        String[] backToArray = fromArray.toArray(new String[0]);      // list to array

        // IMPORTANT: Arrays.asList() returns a FIXED-SIZE List backed by the array
        // It does NOT return an ArrayList. You cannot add/remove from it!
        List<String> fixedList = Arrays.asList("P", "Q", "R");
        // fixedList.add("S"); // throws UnsupportedOperationException!
        System.out.println("Arrays.asList (fixed): " + fixedList);

        // List.of() (Java 9+) - completely immutable
        // List.of("X","Y") - even set() throws UnsupportedOperationException
    }

    // =========================================================================
    // SECTION 2 & 3: LINKEDLIST and ARRAYLIST vs LINKEDLIST
    // =========================================================================

    /**
     * LINKEDLIST - doubly-linked list implementing both List AND Deque
     *
     * INTERNAL WORKING:
     * - Each element stored in a Node: { E item; Node<E> next; Node<E> prev; }
     * - Head and tail pointers maintained
     * - No resizing needed (unlike ArrayList)
     *
     * TIME COMPLEXITY:
     * - get(index)           : O(n) - must traverse from head or tail
     * - add(element)         : O(1) - add to tail (just update tail pointer)
     * - add(index, element)  : O(n) - traverse to index, then O(1) pointer update
     * - remove(index)        : O(n) - traverse to index, then O(1) pointer update
     * - remove(object)       : O(n) - linear scan
     * - contains(object)     : O(n) - linear scan
     * - addFirst/addLast     : O(1) - head/tail pointer update
     * - removeFirst/removeLast: O(1)
     *
     * MEMORY: LinkedList uses more memory per element than ArrayList because
     *         each node stores two extra pointers (next + prev) + object overhead.
     *
     * INTERVIEW Q: ArrayList vs LinkedList - which is faster?
     * A: IT DEPENDS on the operation:
     *    - Random access (get by index): ArrayList O(1) vs LinkedList O(n) -> ArrayList wins
     *    - Insert/delete at beginning: LinkedList O(1) vs ArrayList O(n) -> LinkedList wins
     *    - Insert/delete at middle: Both O(n) for finding index; LinkedList O(1) after finding
     *    - Insert at end: Both ~O(1) amortized
     *    - Iteration: ArrayList faster (cache-friendly contiguous memory vs scattered nodes)
     *    IN PRACTICE: ArrayList is usually preferred because:
     *    1. Cache locality (contiguous memory is faster for CPU cache)
     *    2. Lower memory overhead per element
     *    3. Most use cases are dominated by get() and iteration
     *    Use LinkedList when: you need a Deque/Stack/Queue, or frequent insert/delete at both ends.
     */
    static void demonstrateLinkedList() {
        System.out.println("\n===== LINKEDLIST =====");

        LinkedList<String> ll = new LinkedList<>();

        // List operations (same as ArrayList interface)
        ll.add("Middle");
        ll.addFirst("First");    // O(1) - unique to LinkedList/Deque
        ll.addLast("Last");      // O(1)
        ll.add(1, "Second");

        System.out.println("LinkedList: " + ll);
        System.out.println("First: " + ll.getFirst());   // O(1)
        System.out.println("Last: " + ll.getLast());     // O(1)
        System.out.println("Get(2): " + ll.get(2));      // O(n)

        // Stack operations via LinkedList
        ll.push("OnTop");         // same as addFirst()
        System.out.println("After push: " + ll);
        System.out.println("Pop: " + ll.pop());          // same as removeFirst()

        // Queue operations via LinkedList
        ll.offer("QueueEnd");     // same as addLast()
        System.out.println("Poll (remove head): " + ll.poll()); // same as removeFirst()
        System.out.println("Peek (look at head): " + ll.peek()); // no removal

        // Deque operations
        ll.offerFirst("NewFirst");
        ll.offerLast("NewLast");
        System.out.println("After offerFirst/Last: " + ll);
    }

    // =========================================================================
    // SECTION 4: VECTOR AND STACK
    // =========================================================================

    /**
     * VECTOR - legacy thread-safe dynamic array (Java 1.0)
     *
     * INTERVIEW Q: Vector vs ArrayList?
     * A: Vector:    - Synchronized (thread-safe but slow due to locking overhead)
     *               - Growth: doubles capacity (oldCapacity * 2)
     *               - Legacy class (since Java 1.0)
     *               - All methods are synchronized
     *    ArrayList: - NOT synchronized (not thread-safe by default)
     *               - Growth: 50% increase (oldCapacity * 1.5)
     *               - Preferred (since Java 1.2)
     *               - Faster due to no synchronization overhead
     *
     * INTERVIEW Q: Should I use Vector for thread safety?
     * A: NO. Prefer: Collections.synchronizedList(new ArrayList<>())
     *             or CopyOnWriteArrayList (for read-heavy scenarios)
     *    Vector is considered LEGACY and generally not recommended.
     *
     * STACK - extends Vector (LIFO - Last In First Out)
     * INTERVIEW Q: Should I use Stack class?
     * A: NO. Prefer ArrayDeque which is faster and more complete.
     *    Stack.push/pop work fine but it inherits all Vector's indexed methods
     *    which makes it possible to violate LIFO ordering. ArrayDeque is better.
     */
    static void demonstrateVectorAndStack() {
        System.out.println("\n===== VECTOR and STACK =====");

        // Vector usage (legacy - prefer ArrayList for non-concurrent code)
        Vector<Integer> vector = new Vector<>();
        vector.add(1);
        vector.add(2);
        vector.add(3);
        System.out.println("Vector: " + vector);
        System.out.println("Vector capacity: " + vector.capacity()); // starts at 10, doubles when full

        // Stack - LIFO
        Stack<String> stack = new Stack<>();
        stack.push("First");
        stack.push("Second");
        stack.push("Third");
        System.out.println("Stack: " + stack);
        System.out.println("Peek (top): " + stack.peek());    // look without removing
        System.out.println("Pop: " + stack.pop());            // remove top
        System.out.println("Empty? " + stack.isEmpty());
        System.out.println("Search 'First': " + stack.search("First")); // 1-based position from top

        // PREFERRED: ArrayDeque as stack
        Deque<String> dequeStack = new ArrayDeque<>();
        dequeStack.push("A");    // pushes to front (addFirst)
        dequeStack.push("B");
        dequeStack.push("C");
        System.out.println("ArrayDeque as stack: " + dequeStack);
        System.out.println("Pop from ArrayDeque: " + dequeStack.pop()); // removes from front
    }

    // =========================================================================
    // SECTION 5, 6, 7: SET IMPLEMENTATIONS
    // =========================================================================

    /**
     * HASHSET
     *
     * INTERNAL WORKING:
     * - Backed by a HashMap<E, PRESENT> (where PRESENT is a dummy Object constant)
     * - add(e) internally calls map.put(e, PRESENT)
     * - All the HashMap internals apply: hash(), buckets, load factor, etc.
     *
     * TIME COMPLEXITY:
     * - add(element)     : O(1) average, O(n) worst case (all in same bucket)
     * - remove(element)  : O(1) average
     * - contains(element): O(1) average
     * - iteration        : O(n + capacity) - iterates all buckets including empty ones
     *
     * PROPERTIES:
     * - NO duplicate elements (based on equals() and hashCode())
     * - NO guaranteed order
     * - Allows ONE null element
     * - NOT thread-safe
     *
     * INTERVIEW Q: How does HashSet ensure no duplicates?
     * A: It uses equals() and hashCode(). Two objects are considered equal if
     *    equals() returns true. They must also have the same hashCode().
     *    When you add(e), if an equal element already exists, add() returns false.
     */

    /**
     * LINKEDHASHSET
     *
     * INTERNAL WORKING:
     * - Extends HashSet, backed by LinkedHashMap
     * - Maintains a doubly-linked list through all entries
     * - Preserves INSERTION ORDER (unlike HashSet)
     *
     * TIME COMPLEXITY: Same O(1) average as HashSet
     *
     * PROPERTIES:
     * - No duplicates
     * - INSERTION ORDER preserved (unlike HashSet which has no order)
     * - Slightly more memory than HashSet (linked list overhead)
     * - Allows ONE null element
     */

    /**
     * TREESET
     *
     * INTERNAL WORKING:
     * - Backed by a TreeMap<E, PRESENT>
     * - TreeMap uses a Red-Black Tree (self-balancing BST)
     * - Elements must be Comparable OR a Comparator must be provided
     *
     * TIME COMPLEXITY (all operations are O(log n) due to tree height):
     * - add(element)     : O(log n)
     * - remove(element)  : O(log n)
     * - contains(element): O(log n)
     * - first()/last()   : O(log n)
     * - iteration        : O(n) in sorted order
     *
     * PROPERTIES:
     * - No duplicates
     * - SORTED ORDER (natural order or custom Comparator)
     * - Does NOT allow null elements (compareTo(null) throws NullPointerException)
     * - NOT thread-safe
     *
     * INTERVIEW Q: HashSet vs LinkedHashSet vs TreeSet?
     * A: HashSet:       fastest O(1) ops, no order guarantee, allows null
     *    LinkedHashSet: O(1) ops, insertion order, allows null, slight memory overhead
     *    TreeSet:       O(log n) ops, always sorted, NO null allowed
     *    Use HashSet when order doesn't matter.
     *    Use LinkedHashSet when you need insertion order.
     *    Use TreeSet when you need sorted order or range operations (headSet, tailSet, subSet).
     */
    static void demonstrateSets() {
        System.out.println("\n===== SETS: HashSet, LinkedHashSet, TreeSet =====");

        // ----- HashSet -----
        Set<String> hashSet = new HashSet<>();
        hashSet.add("Banana");
        hashSet.add("Apple");
        hashSet.add("Cherry");
        hashSet.add("Apple");   // duplicate - silently ignored
        hashSet.add(null);      // null allowed

        System.out.println("HashSet (no order): " + hashSet);
        System.out.println("Contains 'Apple': " + hashSet.contains("Apple"));
        System.out.println("Contains null: " + hashSet.contains(null));
        System.out.println("Add duplicate 'Apple' result: " + hashSet.add("Apple")); // returns false

        // ----- LinkedHashSet -----
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("Banana");
        linkedHashSet.add("Apple");
        linkedHashSet.add("Cherry");
        linkedHashSet.add("Apple"); // duplicate ignored

        System.out.println("\nLinkedHashSet (insertion order): " + linkedHashSet);
        // Output: [Banana, Apple, Cherry] - INSERTION ORDER preserved

        // ----- TreeSet -----
        Set<String> treeSet = new TreeSet<>();
        treeSet.add("Banana");
        treeSet.add("Apple");
        treeSet.add("Cherry");
        treeSet.add("Avocado");

        System.out.println("\nTreeSet (sorted order): " + treeSet);
        // Output: [Apple, Avocado, Banana, Cherry] - ALPHABETICAL (natural) order

        // TreeSet - NavigableSet methods
        TreeSet<Integer> numTree = new TreeSet<>(Arrays.asList(5, 3, 8, 1, 4, 7, 9, 2, 6));
        System.out.println("TreeSet: " + numTree); // [1,2,3,4,5,6,7,8,9]
        System.out.println("First: " + numTree.first());           // 1
        System.out.println("Last: " + numTree.last());             // 9
        System.out.println("HeadSet(<5): " + numTree.headSet(5));  // [1,2,3,4] - exclusive
        System.out.println("TailSet(>=6): " + numTree.tailSet(6)); // [6,7,8,9] - inclusive
        System.out.println("SubSet(3,7): " + numTree.subSet(3, 7)); // [3,4,5,6]
        System.out.println("Floor(5): " + numTree.floor(5));       // 5 (<=5)
        System.out.println("Ceiling(5): " + numTree.ceiling(5));   // 5 (>=5)
        System.out.println("Lower(5): " + numTree.lower(5));       // 4 (strictly <5)
        System.out.println("Higher(5): " + numTree.higher(5));     // 6 (strictly >5)

        // TreeSet with Comparator (reverse order)
        TreeSet<String> reverseTree = new TreeSet<>(Comparator.reverseOrder());
        reverseTree.addAll(Arrays.asList("Banana", "Apple", "Cherry"));
        System.out.println("\nTreeSet reverse order: " + reverseTree); // [Cherry, Banana, Apple]

        // try { treeSet.add(null); } // throws NullPointerException - TreeSet doesn't allow null
    }

    // =========================================================================
    // SECTION 8 & 9: HASHMAP INTERNALS
    // =========================================================================

    /**
     * HASHMAP - the most important collection to understand for interviews
     *
     * INTERNAL WORKING (Java 8):
     * - Backed by Node<K,V>[] table array (called "buckets")
     * - DEFAULT_INITIAL_CAPACITY = 16 (must be power of 2)
     * - DEFAULT_LOAD_FACTOR = 0.75 (75% full -> resize)
     * - TREEIFY_THRESHOLD = 8 (bucket converts to TreeMap when chain length >= 8)
     * - UNTREEIFY_THRESHOLD = 6 (TreeMap converts back to linked list when size <= 6)
     * - MIN_TREEIFY_CAPACITY = 64 (table must be at least 64 buckets before treeification)
     *
     * PUT OPERATION - step by step:
     * 1. key.hashCode() is called -> raw hash
     * 2. hash() method applies spreading: (h = key.hashCode()) ^ (h >>> 16)
     *    WHY: Mixes high bits into low bits to reduce clustering in small tables.
     * 3. bucketIndex = hash & (capacity - 1)
     *    WHY power of 2: bitwise AND is faster than modulo, and capacity-1 masks low bits.
     * 4. If bucket is empty: create new Node, place it.
     * 5. If bucket has entries (COLLISION):
     *    a. Check existing entries with equals(). If match found: UPDATE value.
     *    b. If no match: APPEND new Node to end of linked list (Java 8: TAIL insertion)
     *       Before Java 8: HEAD insertion (caused infinite loop in concurrent access)
     *    c. After insertion: if chain length >= TREEIFY_THRESHOLD (8) and
     *       table.length >= MIN_TREEIFY_CAPACITY (64): convert bucket to TreeNode (Red-Black Tree)
     * 6. If size > threshold (capacity * loadFactor): REHASH
     *    - New array double the size (capacity * 2)
     *    - All entries redistributed (expensive: O(n))
     *
     * GET OPERATION - step by step:
     * 1. key.hashCode() -> hash
     * 2. Compute bucket index
     * 3. If bucket has TreeNode: O(log n) tree search
     * 4. If bucket has linked list: O(k) linear scan using equals()
     *    where k = number of entries in that bucket
     *
     * TIME COMPLEXITY:
     * - get(key)    : O(1) average, O(log n) worst case (Java 8 treeification)
     * - put(key)    : O(1) average, O(log n) worst case
     * - remove(key) : O(1) average, O(log n) worst case
     * - containsKey : O(1) average
     * - containsValue: O(n) - must scan all entries!
     * - Iteration   : O(n + capacity)
     *
     * INTERVIEW Q: What is load factor in HashMap?
     * A: Load factor determines when to resize (rehash).
     *    threshold = capacity * loadFactor
     *    When size > threshold: capacity doubles, all entries rehashed.
     *    Default: 0.75 is a good balance between space and time.
     *    Lower load factor (e.g., 0.5): faster gets (fewer collisions) but more memory.
     *    Higher load factor (e.g., 0.9): less memory but more collisions and slower gets.
     *
     * INTERVIEW Q: What happens if two keys have same hashCode?
     * A: That's a HASH COLLISION. Java handles it via SEPARATE CHAINING:
     *    Both entries go in the same bucket. A linked list (or tree in Java 8) chains them.
     *    During get(), equals() is used to find the exact key among chained entries.
     *
     * INTERVIEW Q: What if hashCode() always returns same value?
     * A: All entries go to same bucket. Get/put degrades to O(n) linear scan.
     *    This is a hash flooding attack - deliberately crafted keys to degrade performance.
     *    Java 8 mitigates this: after 8 collisions per bucket, converts to O(log n) tree.
     *
     * INTERVIEW Q: Can HashMap have null keys?
     * A: YES! Exactly ONE null key allowed. null key is always stored in bucket[0].
     *    null values: unlimited null values allowed.
     *    Hashtable: does NOT allow null keys or null values (throws NullPointerException).
     *
     * INTERVIEW Q: Why must we override hashCode() when overriding equals()?
     * A: The CONTRACT: if a.equals(b) then a.hashCode() == b.hashCode().
     *    If violated: two "equal" objects could land in different buckets,
     *    so get(key) would find the wrong bucket and return null even though key exists!
     */
    static void demonstrateHashMap() {
        System.out.println("\n===== HASHMAP INTERNALS =====");

        // Creating HashMap
        Map<String, Integer> map = new HashMap<>();           // default capacity 16, load factor 0.75
        Map<String, Integer> map2 = new HashMap<>(32);       // initial capacity 32
        Map<String, Integer> map3 = new HashMap<>(32, 0.5f); // capacity 32, load factor 0.5

        // Basic operations
        map.put("Alice", 90);
        map.put("Bob", 85);
        map.put("Charlie", 92);
        map.put("Alice", 95);   // UPDATE: duplicate key replaces old value
        map.put(null, 0);       // null key allowed - stored in bucket[0]

        System.out.println("Map: " + map);
        System.out.println("Get 'Bob': " + map.get("Bob"));          // 85
        System.out.println("Get null key: " + map.get(null));         // 0
        System.out.println("Get nonexistent: " + map.get("Dave"));    // null
        System.out.println("ContainsKey 'Charlie': " + map.containsKey("Charlie")); // true
        System.out.println("ContainsValue 92: " + map.containsValue(92));           // O(n)!

        // Java 8+ methods - very useful in interviews
        // getOrDefault: returns default if key not present
        System.out.println("getOrDefault 'Dave': " + map.getOrDefault("Dave", -1)); // -1

        // putIfAbsent: put only if key is not present (or mapped to null)
        map.putIfAbsent("Alice", 100); // Alice already exists -> no change
        map.putIfAbsent("Dave", 88);   // Dave doesn't exist -> inserted
        System.out.println("After putIfAbsent: " + map.get("Alice") + ", " + map.get("Dave"));

        // computeIfAbsent: compute and put only if key absent
        map.computeIfAbsent("Eve", k -> k.length() * 10); // Eve not present -> put 30
        System.out.println("ComputeIfAbsent 'Eve': " + map.get("Eve"));

        // computeIfPresent: compute only if key is present
        map.computeIfPresent("Alice", (k, v) -> v + 5); // Alice present -> update 95+5=100
        System.out.println("ComputeIfPresent 'Alice': " + map.get("Alice"));

        // compute: always compute (insert or update)
        map.compute("Frank", (k, v) -> v == null ? 1 : v + 1); // v is null initially -> 1
        System.out.println("Compute 'Frank': " + map.get("Frank"));

        // merge: merge with existing value using a function
        map.merge("Bob", 5, Integer::sum); // Bob has 85, merge adds 5 -> 90
        System.out.println("Merge 'Bob' (+5): " + map.get("Bob"));

        // Iterating over HashMap (multiple ways)
        System.out.println("\nIterating HashMap:");

        // 1. entrySet (most efficient for key+value access)
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }

        // 2. keySet (if you need keys and then get values)
        System.out.print("KeySet: ");
        for (String key : map.keySet()) { System.out.print(key + " "); }
        System.out.println();

        // 3. values (if you only need values)
        System.out.print("Values: ");
        for (int val : map.values()) { System.out.print(val + " "); }
        System.out.println();

        // 4. forEach (Java 8 - cleanest)
        System.out.println("forEach:");
        map.forEach((k, v) -> System.out.println("  " + k + " => " + v));

        // Frequency counting - classic HashMap interview problem
        System.out.println("\nFrequency count example:");
        String sentence = "the quick brown fox jumps over the lazy dog the fox";
        Map<String, Integer> freq = new HashMap<>();
        for (String word : sentence.split(" ")) {
            freq.merge(word, 1, Integer::sum); // elegant frequency counting
        }
        System.out.println("Word frequencies: " + freq);
    }

    // =========================================================================
    // SECTION 10: LINKEDHASHMAP
    // =========================================================================

    /**
     * LINKEDHASHMAP - HashMap + insertion/access order
     *
     * INTERNAL WORKING:
     * - Extends HashMap, adds doubly-linked list through all entries
     * - Two ordering modes:
     *   1. INSERTION ORDER (default): maintains the order entries were added
     *   2. ACCESS ORDER (accessOrder=true): recently accessed entries moved to tail
     *      This enables LRU (Least Recently Used) cache implementation!
     *
     * TIME COMPLEXITY: Same as HashMap O(1) average
     *
     * INTERVIEW Q: How to implement LRU cache using LinkedHashMap?
     * A: Use LinkedHashMap with accessOrder=true and override removeEldestEntry().
     *    When cache is full, removeEldestEntry() returns true -> eldest (LRU) entry removed.
     */
    static void demonstrateLinkedHashMap() {
        System.out.println("\n===== LINKEDHASHMAP =====");

        // Insertion order (default)
        Map<String, Integer> insertionOrder = new LinkedHashMap<>();
        insertionOrder.put("Charlie", 3);
        insertionOrder.put("Alice", 1);
        insertionOrder.put("Bob", 2);
        System.out.println("Insertion order: " + insertionOrder); // Charlie, Alice, Bob

        // Access order
        Map<String, Integer> accessOrder = new LinkedHashMap<>(16, 0.75f, true);
        accessOrder.put("one", 1);
        accessOrder.put("two", 2);
        accessOrder.put("three", 3);
        accessOrder.get("one");   // Access "one" -> moves it to tail
        accessOrder.get("three"); // Access "three" -> moves it to tail
        System.out.println("Access order after gets: " + accessOrder);
        // [two=2, one=1, three=3] - "two" least recently used, "three" most recently used

        // LRU CACHE implementation using LinkedHashMap
        int cacheCapacity = 3;
        // This anonymous class extends LinkedHashMap and overrides removeEldestEntry
        Map<Integer, String> lruCache = new LinkedHashMap<Integer, String>(
                cacheCapacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                // When map size exceeds capacity, remove the eldest (LRU) entry
                return size() > cacheCapacity;
            }
        };

        lruCache.put(1, "Page1");
        lruCache.put(2, "Page2");
        lruCache.put(3, "Page3");
        System.out.println("LRU Cache: " + lruCache);          // {1=Page1, 2=Page2, 3=Page3}
        lruCache.get(1);                                        // Access page 1 (makes it recent)
        lruCache.put(4, "Page4");                              // Cache full, evict LRU (Page2)
        System.out.println("After adding Page4: " + lruCache); // {3=Page3, 1=Page1, 4=Page4}
        // Page2 was evicted because it was least recently used
    }

    // =========================================================================
    // SECTION 11: TREEMAP
    // =========================================================================

    /**
     * TREEMAP - sorted map backed by Red-Black Tree
     *
     * INTERNAL WORKING:
     * - Red-Black Tree: self-balancing binary search tree
     * - Guarantees O(log n) for all basic operations
     * - Keys must implement Comparable OR a Comparator must be provided
     *
     * TIME COMPLEXITY (all O(log n)):
     * - put(key)        : O(log n)
     * - get(key)        : O(log n)
     * - remove(key)     : O(log n)
     * - containsKey     : O(log n)
     * - firstKey/lastKey: O(log n)
     *
     * PROPERTIES:
     * - Keys in SORTED ORDER (natural or custom Comparator)
     * - Does NOT allow null keys (compareTo(null) -> NPE)
     * - Allows null values
     * - NOT thread-safe
     * - Implements NavigableMap: headMap, tailMap, subMap, floorKey, ceilingKey, etc.
     *
     * INTERVIEW Q: HashMap vs TreeMap vs LinkedHashMap?
     * A: HashMap:       O(1) ops, no order, allows null key, fastest for get/put
     *    LinkedHashMap: O(1) ops, insertion/access order, allows null key
     *    TreeMap:       O(log n) ops, always sorted, no null key, use for range queries
     */
    static void demonstrateTreeMap() {
        System.out.println("\n===== TREEMAP =====");

        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("Charlie", 3);
        treeMap.put("Alice", 1);
        treeMap.put("Bob", 2);
        treeMap.put("Diana", 4);
        treeMap.put("Eve", 5);

        System.out.println("TreeMap (sorted): " + treeMap); // sorted by key
        System.out.println("FirstKey: " + treeMap.firstKey());                       // Alice
        System.out.println("LastKey: " + treeMap.lastKey());                         // Eve
        System.out.println("HeadMap(<Charlie): " + treeMap.headMap("Charlie"));       // {Alice=1, Bob=2}
        System.out.println("TailMap(>=Charlie): " + treeMap.tailMap("Charlie"));      // {Charlie=3,...}
        System.out.println("SubMap(Bob,Diana): " + treeMap.subMap("Bob", "Diana"));   // [Bob, Diana)
        System.out.println("FloorKey(Cathy): " + treeMap.floorKey("Cathy"));          // Charlie? No: Bob
        System.out.println("CeilingKey(Cathy): " + treeMap.ceilingKey("Cathy"));      // Charlie

        // Reverse order
        TreeMap<String, Integer> reverseMap = new TreeMap<>(Comparator.reverseOrder());
        reverseMap.putAll(treeMap);
        System.out.println("Reverse TreeMap: " + reverseMap);
    }

    // =========================================================================
    // SECTION 12: HASHTABLE vs HASHMAP
    // =========================================================================

    /**
     * HASHTABLE vs HASHMAP
     *
     * INTERVIEW Q: What are the differences between Hashtable and HashMap?
     * A:
     * Feature          | HashMap           | Hashtable
     * -----------------|-------------------|--------------------
     * Thread-safe      | NO                | YES (synchronized)
     * Null keys        | 1 null key        | NO null keys
     * Null values      | Multiple          | NO null values
     * Performance      | Faster            | Slower (sync overhead)
     * Inheritance      | AbstractMap       | Dictionary (legacy)
     * Iterator         | Fail-fast         | Enumerator (legacy)
     * Introduced       | Java 1.2          | Java 1.0
     * Recommendation   | Use HashMap       | AVOID (use ConcurrentHashMap)
     *
     * INTERVIEW Q: Why is Hashtable not recommended?
     * A: 1. Synchronized at method level (overly coarse-grained locking)
     *    2. Legacy class with legacy Enumeration iterator
     *    3. ConcurrentHashMap is thread-safe AND faster (segment/CAS-based locking)
     *    4. For non-concurrent use: HashMap is faster
     */
    static void demonstrateHashtable() {
        System.out.println("\n===== HASHTABLE vs HASHMAP =====");

        // Hashtable (legacy - avoid in new code)
        Hashtable<String, Integer> hashtable = new Hashtable<>();
        hashtable.put("one", 1);
        hashtable.put("two", 2);
        // hashtable.put(null, 3);    // NullPointerException!
        // hashtable.put("three", null); // NullPointerException!
        System.out.println("Hashtable: " + hashtable);

        // HashMap - preferred for non-concurrent
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put(null, 0);       // null key OK
        hashMap.put("one", null);   // null value OK
        hashMap.put("two", 2);
        System.out.println("HashMap (with nulls): " + hashMap);
    }

    // =========================================================================
    // SECTION 13: CONCURRENTHASHMAP
    // =========================================================================

    /**
     * CONCURRENTHASHMAP - thread-safe, high-performance map
     *
     * INTERNAL WORKING (Java 8):
     * - Uses CAS (Compare-And-Swap) operations for thread safety without locking
     * - For reads: completely lock-free (volatile reads)
     * - For writes: synchronized only on the specific bucket (node-level locking)
     *   NOT the entire map - this is why it's much faster than Hashtable
     * - Before Java 8: used segment-based locking (16 segments by default)
     *   Java 8: replaced with CAS + synchronized on individual nodes
     *
     * INTERVIEW Q: ConcurrentHashMap vs Hashtable vs synchronized HashMap?
     * A: Hashtable:              synchronized ALL methods on 'this' - full lock
     *    Collections.synchronizedMap: wraps map, synchronized on wrapper object - full lock
     *    ConcurrentHashMap:     bucket-level locking / CAS - concurrent reads, fine-grained writes
     *    For concurrent use: ConcurrentHashMap is ALWAYS preferred.
     *
     * INTERVIEW Q: Does ConcurrentHashMap allow null keys or values?
     * A: NO! Neither null keys nor null values are allowed.
     *    WHY: In concurrent context, get(key) returning null is ambiguous:
     *    does it mean key doesn't exist, or key exists with null value?
     *    This ambiguity doesn't exist in HashMap because you can call containsKey(),
     *    but in concurrent map, state can change between get() and containsKey().
     *
     * INTERVIEW Q: Is ConcurrentHashMap fully consistent?
     * A: NO. It provides WEAKLY CONSISTENT guarantees:
     *    - size() returns an approximate count (not exactly accurate under concurrent updates)
     *    - Iterators reflect state at some point in time (may not see concurrent updates)
     *    - Bulk operations (putAll) are not atomic
     */
    static void demonstrateConcurrentHashMap() {
        System.out.println("\n===== CONCURRENTHASHMAP =====");

        ConcurrentHashMap<String, AtomicInteger> wordCount = new ConcurrentHashMap<>();

        // Thread-safe word counting (a common interview use case)
        String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
        for (String word : words) {
            // computeIfAbsent is atomic: if absent, create new AtomicInteger(0)
            wordCount.computeIfAbsent(word, k -> new AtomicInteger(0)).incrementAndGet();
        }
        System.out.println("Word counts: " + wordCount);

        // Atomic operations
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("counter", 0);

        // putIfAbsent is atomic in ConcurrentHashMap (crucial for thread safety)
        map.putIfAbsent("newKey", 42); // atomic check-then-put
        System.out.println("ConcurrentHashMap: " + map);

        // null not allowed
        try {
            map.put(null, 1); // NullPointerException
        } catch (NullPointerException e) {
            System.out.println("ConcurrentHashMap does NOT allow null keys: " + e.getClass().getSimpleName());
        }
    }

    // =========================================================================
    // SECTION 14: PRIORITYQUEUE
    // =========================================================================

    /**
     * PRIORITYQUEUE - heap-based priority queue
     *
     * INTERNAL WORKING:
     * - Backed by Object[] array representing a BINARY HEAP
     * - MIN-HEAP by default: smallest element is always at the top (head)
     * - Heap property: parent is always <= children (for min-heap)
     * - After add: element bubbled UP (sift up) to maintain heap property
     * - After poll: heap rebuilt by sifting down
     *
     * TIME COMPLEXITY:
     * - offer/add    : O(log n) - sift up
     * - poll/remove  : O(log n) - sift down
     * - peek         : O(1) - just return root
     * - contains     : O(n) - no ordering guarantee except min at root
     * - size()       : O(1)
     *
     * PROPERTIES:
     * - NOT a sorted list - only guarantees smallest at head
     * - Does NOT guarantee order among non-head elements
     * - Does NOT allow null elements
     * - NOT thread-safe (use PriorityBlockingQueue for concurrent use)
     * - Elements must implement Comparable OR provide Comparator
     *
     * INTERVIEW Q: PriorityQueue vs TreeSet?
     * A: PriorityQueue: duplicates allowed, O(log n) insert/remove, O(1) peek at min
     *    TreeSet:        no duplicates, O(log n) all ops, can find any element in O(log n)
     *    Use PriorityQueue for Dijkstra's algorithm, scheduling, k-largest/smallest problems.
     */
    static void demonstratePriorityQueue() {
        System.out.println("\n===== PRIORITYQUEUE (Min-Heap) =====");

        // Min-heap (default) - smallest element polled first
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.offer(5);
        minHeap.offer(1);
        minHeap.offer(8);
        minHeap.offer(3);
        minHeap.offer(2);

        System.out.print("Min-heap poll order: ");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.poll() + " "); // 1, 2, 3, 5, 8
        }
        System.out.println();

        // Max-heap - using Comparator.reverseOrder()
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.addAll(Arrays.asList(5, 1, 8, 3, 2));

        System.out.print("Max-heap poll order: ");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " "); // 8, 5, 3, 2, 1
        }
        System.out.println();

        // PriorityQueue with custom objects
        PriorityQueue<Employee> empQueue = new PriorityQueue<>(); // uses Employee.compareTo() (salary)
        empQueue.offer(new Employee(1, "Alice", 75000, "Engineering"));
        empQueue.offer(new Employee(2, "Bob", 60000, "Marketing"));
        empQueue.offer(new Employee(3, "Charlie", 90000, "Engineering"));
        empQueue.offer(new Employee(4, "Diana", 55000, "HR"));

        System.out.println("Employees by salary (min-heap poll order):");
        while (!empQueue.isEmpty()) {
            Employee e = empQueue.poll();
            System.out.println("  " + e.getName() + ": $" + e.getSalary());
        }

        // K-largest problem (common interview question)
        // Find k-largest elements from a stream using min-heap of size k
        int k = 3;
        int[] numbers = {3, 1, 5, 12, 2, 11, 0, 8};
        PriorityQueue<Integer> kLargest = new PriorityQueue<>(k); // min-heap of size k
        for (int num : numbers) {
            kLargest.offer(num);
            if (kLargest.size() > k) {
                kLargest.poll(); // remove smallest - keeps k largest
            }
        }
        System.out.println(k + " largest elements: " + kLargest); // not sorted but all in heap
    }

    // =========================================================================
    // SECTION 15: ARRAYDEQUE
    // =========================================================================

    /**
     * ARRAYDEQUE - resizable array implementing Deque (double-ended queue)
     *
     * INTERNAL WORKING:
     * - Backed by Object[] array (circular/ring buffer)
     * - head and tail pointers into the circular array
     * - When full: doubles capacity
     * - NO capacity restriction (unlike ArrayBlockingQueue)
     *
     * TIME COMPLEXITY:
     * - addFirst/addLast  : O(1) amortized
     * - removeFirst/removeLast: O(1)
     * - peekFirst/peekLast: O(1)
     * - contains         : O(n)
     * - get(index)       : NOT SUPPORTED (no random access by index)
     *
     * INTERVIEW Q: ArrayDeque vs LinkedList for stack/queue?
     * A: ArrayDeque is FASTER for both stack and queue use cases because:
     *    1. No node object creation overhead (unlike LinkedList which creates Node per element)
     *    2. Better cache locality (contiguous array vs scattered nodes)
     *    3. Less memory overhead per element
     *    Use ArrayDeque as: Stack (addFirst/removeFirst) or Queue (addLast/removeFirst)
     *
     * INTERVIEW Q: ArrayDeque vs Stack class?
     * A: ArrayDeque is preferred because:
     *    1. Stack extends Vector (synchronized overhead even when not needed)
     *    2. Stack inherits all Vector's indexed methods (can violate LIFO)
     *    3. ArrayDeque is faster and follows Deque interface
     */
    static void demonstrateArrayDeque() {
        System.out.println("\n===== ARRAYDEQUE (Deque) =====");

        ArrayDeque<String> deque = new ArrayDeque<>();

        // As a DEQUE (double-ended queue)
        deque.addFirst("Middle");
        deque.addFirst("First");
        deque.addLast("Last");
        deque.offerFirst("VeryFirst");
        deque.offerLast("VeryLast");

        System.out.println("Deque: " + deque);
        System.out.println("PeekFirst: " + deque.peekFirst()); // VeryFirst
        System.out.println("PeekLast: " + deque.peekLast());   // VeryLast

        // Remove from both ends
        System.out.println("PollFirst: " + deque.pollFirst()); // VeryFirst
        System.out.println("PollLast: " + deque.pollLast());   // VeryLast
        System.out.println("After polls: " + deque);

        // As a STACK (LIFO)
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);  // addFirst
        stack.push(2);  // addFirst
        stack.push(3);  // addFirst
        System.out.println("\nStack (ArrayDeque): " + stack); // [3, 2, 1]
        System.out.println("Pop: " + stack.pop());   // removeFirst -> 3
        System.out.println("Peek: " + stack.peek()); // peekFirst -> 2

        // As a QUEUE (FIFO)
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("First in");   // addLast
        queue.offer("Second in");
        queue.offer("Third in");
        System.out.println("\nQueue (ArrayDeque): " + queue);
        System.out.println("Poll (FIFO): " + queue.poll()); // removeFirst -> "First in"
        System.out.println("Peek: " + queue.peek());        // "Second in"

        // Difference: offer vs add, poll vs remove, peek vs element
        // offer/poll/peek: return false/null/null on failure (no exception)
        // add/remove/element: throw exception on failure
        // For queues, prefer offer/poll/peek for graceful failure handling
    }

    // =========================================================================
    // SECTION 16: COLLECTIONS UTILITY CLASS
    // =========================================================================

    /**
     * COLLECTIONS UTILITY CLASS - static helper methods for collections
     *
     * INTERVIEW Q: Collections.sort() vs List.sort() vs Arrays.sort()?
     * A: Collections.sort(list): uses TimSort (merge+insertion sort hybrid), O(n log n), stable
     *    List.sort(comparator):  same algorithm, Java 8+ preferred way
     *    Arrays.sort(primitives): uses Dual-Pivot Quicksort, O(n log n) average
     *    Arrays.sort(objects):   uses TimSort, O(n log n), stable
     *
     * INTERVIEW Q: What is a stable sort?
     * A: Equal elements maintain their RELATIVE ORDER from the original list.
     *    TimSort is stable; Quicksort is not stable.
     *    Stability matters when sorting by multiple fields in sequence.
     */
    static void demonstrateCollectionsUtility() {
        System.out.println("\n===== COLLECTIONS UTILITY CLASS =====");

        List<Integer> nums = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3));

        // sort - uses TimSort, O(n log n), stable, modifies list in-place
        Collections.sort(nums);
        System.out.println("Sorted: " + nums);

        // sort with Comparator (reverse order)
        Collections.sort(nums, Comparator.reverseOrder());
        System.out.println("Reverse sorted: " + nums);

        // binarySearch - MUST be sorted first! Returns index or (-(insertion point) - 1)
        Collections.sort(nums); // sort ascending for binarySearch
        int idx = Collections.binarySearch(nums, 5);
        System.out.println("BinarySearch for 5: index " + idx);
        int notFound = Collections.binarySearch(nums, 7);
        System.out.println("BinarySearch for 7 (not found): " + notFound); // negative value

        // reverse - reverses the order
        Collections.reverse(nums);
        System.out.println("Reversed: " + nums);

        // shuffle - random permutation (useful for randomizing test data)
        Collections.shuffle(nums, new Random(42)); // with seed for reproducibility
        System.out.println("Shuffled: " + nums);

        // min and max - O(n) linear scan
        System.out.println("Min: " + Collections.min(nums));
        System.out.println("Max: " + Collections.max(nums));

        // frequency - count occurrences
        List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "apple");
        System.out.println("Frequency of 'apple': " + Collections.frequency(words, "apple")); // 3

        // fill - replace all elements with a value
        List<String> filled = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Collections.fill(filled, "X");
        System.out.println("Filled: " + filled); // [X, X, X, X]

        // nCopies - create list with n copies of an element
        List<String> copies = Collections.nCopies(5, "Hello");
        System.out.println("nCopies: " + copies); // [Hello, Hello, Hello, Hello, Hello]

        // swap - swap two elements at given indices
        List<String> swapList = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Collections.swap(swapList, 0, 3);
        System.out.println("After swap(0,3): " + swapList); // [d, b, c, a]

        // rotate - rotate list by specified distance
        List<Integer> rotateList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.rotate(rotateList, 2); // rotate right by 2
        System.out.println("After rotate(2): " + rotateList); // [4, 5, 1, 2, 3]

        // disjoint - true if two collections have NO elements in common
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        List<Integer> list2 = Arrays.asList(4, 5, 6);
        List<Integer> list3 = Arrays.asList(3, 4, 5);
        System.out.println("list1 disjoint list2: " + Collections.disjoint(list1, list2)); // true
        System.out.println("list1 disjoint list3: " + Collections.disjoint(list1, list3)); // false (3 in common)

        // THREAD-SAFE WRAPPERS
        // synchronizedList, synchronizedMap, synchronizedSet, etc.
        // These wrap the collection and synchronize all method calls on the wrapper object
        // NOTE: Iterator is NOT thread-safe even with synchronized wrapper - must lock externally
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        syncList.add("Thread-safe add");
        // Iterator usage must be in synchronized block:
        // synchronized (syncList) { for (String s : syncList) { ... } }

        // UNMODIFIABLE WRAPPERS - throws UnsupportedOperationException on modification
        List<String> modifiable = new ArrayList<>(Arrays.asList("A", "B", "C"));
        List<String> unmodifiable = Collections.unmodifiableList(modifiable);
        // unmodifiable.add("D"); // UnsupportedOperationException!
        System.out.println("Unmodifiable list: " + unmodifiable);
        // NOTE: It's a VIEW - if underlying modifiable list changes, unmodifiable reflects it!
        // Use List.copyOf() (Java 10+) for a truly independent unmodifiable copy.

        // EMPTY COLLECTIONS - singleton immutable empty collections
        List<String> emptyList = Collections.emptyList();
        Set<Integer> emptySet = Collections.emptySet();
        Map<String, Integer> emptyMap = Collections.emptyMap();
        System.out.println("Empty list: " + emptyList); // []

        // SINGLETON COLLECTIONS - immutable single-element collections
        List<String> singletonList = Collections.singletonList("only");
        Set<Integer> singletonSet = Collections.singleton(42);
        System.out.println("Singleton list: " + singletonList);
    }

    // =========================================================================
    // SECTION 17: ARRAYS UTILITY CLASS
    // =========================================================================

    /**
     * ARRAYS UTILITY CLASS - static methods for arrays
     */
    static void demonstrateArraysUtility() {
        System.out.println("\n===== ARRAYS UTILITY CLASS =====");

        int[] arr = {5, 3, 8, 1, 9, 2, 7, 4, 6};

        // sort - primitives use Dual-Pivot Quicksort (not stable)
        //       objects use TimSort (stable)
        Arrays.sort(arr);
        System.out.println("Sorted: " + Arrays.toString(arr));

        // binarySearch (array must be sorted)
        int pos = Arrays.binarySearch(arr, 7);
        System.out.println("BinarySearch for 7: index " + pos);

        // fill
        int[] filled = new int[5];
        Arrays.fill(filled, 99);
        System.out.println("Filled: " + Arrays.toString(filled));

        // copyOf and copyOfRange
        int[] copy = Arrays.copyOf(arr, 5);           // first 5 elements
        int[] rangeCopy = Arrays.copyOfRange(arr, 2, 6); // indices 2 to 5
        System.out.println("copyOf(5): " + Arrays.toString(copy));
        System.out.println("copyOfRange(2,6): " + Arrays.toString(rangeCopy));

        // equals and deepEquals
        int[] a1 = {1, 2, 3};
        int[] a2 = {1, 2, 3};
        System.out.println("Arrays.equals: " + Arrays.equals(a1, a2)); // true (element-wise)
        System.out.println("a1 == a2: " + (a1 == a2));                 // false (different refs)

        // 2D arrays need deepEquals
        int[][] matrix1 = {{1, 2}, {3, 4}};
        int[][] matrix2 = {{1, 2}, {3, 4}};
        System.out.println("deepEquals 2D: " + Arrays.deepEquals(matrix1, matrix2)); // true
        System.out.println("deepToString: " + Arrays.deepToString(matrix1)); // [[1, 2], [3, 4]]

        // asList - returns fixed-size List backed by array (not a true ArrayList!)
        String[] strArr = {"a", "b", "c"};
        List<String> asList = Arrays.asList(strArr);
        // asList.add("d"); // UnsupportedOperationException - fixed size!
        asList.set(0, "Z"); // set() is OK (same size), just can't add/remove
        System.out.println("After set on asList: " + Arrays.toString(strArr)); // [Z, b, c] - original array changed!
        // WHY: asList() returns a VIEW backed by the original array. Changes reflect both ways.

        // parallelSort (Java 8+) - uses fork/join parallelism, faster for large arrays
        int[] largeArr = new Random(42).ints(1000, 0, 10000).toArray();
        Arrays.parallelSort(largeArr); // uses multiple threads for large arrays
        System.out.println("ParallelSort done. First 5: " +
            Arrays.toString(Arrays.copyOf(largeArr, 5)));
    }

    // =========================================================================
    // SECTION 18 & 19: ITERATORS AND FAIL-FAST vs FAIL-SAFE
    // =========================================================================

    /**
     * ITERATOR - standard way to traverse collections
     *
     * INTERVIEW Q: What is an Iterator?
     * A: java.util.Iterator<E> has three methods:
     *    - hasNext(): returns true if more elements remain
     *    - next(): returns next element and advances cursor
     *    - remove(): removes last element returned by next() (optional operation)
     *
     * INTERVIEW Q: Iterator vs ListIterator?
     * A: Iterator:     - Works on any Collection (Set, List, Queue)
     *                  - Forward traversal only
     *                  - Only remove() operation
     *    ListIterator: - Works only on List
     *                  - Forward AND backward traversal (hasPrevious, previous)
     *                  - add(), set(), remove() operations
     *                  - Can get current index (nextIndex, previousIndex)
     *
     * FAIL-FAST ITERATORS:
     * - Used by: ArrayList, HashMap, HashSet, TreeMap, LinkedList (java.util package)
     * - Throws ConcurrentModificationException if collection is STRUCTURALLY MODIFIED
     *   (add/remove - NOT set/replace) during iteration (by any thread or even same thread)
     * - INTERNAL: uses modCount field in collection. Iterator captures modCount at creation.
     *   On each next(), iterator checks current modCount == expected modCount.
     *   If different: ConcurrentModificationException thrown.
     * - NOT guaranteed to throw in all cases - "best effort" basis.
     *
     * FAIL-SAFE ITERATORS:
     * - Used by: CopyOnWriteArrayList, ConcurrentHashMap, CopyOnWriteArraySet
     * - Works on a SNAPSHOT copy of the collection (CopyOnWriteArrayList)
     *   OR weakly-consistent traversal (ConcurrentHashMap)
     * - NEVER throws ConcurrentModificationException
     * - Trade-off: may not reflect recent modifications, higher memory (for copy-on-write)
     *
     * INTERVIEW Q: What is the "for-each" loop internally?
     * A: for (Type t : collection) -> uses collection.iterator() internally.
     *    So it can also throw ConcurrentModificationException if collection is modified.
     *    The class must implement Iterable<T> (has iterator() method).
     */
    static void demonstrateIterators() {
        System.out.println("\n===== ITERATORS AND FAIL-FAST vs FAIL-SAFE =====");

        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));

        // Standard Iterator
        System.out.print("Iterator traversal: ");
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
            System.out.print(s + " ");
            if (s.equals("C")) {
                it.remove(); // SAFE: using iterator's own remove()
                // list.remove(s); // would throw ConcurrentModificationException!
            }
        }
        System.out.println("\nAfter iterator removal: " + list);

        // ListIterator - forward and backward
        List<Integer> numList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        ListIterator<Integer> lit = numList.listIterator(numList.size()); // start at end

        System.out.print("ListIterator backward: ");
        while (lit.hasPrevious()) {
            System.out.print(lit.previous() + " ");
        }
        System.out.println();

        // Forward with ListIterator - add and set
        ListIterator<String> litStr = list.listIterator();
        while (litStr.hasNext()) {
            String s = litStr.next();
            litStr.set(s.toLowerCase()); // replace current element - SAFE
        }
        System.out.println("After litStr.set: " + list);

        // FAIL-FAST demonstration
        System.out.println("\nFail-fast Iterator:");
        List<String> failFastList = new ArrayList<>(Arrays.asList("X", "Y", "Z"));
        Iterator<String> failFastIt = failFastList.iterator();
        failFastIt.next(); // advance
        failFastList.add("W"); // structural modification!
        try {
            failFastIt.next(); // ConcurrentModificationException!
        } catch (ConcurrentModificationException e) {
            System.out.println("Caught ConcurrentModificationException (fail-fast)!");
        }

        // FAIL-SAFE demonstration using CopyOnWriteArrayList
        System.out.println("\nFail-safe Iterator (CopyOnWriteArrayList):");
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(
            Arrays.asList("P", "Q", "R"));
        Iterator<String> cowIt = cowList.iterator(); // snapshot taken here
        cowList.add("S"); // modify the original list
        System.out.print("Iterator sees: ");
        while (cowIt.hasNext()) {
            System.out.print(cowIt.next() + " "); // iterates snapshot: P, Q, R (NOT S)
        }
        System.out.println("\nActual list: " + cowList); // [P, Q, R, S]
        System.out.println("Note: fail-safe iterator didn't see 'S' added after it was created");

        // SAFE ways to remove while iterating:
        // 1. Use Iterator.remove()
        // 2. Use removeIf() (Java 8+)
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        numbers.removeIf(n -> n % 2 == 0); // removes even numbers safely
        System.out.println("After removeIf(even): " + numbers);

        // 3. Collect to new list
        // 4. Use CopyOnWriteArrayList if concurrent modification expected
    }

    // =========================================================================
    // SECTION 20: COMPARABLE vs COMPARATOR
    // =========================================================================

    /**
     * COMPARABLE vs COMPARATOR
     *
     * INTERVIEW Q: What is Comparable?
     * A: java.lang.Comparable<T> - ONE method: int compareTo(T o)
     *    Defines NATURAL ORDERING of a class.
     *    Implemented BY the class whose instances are being compared.
     *    Used by: Collections.sort(list), Arrays.sort(arr), TreeSet, TreeMap (as default)
     *    Rule: compareTo MUST be consistent with equals() for TreeSet/TreeMap to work correctly.
     *
     * INTERVIEW Q: What is Comparator?
     * A: java.util.Comparator<T> - ONE method: int compare(T o1, T o2) (functional interface)
     *    Defines CUSTOM ORDERING external to the class.
     *    Used when: a) You can't modify the class (third-party), b) Need multiple sort orders.
     *    Can be passed to: Collections.sort, Arrays.sort, PriorityQueue, TreeSet, TreeMap.
     *
     * INTERVIEW Q: Return values for compareTo/compare?
     * A: negative: first < second (first comes before)
     *    zero:     first == second (equal order)
     *    positive: first > second (first comes after)
     *
     * INTERVIEW Q: Can we sort objects without Comparable?
     * A: YES! Provide a Comparator to Collections.sort() or List.sort().
     *    TreeSet/TreeMap also accept a Comparator in their constructors.
     */
    static void demonstrateComparableAndComparator() {
        System.out.println("\n===== COMPARABLE vs COMPARATOR =====");

        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(3, "Charlie", 75000, "Engineering"));
        employees.add(new Employee(1, "Alice", 90000, "Management"));
        employees.add(new Employee(4, "Diana", 60000, "HR"));
        employees.add(new Employee(2, "Bob", 75000, "Engineering"));

        // NATURAL ORDERING via Comparable.compareTo() (by salary)
        Collections.sort(employees);
        System.out.println("\nNatural order (by salary):");
        employees.forEach(e -> System.out.println("  " + e));

        // CUSTOM ORDERING via Comparator - by name
        Comparator<Employee> byName = Comparator.comparing(Employee::getName);
        Collections.sort(employees, byName);
        System.out.println("\nBy name (Comparator):");
        employees.forEach(e -> System.out.println("  " + e));

        // Comparator with lambda
        employees.sort((e1, e2) -> e1.getName().compareTo(e2.getName())); // same as above

        // Comparator.comparing() - Java 8+ method references
        employees.sort(Comparator.comparing(Employee::getDepartment)
                                 .thenComparing(Employee::getName)); // multi-level sort
        System.out.println("\nBy department then name:");
        employees.forEach(e -> System.out.println("  " + e));

        // Reversed comparator
        employees.sort(Comparator.comparing(Employee::getSalary).reversed());
        System.out.println("\nBy salary descending:");
        employees.forEach(e -> System.out.println("  " + e));

        // Null-safe comparator
        List<String> withNulls = new ArrayList<>(Arrays.asList("Banana", null, "Apple", null, "Cherry"));
        withNulls.sort(Comparator.nullsFirst(Comparator.naturalOrder())); // nulls first
        System.out.println("\nNulls first: " + withNulls);
        withNulls.sort(Comparator.nullsLast(Comparator.naturalOrder()));  // nulls last
        System.out.println("Nulls last: " + withNulls);

        // TreeSet with custom Comparator (ignoring natural order)
        TreeSet<Employee> byIdSet = new TreeSet<>(Comparator.comparingInt(Employee::getId));
        byIdSet.addAll(employees);
        System.out.println("\nTreeSet by ID:");
        byIdSet.forEach(e -> System.out.println("  " + e));
    }

    // =========================================================================
    // SECTION 21: THREAD-SAFE COLLECTIONS
    // =========================================================================

    /**
     * THREAD-SAFE COLLECTIONS OVERVIEW
     *
     * INTERVIEW Q: What are the options for thread-safe collections?
     * A:
     * 1. Legacy synchronized: Vector, Hashtable - avoid (coarse locking)
     * 2. Collections.synchronizedXxx: wraps any collection - method-level sync
     *    Compound operations still need manual synchronization.
     * 3. CopyOnWriteArrayList / CopyOnWriteArraySet:
     *    - Creates fresh copy of backing array on every write (hence name)
     *    - Reads are completely lock-free (always reading stable snapshot)
     *    - Writes are expensive (full array copy: O(n))
     *    - BEST FOR: Read-heavy, write-rare scenarios (listeners, observer lists)
     *    - Iterator: always fail-safe (iterates snapshot, never throws CME)
     *    - Weaknesses: stale reads (iteration), memory-heavy
     * 4. ConcurrentHashMap - see Section 13
     * 5. BlockingQueue implementations:
     *    - ArrayBlockingQueue: bounded blocking queue (fixed capacity array)
     *    - LinkedBlockingQueue: optionally bounded (linked nodes)
     *    - PriorityBlockingQueue: unbounded, ordering by priority
     *    - SynchronousQueue: no internal capacity, handoff between producer/consumer
     *    - All implement BlockingQueue: put() blocks if full, take() blocks if empty
     *    BEST FOR: Producer-Consumer pattern, thread pool work queues
     */
    static void demonstrateThreadSafeCollections() {
        System.out.println("\n===== THREAD-SAFE COLLECTIONS =====");

        // CopyOnWriteArrayList
        System.out.println("--- CopyOnWriteArrayList ---");
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("Element1");
        cowList.add("Element2");
        cowList.add("Element3");

        // Multiple threads reading concurrently - completely lock-free
        // Writing creates a new copy of the entire array
        cowList.add("Element4"); // underlying array replaced with a new copy

        System.out.println("COW List: " + cowList);
        System.out.println("Size: " + cowList.size());

        // Safe iteration even with concurrent modification (operates on snapshot)
        Iterator<String> cowIter = cowList.iterator();
        cowList.add("Element5"); // This won't affect cowIter
        System.out.print("COW iteration (snapshot): ");
        while (cowIter.hasNext()) { System.out.print(cowIter.next() + " "); }
        System.out.println("\nActual list after add: " + cowList);

        // ArrayBlockingQueue - bounded Producer-Consumer
        System.out.println("\n--- ArrayBlockingQueue (Producer-Consumer) ---");
        ArrayBlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(5); // capacity 5

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    blockingQueue.put(i); // blocks if queue is full
                    System.out.println("Produced: " + i + ", Queue size: " + blockingQueue.size());
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(30); // slight delay to let producer fill some items
                while (true) {
                    Integer item = blockingQueue.poll(200, TimeUnit.MILLISECONDS);
                    if (item == null) break; // timeout - queue probably empty
                    System.out.println("Consumed: " + item + ", Queue size: " + blockingQueue.size());
                    Thread.sleep(15);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Producer-Consumer done. Queue: " + blockingQueue);

        // offer vs put, poll vs take
        // offer(e, timeout, unit): waits up to timeout, returns false if still full
        // put(e): waits indefinitely until space available (blocks)
        // poll(timeout, unit): waits up to timeout, returns null if still empty
        // take(): waits indefinitely until element available (blocks)
    }

    // =========================================================================
    // MAIN METHOD - RUNS ALL DEMONSTRATIONS
    // =========================================================================

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=".repeat(65));
        System.out.println("JAVA COLLECTIONS FRAMEWORK - COMPLETE INTERVIEW GUIDE");
        System.out.println("=".repeat(65));

        demonstrateArrayList();
        demonstrateLinkedList();
        demonstrateVectorAndStack();
        demonstrateSets();
        demonstrateHashMap();
        demonstrateLinkedHashMap();
        demonstrateTreeMap();
        demonstrateHashtable();
        demonstrateConcurrentHashMap();
        demonstratePriorityQueue();
        demonstrateArrayDeque();
        demonstrateCollectionsUtility();
        demonstrateArraysUtility();
        demonstrateIterators();
        demonstrateComparableAndComparator();
        demonstrateThreadSafeCollections();

        System.out.println("\n" + "=".repeat(65));
        System.out.println("KEY INTERVIEW QUICK REFERENCE");
        System.out.println("=".repeat(65));
        System.out.println("ArrayList  : O(1) get, O(n) insert/delete mid, dynamic array");
        System.out.println("LinkedList : O(n) get, O(1) insert/delete at ends, doubly-linked");
        System.out.println("HashSet    : O(1) add/remove/contains, no order, 1 null");
        System.out.println("LinkedHashSet: O(1) ops, insertion order, 1 null");
        System.out.println("TreeSet    : O(log n) ops, sorted, NO null");
        System.out.println("HashMap    : O(1) get/put, no order, 1 null key, many null values");
        System.out.println("LinkedHashMap: O(1) ops, insertion/access order, 1 null key");
        System.out.println("TreeMap    : O(log n) ops, sorted keys, NO null key");
        System.out.println("Hashtable  : legacy, synchronized, NO null key or value");
        System.out.println("ConcurrentHashMap: thread-safe, O(1) avg, NO null key or value");
        System.out.println("PriorityQueue: O(log n) offer/poll, O(1) peek, min-heap by default");
        System.out.println("ArrayDeque : O(1) at both ends, preferred over Stack/LinkedList");
        System.out.println("CopyOnWriteArrayList: thread-safe, O(n) writes, O(1) reads, fail-safe iter");
        System.out.println("ArrayBlockingQueue: bounded thread-safe queue, blocking put/take");
    }
}

/*
 * ============================================================
 * ADDITIONAL INTERVIEW Q&A - ADVANCED TOPICS
 * ============================================================
 *
 * Q: What is HashMap's internal structure before Java 8 vs Java 8+?
 * A: Before Java 8: Array of LinkedList (chain is always LinkedList)
 *    Java 8+: Array of LinkedList OR TreeNode (Red-Black Tree)
 *    When chain length >= 8 AND table.length >= 64: LinkedList -> TreeNode
 *    When chain length <= 6: TreeNode -> LinkedList (untreeify)
 *    WHY: TreeNode gives O(log n) worst case instead of O(n) for long chains.
 *
 * Q: What is the default initial capacity and load factor of HashMap?
 * A: Initial capacity: 16 (always a power of 2)
 *    Load factor: 0.75
 *    First resize at: 16 * 0.75 = 12 entries
 *    After resize: capacity = 32, threshold = 32 * 0.75 = 24, etc.
 *
 * Q: Why is HashMap capacity always a power of 2?
 * A: bucketIndex = hash & (capacity - 1)
 *    When capacity is power of 2, (capacity - 1) is all 1s in binary.
 *    Bitwise AND is MUCH faster than modulo (%) for index computation.
 *    e.g., capacity=16: 16-1=15=0b1111, hash & 0b1111 gives index 0-15.
 *
 * Q: What is the difference between HashMap and HashSet?
 * A: HashMap stores key-value pairs. HashSet stores only keys.
 *    HashSet is backed by HashMap<E, PRESENT> where PRESENT = new Object().
 *    So HashSet.add(e) calls map.put(e, PRESENT).
 *
 * Q: How do you make HashMap thread-safe without using ConcurrentHashMap?
 * A: Map<K,V> syncMap = Collections.synchronizedMap(new HashMap<>());
 *    This wraps every method call with synchronized(mutex).
 *    Iterators still need external synchronization.
 *    Preferred: ConcurrentHashMap (more granular locking, higher throughput).
 *
 * Q: What is WeakHashMap?
 * A: HashMap where keys are held by WEAK REFERENCES.
 *    If a key has no other strong references, it becomes eligible for GC.
 *    The entry is automatically removed when key is garbage collected.
 *    Used for: memory-sensitive caches, avoiding memory leaks.
 *
 * Q: What is IdentityHashMap?
 * A: A Map that uses == (reference equality) instead of equals() for key comparison.
 *    Two keys k1 == k2 (same reference) are considered the same.
 *    Used for: serialization, topology-preserving object graph transformations.
 *
 * Q: What is EnumSet and EnumMap?
 * A: EnumSet: very fast Set for enum values. Uses bit vector (long) internally. O(1) all ops.
 *    EnumMap: Map where keys are enum constants. Array-based, extremely fast.
 *    Both are NOT thread-safe. Use them when keys/values are enum types.
 *
 * Q: What happens when we add duplicate key to TreeSet?
 * A: If compareTo() returns 0 for the new element with an existing one,
 *    the new element is NOT added (TreeSet treats them as equal).
 *    CRITICAL: If equals() is not consistent with compareTo(), behavior is undefined.
 *    e.g., if a.compareTo(b) == 0 but !a.equals(b): TreeSet ignores it as dup,
 *    but HashSet might allow it (different buckets if hashCode differs).
 *
 * Q: What is the difference between Iterator.remove() and Collection.remove()?
 * A: Iterator.remove(): removes the LAST element returned by next(). Thread-safe w.r.t. iterator.
 *    Collection.remove(): removes specified element by value search.
 *    Calling Collection.remove() DURING iteration via an external Iterator causes CME.
 *    Always use Iterator.remove() when modifying during iteration.
 *    Or use Collection.removeIf() (Java 8+) - internally uses Iterator.remove().
 *
 * Q: LinkedList implements both List and Deque. What operations does this enable?
 * A: As List: get(index), set(index, e), add(index, e), remove(index)
 *    As Queue: offer(e), poll(), peek()
 *    As Deque: push(e)/pop(), offerFirst(e)/offerLast(e), pollFirst()/pollLast()
 *    As Stack: push(e), pop(), peek()
 *    This makes LinkedList versatile but also potentially confusing. For specific use:
 *    Use ArrayList for List, ArrayDeque for Stack/Queue/Deque.
 *
 * Q: How does PriorityQueue implement min-heap internally?
 * A: Binary heap stored in array: for node at index i:
 *    Left child: 2*i + 1
 *    Right child: 2*i + 2
 *    Parent: (i - 1) / 2
 *    Heap property: parent.compareTo(child) <= 0 (min-heap)
 *    add: place at end, siftUp (swap with parent until heap property restored)
 *    poll: remove root, put last element at root, siftDown (swap with smaller child)
 *
 * Q: Difference between BlockingQueue methods: add/put/offer and remove/take/poll?
 * A:                | Throws Exception | Returns boolean/null | Blocks  | Times out
 *    Insert:        | add(e)           | offer(e)             | put(e)  | offer(e, t, u)
 *    Remove:        | remove()         | poll()               | take()  | poll(t, u)
 *    Examine(peek): | element()        | peek()               | -       | -
 *
 * Q: What is a Spliterator?
 * A: Java 8+ interface for traversing and partitioning elements.
 *    Used internally by Stream.parallel() to split data for parallel processing.
 *    More powerful than Iterator: supports parallelism and late-binding.
 *    Methods: tryAdvance(), forEachRemaining(), trySplit(), estimateSize()
 *
 * Q: What is the difference between Collection.stream() and Collection.parallelStream()?
 * A: stream(): sequential single-threaded stream pipeline
 *    parallelStream(): parallel multi-threaded using ForkJoinPool.commonPool()
 *    Parallel is NOT always faster - overhead of splitting/joining may exceed benefit.
 *    Use parallel for: CPU-intensive ops on large collections with no shared state.
 *
 * Q: Which collections allow null, which don't?
 * A: Allow null keys: HashMap (1), LinkedHashMap (1), Hashtable (NO), TreeMap (NO)
 *    Allow null values: HashMap (yes), LinkedHashMap (yes), Hashtable (NO), TreeMap (yes)
 *    Allow null elements in Set: HashSet (1 null), LinkedHashSet (1 null), TreeSet (NO)
 *    Allow null in List: ArrayList (yes), LinkedList (yes), CopyOnWriteArrayList (yes)
 *    Allow null in Queue: PriorityQueue (NO), ArrayDeque (NO), LinkedList Queue (yes)
 *    Concurrent collections: ConcurrentHashMap (NO null key/value), CopyOnWriteArrayList (yes)
 *
 * Q: Time complexity quick reference table:
 * A:
 *   Collection           | add      | remove   | get/contains | Notes
 *   ---------------------|----------|----------|--------------|------------------
 *   ArrayList            | O(1)*    | O(n)     | O(1)/O(n)    | *amortized
 *   LinkedList (ends)    | O(1)     | O(1)     | O(n)         | add/remove at ends
 *   LinkedList (middle)  | O(n)     | O(n)     | O(n)         | find then O(1) op
 *   HashSet              | O(1)     | O(1)     | O(1)         | average case
 *   TreeSet              | O(log n) | O(log n) | O(log n)     | always
 *   HashMap              | O(1)     | O(1)     | O(1)         | average case
 *   TreeMap              | O(log n) | O(log n) | O(log n)     | always
 *   PriorityQueue        | O(log n) | O(log n) | O(1) peek    | poll is O(log n)
 *   ArrayDeque           | O(1)     | O(1)     | -            | at both ends
 * ============================================================
 */
