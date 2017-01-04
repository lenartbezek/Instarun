package emp.fri.si.instarun.model;

/**
 * Created by Lenart on 19-Dec-16.
 */
public class Person {

    public int id;

    public String globalId;
    public String name;


    public static Person get(String globalId){
        Person p = new Person();
        p.name = "Asfalt Makedamovič";
        return p;
    }
}
