import java.util.concurrent.Callable;

class MyRunnable implements Runnable{
    // @Override 
    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.print("This is Runnale call");
        
    }
    
}
class MyCallable<T> implements Callable<T>{
    // @Override
    @Override
    public T call() throws Exception {
        // TODO Auto-generated method stub
        System.out.print("this is Generic class ");
        return null;
    }

}
public class RunnableVsCallable {
    public static void main(String []arg) throws Exception{
     MyRunnable r = new MyRunnable();
     r.run();
    //  r.
     MyCallable<String>  cal = new MyCallable<>();
     cal.call();
     cal.wait();
     cal.notify();
     cal.notifyAll();
    //  cal.
    }
    
}
