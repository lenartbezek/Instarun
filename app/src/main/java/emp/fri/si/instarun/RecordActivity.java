package emp.fri.si.instarun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

public class RecordActivity extends AppCompatActivity {

    private final int ACCESS_FINE_LOCATION_REQUEST = 1337;

    private FloatingActionButton recordButton;
    private TextView stepsTextView;
    private TextView lengthTextView;
    private TextView statusLabel;
    private TextView timeTextView;
    private Handler timeUpdateHandler;
    private Runnable updateCurrentTime;

    private TrackRecorder.UpdateListener updateListener;

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
                if (length > 1000){
                    text = String.format("%.1f km", length/1000);
                } else {
                    text = String.format("%.0f m", length);
                }
                lengthTextView.setText(text);
                // TODO: Draw track on map
            }

            @Override
            public void onLocationUpdate() {
                // TODO: Show location on map
            }
        };

        // Update timer when recording
        timeUpdateHandler = new Handler();

        updateCurrentTime = new Runnable(){
            @Override
            public void run()
            {
                long time = new Date().getTime() - TrackRecorder.getStartTime().getTime();
                int seconds = (int) (time / 1000 % 60);
                int minutes = (int) (time / 60000 % 60);
                timeTextView.setText(String.format("%02d:%02d", minutes, seconds));
                timeUpdateHandler.postDelayed(this,1000);
            }
        };

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        TrackRecorder.removeUpdateListener(updateListener);
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST);
        } else {
            // Already have permission
            startService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    startService();
                } else {
                    // Permission declined
                    finish();
                }
            }
        }
    }

    private void startRecording(){
        TrackRecorder.start();
        statusLabel.setText("RECORDING");

        timeTextView.setText("00:00");
        timeUpdateHandler.postDelayed(updateCurrentTime,1000);
    }

    private void stopRecording(){
        TrackRecorder.pause();
        statusLabel.setText("STOPPED");

        timeUpdateHandler.removeCallbacks(updateCurrentTime);
    }

    private void startService(){
        Intent serviceIntent = new Intent(this, TrackRecorderService.class);
        startService(serviceIntent);

        TrackRecorder.addUpdateListener(updateListener);
    }
}
