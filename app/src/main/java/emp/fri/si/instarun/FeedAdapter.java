package emp.fri.si.instarun;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import emp.fri.si.instarun.model.Run;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.RunViewHolder> {
    private List<Run> dataset;

    public static class RunViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vOwner;
        protected TextView vSteps;
        protected TextView vLength;

        public RunViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.title);
            vOwner = (TextView)  v.findViewById(R.id.owner);
            vSteps = (TextView)  v.findViewById(R.id.steps);
            vLength = (TextView) v.findViewById(R.id.length);
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
        holder.vLength.setText(r.length +"");
        holder.vSteps.setText(r.steps +"");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
