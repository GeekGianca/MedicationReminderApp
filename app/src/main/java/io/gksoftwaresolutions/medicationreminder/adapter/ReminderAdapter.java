package io.gksoftwaresolutions.medicationreminder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.gksoftwaresolutions.medicationreminder.R;
import io.gksoftwaresolutions.medicationreminder.model.Pill;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderViewHolder> {
    private List<Pill> pillList;
    private Context context;

    public ReminderAdapter(List<Pill> pillList, Context context) {
        this.pillList = pillList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_card_remind_layout, parent, false);
        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Pill p = pillList.get(position);
        holder.dateSelected.setText(p.getDate());
        holder.pillType.setText(p.getName());
        holder.timeSelected.setText(p.getTime());
        holder.statePillRemind.setText(p.getState());
        if (p.getState().equals("Pendiente")){
            holder.bgState.setBackgroundResource(R.drawable.shape_waiting_state);
        }else if (p.getState().equals("Perdida")){
            holder.bgState.setBackgroundResource(R.drawable.shape_lost_state);
        }else if (p.getState().equals("Finalizada")){
            holder.bgState.setBackgroundResource(R.drawable.shape_state_layout);
        }
    }

    @Override
    public int getItemCount() {
        return pillList.size();
    }
}

class ReminderViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.pillType)
    TextView pillType;
    @BindView(R.id.dateSelected)
    TextView dateSelected;
    @BindView(R.id.timeSelected)
    TextView timeSelected;
    @BindView(R.id.statePillRemind)
    TextView statePillRemind;
    @BindView(R.id.bgState)
    ConstraintLayout bgState;
    ReminderViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}