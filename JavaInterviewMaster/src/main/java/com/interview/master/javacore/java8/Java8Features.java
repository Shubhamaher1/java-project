package com.interview.master.javacore.java8;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.*;

/**
 * =============================================================================
 * JAVA 8+ FEATURES - COMPLETE INTERVIEW PREPARATION GUIDE
 * =============================================================================
 *
 * This file covers every major Java 8+ feature with detailed explanations,
 * practical examples, and common interview questions and answers.
 *
 * Topics covered:
 *  1. Lambda Expressions
 *  2. Functional Interfaces
 *  3. Method References
 *  4. Stream API (intermediate + terminal ops, collectors, parallel streams)
 *  5. Optional<T>
 *  6. Default and Static Interface Methods
 *  7. Date/Time API (java.time)
 *  8. CompletableFuture
 *  9. Java 9+: var, List.of(), Map.of(), Stream.takeWhile/dropWhile
 * 10. Java 14+: Records, Switch Expressions, Text Blocks
 * 11. Java 17+: Sealed Classes, Pattern Matching instanceof
 *
 * Author: Interview Preparation Reference
 * =============================================================================
 */
public class Java8Features {

    // =========================================================================
    // SECTION 1: LAMBDA EXPRESSIONS
    // =========================================================================
    //
    // INTERVIEW Q: What is a Lambda Expression?
    // A: A lambda expression is an anonymous function — it has no name, no return
    //    type declaration, and no access modifier. It provides a concise way to
    //    represent a single-method interface (functional interface) using an
    //    expression. Lambdas enable functional programming style in Java.
    //
    // INTERVIEW Q: What is the syntax of a lambda?
    // A: (parameters) -> expression
    //    (parameters) -> { statements; }
    //
    // INTERVIEW Q: What is "effectively final"?
    // A: A variable that is not declared final but is never modified after its
    //    initial assignment. Lambdas can capture such variables from the enclosing
    //    scope. If you try to modify a captured variable inside or after the lambda,
    //    the compiler raises an error.
    //
    // INTERVIEW Q: Can lambdas access instance variables of the enclosing class?
    // A: Yes. Instance variables (and static variables) are accessed through the
    //    enclosing object reference (this), so they don't need to be effectively
    //    final. Only local variables require effective finality.

    static void demonstrateLambdas() {
        System.out.println("\n===== LAMBDA EXPRESSIONS =====");

        // --- Basic lambda with no parameters ---
        Runnable noArgs = () -> System.out.println("Lambda with no args");
        noArgs.run();

        // --- Lambda with a single parameter (parentheses optional) ---
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
        printUpper.accept("hello lambda");

        // --- Lambda with multiple parameters ---
        Comparator<Integer> compare = (a, b) -> a - b;
        System.out.println("Compare 3 and 5: " + compare.compare(3, 5));

        // --- Lambda with a block body ---
        Function<Integer, String> classify = n -> {
            if (n < 0) return "negative";
            else if (n == 0) return "zero";
            else return "positive";
        };
        System.out.println("Classify -3: " + classify.apply(-3));
        System.out.println("Classify 0:  " + classify.apply(0));
        System.out.println("Classify 7:  " + classify.apply(7));

        // --- Variable capture: effectively final ---
        String greeting = "Hello"; // effectively final — never reassigned
        Runnable r = () -> System.out.println(greeting + " from lambda");
        r.run();
        // greeting = "Hi"; // Uncommenting this line would cause a compile error
        //                   // because it makes 'greeting' NOT effectively final.

        // --- Lambda returning another lambda (currying) ---
        Function<Integer, Function<Integer, Integer>> adder = x -> y -> x + y;
        System.out.println("Curried add 3+4: " + adder.apply(3).apply(4));
    }

    // =========================================================================
    // SECTION 2: FUNCTIONAL INTERFACES
    // =========================================================================
    //
    // INTERVIEW Q: What is a Functional Interface?
    // A: An interface with exactly ONE abstract method (SAM - Single Abstract
    //    Method). It may have any number of default/static methods. The
    //    @FunctionalInterface annotation is optional but recommended — it causes
    //    a compile-time error if more than one abstract method is declared.
    //
    // INTERVIEW Q: List the key built-in functional interfaces.
    // A:
    //  Predicate<T>         T -> boolean        (test)
    //  Function<T,R>        T -> R              (apply)
    //  Consumer<T>          T -> void           (accept)
    //  Supplier<T>          () -> T             (get)
    //  BiFunction<T,U,R>    (T,U) -> R          (apply)
    //  UnaryOperator<T>     T -> T              extends Function<T,T>
    //  BinaryOperator<T>    (T,T) -> T          extends BiFunction<T,T,T>
    //  BiPredicate<T,U>     (T,U) -> boolean
    //  BiConsumer<T,U>      (T,U) -> void

    // Custom functional interface
    @FunctionalInterface
    interface StringTransformer {
        String transform(String input);

        // Default method — does NOT break the SAM rule
        default StringTransformer andThen(StringTransformer after) {
            return s -> after.transform(this.transform(s));
        }
    }

    static void demonstrateFunctionalInterfaces() {
        System.out.println("\n===== FUNCTIONAL INTERFACES =====");

        // --- Predicate<T>: test(T) -> boolean ---
        Predicate<String> isLong   = s -> s.length() > 5;
        Predicate<String> startsA  = s -> s.startsWith("A");

        // Predicate composition: and(), or(), negate()
        Predicate<String> longAndStartsA = isLong.and(startsA);
        System.out.println("'Algorithm' long & starts A: " + longAndStartsA.test("Algorithm")); // true
        System.out.println("'Ant' long & starts A:       " + longAndStartsA.test("Ant"));       // false

        Predicate<Integer> isEven     = n -> n % 2 == 0;
        Predicate<Integer> isNotEven  = isEven.negate();
        System.out.println("Is 4 even: " + isEven.test(4));       // true
        System.out.println("Is 4 odd:  " + isNotEven.test(4));    // false

        // --- Function<T,R>: apply(T) -> R ---
        Function<String, Integer> strLen   = String::length;
        Function<Integer, String> intToStr = Object::toString;

        // Function composition: andThen(), compose()
        // f.andThen(g)  = g(f(x))
        // f.compose(g)  = f(g(x))
        Function<String, String> lenAsStr = strLen.andThen(intToStr);
        System.out.println("Length of 'Java' as string: " + lenAsStr.apply("Java")); // "4"

        // --- Consumer<T>: accept(T) -> void ---
        Consumer<String> print     = System.out::println;
        Consumer<String> printBold = s -> System.out.println("**" + s + "**");
        Consumer<String> both      = print.andThen(printBold);
        both.accept("Consumer chaining");

        // --- Supplier<T>: get() -> T ---
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> newList = listFactory.get();
        newList.add("from supplier");
        System.out.println("Supplier-created list: " + newList);

        // --- BiFunction<T,U,R> ---
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println("Repeat 'ab' 3 times: " + repeat.apply("ab", 3));

        // --- UnaryOperator<T> extends Function<T,T> ---
        // NOTE: UnaryOperator.andThen() returns Function<T,R>, not UnaryOperator<T>.
        // To keep the UnaryOperator type, compose manually with a lambda.
        UnaryOperator<String> trim    = String::trim;
        UnaryOperator<String> upper   = String::toUpperCase;
        // Explicit lambda composition preserves the UnaryOperator<String> type
        UnaryOperator<String> process = s -> upper.apply(trim.apply(s));
        System.out.println("UnaryOperator: '" + process.apply("  hello  ") + "'");

        // --- BinaryOperator<T> extends BiFunction<T,T,T> ---
        BinaryOperator<Integer> sum = Integer::sum;
        System.out.println("BinaryOperator sum: " + sum.apply(10, 20));

        // --- Custom functional interface with andThen composition ---
        StringTransformer trimmer   = String::trim;
        StringTransformer upperCase = String::toUpperCase;
        StringTransformer pipeline  = trimmer.andThen(upperCase);
        System.out.println("Custom FI pipeline: '" + pipeline.transform("  java 8  ") + "'");
    }

