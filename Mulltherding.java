
class Increment {

    int val = 0;

    public synchronized void increment() {
        this.val++;

    }

    public synchronized int getVal() {
        return this.val;
    }
}

class MyThread extends Thread {

    Increment myObj = null;

    public MyThread(Increment myObj) {
        this.myObj = myObj;
    }

    @Override
    // @Override
    public void run() {
        // TODO Auto-generated method stub
        for (int i = 0; i < 100; i++) {

            myObj.increment();
            try {

                Thread.sleep(2000);

            } catch (Exception e) {
            }
        }
        // super.run();
    }

}

public class Mulltherding {

    public static void main(String[] args)throws Exception {
        Increment ic = new Increment();
        MyThread mc = new MyThread(ic);
        mc.start();

    }

}
