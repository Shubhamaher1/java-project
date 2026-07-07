
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class EmployeeItem {

    String name;
    String dept;

    EmployeeItem(String name, String dept) {
        this.name = name;
        this.dept = dept;
    }

    public String getDepartmaent() {
        return this.dept;
    }
}

class Employeed {

    String name;
    String dept;
    double salary;

    public Employeed(String name, String dept, double salary) {
        this.name = name;
        this.dept = dept;
        this.salary = salary;
    }
    public double getSalary(){
        return  this.salary;
    }
    public  String getDepartment(){
        return  this.dept;
    }
    public  String toString(){
        return  this.name +"-"+this.dept+"-"+this.salary;
    }

}

class Java8Revision {

    public static void main(String[] args) {

        // List of Arrays i have and  i wanted to convet it int the list by stream 
        // int arr[] = new int[]{1,2,3,4,5};
        //  Arrays.stream(arr).map(e->e).toList();
        List<Integer> list = Arrays.asList(2, 34, 54, 5, 6);
        // filters by any condition 
        List<Integer> ans = list.stream().filter(e -> e > 100).toList();

        //  count all in 2d array
        List<List<Integer>> dp = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(2, 5, 6)
        );

        List<Integer> allInOneList = dp.stream().flatMap(e -> e.stream()).distinct().sorted().collect(Collectors.toList());
        System.out.println(allInOneList);

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
        // find the top marks student 
        // System.err.println(listtech.stream().flatMap(e->e.getStudents().stream()).max(Collectors.maxBy(Students::getMarks)));

        //convert list of strings to uppercase 
        List<String> words = Arrays.asList("apple", "mango", "banana", "cherry");
        List<String> upperWords = words.stream().map(e -> e.toUpperCase()).toList();
        System.err.println(upperWords);

        // filters the even numbers 
        List<Integer> listOfIntegers = Arrays.asList(20, 12, 34, 56, 77, 77);
        List<Integer> evenInteger = listOfIntegers.stream().filter(e -> e % 2 == 0).toList();
        System.err.println(evenInteger);
        // count the numbers which is greater than 10 
        long greaterThan10 = listOfIntegers.stream().filter(e -> e > 10).count();
        System.err.println(greaterThan10);
        // find the all name start with A
        List<String> startWithA = words.stream().filter(e -> e.toLowerCase().startsWith("a")).toList();
        System.err.println(startWithA);
        // Squarwe the all Number
        List<Integer> sqOfAllNo = listOfIntegers.stream().map(e -> e * e).toList();
        System.err.println(sqOfAllNo);
        // Square all even no on'ly 
        List<Integer> sqofAllEvenNo = listOfIntegers.stream().map(e -> e % 2 == 0 ? e * e : e).toList();
        System.err.println(sqofAllEvenNo);

        // Collectors (Very Important)  Concepts Covered: collect(), Collectors.toList(), joining(), counting()
        // convert stream into the list 
        System.err.println(words.stream().collect(Collectors.toList()));
        // join the all string with ' ,'
        String joinedString = words.stream().collect(Collectors.joining(","));
        System.err.println("" + joinedString);

        // count the all element vie collect 
        long countAllElement = words.stream().collect(Collectors.counting());
        System.err.println("Count " + countAllElement);

        // covert list into Set 
        Set<String> listToSet = words.stream().collect(Collectors.toSet());
        System.err.println("Set" + listToSet);

        // Concepts Covered: groupingBy(), partitioningBy()
        // group element by even or odd 
        Map<String, List<Integer>> listOfgropued = listOfIntegers.stream().collect(Collectors.groupingBy(e -> e % 2 == 0 ? "odd" : "even"));
        System.err.println("Grouped Element " + listOfgropued);

        List<EmployeeItem> employees = Arrays.asList(
                new EmployeeItem("Sarvesh", "SDE"),
                new EmployeeItem("Sonali", "HR"),
                new EmployeeItem("Barvesh", "SDE"),
                new EmployeeItem("Kavesh", "Finance"),
                new EmployeeItem("Moresh", "HR"),
                new EmployeeItem("Paramesh", "SDE")
        );

        // group employee by Departement 
        Map<String, List<EmployeeItem>> groupByDep = employees.stream().collect(Collectors.groupingBy(e -> e.dept));
        System.out.println(groupByDep);

        // count all department wise 
        Map<String, Long> countingBydep = employees.stream().collect(Collectors.groupingBy(e -> e.dept, Collectors.counting()));
        System.err.println(countingBydep);

        // Sorting & Comparators
        // sort the element asecnding 
        List<Integer> sortedEle = listOfIntegers.stream().sorted().toList();
        System.err.println(sortedEle);
        // decendint sort 
        List<Integer> soertedfEleDec = listOfIntegers.stream().sorted(Comparator.reverseOrder()).toList();
        System.err.println(soertedfEleDec);

        // sort employee by salarey 
        listtech.stream().flatMap(e -> e.getStudents().stream()).collect(Collectors.toList());

        System.out.println(
                listtech.stream().flatMap(e -> e.getStudents().stream()).sorted(Comparator.comparingDouble(Students::getMarks)).toList()
        );

        // sort by multiple filed 
        System.out.println(listtech.stream().flatMap(e -> e.getStudents().stream()).sorted(Comparator.comparing(Students::getMarks).thenComparing(Students::getName)).toList());

        // remove deublicate 
        System.out.println(listOfIntegers.stream().distinct().toList());

        // get only first 3 element 
        System.out.println(listOfIntegers.stream().limit(3).toList());

        // skip first 2 element 
        System.err.println(listOfIntegers.stream().skip(2).toList());

        // find the first eleem,entn  
        System.err.println(listOfIntegers.stream().findFirst().orElse(null).toString());

        // sum of all element 
        System.out.println(listOfIntegers.stream().reduce((a, b) -> a + b).get().toString());

        // product of each eleemnet 
        System.err.println(listOfIntegers.stream().reduce(-1, (A, b) -> A * b));

        List<List<Integer>> listOfLists = Arrays.asList(
                Arrays.asList(1, 2, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(6, 7, 8)
        );

        // convert in flatlist 
        System.out.println(listOfLists.stream().flatMap(e -> e.stream()).distinct().toList());

        // 9. Frequency / HashMap Problems (Very Common)
        String str = "streamapi";
        // find first no reapatating char in this string 
        Map<Character, Long> counting = str.chars().mapToObj(c -> (char) c).collect(
                Collectors.groupingBy(
                        Function.identity(),
                        LinkedHashMap::new, // keep in seques
                        Collectors.counting()
                )
        );
        System.out.println(counting);
        System.err.println(counting.entrySet().stream().filter(enrt -> enrt.getValue() == 1).findFirst().get().getKey());

        // frq of each eleemnt 
        System.out.println(listOfIntegers.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().max(Comparator.comparing(ele -> ele.getValue())));

        // Question 32: Find second highest salary
        List<Employeed> employeesed = Arrays.asList(
                new Employeed("Amit", "SDE", 100000.0),
                new Employeed("Riya", "HR", 80000.0),
                new Employeed("Karan", "Finance", 90000.0),
                new Employeed("Neha", "SDE", 120000.0),
                new Employeed("Rahul", "HR", 75000.0)
        );
        // find the second higest salary 
        System.out.println(employeesed.stream().sorted(Comparator.comparing(Employeed::getSalary).reversed()).skip(1).limit(1).toList());
        // find max salay each department 
        System.out.println(
            employeesed.stream().collect(Collectors.groupingBy(Employeed::getDepartment, Collectors.maxBy(Comparator.comparing(Employeed::getSalary)))));



    }
}
