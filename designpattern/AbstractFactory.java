package designpattern;
interface  Vehical {
    void start();
}
class  Car implements  Vehical{
    public void start(){
        System.err.println("This is Car Stazte");
    }
}

interface createCall{
    Vehical CreateClass(String type);
}
class factoryClass implements createCall{
    public Vehical CreateClass(String type){
        if(type.equalsIgnoreCase("Car"))return new Car();
        return  null;
    }
}

public class AbstractFactory {
    // this is one lear to the top of the factory call it make implement on the interafce
  public static void main(String[] args) {
       Vehical car  = new  factoryClass().CreateClass("Car");
       car.start();
  }
   
  
}
