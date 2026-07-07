package designpattern;

public class SingleTone {
    // Uses 1- logger , db connection , config manager , cache 
    
    
    private SingleTone() {}
    private  static  class  Holder{
        private static  final SingleTone  INSTANCE  = new SingleTone();
    }
    

    public static SingleTone getInstance() {
       return  Holder.INSTANCE;
    }
}
