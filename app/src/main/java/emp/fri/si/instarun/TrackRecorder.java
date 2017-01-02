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
import android.util.Log;
import emp.fri.si.instarun.model.Run;

import java.util.HashSet;
import java.util.LinkedList;

public class TrackRecorder implements LocationListener, SensorEventListener {

    private final static String TAG = "StepDetector";
    private float   mLimit = 10;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;

    private boolean              tracking;
    private LinkedList<Location> track;
    private int                  steps;

    private static TrackRecorder singleton;

    private TrackRecorder(){
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
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

    private static HashSet<UpdateListener> listeners;

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

    @Override
    public void onLocationChanged(Location loc) {
        if (!tracking) return;

        track.add(loc);
        onUpdate();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!tracking) return;

        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                // PASS
            } else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    for (int i=0 ; i<3 ; i++) {
                        final float v = mYOffset + event.values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;

                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == - mLastDirections[k]) {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                        if (diff > mLimit) {

                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                Log.i(TAG, "step");
                                steps++;
                                onUpdate();
                                mLastMatch = extType;
                            }
                            else {
                                mLastMatch = -1;
                            }
                        }
                        mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                }
            }
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
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // Register track recorder as sensor listener
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(singleton, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Register track recorder as location listener
        locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER, 5000, 10, singleton);
    }

    /**
     * Starts recording or resets current progress.
     * Track recorder only logs steps and location when recording.
     */
    public static void start(){
        singleton.track.clear();
        singleton.steps = 0;
        singleton.tracking = true;
    }

    /**
     * Resumes recording.
     */
    public static void resume(){
        singleton.tracking = true;
    }

    /**
     * Stops recording. Can be resumed.
     */
    public static void stop(){
        singleton.tracking = false;
    }

    public static LinkedList<Location> getTrack(){
        return singleton.track;
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
        return singleton.steps;
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
    public static boolean isTracking(){
        return singleton.tracking;
    }
}
