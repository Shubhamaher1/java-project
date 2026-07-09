
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public class Giude {

    public static void main(String[] args) throws  InterruptedException{
        // string builder vs stringbuffer 
            StringBuffer st = new StringBuffer("Shubham");
            StringBuilder st2 = new StringBuilder("Shubham");
            Thread t1 = new Thread(()-> {
                for(int i=0; i< 10 ; i++){
                    st.append(i+" ");
                    st2.append(i+" ");
                }
            });
             Thread t2 = new Thread(()-> {
                for(int i=0; i< 10 ; i++){
                    st.append(i+" ");
                    st2.append(i+" ");
                }
            });
            t1.start();
            t2.start();
            // t1.join();
            // t2.join();
            System.out.println(st);
            System.out.println(st2);
            // ConcurrentHashMap, Hashmap , SyncronsiedMap 
            ConcurrentHashMap<Integer, String> conc1 = new ConcurrentHashMap<>();
            HashMap<Integer, String> conc2 = new HashMap<>();
            Map<Integer, String> conc3 = Collections.synchronizedMap(new HashMap<>());

            Thread ts1 = new Thread(()->{
                for (int i = 0; i < 10; i++) {
                    conc1.put(i, i+"");
                     conc2.put(i, i+"");
                      conc3.put(i, i+"");
                }

            });
            Thread ts2 = new Thread(()->{
                for (int i = 0; i < 10; i++) {
                    conc1.put(i+10, i+"");
                     conc2.put(i+10, i+"");
                      conc3.put(i+10, i+"");
                }

            });
            ts1.start();
            ts2.start();
            ts2.join();
            System.out.println(conc1);
            System.out.println(conc2);
            System.out.println(conc3);


            // concurrency - multiple task done by at a time with shared resuorses 
            // problem ->  race condition , deadlock , 

        //  Solutiopn 
//         | Solution                     | Use Case                   | Example                           |
// | ---------------------------- | -------------------------- | --------------------------------- |
// | `synchronized`               | Protect critical sections  | `synchronized` method/block       |
// | `Lock` (ReentrantLock)       | Advanced locking           | `lock.lock()` / `unlock()`        |
// | Atomic Classes               | Single variable operations | `AtomicInteger`, `AtomicLong`     |
// | Concurrent Collections       | Thread-safe collections    | `ConcurrentHashMap`               |
// | `volatile`                   | Visibility of variables    | `volatile boolean flag`           |
// | ThreadLocal                  | Thread-specific data       | `ThreadLocal<User>`               |
// | Executors                    | Manage thread pools        | `ExecutorService`                 |
// | Immutable Objects            | Avoid shared mutable state | `String`, custom immutable class` |
// | Semaphore                    | Limit concurrent access    | Database connections              |
// | CountDownLatch/CyclicBarrier | Thread coordination        | Wait for multiple tasks           |
// | ReadWriteLock                | More reads than writes     | Cache implementations             |


            


    }
    
}
