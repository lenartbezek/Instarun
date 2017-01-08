package emp.fri.si.instarun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import emp.fri.si.instarun.model.Run;

import java.io.*;
import java.util.Date;

public class ViewActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private Run run;
    private TextView tv, distance, steps, time;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        tv =  (TextView) findViewById(R.id.testView);
        distance =  (TextView) findViewById(R.id.distanceTextView);
        steps =  (TextView) findViewById(R.id.stepsTextView);
        time =  (TextView) findViewById(R.id.timeTextView);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                long id = bundle.getLong("runId");
                run = Run.get(id);

                float length = run.length;
                String text;
                if (length > 1000) {
                    text = String.format("%.1f km", length / 1000);
                } else {
                    text = String.format("%.0f m", length);
                }
                distance.setText(text);

                steps.setText(Integer.toString(run.steps));

                long t = run.endTime.getTime() - run.startTime.getTime();
                int seconds = (int) (t / 1000 % 60);
                int minutes = (int) (t / 60000 % 60);
                time.setText(String.format("%02d:%02d", minutes, seconds));


                try{
                    FileInputStream fis = openFileInput("track-"+id+".gpx");
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    isr.close();
                    fis.close();
                    tv.setText(sb.toString());
                } catch (FileNotFoundException e) {
                    tv.setText(e.toString());
                } catch (IOException e) {
                    tv.setText(e.toString());
                }
                tv.setText(tv.getText() + "\n" +run.track);
            }
        }
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
}
