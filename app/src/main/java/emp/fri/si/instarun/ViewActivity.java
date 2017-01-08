package emp.fri.si.instarun;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import emp.fri.si.instarun.model.Run;

import java.io.*;

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

}
