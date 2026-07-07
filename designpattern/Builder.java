package designpattern;

class Student {

    private  int id;
    private  String name;
    private  String dep;
    private  int age;
   private   int catM;
    private  String div;

    private Student(StudentBuilder std) {
        this.id = std.id;
        this.name = std.name;

    }

    static class StudentBuilder {

        private  int id;
        private  String name;
        private  String dep;
        private  int age;
        private  int catM;
        private  String div;

        public StudentBuilder setName(String name) {
            this.name = name;
            return this;
        }
        public Student build(){
            return  new Student(this);
        }
    }
    public String toString(){
        return  this.name;
    }

}

public class Builder {
    //  create object step by step eg student data
     public static void main(String[] args) {
          Student s = new Student.StudentBuilder().setName("Shu").build();
          System.err.println(s);
     }
      
}
