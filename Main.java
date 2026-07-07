import designpattern.SingleTone;

class MyThread extends  Thread{

    @Override
    public void run() {
        SingleTone s = SingleTone.getInstance();
        System.out.println(s.hashCode());
        try {
            
            Thread.sleep(2000);
        } catch (Exception e) {
        }
    }
    
}

class Main{
    public static void main(String arg[]) throws Exception{
        // SingleTone s = SingleTone.getInstance();
        // System.out.println(s.hashCode());
        // SingleTone s2 = SingleTone.getInstance();
        // System.out.println(s2.hashCode());
        // System.out.println("Same instance: " + (s == s2));
        // May break by thread 
        // Thread t1 = new Thread();
        MyThread m1 = new  MyThread();
        MyThread m2 = new  MyThread();
         m1.start();
         m2.start();
        
        
        System.out.println("This is Response ");
    }
}