    // =========================================================================
    // SECTION 3: METHOD REFERENCES
    // =========================================================================
    //
    // INTERVIEW Q: What are the four types of method references?
    // A:
    //  1. Static method reference:            ClassName::staticMethod
    //     Equivalent lambda: (args) -> ClassName.staticMethod(args)
    //
    //  2. Instance method of a particular object: instance::instanceMethod
    //     Equivalent lambda: (args) -> instance.instanceMethod(args)
    //
    //  3. Instance method of an arbitrary object of a particular type:
    //     ClassName::instanceMethod
    //     Equivalent lambda: (obj, args) -> obj.instanceMethod(args)
    //     The first parameter of the functional interface becomes the receiver.
    //
    //  4. Constructor reference: ClassName::new
    //     Equivalent lambda: (args) -> new ClassName(args)
    //
    // INTERVIEW Q: When should you use a method reference over a lambda?
    // A: Use a method reference when the lambda body does nothing more than call
    //    a single existing method. Method references are more concise and readable.

    static void demonstrateMethodReferences() {
        System.out.println("\n===== METHOD REFERENCES =====");

        // TYPE 1: Static method reference — ClassName::staticMethod
        // Equivalent lambda: (x) -> Integer.parseInt(x)
        Function<String, Integer> parser1  = Integer::parseInt;
        Function<String, Integer> parser2  = s -> Integer.parseInt(s); // equivalent
        System.out.println("Static ref parseInt('42'): " + parser1.apply("42"));

        // TYPE 1 example: Math::abs
        Function<Integer, Integer> abs = Math::abs;
        System.out.println("Static ref Math.abs(-7): " + abs.apply(-7));

        // TYPE 2: Instance method of a particular (already-known) object
        String prefix  = "Hello, ";
        Function<String, String> greeter1 = prefix::concat;  // method ref
        Function<String, String> greeter2 = s -> prefix.concat(s);  // equivalent
        System.out.println("Instance (particular obj) ref: " + greeter1.apply("World"));

        // TYPE 2 example with StringBuilder
        StringBuilder sb = new StringBuilder("Start:");
        Consumer<String> appender = sb::append;
        appender.accept(" appended");
        System.out.println("StringBuilder after Consumer: " + sb);

        // TYPE 3: Instance method of an arbitrary object of a particular type
        // The functional interface's first param is the receiver object.
        // Equivalent lambda: (str) -> str.toUpperCase()
        Function<String, String> toUpper1 = String::toUpperCase;
        Function<String, String> toUpper2 = s -> s.toUpperCase(); // equivalent
        System.out.println("Arbitrary instance ref: " + toUpper1.apply("hello"));

        // TYPE 3: BiFunction — first param is receiver, second is argument
        // Equivalent lambda: (str, ch) -> str.indexOf(ch)
        BiFunction<String, String, Integer> indexOf = String::indexOf;
        System.out.println("indexOf 'l' in 'hello': " + indexOf.apply("hello", "l"));

        // TYPE 3: Comparator.comparing uses this pattern
        List<String> words = Arrays.asList("banana", "apple", "cherry", "date");
        words.sort(Comparator.comparing(String::length));  // String::length is TYPE 3
        System.out.println("Sorted by length: " + words);

        // TYPE 4: Constructor reference — ClassName::new
        Supplier<ArrayList<String>>          listMaker  = ArrayList::new;
        Function<Integer, ArrayList<String>> sizedList  = ArrayList::new;

        ArrayList<String> l1 = listMaker.get();
        ArrayList<String> l2 = sizedList.apply(20);  // initialCapacity=20
        l1.add("from no-arg constructor ref");
        System.out.println("Constructor ref (no-arg): " + l1);

        // TYPE 4: Used heavily in stream collect patterns
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        // Collecting into a new TreeSet using constructor reference
        TreeSet<String> nameSet = names.stream()
                .collect(Collectors.toCollection(TreeSet::new));
        System.out.println("Constructor ref (TreeSet::new): " + nameSet);
    }

    // =========================================================================
    // SECTION 4: STREAM API
    // =========================================================================
    //
    // INTERVIEW Q: What is a Stream?
    // A: A Stream is a sequence of elements from a source (collection, array, I/O)
    //    that supports aggregate operations. Streams do NOT store data; they process
    //    it on demand. They are NOT data structures.
    //
    // INTERVIEW Q: What is the difference between intermediate and terminal ops?
    // A: Intermediate operations are LAZY — they return a new Stream and are not
    //    executed until a terminal operation is invoked. Terminal operations are
    //    EAGER — they trigger processing of the stream pipeline and produce a result
    //    or side effect. After a terminal op, the stream is consumed and cannot be reused.
    //
    // INTERVIEW Q: What is lazy evaluation and why is it useful?
    // A: Lazy evaluation means intermediate operations are only executed when a
    //    terminal operation is called. This allows optimizations like short-circuiting
    //    (e.g., findFirst() stops after the first match) and avoids processing
    //    elements that don't contribute to the result.
    //
    // INTERVIEW Q: Difference between map() and flatMap()?
    // A: map()     transforms each element to exactly one element: Stream<Stream<T>> scenario.
    //    flatMap() transforms each element to zero or more elements and flattens the
    //              resulting streams into a single stream. Use flatMap() when each
    //              element produces a collection/stream of results.

    static void demonstrateStreams() {
        System.out.println("\n===== STREAM API =====");

        // --- STREAM CREATION ---
        System.out.println("-- Stream Creation --");

        // Stream.of()
        Stream<String> ofStream = Stream.of("a", "b", "c");
        System.out.println("Stream.of: " + ofStream.collect(Collectors.toList()));

        // Stream.generate() — infinite stream, must be limited
        Stream<Double> randoms = Stream.generate(Math::random).limit(3);
        System.out.println("Stream.generate (3 randoms): " + randoms.collect(Collectors.toList()));

        // Stream.iterate() — Java 8 version
        Stream<Integer> evens = Stream.iterate(0, n -> n + 2).limit(6);
        System.out.println("Stream.iterate (evens): " + evens.collect(Collectors.toList()));

        // Stream.iterate() with predicate — Java 9+
        Stream<Integer> upTo20 = Stream.iterate(1, n -> n <= 20, n -> n * 2);
        System.out.println("Stream.iterate(seed,pred,next): " + upTo20.collect(Collectors.toList()));

        // Collection.stream() and Collection.parallelStream()
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Stream<Integer> seqStream  = numbers.stream();
        Stream<Integer> parStream  = numbers.parallelStream();

        // Arrays.stream()
        int[] arr = {10, 20, 30};
        IntStream arrStream = Arrays.stream(arr);
        System.out.println("Arrays.stream sum: " + arrStream.sum());

        // IntStream.range() and IntStream.rangeClosed()
        IntStream range = IntStream.range(1, 6);       // 1,2,3,4,5
        IntStream rangeClosed = IntStream.rangeClosed(1, 5); // 1,2,3,4,5
        System.out.println("IntStream.range(1,6): " + range.boxed().collect(Collectors.toList()));

        // --- INTERMEDIATE OPERATIONS (lazy) ---
        System.out.println("\n-- Intermediate Operations --");

        List<String> words = Arrays.asList("banana", "apple", "cherry", "date",
                "avocado", "blueberry", "apricot", "cherry");

        // filter() — keeps elements matching predicate
        List<String> startWithA = words.stream()
                .filter(w -> w.startsWith("a"))
                .collect(Collectors.toList());
        System.out.println("filter(startsWith 'a'): " + startWithA);

        // map() — transforms each element
        List<Integer> wordLengths = words.stream()
                .map(String::length)
                .collect(Collectors.toList());
        System.out.println("map(length): " + wordLengths);

        // flatMap() — each element maps to a stream; results are flattened
        List<List<Integer>> nested = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(6, 7, 8, 9)
        );
        List<Integer> flat = nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        System.out.println("flatMap (nested lists): " + flat);

