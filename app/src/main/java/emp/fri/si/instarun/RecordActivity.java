package emp.fri.si.instarun;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import emp.fri.si.instarun.model.Run;

import java.util.Date;

public class RecordActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private final int PERMISSION_REQUEST_CODE = 1337;

    private FloatingActionButton recordButton;
    private TextView stepsTextView;
    private TextView lengthTextView;
    private TextView statusLabel;
    private TextView timeTextView;

    private SupportMapFragment supportMapFragment;
    private GoogleMap map;

    private Handler timeUpdateHandler;
    private Runnable updateCurrentTime;
    private TrackRecorder.UpdateListener updateListener;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
        lengthTextView = (TextView) findViewById(R.id.lengthTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        statusLabel = (TextView) findViewById(R.id.statusLabel);

        // Toggle recording on click
        recordButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TrackRecorder.isTracking())
                    stopRecording();
                else
                    startRecording();
            }
        });

        // Create listener to track changes and updates user interface
        updateListener = new TrackRecorder.UpdateListener() {
            @Override
            public void onStepUpdate() {
                stepsTextView.setText(String.valueOf(TrackRecorder.getSteps()));
            }

            @Override
            public void onTrackUpdate() {
                float length = TrackRecorder.getLength();
                String text;
                if (length > 1000) {
                    text = String.format("%.1f km", length / 1000);
                } else {
                    text = String.format("%.0f m", length);
                }
                lengthTextView.setText(text);
                // TODO: Draw track on map
            }
        };

        // Update timer when recording
        timeUpdateHandler = new Handler();

        updateCurrentTime = new Runnable() {
            @Override
            public void run() {
                long time = new Date().getTime() - TrackRecorder.getStartTime().getTime();
                int seconds = (int) (time / 1000 % 60);
                int minutes = (int) (time / 60000 % 60);
                timeTextView.setText(String.format("%02d:%02d", minutes, seconds));
                timeUpdateHandler.postDelayed(this, 1000);
            }
        };

        // Initialize Google Map
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        // Initialize everything that needs location permission
        checkAndRequestPermission();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if ( locationManager != null && !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TrackRecorder.removeUpdateListener(updateListener);
    }

    private void onPermissionGranted() {
        supportMapFragment.getMapAsync(this);
        startService();
    }

    private void onPermissionDeclined() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void checkAndRequestPermission() {
        // Check for permission

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            // Already have permission
            onPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    onPermissionGranted();
                } else {
                    // Permission declined
                    onPermissionDeclined();
                }
            }
        }
    }

    private void resetRecording(){
        TrackRecorder.pause();
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flag, getTheme()));
        statusLabel.setText("READY");
    }

    private void startRecording() {
        TrackRecorder.start();
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flag, getTheme()));
        statusLabel.setText("RECORDING");

        timeTextView.setText("00:00");
        timeUpdateHandler.postDelayed(updateCurrentTime, 1000);
    }

    private void stopRecording() {
        TrackRecorder.pause();
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_run, getTheme()));
        statusLabel.setText("STOPPED");

        timeUpdateHandler.removeCallbacks(updateCurrentTime);

        Run run = TrackRecorder.getRun();
        run.save();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("runId", run.id);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, TrackRecorderService.class);
        startService(serviceIntent);

        TrackRecorder.addUpdateListener(updateListener);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS is disabled");
        builder.setMessage("Enable GPS to record your run.")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        try {
            map.setMyLocationEnabled(true);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 100, this);

            if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                buildAlertMessageNoGps();
            }
        } catch (SecurityException e){
            // PASS: This method shouldn't be called without permissions
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
