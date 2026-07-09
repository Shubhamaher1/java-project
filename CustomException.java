class  MyException extends  Exception{

    public MyException(String myName) {
        super(myName);
    }
    
}


class CustomException{
    public static void main(String[] args) {
     try {
        throw  new MyException("This ");
         
     } catch (Exception e) {
        System.err.println(e);
     }
    }
}


