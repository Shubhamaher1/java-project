import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.GroupLayout.Group;

class Teachers {
    String name;
    List<Students> st;

    Teachers(String name, List<Students> st) {
        this.name = name;
        this.st = st;
    }

    String getName() {
        return this.name;
    }

    List<Students> getStudents() {
        return this.st;
    }

}

class Students {
    private int id;
    private String name;
    private int marks;

    Students(int id, String name, int marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    int getMarks() {
        return this.marks;
    }

    String getName() {
        return this.name;
    }
}

class Emplyee {
    private int id;
    private String name;
    private String dept;
    private double salary;

    Emplyee(int id, String name, String dept, double salary) {
        this.id = id;
        this.dept = dept;
        this.name = name;
        this.salary = salary;
    }

    public int getId() {
        return this.id;
    }

    String getName() {
        return this.name;
    }

    String getDep() {
        return this.dept;
    }

    double getSalary() {
        return this.salary;
    }

    //
    @Override
    public String toString() {
        return name + " " + dept + " " + salary;
    }

}

public class Java8 {
    public static void main(String[] args) {

        // From a Collection: Create a stream directly from a List, Set or any
        // Collection using stream()
        List<String> name = new ArrayList<>();
        name.add("Shubhab");
        name.stream().forEach(e -> Arrays.stream(e.split("")).forEach(ee -> System.out.println(ee)));
        // From an Array: Use Arrays.stream(array) to convert an array into a stream.

        int arr[] = new int[] { 1, 345, 5, 6, 7 };
        System.out.println(Arrays.stream(arr).map(e -> e).count());
        // Using Stream.of(): Create a stream from a fixed set of values using
        // Stream.of().
        // Infinite Stream: Generate an unbounded sequence using Stream.iterate() or
        // Stream.generate()

        // filter(): Filters elements based on a specified condition.
        List<Integer> ls = Arrays.asList(2, 34, 5, 65, 6);
        List<Integer> ans = ls.stream().filter(e -> e % 2 == 0).toList();
        System.out.println(ans);
        // map(): Transforms each element in a stream to another value.
        List<Integer> anss = ls.stream().map(e -> e / 2).toList();
        System.out.println(anss);
        // Sorted(): Sorts the elements of a stream.
        List<Integer> sortedOrder = ls.stream().sorted().toList();
        System.out.println(sortedOrder);
        List<Integer> reverSorted = ls.stream().sorted(Comparator.reverseOrder()).toList();
        System.out.println(reverSorted);
        // Distinct(): Remove duplicates.
        List<Integer> dist = ls.stream().distinct().toList();
        // Skip(): Skip first n elements.
        List<Integer> no = ls.stream().skip(3).toList();
        System.out.println(no);

        // Terminal Operations are the operations that on execution return a final
        // result as an absolute value.

        // ForEach(): It iterates all the elements in a stream.
        ls.stream().forEach(e -> System.out.println(e));
        // collect(Collectors.toList()): It collects stream elements into a list (or
        // other collections like set/map).
        List<Integer> coll = ls.stream().collect(Collectors.toList());
        System.out.println(coll);
        // Reduce(): It reduces stream elements into a single aggregated result.
        // List<Integer> red = ls.stream().reduce(e, +);
        // count(): It returns the total number of elements in a stream.
        long count = ls.stream().count();
        // anyMatch() / allMatch() / noneMatch(): They check whether elements match a
        // given condition.
        boolean findMatch = ls.stream().anyMatch(e -> e > 4);

        // findFirst() / findAny(): They return the first or any element from a stream.
        // int findf = ls.stream().findFirst();

        List<Students> sts = new ArrayList<>();
        sts.add(new Students(0, "Shubham", 23));
        sts.add(new Students(1, "Nikita", 234));
        sts.add(new Students(2, "Om", 235));
        sts.add(new Students(3, "sagr", 223));
        sts.add(new Students(4, "sb", 273));
        List<Students> sts2 = new ArrayList<>();
        sts2.add(new Students(0, "Shubham", 234));
        sts2.add(new Students(1, "Nikita", 2334));
        sts2.add(new Students(2, "Om", 235));
        sts2.add(new Students(3, "sagr", 223));
        sts2.add(new Students(4, "sb", 273));
        List<Teachers> listtech = new ArrayList<>();
        Teachers t1 = new Teachers("Soham", sts);
        Teachers t2 = new Teachers("Sachine", sts2);
        listtech.add(t1);
        listtech.add(t2);

        Map<String, Students> ansTopMarks = listtech.stream()
                .collect(Collectors.toMap(
                        Teachers::getName,
                        teacher -> teacher.getStudents().stream().max(Comparator.comparingInt(Students::getMarks))
                                .orElse(null)));

        // Map<String, Students> map =
        // listtech.stream()
        // .collect(Collectors.toMap(
        // Teachers::getName,
        // teacher -> teacher.getStudents()
        // .stream()
        // .max(Comparator.comparingInt(Students::getMarks))
        // .orElse(null)
        // ));

        // for(Map.Entry<String, Students> ele: map.entrySet()){
        // System.out.println(ele .getKey()+ ele.getValue().marks);
        // }

        // 1. Get all student names from all teachers using Stream API.

        List<String> std = listtech.stream().flatMap(e -> e.getStudents().stream()).collect(Collectors.toList())
                .stream().map(e -> e.getName()).toList();
        System.out.println(std);
        // for(Students s : std){
        // System.out.println(s.getName());
        // }

        // 2. Get all students whose marks are greater than 80.

        List<Students> topOF80 = listtech.stream()
                .flatMap(e -> e.getStudents().stream().filter(es -> es.getMarks() >= 800)).toList();
        for (Students sm : topOF80) {
            System.out.println(sm.getName() + " " + sm.getMarks());
        }
        // 3. Count total students across all teachers.
        long countAllStudent = listtech.stream().flatMap(e -> e.getStudents().stream()).count();
        System.out.println(countAllStudent);

        // 4. Find the student with the highest marks.
        Optional<Students> higestMarks = listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .max(Comparator.comparingInt(Students::getMarks));
        System.out.println(higestMarks);
        // 5. Find the student with the lowest marks.
        Optional<Students> minMarks = listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .min(Comparator.comparingInt(Students::getMarks));
        System.out.println(minMarks);
        // 6. Get average marks of all students.
        OptionalDouble avgOfMarks = listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .mapToInt(el -> el.getMarks()).average();
        listtech.stream().flatMap(e -> e.getStudents().stream()).collect(Collectors.averagingInt(Students::getMarks));
        System.out.println(avgOfMarks);

        // 7. Get total sum of all students' marks.
        IntSummaryStatistics sumOfAll = listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .collect(Collectors.summarizingInt(Students::getMarks));
        System.out.println(sumOfAll.getSum());

        // 8. Sort all students by marks ascending order.
        List<Students> sorted = listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .sorted(Comparator.comparingInt(Students::getMarks)).collect(Collectors.toList());
        for (Students st : sorted) {
            System.out.println(st.getName() + st.getMarks());
        }

        // Sort all students by marks descending order.
        List<Students> sortedDes = listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .sorted(Comparator.comparingInt(Students::getMarks).reversed()).toList();
        for (Students st : sortedDes) {
            System.out.println(st.getName() + st.getMarks());
        }

        // Group students by marks usin
        // Map<Integer, List<Students>> listofGrop =
        // listtech.stream().flatMap(e->e.getStudents().stream()).toList().stream().collect(Collectors.groupingBy(Students::getMarks));
        // for(Map.Entry<Integer, List<Students>> stsEntry: listofGrop.entrySet()){
        // System.out.print(stsEntry.getKey() );
        // stsEntry.getValue().stream().forEach(e-> System.out.print(e.getName()));
        // System.out.println();

        // }
        // 11. Partition students into pass/fail using `partitioningBy()`.
        System.out.println(listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .collect(Collectors.partitioningBy(ele -> ele.getMarks() > 300)));
        // Convert all student names into uppercase using
        listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream()
                .map(el -> el.getName().toUpperCase());
        // 13. Remove duplicate students by name using `distinct()`.

        System.out.println(
                listtech.stream().flatMap(e -> e.getStudents().stream()).toList().stream().distinct().toList());

        List<Integer> numbers = Arrays.asList(10, 20, 30, 40, 20, 10, 50);
        List<String> names = Arrays.asList("Java", "Spring", "Boot", "Java", "API", "", null);

        // Que 1 Find Even number
        System.out.println(numbers.stream().filter(e -> e % 2 == 0).toList());

        // Que filters the ODD numbers
        System.out.println(numbers.stream().filter(e -> e % 2 != 0).collect(Collectors.toList()));

        // find numbers greater than 25
        System.out.println(numbers.stream().filter(e -> e > 25).toList());
        // Count the total element
        System.out.println(numbers.stream().count());

        // Remove the the dublicate
        System.out.println(numbers.stream().distinct().toList());

        // 6 sort acensding
        System.out.println(numbers.stream().sorted().toList());
        // 7 sort decending
        System.out.println(numbers.stream().sorted(Comparator.reverseOrder()).toList());
        // 8 covent string to uppercase
        System.out.println(names.stream().filter(e -> e != null).map(ele -> ele.toUpperCase()).toList());

        // 9 String to lowercase
        System.out
                .println(names.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toList()));

