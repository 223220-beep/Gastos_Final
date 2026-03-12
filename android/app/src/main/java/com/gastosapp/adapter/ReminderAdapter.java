package com.gastosapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gastosapp.R;
import com.gastosapp.data.local.ReminderEntity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<ReminderEntity> reminders = new ArrayList<>();
    private OnReminderClickListener listener;

    public interface OnReminderClickListener {
        void onReminderClick(ReminderEntity reminder);

        void onReminderToggle(ReminderEntity reminder);
    }

    public ReminderAdapter(OnReminderClickListener listener) {
        this.listener = listener;
    }

    public void setReminders(List<ReminderEntity> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderEntity reminder = reminders.get(position);
        holder.bind(reminder, listener);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbCompleted;
        private TextView tvDescription;
        private TextView tvDate;
        private TextView tvAmount;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(ReminderEntity reminder, OnReminderClickListener listener) {
            tvDescription.setText(reminder.getDescription());
            tvDate.setText(reminder.getDate() + " • " + reminder.getCategory());
            tvAmount.setText(formatCurrency(reminder.getAmount()));

            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(reminder.isCompleted());

            if (reminder.isCompleted()) {
                tvDescription.setAlpha(0.5f);
                tvAmount.setAlpha(0.5f);
            } else {
                tvDescription.setAlpha(1.0f);
                tvAmount.setAlpha(1.0f);
            }

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Notifying toggle to activity to handle DB update
                listener.onReminderToggle(reminder);
            });

            itemView.setOnClickListener(v -> listener.onReminderClick(reminder));
        }

        private String formatCurrency(double amount) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            return format.format(amount);
        }
    }
}
