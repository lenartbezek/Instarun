package emp.fri.si.instarun.model;

import android.content.Context;
import android.location.Location;
import android.provider.BaseColumns;
import emp.fri.si.instarun.InstarunApp;
import emp.fri.si.instarun.database.RunDbHelper;

import java.util.Date;
import java.util.List;

public class Run {

    public long id;

    public String globalId;
    public String ownerId;

    public String title;

    public float length;
    public int steps;

    public List<Location> track;
    public String trackFile;

    public Date startTime;
    public Date endTime;

    public void save() {
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        id = db.insert(this).id;
    }

    public void delete(){
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        db.delete(this);
    }

    public static Run get(int id){
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        return db.read(id);
    }

    public static List<Run> getAll(){
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        return db.read();
    }
}
