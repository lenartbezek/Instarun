package emp.fri.si.instarun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import emp.fri.si.instarun.data.GpxHelper;
import emp.fri.si.instarun.model.Run;
import io.ticofab.androidgpxparser.parser.domain.Gpx;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

public class TrackRecorderService extends Service implements LocationListener, SensorEventListener {

    private static TrackRecorderService singleton;

    private static LocationManager  locationManager;
    private static SensorManager    sensorManager;
    private static HashSet<UpdateListener> listeners = new HashSet<>();

    private boolean ready;
    private boolean tracking;
    private LinkedList<Location> track;
    private int steps;
    private Location currentLocation;
    private Date startTime;
    private Date endTime;

    private static void handleStepUpdate() {
        for (UpdateListener listener : listeners)
            listener.onStepUpdate();
    }

    private static void handleTrackUpdate() {
        for (UpdateListener listener : listeners)
            listener.onTrackUpdate();
    }

    /**
     * Adds UpdateListener to be called when getting a location or step update.
     * To be used for updating the user interface.
     *
     * @param listener
     */
    public static void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Usubscribes UpdateListener. To be called when no longer requiring location or step updates,
     * i.e. when destroying activity.
     *
     * @param listener
     */
    public static void removeUpdateListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears all UpdateListeners. To be called on service destroy.
     */
    public static void clearUpdateListeners() {
        listeners.clear();
    }

    /**
     * Starts the sensors to pinpoint the location for accurate start.
     */
    public static void ready() {
        // Register track recorder as sensor listener
        Sensor stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(singleton, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Register track recorder as location listener
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, singleton);
        singleton.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        singleton.ready = true;
    }

    /**
     * Starts recording or resets current progress.
     * Track recorder logs steps and location only when recording.
     */
    public static void start() {
        singleton.track.clear();
        singleton.steps = 0;
        singleton.startTime = new Date();
        handleStepUpdate();
        handleTrackUpdate();

        resume();
        RecordingNotification.notify(singleton.getApplicationContext(), 0, 0);
    }

    /**
     * Resumes recording.
     */
    public static void resume() {
        if (!singleton.ready) ready();
        singleton.tracking = true;
    }

    /**
     * Stops recording with intent of resuming it.
     */
    public static void pause() {
        singleton.endTime = new Date();
        singleton.tracking = false;
    }

    /**
     * Stops recording and un-readies it.
     */
    public static void stop() {
        if (singleton.ready) {
            sensorManager.unregisterListener(singleton);
            locationManager.removeUpdates(singleton);

            singleton.ready = false;
            singleton.tracking = false;
            singleton.endTime = new Date();
            RecordingNotification.cancel(singleton.getApplicationContext());

            singleton.stopSelf();
        }
    }

    /**
     * Returns a list of Locations. Used for realtime calculations while recording, not for storage.
     *
     * @return
     */
    public static LinkedList<Location> getTrack() {
        return singleton != null ? singleton.track : new LinkedList<Location>();
    }

    /**
     * Returns a Gpx track. Used for writing to a file.
     *
     * @return
     */
    public static Gpx getGpx() {
        return GpxHelper.buildGpx(singleton.track);
    }

    public static Location getCurrentLocation() {
        return singleton.currentLocation;
    }

    public static float getLength() {
        if (singleton == null) return 0;

        float length = 0;
        for (int i = 1; i < singleton.track.size(); i++) {
            Location a = singleton.track.get(i - 1);
            Location b = singleton.track.get(i);
            length += a.distanceTo(b);
        }
        return length;
    }

    public static Date getStartTime() {
        return singleton != null ? singleton.startTime : new Date();
    }

    public static Date getEndTime() {
        return singleton != null ? singleton.endTime : new Date();
    }

    public static int getSteps() {
        return singleton != null ? singleton.steps : 0;
    }

    /**
     * Returns a new Run object with data from the current recording.
     * Other Run metadata such as person or title are null.
     *
     * @return emp.fri.si.instarun.model.Run
     */
    public static Run getRun() {
        Run run = new Run();
        run.steps = getSteps();
        run.length = getLength();
        run.track = getGpx();
        run.startTime = getStartTime();
        run.endTime = getEndTime();
        return run;
    }

    /**
     * Returns true if currently recording.
     *
     * @return
     */
    public static boolean isTracking() {
        return singleton != null ? singleton.tracking : false;
    }

    @Override
    public void onLocationChanged(Location loc) {
        currentLocation = loc;

        if (tracking) {
            track.add(loc);
            handleTrackUpdate();
            RecordingNotification.notify(singleton.getApplicationContext(), getSteps(), getLength());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!tracking) return;

        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
            RecordingNotification.notify(singleton.getApplicationContext(), getSteps(), getLength());
            handleStepUpdate();
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        singleton = this;
        track = new LinkedList<>();

        // Initialize location manager and sensor manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        ready();
    }

    @Override
    public void onDestroy() {
        stop();
        clearUpdateListeners();
    }

    public interface UpdateListener {
        void onStepUpdate();

        void onTrackUpdate();
    }
}
