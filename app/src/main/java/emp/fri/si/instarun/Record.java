package emp.fri.si.instarun;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Record extends AppCompatActivity implements SensorEventListener {
    private SensorManager sManager;
    private Sensor stepSensor;
    private boolean count = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);

        Button bt = (Button) findViewById(R.id.Start);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                count = true;
            }
        });


    }

    @Override
    protected void onResume() {

        super.onResume();

        sManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    protected void onStop() {
        super.onStop();
        sManager.unregisterListener(this, stepSensor);
    }

    private long steps = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }


        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if(count)
                steps++;
        }
        TextView tv = (TextView) findViewById(R.id.textView2);
        tv.setText(Long.toString(steps));
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        sManager.unregisterListener(this);
    }


}