        // 10 Find the first element
        System.out.println(names.stream().findFirst());

        // 11 Sum of Numebers
        System.out.println(numbers.stream().mapToInt(Integer::intValue).sum());
        System.out.println(numbers.stream().collect(Collectors.summarizingInt(Integer::intValue)).getSum());

        // 12 avg no numbers
        System.out.println(numbers.stream().mapToInt(Integer::intValue).average());
        System.out.println(numbers.stream().collect(Collectors.averagingInt(Integer::intValue)));

        // maximum number
        System.out.println(numbers.stream().mapToInt(Integer::intValue).max());
        System.out.println(numbers.stream().collect(Collectors.summarizingInt(Integer::intValue)).getMax());

        // minimum Nuber from the numbers list

        System.out.println(numbers.stream().collect(Collectors.summarizingInt(Integer::intValue)).getMin());
        System.out.println(numbers.stream().mapToInt(Integer::intValue).min());

        // 15 second highest numbers - sort then skip fist element then findfirst
        System.out.println(numbers.stream().sorted(Comparator.reverseOrder()).skip(1).findFirst());
        // 15 second lowest number from numbers list
        System.out.println(numbers.stream().sorted().skip(1).findFirst());

        // 17 top three highest numbers
        System.out.println(numbers.stream().sorted(Comparator.reverseOrder()).limit(3).toList());

