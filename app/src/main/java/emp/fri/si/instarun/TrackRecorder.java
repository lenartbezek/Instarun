package emp.fri.si.instarun;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.LinkedList;

public class TrackRecorder implements LocationListener {

    private boolean tracking;
    private LinkedList<Location> track;

    private static TrackRecorder instance;

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

        instance = new TrackRecorder();
        locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER, 5000, 10, instance);
    }

    public static void Start(){
        instance.track = new LinkedList<Location>();
        instance.tracking = true;
    }

    public static void Stop(){
        instance.tracking = false;
    }

    public static LinkedList<Location> GetTrack(){
        return instance.track;
    }

    public static float GetLength(){
        float length = 0;
        for (int i = 1; i < instance.track.size(); i++){
            Location a = instance.track.get(i-1);
            Location b = instance.track.get(i);
            length += a.distanceTo(b);
        }
        return length;
    }

    public static boolean IsTracking(){
        return instance.tracking;
    }
}
