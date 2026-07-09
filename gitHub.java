
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class gitHub {

    public static void main(String[] args) {

        // Employee e = new Employee(id, name, department, salary, age, skills)()
        
        
	   List<EmployeeS> employeesLists = Arrays.asList(
			new EmployeeS(1, "Abraham", 29, "IT", "Mumbai", 20000, "Male"),
			new EmployeeS(2, "Mary", 27, "Sales", "Chennai", 25000, "Female"),
			new EmployeeS(3, "Joe", 28, "IT", "Chennai", 22000, "Male"),
			new EmployeeS(4, "John", 29, "Sales", "Gurgaon", 29000, "Male"),
			new EmployeeS(5, "Liza", 25, "Sales", "Bangalore", 32000, "Female"),
			new EmployeeS(6, "Peter", 27, "Admin", "Mumbai", 31500, "Male"),
			new EmployeeS(7, "Harry", 30, "Research", "Kochi", 21000, "Male")
			);


			// Find the list of user whose name start with alphabet A 
			List<EmployeeS> list  = employeesLists.stream().filter(e->e.getName().startsWith("S")).toList();
			System.out.println(list);

			// Group the employees by department name 
			Map<String, List<EmployeeS>> grpByDep = employeesLists.stream().collect(Collectors.groupingBy(e->e.getDepartNames()));

			// Total count of employee 
			double count = employeesLists.stream().count();
			System.out.println(count);

			// find the max age of employee 
			int  ageMax = employeesLists.stream().mapToInt(e->e.getAge()).max().getAsInt();

			//  Find all  Deprtment Name 
			List<String> listOFDep = employeesLists.stream().map(e-> e.getDepartNames()).distinct().toList();
			System.out.println(listOFDep);

			// Find the count of employee by department 
			Map<String, Long> mp = employeesLists.stream().collect(Collectors.groupingBy(e->e.getDepartNames(), Collectors.counting()));

			// find the list of employees whose age is less than 30
			List<EmployeeS> listEm = employeesLists.stream().filter(e->e.getAge()<30).toList();

			// Fins the average age of male and female employee 
			Map<String, Double > avgage = employeesLists.stream().collect(Collectors.groupingBy(e->e.getGender(), Collectors.averagingLong(EmployeeS::getAge)));

			// find the deparment who have maximum no of employee 
			String dep =  employeesLists.stream().collect(Collectors.groupingBy(EmployeeS::getDepartNames, Collectors.counting())).entrySet().stream().map(Map.Entry.comparingByValue()).get();

			// find all employee who stay in delhi and sort them by their name 
			 employeesLists.stream().filter(e->e.getAddress()=="Mumbai").sorted(Comparator.comparing(EmployeeS::getName)).toList();

			 // find all department avg salary 
			 Map<String, Double> avgDep = employeesLists.stream().collect(Collectors.groupingBy(e->e.getDepartNames(), Collectors.averagingDouble(EmployeeS::getSalary)));
			 System.out.println(avgDep);

			 // find the higest salary for each department 
	           employeesLists.stream().collect(Collectors.groupingBy(e->e.getDepartNames(), Collectors.maxBy(Comparator.comparing(EmployeeS::getSalary))));

			   // sort by salary 
			   employeesLists.stream().sorted(Comparator.comparing(EmployeeS::getSalary));

			   // 






			
    }
    
}