        // flatMap on strings — split into characters
        List<String> sentences = Arrays.asList("Hello World", "Stream API");
        List<String> chars = sentences.stream()
                .flatMap(s -> Arrays.stream(s.split("")))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("flatMap + distinct + sorted chars: " + chars);

        // distinct() — removes duplicates using equals()
        List<String> distinct = words.stream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println("distinct(): " + distinct);

        // sorted() — natural order
        List<String> sorted = words.stream()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("sorted(): " + sorted);

        // sorted(Comparator) — custom order
        List<String> sortedByLen = words.stream()
                .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                .distinct()
                .collect(Collectors.toList());
        System.out.println("sorted by length then alpha: " + sortedByLen);

        // peek() — intermediate side-effect operation (great for debugging)
        //         Does NOT modify elements; just observes them.
        List<String> peeked = words.stream()
                .filter(w -> w.length() > 5)
                .peek(w -> System.out.println("  peek after filter: " + w))
                .map(String::toUpperCase)
                .limit(3)
                .collect(Collectors.toList());
        System.out.println("After peek pipeline: " + peeked);

        // limit(n) — truncate to first n elements
        // skip(n)  — skip first n elements
        List<Integer> limited = numbers.stream().skip(3).limit(4).collect(Collectors.toList());
        System.out.println("skip(3).limit(4): " + limited); // [4, 5, 6, 7]

        // mapToInt / mapToLong / mapToDouble — primitive specializations
        // These return IntStream/LongStream/DoubleStream which have extra methods
        // like sum(), average(), min(), max(), summaryStatistics()
        IntStream lengths = words.stream().mapToInt(String::length);
        System.out.println("Sum of word lengths: " + words.stream().mapToInt(String::length).sum());
        OptionalDouble avg = words.stream().mapToInt(String::length).average();
        System.out.println("Avg word length: " + avg.orElse(0));

        // --- TERMINAL OPERATIONS (eager) ---
        System.out.println("\n-- Terminal Operations --");

        // collect() — most versatile terminal op; uses a Collector
        List<Integer> numList = numbers.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
        System.out.println("collect even numbers: " + numList);

        // forEach() — side-effect terminal op (no return value)
        System.out.print("forEach: ");
        numbers.stream().limit(5).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // count() — returns long
        long countLong = words.stream().filter(w -> w.contains("a")).count();
        System.out.println("count(contains 'a'): " + countLong);

        // findFirst() — returns Optional<T>, short-circuits (stops at first match)
        Optional<String> first = words.stream().filter(w -> w.startsWith("b")).findFirst();
        System.out.println("findFirst(startsWith 'b'): " + first.orElse("none"));

        // findAny() — returns Optional<T>, useful in parallel streams (no ordering guarantee)
        Optional<String> any = words.parallelStream().filter(w -> w.startsWith("c")).findAny();
        System.out.println("findAny(startsWith 'c'): " + any.orElse("none"));

        // anyMatch() — true if ANY element matches (short-circuits)
        boolean hasLong = numbers.stream().anyMatch(n -> n > 8);
        System.out.println("anyMatch(n > 8): " + hasLong);

        // allMatch() — true if ALL elements match (short-circuits on first false)
        boolean allPos = numbers.stream().allMatch(n -> n > 0);
        System.out.println("allMatch(n > 0): " + allPos);

        // noneMatch() — true if NO element matches (short-circuits)
        boolean noneNeg = numbers.stream().noneMatch(n -> n < 0);
        System.out.println("noneMatch(n < 0): " + noneNeg);

        // reduce() — fold all elements into a single value
        // FORM 1: reduce(identity, BinaryOperator) — always returns T
        int sum = numbers.stream().reduce(0, Integer::sum);
        System.out.println("reduce sum: " + sum);

        // FORM 2: reduce(BinaryOperator) — returns Optional<T> (stream might be empty)
        Optional<Integer> product = numbers.stream().reduce((a, b) -> a * b);
        System.out.println("reduce product: " + product.orElse(0));

        // min() and max() — require Comparator, return Optional<T>
        Optional<Integer> min = numbers.stream().min(Integer::compareTo);
        Optional<Integer> max = numbers.stream().max(Integer::compareTo);
        System.out.println("min: " + min.orElse(-1) + ", max: " + max.orElse(-1));

        // toArray()
        Object[] arr2 = words.stream().filter(w -> w.length() == 4).toArray();
        System.out.println("toArray (length=4): " + Arrays.toString(arr2));

        // --- COLLECTORS ---
        System.out.println("\n-- Collectors --");

        // toList(), toSet()
        List<String>     toList = words.stream().distinct().collect(Collectors.toList());
        Set<String>      toSet  = words.stream().collect(Collectors.toSet());
        System.out.println("toList size: " + toList.size() + ", toSet size: " + toSet.size());

