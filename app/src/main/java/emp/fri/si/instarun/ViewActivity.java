package emp.fri.si.instarun;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewActivity extends AppCompatActivity {

    String myString = "nothing happened";
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
                myString = "Izbral si vrstico: " + bundle.getString("adapterPosition") +
                            " in aktivnost: " + bundle.getString("runID");
            }
        }
        tv.setText(myString);
    }

}
