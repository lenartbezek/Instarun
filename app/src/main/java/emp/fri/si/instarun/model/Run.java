package emp.fri.si.instarun.model;

import android.content.Context;
import android.location.Location;
import emp.fri.si.instarun.InstarunApp;
import emp.fri.si.instarun.data.GpxHelper;
import emp.fri.si.instarun.data.RunDbHelper;
import io.ticofab.androidgpxparser.parser.domain.Gpx;

import java.util.*;

public class Run {

    public Run(){
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if (hours >= 21 || hours < 5){
            title = "Night run";
        } else if (hours <= 9){
            title = "Morning run";
        } else if (hours <= 13) {
            title = "Noon run";
        } else if (hours <= 18) {
            title = "Afternoon run";
        } else {
            title = "Evening run";
        }
    }

    /**
     * Local ID for storing in SQLite database.
     */
    public long id;

    /**
     * Is the run saved in the local database.
     * If saved, the run is updated Run.save() call, otherwise it's inserted.
     */
    public boolean isSaved = false;

    public String globalId;
    public String ownerId;

    public String title;

    public float length;
    public int steps;

    public Gpx track;

    public Date startTime;
    public Date endTime;

    public void save() {
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        db.save(this);
        GpxHelper.writeToFile("track-"+id+".gpx", title, track);
    }

    public void delete(){
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        if (db.delete(this))
            InstarunApp.getContext().deleteFile("track-"+id+".gpx");
    }

    public static Run get(long id){
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        Run run = db.read(id);
        run.track = GpxHelper.readFromFile("track-"+id+".gpx");
        return run;
    }

    public static List<Run> getAll(){
        RunDbHelper db = new RunDbHelper(InstarunApp.getContext());

        List<Run> runs = db.read();
        for (Run run: runs)
            run.track = GpxHelper.readFromFile("track-"+run.id+".gpx");
        Collections.sort(runs, new Comparator<Run>() {
            @Override
            public int compare(Run a, Run b) {
                return a.startTime.after(b.startTime)
                        ? -1
                        : 1;
            }
        });
        return runs;
    }
}