        // toMap(keyMapper, valueMapper)
        // INTERVIEW Q: What happens if two elements map to the same key in toMap()?
        // A: It throws IllegalStateException (duplicate key). Provide a merge function
        //    as the third argument to resolve conflicts.
        Map<String, Integer> wordLengthMap = words.stream()
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),  // key: the word itself
                        String::length        // value: its length
                ));
        System.out.println("toMap (word->length): " + wordLengthMap);

        // toMap with merge function (handles duplicate keys)
        Map<Integer, String> lenToWord = words.stream()
                .collect(Collectors.toMap(
                        String::length,      // key: length
                        Function.identity(), // value: word
                        (existing, current) -> existing + "," + current // merge duplicates
                ));
        System.out.println("toMap with merge: " + lenToWord);

        // groupingBy() — groups elements by a classifier function -> Map<K, List<V>>
        Map<Integer, List<String>> byLength = words.stream()
                .collect(Collectors.groupingBy(String::length));
        System.out.println("groupingBy length: " + byLength);

        // groupingBy with downstream collector (counting per group)
        Map<Integer, Long> countByLength = words.stream()
                .collect(Collectors.groupingBy(String::length, Collectors.counting()));
        System.out.println("groupingBy + counting: " + countByLength);

        // groupingBy with downstream collector (joining per group)
        Map<Character, String> joinedByFirstChar = words.stream()
                .distinct()
                .collect(Collectors.groupingBy(
                        w -> w.charAt(0),
                        Collectors.joining(", ")
                ));
        System.out.println("groupingBy first char + joining: " + joinedByFirstChar);

        // partitioningBy() — special case of groupingBy with boolean key
        //                    always returns Map<Boolean, List<T>>
        Map<Boolean, List<Integer>> evenOdd = numbers.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("partitioningBy even: " + evenOdd.get(true));
        System.out.println("partitioningBy odd:  " + evenOdd.get(false));

        // joining() — concatenates strings
        String joined1 = words.stream().distinct().collect(Collectors.joining());
        String joined2 = words.stream().distinct().collect(Collectors.joining(", "));
        String joined3 = words.stream().distinct().collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining (no delim):   " + joined1);
        System.out.println("joining (', '):        " + joined2);
        System.out.println("joining ([prefix]):    " + joined3);

        // counting()
        long total = words.stream().collect(Collectors.counting());
        System.out.println("counting(): " + total);

        // summarizingInt() — returns IntSummaryStatistics
        IntSummaryStatistics stats = words.stream()
                .collect(Collectors.summarizingInt(String::length));
        System.out.println("summarizingInt: count=" + stats.getCount()
                + ", sum=" + stats.getSum()
                + ", min=" + stats.getMin()
                + ", max=" + stats.getMax()
                + ", avg=" + stats.getAverage());

        // --- PARALLEL STREAMS ---
        System.out.println("\n-- Parallel Streams --");
        //
        // INTERVIEW Q: How does a parallel stream work?
        // A: Parallel streams use the ForkJoinPool.commonPool() under the hood. The
        //    stream is split into sub-streams (using Spliterator), processed in
        //    parallel by multiple threads, and the results are merged.
        //
        // INTERVIEW Q: When SHOULD you use parallel streams?
        // A: 1. CPU-bound operations (not I/O-bound)
        //    2. Large data sets (enough to overcome splitting/merging overhead)
        //    3. Operations where ORDER doesn't matter (or you don't need encounter order)
        //    4. Stateless, non-interfering operations
        //
        // INTERVIEW Q: When SHOULD NOT you use parallel streams?
        // A: 1. Small data sets — overhead of parallelization outweighs benefit
        //    2. When order matters (sorted(), findFirst() have overhead in parallel)
        //    3. When operations have side effects or shared mutable state
        //    4. I/O-bound tasks (blocking threads in ForkJoinPool is dangerous)
        //    5. When operations are inherently sequential

        long bigSum = LongStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
        System.out.println("Parallel sum 1..1M: " + bigSum);

        // IMPORTANT: parallel streams use ForkJoinPool.commonPool()
        // To use a custom pool, submit the stream task to a ForkJoinPool:
        //   ForkJoinPool pool = new ForkJoinPool(4);
        //   pool.submit(() -> myList.parallelStream().forEach(...)).get();

        // forEachOrdered() — processes in encounter order even in parallel stream
        System.out.print("forEachOrdered (parallel): ");
        Stream.of(1, 2, 3, 4, 5).parallel().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // Java 9: Stream.takeWhile and dropWhile
        System.out.println("\n-- Java 9: takeWhile / dropWhile --");
        List<Integer> ascending = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        // takeWhile: takes elements while predicate is true, stops at first false
        List<Integer> taken = ascending.stream().takeWhile(n -> n < 5).collect(Collectors.toList());
        System.out.println("takeWhile(n < 5): " + taken); // [1,2,3,4]

        // dropWhile: drops elements while predicate is true, keeps rest
        List<Integer> dropped = ascending.stream().dropWhile(n -> n < 5).collect(Collectors.toList());
        System.out.println("dropWhile(n < 5): " + dropped); // [5,6,7,8]
    }

    // =========================================================================
    // SECTION 5: OPTIONAL<T>
    // =========================================================================
    //
    // INTERVIEW Q: What is Optional and why was it introduced?
    // A: Optional<T> is a container that may or may not hold a non-null value.
    //    It was introduced in Java 8 to represent the explicit absence of a value
    //    and to avoid NullPointerExceptions. It forces the caller to think about
    //    the case where no value is present.
    //
    // INTERVIEW Q: What is the difference between orElse() and orElseGet()?
    // A: orElse(T other) — always evaluates the argument expression eagerly.
    //    orElseGet(Supplier<T>) — evaluates the supplier lazily, only if empty.
    //    Prefer orElseGet() when the fallback computation is expensive.
    //
    // INTERVIEW Q: When should you NOT use Optional?
    // A: 1. Do NOT use as a field in a class (not Serializable, heap overhead)
    //    2. Do NOT use as a method parameter
    //    3. Do NOT use for collections — return an empty collection instead
    //    4. Do NOT call get() without first checking isPresent() — defeats purpose

    static void demonstrateOptional() {
        System.out.println("\n===== OPTIONAL<T> =====");

        // Creation
        Optional<String> present   = Optional.of("Java 8");          // must be non-null
        Optional<String> empty     = Optional.empty();                // explicitly empty
        Optional<String> nullable1 = Optional.ofNullable("Hello");   // may be null
        Optional<String> nullable2 = Optional.ofNullable(null);      // becomes empty

        System.out.println("present: " + present);
        System.out.println("empty:   " + empty);
        System.out.println("nullable2 (null): " + nullable2);

        // isPresent() and isEmpty() (isEmpty added in Java 11)
        System.out.println("present.isPresent(): " + present.isPresent());
        System.out.println("empty.isPresent():   " + empty.isPresent());

        // ifPresent(Consumer) — execute only if value is present
        present.ifPresent(v -> System.out.println("ifPresent: " + v));
        empty.ifPresent(v -> System.out.println("This won't print"));

        // Java 9: ifPresentOrElse(Consumer, Runnable)
        empty.ifPresentOrElse(
                v -> System.out.println("Value: " + v),
                () -> System.out.println("ifPresentOrElse: value was empty")
        );

        // get() — throws NoSuchElementException if empty (use with care!)
        System.out.println("get(): " + present.get());

        // orElse(T) — returns value or default (default always evaluated!)
        String v1 = empty.orElse("default value");
        System.out.println("orElse: " + v1);

        // orElseGet(Supplier) — returns value or supplier result (lazy evaluation)
        String v2 = empty.orElseGet(() -> "computed " + "default");
        System.out.println("orElseGet: " + v2);

        // orElseThrow(Supplier) — throw exception if empty
        try {
            String v3 = empty.orElseThrow(() -> new IllegalStateException("Value required!"));
        } catch (IllegalStateException e) {
            System.out.println("orElseThrow caught: " + e.getMessage());
        }

        // Java 10: orElseThrow() with no args throws NoSuchElementException
        // empty.orElseThrow(); // throws NoSuchElementException

        // map(Function) — transforms value if present, stays empty if not
        Optional<Integer> length = present.map(String::length);
        System.out.println("map(length): " + length);
        Optional<Integer> emptyLen = empty.map(String::length);
        System.out.println("map on empty: " + emptyLen);

        // flatMap(Function that returns Optional) — avoids Optional<Optional<T>>
        Optional<Optional<String>> nested = Optional.of(Optional.of("nested"));
        Optional<String> flat = nested.flatMap(Function.identity());
        System.out.println("flatMap result: " + flat);

        // Practical flatMap example
        Optional<String> userName = Optional.of("  Alice  ");
        Optional<String> trimmed  = userName
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase);
        System.out.println("Optional pipeline: " + trimmed);

        // filter(Predicate) — keeps value only if predicate is true
        Optional<String> filtered = present.filter(s -> s.length() > 3);
        System.out.println("filter (len > 3): " + filtered);
        Optional<String> filtered2 = present.filter(s -> s.length() > 100);
        System.out.println("filter (len > 100): " + filtered2);

        // Java 9: or(Supplier<Optional<T>>) — if empty, return alternative Optional
        Optional<String> alternative = empty.or(() -> Optional.of("fallback optional"));
        System.out.println("or(): " + alternative);

        // Java 9: stream() — converts Optional to a Stream (0 or 1 element)
        long count = present.stream().count();
        System.out.println("Optional.stream() count: " + count);

        // Real-world pattern: chaining Optional to avoid null checks
        // Instead of:  if (user != null && user.getAddress() != null) { ... }
        // Use:
        Optional<String> city = findUser("Alice")
                .map(Java8Features::getCity)
                .filter(c -> !c.isEmpty());
        System.out.println("Chained Optional for city: " + city.orElse("unknown"));
    }

    // Helper methods for Optional demo
    static Optional<String> findUser(String name) {
        return "Alice".equals(name) ? Optional.of("Alice_UserObject") : Optional.empty();
    }
    static String getCity(String user) {
        return "Alice_UserObject".equals(user) ? "Mumbai" : "";
    }

    // =========================================================================
    // SECTION 6: DEFAULT AND STATIC INTERFACE METHODS
    // =========================================================================
    //
    // INTERVIEW Q: Why were default methods added to interfaces in Java 8?
    // A: To allow backward-compatible evolution of interfaces. Before Java 8, adding
    //    a new method to an interface broke all existing implementations. Default
    //    methods provide a default implementation, so existing classes are not forced
    //    to implement the new method.
    //
    // INTERVIEW Q: What happens if a class implements two interfaces with the same
    //    default method?
    // A: The class must override the method; otherwise it's a compile-time error.
    //    Inside the override, you can call a specific interface's default method
    //    using InterfaceName.super.methodName().
    //
    // INTERVIEW Q: What is the Diamond Problem with default methods?
    // A: If class C extends A and B (where A and B both have the same default method),
    //    the compiler cannot decide which default to use -> compile error.
    //    Resolution rule: class always wins > sub-interface wins > compiler error.

    interface Flyable {
        void fly();  // abstract — must be implemented

        default String describe() {
            return "I can fly!";
        }

        static Flyable create() {
            return () -> System.out.println("Flying (from static factory)");
        }
    }

    interface Swimmable {
        void swim();

        default String describe() {
            return "I can swim!";
        }
    }

    static class Duck implements Flyable, Swimmable {
        @Override
        public void fly()  { System.out.println("Duck flying"); }

        @Override
        public void swim() { System.out.println("Duck swimming"); }

        // MUST override because both Flyable and Swimmable have describe()
        @Override
        public String describe() {
            return Flyable.super.describe() + " " + Swimmable.super.describe();
        }
    }

    static void demonstrateDefaultAndStaticInterfaceMethods() {
        System.out.println("\n===== DEFAULT AND STATIC INTERFACE METHODS =====");

        Duck duck = new Duck();
        duck.fly();
        duck.swim();
        System.out.println("Duck describe: " + duck.describe());

        // Static interface method called on the interface itself (not on instance)
        Flyable f = Flyable.create();
        f.fly();

        // Real-world example: Comparator's default methods
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob", "Dave");
        Comparator<String> byLength  = Comparator.comparingInt(String::length);
        Comparator<String> byAlpha   = Comparator.naturalOrder();
        Comparator<String> combined  = byLength.thenComparing(byAlpha);
        Comparator<String> reversed  = combined.reversed();

        names.sort(combined);
        System.out.println("sorted by length then alpha: " + names);
        names.sort(reversed);
        System.out.println("reversed: " + names);
    }

    // =========================================================================
    // SECTION 7: DATE/TIME API (java.time)
    // =========================================================================
    //
    // INTERVIEW Q: What was wrong with the old java.util.Date and Calendar?
    // A: 1. java.util.Date was mutable and not thread-safe.
    //    2. Month indexing was 0-based (January = 0), confusing.
    //    3. Date represented both date and time — no separate date-only class.
    //    4. No timezone support in Date; Calendar API was verbose and error-prone.
    //    5. toString() was misleading.
    //
    // INTERVIEW Q: Key classes in java.time package?
    // A:
    //  LocalDate        — date only, no time, no timezone (2024-06-26)
    //  LocalTime        — time only, no date, no timezone (14:30:00)
    //  LocalDateTime    — date + time, no timezone (2024-06-26T14:30:00)
    //  ZonedDateTime    — date + time + timezone
    //  Instant          — machine time (epoch seconds), UTC
    //  Duration         — time-based amount (hours, minutes, seconds, nanos)
    //  Period           — date-based amount (years, months, days)
    //  DateTimeFormatter — formatting and parsing

    static void demonstrateDateTimeAPI() {
        System.out.println("\n===== DATE/TIME API =====");

        // --- LocalDate ---
        LocalDate today    = LocalDate.now();
        LocalDate specific = LocalDate.of(2024, Month.JANUARY, 15);
        LocalDate parsed   = LocalDate.parse("2024-06-26");

        System.out.println("today:     " + today);
        System.out.println("specific:  " + specific);
        System.out.println("parsed:    " + parsed);
        System.out.println("year:      " + today.getYear());
        System.out.println("month:     " + today.getMonth());           // Month enum
        System.out.println("monthVal:  " + today.getMonthValue());     // 1-12
        System.out.println("day:       " + today.getDayOfMonth());
        System.out.println("dayOfWeek: " + today.getDayOfWeek());
        System.out.println("isLeap:    " + today.isLeapYear());

        // Arithmetic — all operations return NEW objects (immutable!)
        LocalDate nextWeek  = today.plusWeeks(1);
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate adjusted  = today.withDayOfMonth(1); // first day of current month
        System.out.println("next week:   " + nextWeek);
        System.out.println("last month:  " + lastMonth);
        System.out.println("first of month: " + adjusted);

        // Comparison
        System.out.println("today.isBefore(nextWeek): " + today.isBefore(nextWeek));
        System.out.println("today.isAfter(lastMonth): " + today.isAfter(lastMonth));

        // --- LocalTime ---
        LocalTime now       = LocalTime.now();
        LocalTime noon      = LocalTime.of(12, 0, 0);
        LocalTime parsed2   = LocalTime.parse("14:30:45");

        System.out.println("\nnow (time):  " + now);
        System.out.println("noon:        " + noon);
        System.out.println("parsed time: " + parsed2);
        System.out.println("hour:        " + now.getHour());
        System.out.println("minute:      " + now.getMinute());

        // --- LocalDateTime ---
        LocalDateTime ldt     = LocalDateTime.now();
        LocalDateTime ldt2    = LocalDateTime.of(2024, 6, 26, 14, 30, 0);
        LocalDateTime ldt3    = LocalDate.now().atTime(LocalTime.now());
        LocalDateTime ldt4    = LocalDate.now().atStartOfDay();

        System.out.println("\nLocalDateTime now:  " + ldt);
        System.out.println("LocalDateTime of(): " + ldt2);
        System.out.println("atStartOfDay:       " + ldt4);

        // --- ZonedDateTime ---
        ZonedDateTime zdtUTC      = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime zdtIST      = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime zdtNY       = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime converted   = zdtUTC.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        System.out.println("\nUTC:      " + zdtUTC);
        System.out.println("IST:      " + zdtIST);
        System.out.println("NYC:      " + zdtNY);
        System.out.println("UTC->IST: " + converted);

        // --- Instant ---
        Instant instant1 = Instant.now();
        Instant instant2 = Instant.ofEpochSecond(0); // Unix epoch
        System.out.println("\nInstant now:   " + instant1);
        System.out.println("Epoch start:   " + instant2);
        System.out.println("Epoch seconds: " + instant1.getEpochSecond());

        // --- Duration — time-based (for hours, minutes, seconds, nanos) ---
        Duration d1 = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        Duration d2 = Duration.ofHours(2).plusMinutes(30);
        Duration d3 = Duration.ofDays(1);

        System.out.println("\nDuration 9am->5:30pm: " + d1);
        System.out.println("Duration hours:  " + d1.toHours());
        System.out.println("Duration minutes: " + d1.toMinutes());

        // --- Period — date-based (for years, months, days) ---
        Period p1 = Period.between(LocalDate.of(1990, 1, 1), LocalDate.now());
        Period p2 = Period.of(1, 6, 15); // 1 year, 6 months, 15 days

        System.out.println("\nPeriod from 1990-01-01 to today: " + p1);
        System.out.println("Years: " + p1.getYears() + ", Months: " + p1.getMonths()
                + ", Days: " + p1.getDays());

        // ChronoUnit for calculations
        long daysBetween  = ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), LocalDate.now());
        long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.of(2020, 1, 1), LocalDate.now());
        System.out.println("Days since 2024-01-01: " + daysBetween);
        System.out.println("Months since 2020-01-01: " + monthsBetween);

        // --- DateTimeFormatter ---
        DateTimeFormatter fmt1 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter fmt3 = DateTimeFormatter.ISO_LOCAL_DATE;

        System.out.println("\nFormatted dd/MM/yyyy:   " + today.format(fmt1));
        System.out.println("Formatted long format:   " + today.format(fmt2));
        System.out.println("ISO format:              " + today.format(fmt3));

        // Parsing
        LocalDate parsedDate = LocalDate.parse("26/06/2024", fmt1);
        System.out.println("Parsed from dd/MM/yyyy: " + parsedDate);
    }

    // =========================================================================
    // SECTION 8: COMPLETABLEFUTURE
    // =========================================================================
    //
    // INTERVIEW Q: What is CompletableFuture and how is it different from Future?
    // A: Future represents a pending asynchronous computation but has limitations:
    //    - get() blocks the calling thread
    //    - No way to chain/combine futures
    //    - No exception handling mechanism
    //    - Cannot complete manually
    //
    //    CompletableFuture implements both Future and CompletionStage.
    //    It supports:
    //    - Non-blocking callbacks (thenApply, thenAccept, thenRun)
    //    - Chaining of async operations
    //    - Combining multiple futures (thenCombine, allOf, anyOf)
    //    - Exception handling (exceptionally, handle, whenComplete)
    //    - Manual completion (complete(), completeExceptionally())
    //
    // INTERVIEW Q: Difference between thenApply, thenAccept, thenRun?
    // A: thenApply(Function<T,U>)   — transforms result, returns CompletableFuture<U>
    //    thenAccept(Consumer<T>)    — consumes result, returns CompletableFuture<Void>
    //    thenRun(Runnable)          — ignores result, returns CompletableFuture<Void>
    //
    // INTERVIEW Q: Difference between thenCompose and thenCombine?
    // A: thenCompose(Function<T, CompletableFuture<U>>) — flatMap for CompletableFuture.
    //    Used for dependent async tasks (result of first feeds into second).
    //    thenCombine(CompletableFuture<U>, BiFunction<T,U,V>) — combines two independent
    //    futures when both complete.

    static void demonstrateCompletableFuture() {
        System.out.println("\n===== COMPLETABLEFUTURE =====");

        // --- Basic async computation ---
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            // Simulates async work (e.g., database call, HTTP request)
            return "Hello from async";
        });

        // thenApply — transform result (like map)
        CompletableFuture<Integer> cf2 = cf1.thenApply(s -> {
            System.out.println("thenApply executing on: " + Thread.currentThread().getName());
            return s.length();
        });

        // thenAccept — consume result
        CompletableFuture<Void> cf3 = cf2.thenAccept(len ->
                System.out.println("String length: " + len));

        // thenRun — run after completion, ignores result
        CompletableFuture<Void> cf4 = cf3.thenRun(() ->
                System.out.println("thenRun: pipeline finished"));

        // Block to get result (in production code, avoid blocking if possible)
        cf4.join(); // join() is like get() but throws unchecked CompletionException

        // --- thenCompose: chain dependent async tasks (flatMap) ---
        CompletableFuture<String> userId   = CompletableFuture.supplyAsync(() -> "user-123");
        CompletableFuture<String> userInfo = userId.thenCompose(id ->
                CompletableFuture.supplyAsync(() -> "UserInfo for " + id)); // dependent on id
        System.out.println("thenCompose: " + userInfo.join());

        // --- thenCombine: combine two INDEPENDENT futures ---
        CompletableFuture<Integer> price    = CompletableFuture.supplyAsync(() -> 100);
        CompletableFuture<Integer> discount = CompletableFuture.supplyAsync(() -> 20);
        CompletableFuture<Integer> finalPrice = price.thenCombine(discount,
                (p, d) -> p - d);
        System.out.println("thenCombine (price - discount): " + finalPrice.join());

        // --- exceptionally: handle exceptions ---
        CompletableFuture<String> failing = CompletableFuture.supplyAsync(() -> {
            if (true) throw new RuntimeException("Something went wrong!");
            return "success";
        });
        String result = failing
                .exceptionally(ex -> "Recovered: " + ex.getMessage())
                .join();
        System.out.println("exceptionally: " + result);

        // --- handle: like thenApply but also receives exception (if any) ---
        CompletableFuture<String> handled = CompletableFuture
                .supplyAsync(() -> { throw new RuntimeException("error"); })
                .handle((res, ex) -> {
                    if (ex != null) return "Handled exception: " + ex.getMessage();
                    return res.toString();
                });
        System.out.println("handle: " + handled.join());

        // --- whenComplete: side-effect after completion (does NOT transform result) ---
        CompletableFuture.supplyAsync(() -> "data")
                .whenComplete((res, ex) -> {
                    if (ex != null) System.out.println("Failed: " + ex);
                    else            System.out.println("whenComplete success: " + res);
                })
                .join();

        // --- allOf: wait for ALL futures to complete ---
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> "Task 1 done");
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> "Task 2 done");
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> "Task 3 done");

        CompletableFuture<Void> all = CompletableFuture.allOf(task1, task2, task3);
        all.join(); // waits for all
        // Retrieve individual results after allOf
        String combinedResults = Stream.of(task1, task2, task3)
                .map(CompletableFuture::join)
                .collect(Collectors.joining(", "));
        System.out.println("allOf results: " + combinedResults);

        // --- anyOf: complete as soon as ANY future completes ---
        CompletableFuture<Object> anyDone = CompletableFuture.anyOf(task1, task2, task3);
        System.out.println("anyOf first result: " + anyDone.join());

        // --- Async variants: thenApplyAsync, thenAcceptAsync, etc. ---
        // Without "Async" suffix: callback runs in the same thread as the
        //   completing stage (or calling thread if already complete).
        // With "Async" suffix: callback is submitted to ForkJoinPool.commonPool()
        //   (or a custom Executor if provided).
        CompletableFuture.supplyAsync(() -> "async result")
                .thenApplyAsync(s -> s + " (processed async)")
                .thenAcceptAsync(s -> System.out.println("thenApplyAsync: " + s))
                .join();
    }

    // =========================================================================
    // SECTION 9: JAVA 9+ FEATURES
    // =========================================================================
    //
    // INTERVIEW Q: What is the var keyword (Java 10)?
    // A: var is a local variable type inference. The compiler infers the type
    //    from the right-hand side initializer. It cannot be used for:
    //    - Fields, method parameters, return types
    //    - Local variables without an initializer
    //    - Lambda parameters (in Java 10; added in Java 11)
    //    - Array initializers without explicit type
    //
    // INTERVIEW Q: What are the immutable collection factories added in Java 9?
    // A: List.of(), Set.of(), Map.of(), Map.entry() — produce unmodifiable
    //    collections. They do NOT allow null elements. The iteration order of
    //    Set.of() and Map.of() is intentionally unspecified.

    static void demonstrateJava9Plus() {
        System.out.println("\n===== JAVA 9+ FEATURES =====");

        // --- var (Java 10) ---
        var list     = new ArrayList<String>(); // inferred as ArrayList<String>
        var number   = 42;                      // inferred as int
        var message  = "Hello Java 10";         // inferred as String
        list.add("one");
        list.add("two");
        System.out.println("var list: " + list + ", var number: " + number);

        // var in for-each and for loops
        var names = List.of("Alice", "Bob", "Charlie");
        for (var name : names) {
            System.out.print("var in for-each: " + name + "  ");
        }
        System.out.println();

        // --- Immutable collections (Java 9) ---
        List<String>    immutableList = List.of("a", "b", "c");
        Set<Integer>    immutableSet  = Set.of(1, 2, 3, 4);
        Map<String, Integer> immutableMap = Map.of("one", 1, "two", 2, "three", 3);

        System.out.println("List.of: " + immutableList);
        System.out.println("Set.of:  " + immutableSet);
        System.out.println("Map.of:  " + immutableMap);

        // Map.ofEntries for more than 10 entries
        Map<String, Integer> bigMap = Map.ofEntries(
                Map.entry("a", 1),
                Map.entry("b", 2),
                Map.entry("c", 3)
        );
        System.out.println("Map.ofEntries: " + bigMap);

        // Attempting mutation throws UnsupportedOperationException
        try {
            immutableList.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of is immutable: " + e.getClass().getSimpleName());
        }

        // --- Optional improvements (Java 9+) ---
        // or() — returns alternative Optional if empty (Java 9)
        Optional<String> opt = Optional.empty();
        Optional<String> result = opt.or(() -> Optional.of("fallback"));
        System.out.println("Optional.or(): " + result);

        // ifPresentOrElse() — Java 9
        Optional.of("value").ifPresentOrElse(
                v -> System.out.println("Present: " + v),
                () -> System.out.println("Empty")
        );

        // stream() — Java 9: convert Optional to Stream
        long count = Optional.of("hello").stream().filter(s -> s.length() > 3).count();
        System.out.println("Optional.stream() count: " + count);
    }

    // =========================================================================
    // SECTION 10: JAVA 14+ FEATURES
    // =========================================================================
    //
    // INTERVIEW Q: What is a Record (Java 16, preview in 14)?
    // A: A record is a special kind of class designed to be a transparent carrier
    //    for immutable data. The compiler automatically generates:
    //    - private final fields for each component
    //    - A canonical constructor
    //    - Accessor methods (same name as component, no "get" prefix)
    //    - equals(), hashCode(), toString()
    //    Records cannot extend classes (implicitly extend java.lang.Record),
    //    cannot declare instance fields other than record components, but CAN
    //    implement interfaces.
    //
    // INTERVIEW Q: What are Switch Expressions (Java 14)?
    // A: A new form of switch that is an expression (returns a value) using
    //    arrow (->) syntax. Eliminates fall-through and break. Multiple labels
    //    are supported with commas. yield is used to return values from blocks.
    //
    // INTERVIEW Q: What are Text Blocks (Java 15)?
    // A: A multi-line string literal delimited by """ (triple double-quotes).
    //    Preserves intended indentation. Avoids escape sequences for newlines.

    // Record declaration (Java 16 standard feature)
    record Point(double x, double y) {
        // Compact canonical constructor — validation
        Point {
            if (Double.isNaN(x) || Double.isNaN(y))
                throw new IllegalArgumentException("NaN coordinates not allowed");
        }

        // Custom method in a record
        double distanceTo(Point other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }
    }

    record Person(String name, int age) implements Comparable<Person> {
        // Instance methods are fine in records
        boolean isAdult() { return age >= 18; }

        @Override
        public int compareTo(Person other) { return Integer.compare(this.age, other.age); }
    }

    static void demonstrateJava14Plus() {
        System.out.println("\n===== JAVA 14+ FEATURES =====");

        // --- Records ---
        System.out.println("-- Records --");
        Point p1 = new Point(3.0, 4.0);
        Point p2 = new Point(0.0, 0.0);

        System.out.println("p1: " + p1);                    // auto toString
        System.out.println("p1.x(): " + p1.x());            // accessor (not getX)
        System.out.println("p1.y(): " + p1.y());
        System.out.println("distance p1 to p2: " + p1.distanceTo(p2));

        // Records have auto-generated equals and hashCode
        Point p3 = new Point(3.0, 4.0);
        System.out.println("p1.equals(p3): " + p1.equals(p3)); // true
        System.out.println("p1 == p3: " + (p1 == p3));          // false

        Person alice = new Person("Alice", 30);
        Person bob   = new Person("Bob", 25);
        System.out.println("alice: " + alice);
        System.out.println("alice.isAdult(): " + alice.isAdult());

        List<Person> people = Arrays.asList(alice, bob, new Person("Charlie", 35));
        people.stream()
                .sorted()
                .forEach(p -> System.out.println("  " + p));

        // --- Switch Expressions (Java 14) ---
        System.out.println("\n-- Switch Expressions --");

        // Traditional switch statement (still works)
        String day = "MONDAY";
        String type1;
        switch (day) {
            case "MONDAY": case "TUESDAY": case "WEDNESDAY":
            case "THURSDAY": case "FRIDAY":
                type1 = "Weekday"; break;
            default: type1 = "Weekend";
        }
        System.out.println("Traditional switch: " + type1);

        // Switch expression with arrow syntax (Java 14+)
        String type2 = switch (day) {
            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
            case "SATURDAY", "SUNDAY" -> "Weekend";
            default -> "Unknown";
        };
        System.out.println("Switch expression (arrow): " + type2);

        // Switch expression with yield (for multi-statement blocks)
        int numLetters = switch (day) {
            case "MONDAY", "FRIDAY", "SUNDAY" -> 6;
            case "TUESDAY"                    -> 7;
            case "THURSDAY", "SATURDAY"       -> 8;
            case "WEDNESDAY"                  -> {
                System.out.println("  Wednesday has 9 letters");
                yield 9;  // 'yield' returns value from a block
            }
            default -> -1;
        };
        System.out.println("Letters in " + day + ": " + numLetters);

        // Switch expression used in lambda/stream
        List<String> days = List.of("MONDAY", "SATURDAY", "WEDNESDAY", "SUNDAY");
        days.stream()
                .map(d -> switch (d) {
                    case "SATURDAY", "SUNDAY" -> d + " (Weekend)";
                    default                   -> d + " (Weekday)";
                })
                .forEach(System.out::println);

        // --- Text Blocks (Java 15) ---
        System.out.println("\n-- Text Blocks --");

        // Before Java 15:
        String jsonOld = "{\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"age\": 30\n" +
                "}";

        // Text block (Java 15+):
        String jsonNew = """
                {
                  "name": "Alice",
                  "age": 30
                }
                """;

        System.out.println("Text block JSON:");
        System.out.println(jsonNew);

        String html = """
                <html>
                    <body>
                        <p>Hello, Java 15!</p>
                    </body>
                </html>
                """;
        System.out.println("Text block HTML:");
        System.out.println(html);

        // String::formatted (Java 15) — equivalent to String.format()
        String sql = """
                SELECT *
                FROM users
                WHERE name = '%s'
                  AND age > %d
                """.formatted("Alice", 18);
        System.out.println("Formatted text block:\n" + sql);
    }

    // =========================================================================
    // SECTION 11: JAVA 17+ FEATURES
    // =========================================================================
    //
    // INTERVIEW Q: What are Sealed Classes (Java 17)?
    // A: Sealed classes restrict which other classes can extend/implement them.
    //    They work in conjunction with permits clause. Sealed hierarchies enable
    //    exhaustive pattern matching in switch expressions. A permitted class must
    //    be: final (no further extension), sealed (further restricted), or
    //    non-sealed (open to any extension).
    //
    // INTERVIEW Q: What is Pattern Matching for instanceof (Java 16)?
    // A: Eliminates the need for an explicit cast after instanceof check.
    //    Instead of: if (obj instanceof String) { String s = (String) obj; ... }
    //    You write:  if (obj instanceof String s) { ... use s directly ... }
    //    The binding variable 's' is only in scope when the pattern matches.

    // Sealed class hierarchy
    sealed interface Shape permits Circle, Rectangle, Triangle {
        double area();
    }

    record Circle(double radius) implements Shape {
        public double area() { return Math.PI * radius * radius; }
    }

    record Rectangle(double width, double height) implements Shape {
        public double area() { return width * height; }
    }

    static final class Triangle implements Shape {
        private final double base, height;
        Triangle(double base, double height) { this.base = base; this.height = height; }
        public double area() { return 0.5 * base * height; }
    }

    static void demonstrateJava17Plus() {
        System.out.println("\n===== JAVA 17+ FEATURES =====");

        // --- Pattern Matching for instanceof (Java 16) ---
        System.out.println("-- Pattern Matching instanceof --");

        Object obj = "Hello, Pattern Matching!";

        // Old style (verbose, requires explicit cast)
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("Old style: length = " + s.length());
        }

        // New style (Java 16) — binding variable 's' in scope only when matched
        if (obj instanceof String s) {
            System.out.println("Pattern matching: length = " + s.length());
        }

        // With additional condition
        if (obj instanceof String s && s.length() > 5) {
            System.out.println("Long string: " + s.toUpperCase());
        }

        // Use in method dispatch
        Object[] objects = { "hello", 42, 3.14, List.of(1, 2, 3), null };
        for (Object o : objects) {
            String desc;
            if      (o instanceof String s)  desc = "String of length " + s.length();
            else if (o instanceof Integer i)  desc = "Integer: " + i;
            else if (o instanceof Double d)   desc = "Double: " + d;
            else if (o instanceof List<?> l)  desc = "List of " + l.size();
            else if (o == null)               desc = "null";
            else                              desc = "other: " + o.getClass();
            System.out.println("  " + desc);
        }

        // --- Sealed Classes (Java 17) ---
        System.out.println("\n-- Sealed Classes --");

        List<Shape> shapes = List.of(
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 8)
        );

        // NOTE: Pattern matching in switch (case Circle c -> ...) requires Java 21+.
        // On Java 17 we use pattern matching instanceof (Java 16+) in an if/else chain.
        // The sealed hierarchy still gives us compile-time exhaustiveness awareness.
        shapes.forEach(shape -> {
            String desc;
            if (shape instanceof Circle c) {
                desc = String.format("Circle r=%.1f area=%.2f", c.radius(), c.area());
            } else if (shape instanceof Rectangle r) {
                desc = String.format("Rect %.1fx%.1f area=%.2f", r.width(), r.height(), r.area());
            } else if (shape instanceof Triangle t) {
                desc = String.format("Triangle area=%.2f", t.area());
            } else {
                desc = "Unknown shape";
            }
            System.out.println("  " + desc);
        });

        // Total area using sealed type exhaustiveness
        double total = shapes.stream().mapToDouble(Shape::area).sum();
        System.out.printf("Total area: %.2f%n", total);
    }

    // =========================================================================
    // MAIN METHOD — runs all demonstrations
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  JAVA 8+ FEATURES — COMPREHENSIVE INTERVIEW PREPARATION");
        System.out.println("=".repeat(70));

        demonstrateLambdas();
        demonstrateFunctionalInterfaces();
        demonstrateMethodReferences();
        demonstrateStreams();
        demonstrateOptional();
        demonstrateDefaultAndStaticInterfaceMethods();
        demonstrateDateTimeAPI();
        demonstrateCompletableFuture();
        demonstrateJava9Plus();
        demonstrateJava14Plus();

        // Java 17 features - instantiate outer class to call instance method indirectly
        Java8Features f = new Java8Features();
        Java8Features.demonstrateJava17Plus();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  ALL DEMONSTRATIONS COMPLETE");
        System.out.println("=".repeat(70));
    }

    /*
     * ==========================================================================
     * QUICK-REFERENCE INTERVIEW Q&A SUMMARY
     * ==========================================================================
     *
     * Q: What is the difference between Collection and Stream?
     * A: Collection stores elements; Stream processes them on demand.
     *    Stream is a pipeline, not a data structure. Stream is consumed after
     *    terminal operation; Collections can be iterated multiple times.
     *
     * Q: Can a stream be reused after a terminal operation?
     * A: No. Calling a second terminal operation throws IllegalStateException.
     *    You must create a new stream from the source.
     *
     * Q: What is a Spliterator?
     * A: A Spliterator (Splittable Iterator) is used to partition elements for
     *    parallel processing. It can split itself into two halves. Parallel
     *    streams use Spliterators under the hood via ForkJoinPool.
     *
     * Q: What does Collectors.toUnmodifiableList() do vs List.copyOf()?
     * A: Both return unmodifiable lists. toUnmodifiableList() is a stream terminal
     *    collector. List.copyOf(collection) creates an unmodifiable copy of an
     *    existing collection.
     *
     * Q: What is the Nashorn JavaScript Engine (Java 8)?
     * A: Nashorn was a JavaScript engine embedded in the JVM, replacing Rhino.
     *    It allowed executing JS from Java via javax.script API. It was deprecated
     *    in Java 11 and removed in Java 15 in favor of GraalVM.
     *
     * Q: What is the difference between LocalDate.now() and new Date()?
     * A: LocalDate.now() returns an immutable, thread-safe date object with no
     *    time component. new Date() returns a mutable object representing a
     *    specific moment in time (date + time). Date is legacy; prefer java.time.
     *
     * Q: How does CompletableFuture differ from RxJava Observable?
     * A: CompletableFuture handles a SINGLE async value/event.
     *    RxJava Observable handles MULTIPLE async values (a stream of events).
     *    For multiple values use CompletableFuture<List<T>> or reactive streams.
     * ==========================================================================
     */
}
