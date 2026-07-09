# Company-Wise Java Full Stack Interview Questions (Real, Sourced)

Compiled from LinkedIn interview-experience posts, Glassdoor candidate reviews, Medium interview write-ups, and GeeksforGeeks interview experiences (2024–2026). Each section reflects what candidates *actually reported being asked* at that company, organized by round.

---

## 🏢 TCS (Tata Consultancy Services)

**Typical rounds:** Online assessment (aptitude + coding) → Technical Round 1 (Core Java/OOPs/Collections/SQL) → Technical Round 2 (Spring Boot/project/coding) → Managerial/HR

**Core Java & OOPs**
- OOP concepts (Encapsulation, Inheritance, Abstraction, Polymorphism)
- Association, Aggregation, and Composition — differences
- What is immutability? Design a custom immutable class (e.g., `Employee` with id, name, `List<String>`, `Address`)
- Deep copy vs shallow copy — implement cloning
- Comparable vs Comparator — when to use which
- `equals()` and `hashCode()` — implement and explain contract
- JVM internals: Bootstrap ClassLoader, JIT compiler, Execution Engine
- What is `ConcurrentModificationException`? When does it occur?
- What is Method Hiding (static method + inheritance)?

**Collections & Java 8**
- Internal working of HashMap, LinkedHashMap, ConcurrentHashMap, TreeMap
- What happens during HashMap resizing?
- New Java 8 features; Method References; Default methods in interfaces
- Explain intermediate vs terminal Stream operations

**Spring Boot / Backend / Real Production Scenarios**
- How do you handle exceptions in Spring Boot?
- If multiple microservices are failing in your project, how do you identify the root cause and debug interconnected services?
- How does your Spring Boot app communicate with the database?
- How do you containerize your microservices (Docker)?
- Explain the Authentication layer in your project; how does JWT work?
- How do you integrate 3rd-party APIs/data into your project?
- What is HTTP status code 204, and when is it used?
- Explain garbage collection and how it works

**Coding round (reported live problems)**
- Write a Java program to filter even numbers from a list and sort them ascending
- Given a REST endpoint, write a simple frontend (HTML/CSS/JS) to fetch and display the data

---

## 🏢 Infosys

**Typical rounds:** Online coding assessment (multi-topic MCQ + coding) → Technical phone/panel round → Manager round → HR

**Reported online-assessment structure (Bengaluru, 2023–24):**
- Section 1: Java (~15 Qs, mostly Stream API)
- Section 2: REST API (~15 Qs)
- Section 3: Spring Boot (~15 Qs)
- Section 4: Microservices (~15 Qs)
- Section 5: DevOps (~5 Qs)
- Section 6: Agile (~5 Qs)

**Core Java / Concurrency (Infosys leans heavily here)**
- Why make objects immutable, and how do you create an immutable class?
- Thread creation, synchronization, deadlocks, concurrent collections
- Runnable vs Thread — why is `implements Runnable` usually preferred?
- Synchronized blocks/methods vs `ReentrantLock` vs `ReadWriteLock`
- `volatile` keyword, functional interfaces, `Optional`/`OptionalList`
- Exception handling design for resilient production code

**Backend/Project**
- Write the structure of a typical REST API you built
- What is Spring Boot Actuator? What is an initializer?
- What is an Entity (JPA)? How do you improve API performance?
- Single-token authentication/authorization — how did you implement it?
- Transaction management; design patterns used in your project
- Unit testing approach: `@SpringBootTest`, `@MockBean`, `@WebMvcTest`, JUnit 5 + Mockito

**Frontend (Angular/React — Infosys full stack roles test both)**
- Angular: pipes, component lifecycle, how to consume an API from the frontend, lazy loading
- React: conditional rendering, rendering lists (`.map()`), component lifecycle methods
- React state management: `useState`, `useReducer`, Context API, Redux basics
- React Router v6: `<Route>`, `<Link>`, dynamic routing
- JavaScript: hoisting, closures, `==` vs `===`, `var`/`let`/`const`, event loop & call stack, `map`/`filter`/`reduce`, async/await, shallow vs deep copy

---

## 🏢 Wipro

**Typical rounds:** Online assessment (aptitude + Java MCQs + SQL + HTML/CSS/JS basics) → Technical round (backend → gradually shifts to full stack) → HR/Final

**Stack commonly probed:** Java, Spring Boot, Microservices, Kafka, Angular/React, SQL, AWS

