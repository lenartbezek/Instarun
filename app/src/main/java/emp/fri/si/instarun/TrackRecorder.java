package emp.fri.si.instarun;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import emp.fri.si.instarun.model.Run;

import java.util.HashSet;
import java.util.LinkedList;

public class TrackRecorder implements LocationListener, SensorEventListener {

    private boolean              ready;
    private boolean              tracking;
    private LinkedList<Location> track;
    private int                  steps;
    private Location             currentLocation;

    private static TrackRecorder singleton;

    private static LocationManager locationManager;
    private static SensorManager   sensorManager;

    private static Sensor stepCounterSensor;
    private static Sensor stepDetectorSensor;

    private TrackRecorder(){
        track = new LinkedList<Location>();
    }

    public interface UpdateListener{
        public void onUpdate();
    }

    private void onUpdate(){
        for (UpdateListener listener : listeners) {
            listener.onUpdate();
        }
    }

    private static HashSet<UpdateListener> listeners = new HashSet<UpdateListener>();

    /**
     * Adds UpdateListener to be called when getting a location or step update.
     * To be used for updating the user interface.
     * @param listener
     */
    public static void addUpdateListener(UpdateListener listener){
        listeners.add(listener);
    }

    /**
     * Usubscribes UpdateListener. To be called when no longer requiring location or step updates,
     * i.e. when destroying activity.
     * @param listener
     */
    public static void removeUpdateListener(UpdateListener listener){
        listeners.remove(listener);
    }

    /**
     * Clears all UpdateListeners. To be called on service destroy.
     */
    public static void clearUpdateListeners(){ listeners.clear(); }

    @Override
    public void onLocationChanged(Location loc) {
        currentLocation = loc;

        if (tracking){
            track.add(loc);
        }

        onUpdate();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!tracking) return;

        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            steps += value;
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps += value;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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

    /**
     * Initializes singleton for given context.
     * Must be called before calling any other static functions.
     * @param context
     */
    public static void initialize(Context context){
        singleton = new TrackRecorder();

        // Initialize location manager and sensor manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Starts the sensors to pinpoint the location for accurate start.
     */
    public static void ready(){
        // Register track recorder as sensor listener
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(singleton, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(singleton, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Register track recorder as location listener
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, singleton);
        singleton.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        singleton.ready = true;
    }

    /**
     * Starts recording or resets current progress.
     * Track recorder only logs steps and location when recording.
     */
    public static void start(){
        singleton.track.clear();
        singleton.steps = 0;

        resume();
    }

    /**
     * Resumes recording.
     */
    public static void resume(){
        if (!singleton.ready) ready();
        singleton.tracking = true;
    }

    /**
     * Stops recording with intent of resuming it.
     */
    public static void pause(){
        singleton.tracking = false;
    }

    /**
     * Stops recording and un-readies it.
     */
    public static void stop(){
        if (singleton.ready){
            sensorManager.unregisterListener(singleton, stepCounterSensor);
            sensorManager.unregisterListener(singleton, stepDetectorSensor);
            locationManager.removeUpdates(singleton);
            singleton.ready = false;
        }
        pause();
    }

    public static LinkedList<Location> getTrack(){
        return singleton != null ? singleton.track : new LinkedList<Location>();
    }

    public static Location getCurrentLocation(){
        return singleton.currentLocation;
    }

    public static float getLength(){
        float length = 0;
        for (int i = 1; i < singleton.track.size(); i++){
            Location a = singleton.track.get(i-1);
            Location b = singleton.track.get(i);
            length += a.distanceTo(b);
        }
        return length;
    }

    public static int getSteps(){
         return singleton != null ? singleton.steps : 0;
    }

    /**
     * Returns a new Run object with data from the current recording.
     * Other Run metadata such as person or title are null.
     * @return emp.fri.si.instarun.model.Run
     */
    public static Run getRun(){
        Run run = new Run();
        run.steps = getSteps();
        run.length = getLength();
        run.track = getTrack();
        return run;
    }

    /**
     * Returns true if currently recording.
     * @return
     */
    public static boolean isTracking() {
        return singleton != null && singleton.tracking;
    }
}
