package emp.fri.si.instarun;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

//import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.maps.android.SphericalUtil;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import emp.fri.si.instarun.model.Run;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Run run;
    private List<TrackPoint> points;

    private TextView titleTextView, dateTextView, stepsTextView, lengthTextView, timeTextView, speedTextView, inclineTextView;
    private GraphView graph;
    private GoogleMap map;
    private TabHost tabHost;
    private SupportMapFragment mapFragment;

    private boolean mapInitialized;
    private boolean graphInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);

        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("Info");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Info");
        tabHost.addTab(spec);

        //Tab 2
        spec = tabHost.newTabSpec("Map");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Map");
        tabHost.addTab(spec);

        //Tab 3
        spec = tabHost.newTabSpec("Graph");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Graph");
        tabHost.addTab(spec);

        // Do heavy data initializations lazily on tab switch
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String arg0) {
                if (tabHost.getCurrentTab() == 1 && !mapInitialized){
                    mapFragment.getMapAsync(ViewActivity.this);
                    mapInitialized = true;
                } else if (tabHost.getCurrentTab() == 2 && !graphInitialized){
                    drawGraph();
                    graphInitialized = true;
                }
            }
        });

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        lengthTextView = (TextView) findViewById(R.id.lengthTextView);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        inclineTextView = (TextView) findViewById(R.id.inclineTextView);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        graph = (GraphView) findViewById(R.id.graph);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                long id = bundle.getLong("runId");

                run = Run.get(id);
                points = run.track.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();

                // Title
                titleTextView.setText(run.title);

                // Date
                LocalTime date = new DateTime(run.startTime).toLocalTime();
                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", getResources().getConfiguration().locale);
                dateTextView.setText(df.format(date.toDateTimeToday().toDate()));

                // Length
                String text = run.length > 1000
                    ? String.format("%.1f km", run.length / 1000)
                    : String.format("%.0f m", run.length);
                lengthTextView.setText(text);

                // Steps
                stepsTextView.setText(Integer.toString(run.steps));

                // Time
                long t = run.endTime.getTime() - run.startTime.getTime();
                int seconds = (int) (t / 1000 % 60);
                int minutes = (int) (t / 60000 % 60);
                timeTextView.setText(String.format("%02d:%02d", minutes, seconds));

                // Incline
                double startElevation = points.get(0).getElevation();
                double finishElevation = points.get(points.size() - 1).getElevation();
                double incline = (finishElevation - startElevation) / run.length * 100;
                inclineTextView.setText(String.format("%.1f %%", incline));

                // Speed
                double speed = run.length / (t / 1000) * 3.6;
                speedTextView.setText(String.format("%.1f km/h", speed));

                // Calories

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Set camera to the middle of the trail
        Location start = new Location("");
        start.setLatitude(points.get(0).getLatitude());
        start.setLongitude(points.get(0).getLongitude());

        Location finish = new Location("");
        finish.setLatitude(points.get(points.size() - 1).getLatitude());
        finish.setLongitude(points.get(points.size() - 1).getLongitude());

        LatLng middle = new LatLng(
                (start.getLatitude() + finish.getLatitude())/2,
                (start.getLongitude() + finish.getLongitude())/2
        );

        double radius = start.distanceTo(finish) / 2;

        LatLng southwest = SphericalUtil.computeOffset(middle, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(middle, radius * Math.sqrt(2.0), 45);
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        map.moveCamera(cameraUpdate);

        // Draw trail
        for (int i = 1; i < points.size(); i++) {
            TrackPoint a = points.get(i - 1);
            TrackPoint b = points.get(i);
            map.addPolyline(new PolylineOptions()
                    .add(new LatLng(a.getLatitude(), a.getLongitude()), new LatLng(b.getLatitude(), b.getLongitude()))
                    .width(5)
                    .color(Color.BLUE));
        }

        // Draw start and finish markers
        map.addMarker(new MarkerOptions()
            .position(new LatLng(start.getLatitude(), start.getLongitude()))
            .title("Start")
            .alpha(0.5f));
        map.addMarker(new MarkerOptions()
            .position(new LatLng(finish.getLatitude(), finish.getLongitude()))
            .title("Finish")
            .alpha(0.5f));
    }

    private void drawGraph() {
        if (points.size() < 2) return;

        DataPoint[] speedDt = new DataPoint[points.size()];
        DataPoint[] elevDt = new DataPoint[points.size()];

        // First point and initial values
        double lastSpeed = run.length / ((run.endTime.getTime() - run.startTime.getTime()) / 1000);
        double totalDistance = 0;
        double minSpeed = -1;
        double maxSpeed = 9999;
        double minElevation = points.get(0).getElevation();
        double maxElevation = points.get(0).getElevation();
        speedDt[0] = new DataPoint(0, 0);
        elevDt[0] = new DataPoint(0, points.get(0).getElevation());

        // Iterate through points
        for (int i = 1; i < points.size(); i++) {
            // Current segment
            TrackPoint a = points.get(i - 1);
            TrackPoint b = points.get(i);

            // Delta time in seconds
            double deltaTime = (b.getTime().getMillis() - a.getTime().getMillis()) / 1000f;

            // Add segment distance to total distance
            double distance = getDistance(a, b);
            totalDistance += distance;

            // Smooth speed
            double speed = (lastSpeed + (distance / deltaTime)) / 2f;
            lastSpeed = speed;

            // Elevation
            double elevation = b.getElevation();

            // Save values
            speedDt[i] = new DataPoint(totalDistance, speed * 3.6);
            elevDt[i] = new DataPoint(totalDistance, elevation);

            // Move limits
            minSpeed = Math.min(minSpeed, speed);
            maxSpeed = Math.max(maxSpeed, speed);
            minElevation = Math.min(minElevation, elevation);
            maxElevation = Math.max(maxElevation, elevation);
        }

        // First scale: speed
        LineGraphSeries<DataPoint> speedSeries = new LineGraphSeries<>(speedDt);
        speedSeries.setColor(Color.RED);
        speedSeries.setThickness(6);

        graph.addSeries(speedSeries);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.RED);
        graph.getGridLabelRenderer().setTextSize(30);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // X values are distance
                    String label = value > 1000
                            ? String.format("%.1f km", value / 1000f)
                            : String.format("%.0f m", value);
                    return label;
                } else {
                    // Y values are speed in km/h
                    String label = String.format("%.1f km/h", value);
                    return label;
                }
            }
        });

        // Second scale: elevation
        LineGraphSeries<DataPoint> elevSeries = new LineGraphSeries<>(elevDt);
        elevSeries.setTitle("Elevation");
        elevSeries.setColor(Color.BLUE);
        elevSeries.setThickness(6);

        double padding = (maxElevation - minElevation) / 10f;
        graph.getSecondScale().setMinY(minElevation - padding);
        graph.getSecondScale().setMaxY(maxElevation + padding);
        graph.getSecondScale().addSeries(elevSeries);
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.BLUE);
        graph.getSecondScale().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // X values are distance
                    String label = value > 1000
                            ? String.format("%.1f km", value / 1000f)
                            : String.format("%.0f m", value);
                    return label;
                } else {
                    // Y values are elevation in m
                    String label = String.format("%.0f m", value);
                    return label;
                }
            }
        });
    }

    private double getDistance(TrackPoint from, TrackPoint to){
        Location a = new Location("");
        a.setLatitude(from.getLatitude());
        a.setLongitude(from.getLongitude());

        Location b = new Location("");
        b.setLatitude(to.getLatitude());
        b.setLongitude(to.getLongitude());

        return a.distanceTo(b);
    }
}