        // 18 bottom 3 smallest numbers
        System.out.println(numbers.stream().sorted().distinct().limit(3).toList());
        // multiply all numers
        System.out.println(numbers.stream().collect(Collectors.summarizingInt(Integer::intValue)));
        System.out.println(numbers.stream().mapToInt(Integer::intValue).sum());
        System.err.println(numbers.stream().reduce(-1, (a, b) -> a * b));
        // 20 sume of squers of ele

        System.err.println(numbers.stream().map(e -> e * e).mapToInt(Integer::intValue).sum());

        // For above questions from 21-50 need Emplyee Class

        // List of Employee
        List<Emplyee> empList = Arrays.asList(
                new Emplyee(1, "Amit", "IT", 60000),
                new Emplyee(2, "Rahul", "HR", 50000),
                new Emplyee(3, "Sneha", "IT", 80000),
                new Emplyee(4, "Priya", "Finance", 70000),
                new Emplyee(5, "Kunal", "HR", 55000));
        // 21 group by Department 

        Map<String, List<Emplyee>> groupByDep = empList.stream().collect(Collectors.groupingBy(Emplyee::getDep));
        System.out.println(groupByDep);

        // 22 count emplyee per deparment
        Map<String, Long> countPerDep = empList.stream().collect(Collectors.groupingBy(Emplyee::getDep, Collectors.counting()));
        System.out.println(countPerDep);
        // 23 Highest Paid Emplyee per department 
         System.out.println(empList.stream().collect(Collectors.groupingBy(Emplyee::getDep,Collectors.maxBy(Comparator.comparing(Emplyee::getSalary)))));

         // 24 conver employee list to Map with id & Employee 
         Map<Integer , Emplyee> emopl = empList.stream().collect(Collectors.toMap(Emplyee::getId, e->e));
         System.out.println(emopl);
        // 25 total Salary for All Emplyoee 
         System.out.println(empList.stream().mapToDouble(Emplyee::getSalary).sum());
         System.out.println(empList.stream().collect(Collectors.summingDouble(Emplyee::getSalary)));
         

