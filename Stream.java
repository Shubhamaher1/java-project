
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Employee {

    private int id;
    private String name;
    private String department;
    private double salary;
    private int age;
    private List<String> skills;

    // all arg  
    public Employee(int id, String name, String department, double salary, int age, List<String> skills) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.age = age;
        this.skills = skills;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDepartment() {
        return this.department;
    }

    public double getSalary() {
        return this.salary;
    }

    public int getAge() {
        return this.age;
    }

    public List<String> getSkills() {
        return this.skills;
    }

    public String toString() {
        return this.name + "-" + this.department + "-" + this.age+"-sal: "+this.salary;
    }

}

public class Stream {

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Amit", "IT", 90000, 28,
                        Arrays.asList("Java", "Spring")),
                new Employee(2, "Neha", "HR", 60000, 32,
                        Arrays.asList("Communication")),
                new Employee(3, "Raj", "IT", 120000, 35,
                        Arrays.asList("Java", "Microservices")),
                new Employee(4, "Priya", "Finance", 95000, 30,
                        Arrays.asList("Excel", "Accounting")),
                new Employee(5, "Karan", "IT", 70000, 26,
                        Arrays.asList("Java", "AWS"))
        );
       List<Employee>filted=  employees.stream().filter(e->e.getSalary()>10000).toList();
       System.out.println(filted);
       // Map employee name get all employeename 
       List<String> employeeName = employees.stream().map(e->e.getName()).collect(Collectors.toList());
       System.out.println(employeeName);

       //sort by salary low to high 
       List<Employee> emp = employees.stream().sorted(Comparator.comparing(Employee::getSalary)).toList();
       System.out.println(emp);
       // just reversed()
        // Higest Salary Employee 
        Employee empWithHighSal = employees.stream().sorted(Comparator.comparing(Employee::getSalary).reversed()).skip(0).findFirst().orElse(null);
        System.out.println(empWithHighSal);
        Employee higest  = employees.stream().max(Comparator.comparing(Employee::getSalary)).get();
        System.out.println(higest);

        //Group Employee By department 

        Map<String, List<Employee>> mp = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment));
        System.out.println(mp);

        // count employee each depatment 
        Map<String , Long> countEchDep = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment, 
            Collectors.counting()
        ));
        System.out.println(countEchDep);

        // Avg Salary for department 
        Map<String , Double> avgSalEach = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment,
            Collectors.averagingDouble(Employee::getSalary)
        ));
        System.err.println(avgSalEach);

        // get all skills 
        List<String> skil = employees.stream().flatMap(e->e.getSkills().stream()).toList();
        System.err.println(skil);

        // find departement with higest avgsalry 

         Map.Entry<String , Double> findMaxDepAvg = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment,
            Collectors.averagingDouble(Employee::getSalary)
         )).entrySet().stream().max(Map.Entry.comparingByValue()).get();

         // Siner leveel -salary 60000>
         Map<String , List<Employee>> mplist = employees.stream().collect(Collectors.groupingBy(e->e.getSalary()>60000? "High":"Low"));
         System.err.println(mplist);

         // 
         List<Integer> intNo  = Arrays.asList(100, 200, 500, 600,700,450, 3000);
         // remove all number which garater than 100 
         System.err.println(intNo.stream().filter(e->e<=100).toList());

         List<Integer> intNo2 = Arrays.asList(200,300,100,500,600);
         // find the all emlemnt contains
         HashSet<Integer> oneList = new HashSet<>();
         intNo.stream().forEach(e-> oneList.add(e));
         System.out.println(oneList);
         System.err.println(intNo2.stream().filter(ele->oneList.contains(ele)).toList());
         // continas both list
         System.out.println(intNo.stream().filter(intNo2::contains).toList());

         // not contiains like 
        //  System.out.println(intNo.stream().filter(e->!intNo2.contains(e)).toList().addall(intNo2.stream().filter(e->!intNo.contains(e)).toList());
    }

}
