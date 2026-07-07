
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;


class MyClass {
    private int count =0;
    public synchronized   boolean incr(){
        this.count++;
        return  true;
    }
    public    int getValue(){
        return  this.count;
    }
}
class  MyException extends  Exception{

    public MyException(String thisISforExeptionString) {
        System.err.println("MU ");
    }
    
}
class MyThread extends Thread {
    private MyClass obj;

    public MyThread(MyClass obj) {
        this.obj = obj;
    }

    @Override
    public void run() {
        obj.incr();
        // obj.incr();
        //  obj.incr();
        //   obj.incr();
        //    obj.incr();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    
public class MultiThreading {

    // //    @Override
    //    @Override
    //    public void run() {

    //    }
        
    public static void main(String[] args) throws Exception{
        // MyClass my = new MyClass();
        // MyThread m1 = new MyThread(my);
        // MyThread m2 = new  MyThread(my);
        // throw new MyException("This is the ");
        // // m1.start();
        // m2.start();
        // m1.join();
        // m1.join();

        // System.out.println(my.getValue());
        // var name = new LinkedList<>();
        String name = "Shubham";
       System.out.println(name.chars().mapToObj(c->(char)c)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().max(Comparator.comparing(ele->ele.getValue())));
    
    }
    
}
