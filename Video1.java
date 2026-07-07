
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


interface  A {
    default  void show(){
        // get();
        System.out.println("A");
    }
}
interface  B {
    default  void show(){
        get();
        System.out.println("B");
    }
    private  void get(){
        System.out.println("This will contantn");
    }
}
class  AB implements A, B{
    @Override
    public void  show(){
        A.super.show();  // override  & exciplity tell them call which default method 
    }
    public void item(){
        System.out.println("ITem");
    }
}

class Encapsulation{
    private  int id ;
    private  String name ;

    public Encapsulation(int id , String name ) {
        this.id = id;
        this.name = name;
    }
    // pub
    
}
public class Video1 {
    public static void main(String[] args) {
        //Question
        // Two interfaces have the same default method. A class implements both interfaces. Which method gets called?
        // Answer
        // The compiler throws an error because of ambiguity. The implementing class must override the method and explicitly choose which interface's default implementation to use.
        AB  a = new  AB();
        a.show();
        

// Why Private Methods in Interfaces?
// Answer

// Introduced in Java 9.

// Used to:

// Avoid code duplication
// Share logic between default methods
// Share logic between static methods

// What is encapsulation 
// it is the hinding the data 

// Find Second Largest Element in Array
  List<Integer> list = Arrays.asList(2,2,2);
      int nas =  list.stream().distinct().sorted(Collections.reverseOrder()).skip(1).findFirst().get();
      System.err.println(nas);

//Feign Client vs RestTemplate  - spring boot 
// @autowired 
// private RestTemplate restTemplate;
//            // to call  restTemplate.getForObject(url, Class)


// @FeignClient(name = "product-service")
// public interface ProductClient {

//     @GetMapping("/products/{id}")
//     Product getProduct(@PathVariable Long id);
// }

    }
 

}
