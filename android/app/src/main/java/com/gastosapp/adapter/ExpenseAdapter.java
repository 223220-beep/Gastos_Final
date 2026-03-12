package com.gastosapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gastosapp.R;
import com.gastosapp.data.local.ExpenseEntity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<ExpenseEntity> expenses;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseEntity expense);
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.expenses = new ArrayList<>();
        this.listener = listener;
    }

    public void setExpenses(List<ExpenseEntity> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDescription;
        private TextView tvCategoryDate;
        private TextView tvAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategoryDate = itemView.findViewById(R.id.tvCategoryDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onExpenseClick(expenses.get(position));
                }
            });
        }

        public void bind(ExpenseEntity expense) {
            tvDescription.setText(expense.getDescription());

            String categoryDate = expense.getCategory() + " • " + formatDate(expense.getTimestamp());
            tvCategoryDate.setText(categoryDate);

            tvAmount.setText(formatCurrency(expense.getAmount()));
        }

        private String formatCurrency(double amount) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            return format.format(amount);
        }

        private String formatDate(long timestamp) {
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM", new Locale("es", "MX"));
            return outputFormat.format(new Date(timestamp));
        }
    }
}
