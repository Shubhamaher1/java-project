
class CoreJava {
    enum Days{
        Sunday
    }
    static class MyThread extends Thread{
            MyThread(Object obj){
                
            }
        }
        static class MyClass{
           int count =0;
           public void incr(){
            count++;
           }
           public int getInt(){
            return  this.count;
           }
        }
    public static void main(String[] arg) {

        //1 What is java 
        // Java is hight level programming laba and platfrom indepand  
        // 2 main feature in java 
        //Difference betweem ArrayList vs Vector -> Vector is Thread safe used Scronised 
        // Thread Full - Group of Excution int once thread  - it create multiple task once - it worked in Queue 
        // submit -> queue -> excute -> terminat ->shutdown
        //     ExecutorService service = Executors.newFixedThreadPool(4);
        //    for(int i=0; i<10; i++){
        //      service.submit(()->{System.out.println("None");
        //     }
        //     );
        //    }
        //     service.shutdown();
        Thread t1 = new Thread(() -> System.out.println("Thread 1"));
        Thread t2 = new Thread(() -> System.out.println("Thread 2"));

        t1.start();
        t2.start();

        // t1.join();  // Main thread waits for t1 to finish
        // t2.join();  // Main thread waits for t2 to finish
        System.out.println("All threads have finished");
        System.err.println(Days.Sunday);

        // Question hard 
        // Make class Thread Safe 

       
    
        MyClass c = new MyClass();
        MyThread trt1 = new MyThread(c);

    }
}
