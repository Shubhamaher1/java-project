package designpattern;
interface  Vehical{
    void start();
}
class  Car implements  Vehical{
    public  void start(){
        System.out.println("Car is started ");
    }
}
class  Bike implements  Vehical{
    public  void  start(){
        System.err.println("Bike is started");
    }
}

public class Factory {
    // Object creation logic centralized. - without factory we have to use if else condition 
    // step 1 -  create interface 
    // step2 - create class implement inteface 
    // step3 - make factory method 

    public static  Vehical getClass(String type){
        if(type.equalsIgnoreCase("Car")){
            return  new Car();
        }else if(type.equalsIgnoreCase("bike")){
            return  new Bike();
        }else{
            return  null;
        }
    }
   public static void main(String[] args) {
         Vehical car = getClass("Car");
    car.start();
   }


}
