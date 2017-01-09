package emp.fri.si.instarun;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import emp.fri.si.instarun.model.Person;
import emp.fri.si.instarun.model.Run;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.RunViewHolder> {
    private List<Run> dataset;
    private static Context context;

    public static class RunViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        protected long runId;
        protected TextView vTitle;
        protected TextView vTime;
        protected TextView vDate;
        protected TextView vSteps;
        protected TextView vLength;

        public RunViewHolder(View v) {
            super(v);
            context = itemView.getContext();
            v.setOnClickListener(this);
            vTitle = (TextView) v.findViewById(R.id.title);
            vTime = (TextView) v.findViewById(R.id.time);
            vDate = (TextView)  v.findViewById(R.id.date);
            vSteps = (TextView)  v.findViewById(R.id.steps);
            vLength = (TextView) v.findViewById(R.id.length);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            Intent intent = new Intent(v.getContext(), ViewActivity.class);
            bundle.putLong("runId", runId);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedAdapter(List<Run> d) {
        dataset = d;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RunViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        v.setPadding(8, 8, 8, 8);

        return new RunViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RunViewHolder holder, int i) {
        Run r = dataset.get(i);
        holder.runId = r.id;
        holder.vTitle.setText(r.title);

        long time = r.endTime.getTime() - r.startTime.getTime();
        int seconds = (int) (time / 1000 % 60);
        int minutes = (int) (time / 60000 % 60);
        holder.vTime.setText(String.format("%02d:%02d", minutes, seconds));

        DateTimeFormatter df = DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm");
        holder.vDate.setText(df.print(r.startTime.getTime()));

        String lengthText;
        if (r.length > 1000){
            lengthText = String.format("%.1f km", r.length/1000);
        } else {
            lengthText = String.format("%.0f m", r.length);
        }
        holder.vLength.setText(lengthText);
        holder.vSteps.setText(String.valueOf(r.steps));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
