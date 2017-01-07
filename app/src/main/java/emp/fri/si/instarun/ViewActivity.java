package emp.fri.si.instarun;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import emp.fri.si.instarun.model.Run;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class ViewActivity extends AppCompatActivity {

    private Run run;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        tv =  (TextView) findViewById(R.id.testView);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                long id = bundle.getLong("runId");
                run = Run.get(id);
                tv.setText(run.title);
            }
        }
    }

}
