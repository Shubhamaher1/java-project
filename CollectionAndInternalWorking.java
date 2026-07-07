import java.util.HashSet;
import java.util.TreeSet;

class TreeSetComprator implements Comparable<TreeSetComprator>{
    int id;
     String name ;
     TreeSetComprator(int id , String name ){
        this.id = id;
        this.name = name;
     }
     public int compareTo( TreeSetComprator id ){
        return id.id- this.id;
     }
     public TreeSetComprator get(){
        return this;
     }
}
public class CollectionAndInternalWorking {
    // Hashset , Hashmap - both are same 
    // Linked hashmap , LinkedHashset - both same but it's have linked 
    // TreeSet
   public static void main(String []arg){
     TreeSet<TreeSetComprator> t = new TreeSet<>();

    t.add( new TreeSetComprator(23,"Shubha"));
    t.add( new TreeSetComprator(1,"Shubha"));

    for( TreeSetComprator te: t){
        System.out.println(te.id);
    }
    
   }
}
