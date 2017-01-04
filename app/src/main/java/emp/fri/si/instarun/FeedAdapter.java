package emp.fri.si.instarun;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import emp.fri.si.instarun.model.Run;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.RunViewHolder> {
    private List<Run> dataset;
    private static Context context;

    public static class RunViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        protected TextView vTitle;
        protected TextView vOwner;
        protected TextView vSteps;
        protected TextView vLength;

        public RunViewHolder(View v) {
            super(v);
            context = itemView.getContext();
            v.setOnClickListener(this);
            vTitle =  (TextView) v.findViewById(R.id.title);
            vOwner = (TextView)  v.findViewById(R.id.owner);
            vSteps = (TextView)  v.findViewById(R.id.steps);
            vLength = (TextView) v.findViewById(R.id.length);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            String runID = vTitle.getText().toString();
            String adapterPosition = Integer.toString(getAdapterPosition());
            Intent intent = new Intent(v.getContext(), ViewActivity.class);
            bundle.putString("runID", runID);
            bundle.putString("adapterPosition", adapterPosition);
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
    public RunViewHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
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
        holder.vTitle.setText(r.title);
        holder.vOwner.setText(r.owner.name);

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
