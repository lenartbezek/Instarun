package emp.fri.si.instarun;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Date;
import java.util.LinkedList;

public class RunRecorder implements LocationListener {

    private boolean tracking;
    private LinkedList<Location> track;

    private static RunRecorder instance;

    @Override
    public void onLocationChanged(Location loc) {
        if (tracking){
            track.add(loc);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    public static void Initialize(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        instance = new RunRecorder();
        locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER, 1000, 5, instance);
    }

    public void StartRun(){
        track = new LinkedList<Location>();
        tracking = true;
    }

    public void StopRun(){
        tracking = false;
    }

    public LinkedList<Location> GetTrack(){
        return track;
    }

    public boolean IsTracking(){
        return tracking;
    }
}
