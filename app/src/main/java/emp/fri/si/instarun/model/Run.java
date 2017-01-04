package emp.fri.si.instarun.model;

import android.location.Location;

import java.util.Date;
import java.util.List;

public class Run {

    public Person owner;

    public String title;

    public float length;
    public long steps;

    public List<Location> track;

    public Date startTime;
    public Date endTime;

}
