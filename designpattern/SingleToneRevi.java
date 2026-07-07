package designpattern;

public class SingleToneRevi {
    private SingleToneRevi(){
        
    }
    private  static class  Contain {
        private static final  SingleToneRevi instance = new SingleToneRevi();
      
    }
    public SingleToneRevi getObj(){
        return Contain.instance;
    }
    
}
