class emptyCustomException extends Exception{
    emptyCustomException(String error){
        System.out.print("getClass()");
        // super(error);
    }
}
class MyCustomException{
    public static void main(String arg[]) throws emptyCustomException{
        throw new emptyCustomException("this ");
    }
}