         // 26 Avg Salrary of 
         System.out.println(empList.stream().mapToDouble(Emplyee::getSalary).average());
        //  System.err.println(empList.stream().);
        // 27 Emplyee with max salary 
        System.err.println(empList.stream().max(Comparator.comparing(Emplyee::getSalary)));

        // 28 get all emplye which enaring more that avg
        double avgOfSal = empList.stream().mapToDouble(Emplyee::getSalary).average().orElse(0.0);
        System.out.println(empList.stream().filter(e-> e.getSalary()> avgOfSal).toList());

        // Partition employees based on Salary > 6000
        System.out.println(empList.stream().collect(Collectors.partitioningBy( (sal)->sal.getSalary()>60000)));

        // 30 sort emplyee by salary 
        System.out.println(empList.stream().sorted(Comparator.comparing(Emplyee::getSalary)).toList());
        //  get name all Employee 
        List<String> nameOfAllEmployee = empList.stream().map(e->e.getName()).toList();
        System.err.println(nameOfAllEmployee);

        //32  count total employee with salary more than 60000
        long coutnZTh = empList.stream().filter(e->e.getSalary()>60000).count();
        System.err.println(coutnZTh);
        

        // 33 check if any employee from HR
        System.out.println(empList.stream().anyMatch(e -> "HR".equals(e.getDep())));

        // 34 check if all employee earn more than 40000
        System.out.println(empList.stream().allMatch(e-> e.getSalary()> 40000));

        // 35 find employee by name 
        System.out.println(empList.stream().filter(e->e.getName()=="Kunal").toList());
        // 36 sort by name 
        System.out.println(empList.stream().sorted(Comparator.comparing(Emplyee::getName)).toList());

        // 37 group salary by range 
        System.out.println(empList.stream().collect(Collectors.groupingBy(e->e.getSalary()>60000 ? "HIGH" : "LOW")));
        System.out.println(empList.stream().collect(Collectors.partitioningBy(ele->ele.getSalary()>=60000)));

        // 38 find all department name 
        System.err.println(empList.stream().map(e->e.getDep()).distinct().toList());
        // 39 count the Employee 
        System.err.println(empList.stream().count());
        //40  convert emplyee into emmutablel list -> toList() make immutable list 

    }

}

// 1. Get all student names from all teachers using Stream API.

// 2. Get all students whose marks are greater than 80.

// 3. Count total students across all teachers.

// 4. Find the student with the highest marks.

// 5. Find the student with the lowest marks.

// 6. Get average marks of all students.

// 7. Get total sum of all students' marks.

// 8. Sort all students by marks ascending order.

// 9. Sort all students by marks descending order.

// 10. Group students by marks using `Collectors.groupingBy()`.

// 11. Partition students into pass/fail using `partitioningBy()`.

// 12. Convert all student names into uppercase using `map()`.

// 13. Remove duplicate students by name using `distinct()`.

// 14. Find first student whose marks are greater than 90.

// 15. Check whether all students passed (marks > 35).

// 16. Check whether any student scored 100.

// 17. Create a Map where:

// ```java
// key = teacher name
// value = list of students
// ```

// 18. Create a Map where:

// ```java
// key = student id
// value = student object
// ```

// 19. Handle duplicate student IDs in `Collectors.toMap()`.

// 20. Use `flatMap()` to create a single list of all students from all
// teachers.

// 21. Get top 3 highest-scoring students.

// 22. Skip first 2 students after sorting by marks.

// 23. Find teacher whose students have highest average marks.

// 24. Count students teacher-wise.

// 25. Find student names starting with "A".

// 26. Join all student names into comma-separated string.

// 27. Get marks statistics using `IntSummaryStatistics`.

// 28. Find duplicate student names.

// 29. Separate students into grades:

// * A → > 80
// * B → 60–80
// * C → < 60

// 30. Find second-highest marks student using streams.

// 31. Find teacher having maximum number of students.

// 32. Convert List<Student> into:

// ```java
// Map<String, Integer>
// name -> marks
// ```

// 33. Find students common between two teachers.

// 34. Find unique student names across all teachers.

// 35. Use `peek()` to debug stream processing.

// 36. Demonstrate lazy evaluation using `filter()` and `map()`.

// 37. Use `reduce()` to calculate total marks.

// 38. Find teachers whose all students scored above 60.

// 39. Find teachers having at least one student failed.

// 40. Convert nested student list into flat list without using loops.
