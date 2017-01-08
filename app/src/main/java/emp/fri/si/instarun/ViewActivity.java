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

//import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import emp.fri.si.instarun.model.Run;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPointInterface;

public class ViewActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private final int PERMISSION_REQUEST_CODE = 1337;
    private Run run;
    private TextView distance, steps, time, title;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private LocationManager locationManager;
    private int lastLocationIndex = 1;
    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        distance =  (TextView) findViewById(R.id.distanceTextView);
        steps =  (TextView) findViewById(R.id.stepsTextView);
        time =  (TextView) findViewById(R.id.timeTextView);
        title =  (TextView) findViewById(R.id.titleLabel);
        graph = (GraphView) findViewById(R.id.graph);

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


                int trackPointsSize = run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().size();

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                DataPoint[] dt = new DataPoint[trackPointsSize];

                try {
                    double y = 300.0;
                    for (int i = 0; i < trackPointsSize; i++) {
                        if(run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(i).getElevation() != null) {
                            y = run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(i).getElevation();
                        }
                        Double x = (double) run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints().get(i).getTime().getMillis();
                        DataPoint dp = new DataPoint(x,y);
                        dt[i] = dp;
                    }

                    series = new LineGraphSeries<>(dt);
                    graph.addSeries(series);

                } catch (Exception ex) {
                    title.setText(ex.toString());
                }
            }
        }
        // Initialize Google Map
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onLocationChanged(Location location) {

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

}
