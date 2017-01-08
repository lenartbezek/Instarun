package emp.fri.si.instarun;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import emp.fri.si.instarun.data.RunDbHelper;
import emp.fri.si.instarun.model.Person;
import emp.fri.si.instarun.model.Run;

import java.util.*;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private RecyclerView.LayoutManager layoutManager;
    private FeedAdapter adapter;

    private List<Run> dataset = new LinkedList<>();

    public static final int REQUEST_RECORD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display welcome screen on first time
        RunDbHelper db = new RunDbHelper(this);
        boolean firstTime = db.count() == 0;

        if (firstTime){
            setContentView(R.layout.activity_welcome);
        } else {
            setContentView(R.layout.activity_feed);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecordActivity();
            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);

        // No need to do anything else but bind FAB on first time
        if (firstTime) return;

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Changes in content do not change the layout size of the view
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Specify an adapter
        dataset = Run.getAll();
        adapter = new FeedAdapter(dataset);
        recyclerView.setAdapter(adapter);

        // Forward to ViewActivity on intent with runId (from just finished RecordActivity)
        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("runId")){
            Intent intent = new Intent(this, ViewActivity.class);
            long runId = receivedIntent.getLongExtra("runId", -1);
            intent.putExtra("runId", runId);
            startActivity(intent);
        }
    }

    private void startRecordActivity(){
        Intent intent = new Intent(this, RecordActivity.class);
        startActivityForResult(intent, REQUEST_RECORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_RECORD) {
            if(resultCode == Activity.RESULT_OK){
                long runId = data.getLongExtra("runId", -1);
                if (runId > 0){
                    // Update dataset
                    dataset.add(Run.get(runId));
                    adapter.notifyDataSetChanged();

                    // Forward to ViewActivity
                    Intent intent = new Intent(this, ViewActivity.class);
                    intent.putExtra("runId", runId);
                    startActivity(intent);
                }
            }
        }
    }
}
