package emp.fri.si.instarun.model;

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
