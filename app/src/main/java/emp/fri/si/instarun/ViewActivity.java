package emp.fri.si.instarun;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import emp.fri.si.instarun.model.Run;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

import java.io.*;
import java.util.Date;

public class ViewActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private final int PERMISSION_REQUEST_CODE = 1337;
    private Run run;
    private TextView distance, steps, time, title, distanceLabel;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private LocationManager locationManager;
    private int lastLocationIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        distance =  (TextView) findViewById(R.id.distanceTextView);
        steps =  (TextView) findViewById(R.id.stepsTextView);
        time =  (TextView) findViewById(R.id.timeTextView);
        title =  (TextView) findViewById(R.id.titleLabel);
        distanceLabel =  (TextView) findViewById(R.id.distanceLabel);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                long id = bundle.getLong("runId");
                run = Run.get(id);

                // set distanceTextView
                float length = run.length;
                String text;
                if (length > 1000) {
                    text = String.format("%.1f km", length / 1000);
                } else {
                    text = String.format("%.0f m", length);
                }
                distance.setText(text);

                // set titleLabel, stepsTextView
                title.setText(run.title);
                steps.setText(Integer.toString(run.steps));

                // set timeTextView
                long t = run.endTime.getTime() - run.startTime.getTime();
                int seconds = (int) (t / 1000 % 60);
                int minutes = (int) (t / 60000 % 60);
                time.setText(String.format("%02d:%02d", minutes, seconds));


            }
        }
        // Initialize Google Map
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        checkAndRequestPermission();

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

        // Set camera to start of trail
        try {
            LatLng latLng = new LatLng(
                    run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(0).getLatitude(),
                    run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(0).getLongitude()
            );
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.animateCamera(cameraUpdate);
        } catch (Exception ex) {

        }

        // Draw trail
        try {
            for (int i = lastLocationIndex; i < run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().size(); i++) {
                TrackPoint a = run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(i - 1);
                TrackPoint b = run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(i);
                map.addPolyline(new PolylineOptions()
                        .add(new LatLng(a.getLatitude(), a.getLongitude()), new LatLng(b.getLatitude(), b.getLongitude()))
                        .width(5)
                        .color(Color.BLUE));
            }
        } catch (Exception ex) {

        }
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

    private void onPermissionGranted() {
        supportMapFragment.getMapAsync(this);
    }

}