**Reported questions**
- Tell me about your role and responsibilities in your project (always opens the technical round)
- SOLID principles — explain each with real-world examples
- `map()` vs `flatMap()` in Java Streams
- OOP concepts with examples; Exception handling — write sample code
- Multithreading — write a small multithreaded program
- Spring Boot lifecycle and annotations: `@RestController`, `@Service`, `@Autowired`
- Creating REST APIs and securing them
- SQL: joins, and a live scenario — design a schema and write queries for it
- JavaScript/DOM manipulation basics
- React: components, props, state management (if mentioned on resume)
- Coding: reverse words in a string; efficiently traverse a matrix

---

## 🏢 Accenture

**Typical rounds:** MCQ round → Technical MCQ → Technical interview → Manager interview

**Reported questions**
- Implement Merge Sort (live coding)
- Apache Kafka — internal settings and configuration questions
- SQL databases — deep questions, especially if the project is in banking/finance domain
- Core Java fundamentals + basic OOP (screening-level, then deeper in tech rounds)
- Project architecture walkthroughs — expect follow-up "why" questions on every design decision

---

## 🏢 Cognizant

**Typical rounds:** Technical round → Communication round → "Digital Nurture" course/assessment → Final interview (all elimination rounds)

**Reported questions**
- Java fundamentals, Spring, REST
- JavaScript basics
- SQL and DBMS fundamentals
- C++ fundamentals (sometimes tested alongside Java depending on the requisition)
- Coding: reverse a string using multiple different methods; count vowels and consonants in a string

---

## 🏢 Amazon (Product company — different bar entirely)

**Typical rounds:** Online Assessment (2 DS/Algo coding problems, ~90 min) → Work Simulation → Work Style Survey → 4–5 technical/behavioral interviews → Bar Raiser debrief

**Amazon tests DSA and system fundamentals far more than framework trivia. It also weighs Leadership Principles heavily in every round.**

**Coding (DS/Algo — reported)**
- Find k closest numbers to a target in an unsorted array
- Find the missing number in an array of 1..n
- Two-sum style problems
- Merge two sorted linked lists; copy a linked list with random pointers
- Print each level of a binary tree on a new line
- Top-10 elements from an integer array

**CS Fundamentals / Design**
- OOP concepts: Polymorphism, Inheritance, Encapsulation, Abstraction
- Aggregation vs Composition
- Design patterns you've implemented — implement Singleton
- Design a system to model a deck of cards (mini low-level design)
- Threads, synchronization, deadlocks
- Differences between Windows and Unix (older reports, less common now)

**Behavioral (every round includes at least one Leadership Principle question)**
- Describe the architecture of a project you're proud of
- Tell me about the most challenging work you've faced
- Prepare 6–8 STAR stories mapped to Amazon's Leadership Principles (Ownership, Bias for Action, Dive Deep, etc.) — this matters as much as the coding round

---

## Cross-company patterns (what to prioritize)

| Theme | Companies where it's heavily tested |
|---|---|
| Core Java + OOP + Collections | All (TCS, Infosys, Wipro, Accenture, Cognizant) |
| Multithreading/Concurrency | Infosys (heavy), Amazon, TCS |
| Spring Boot annotations & lifecycle | TCS, Infosys, Wipro |
| Microservices debugging/scenarios | TCS, Wipro |
| SQL/joins/schema design | Wipro, Accenture, Cognizant |
| Frontend (React/Angular/JS) | Infosys, Wipro |
| Kafka | Wipro, Accenture |
| Pure DSA/Algorithms | Amazon (heavy), TCS Digital, Infosys assessments |
| Behavioral/Leadership Principles | Amazon (very heavy), all companies for HR round |

## What this means for your prep

1. **Service companies (TCS/Infosys/Wipro/Cognizant/Accenture)** weight your **project explanation** heavily — nearly every technical round starts with "walk me through your project," then drills into whatever tech you mention. Know your own project's architecture cold, including *why* you made specific choices (why JWT over sessions, why this DB, how you handled a specific bug).
2. **Product companies (Amazon and similar)** weight **DSA + system design + behavioral STAR stories** much more than framework trivia — practice LeetCode-style problems and prepare Leadership-Principle-style stories regardless of which company you interview at, since most product companies use similar formats.
3. Across every company, **Java 8 Streams coding questions** (group by field, find duplicates, second-highest value, palindrome check) show up as live coding exercises — practice writing these from scratch without an IDE.

---

*Sources: LinkedIn interview-experience posts (Java By Madhu Vundavalli, Anil, Vishal Junghare, Hemanth Vishwakarma), Glassdoor candidate-submitted interview reviews (TCS, Infosys, Wipro, Accenture, Cognizant, Amazon), GeeksforGeeks interview experiences, Medium (Javarevisited, DevInDepth).*
