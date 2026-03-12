package com.gastosapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gastosapp.adapter.ExpenseAdapter;
import com.gastosapp.data.local.ExpenseEntity;
import com.gastosapp.data.local.UserEntity;
import com.gastosapp.viewmodel.ExpenseViewModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {

    private UserEntity currentUser;
    private TextView tvUserName;
    private TextView tvTotalMonth;
    private RecyclerView rvRecentExpenses;
    private View emptyView;
    private ProgressBar progressBar;
    private ExpenseAdapter adapter;
    private ExpenseViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        currentUser = (UserEntity) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            // Redirect to welcome if no user
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        initViews();
        setupRecyclerView();
        observeViewModel();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvTotalMonth = findViewById(R.id.tvTotalMonth);
        rvRecentExpenses = findViewById(R.id.rvRecentExpenses);
        emptyView = findViewById(R.id.emptyView);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnViewAll = findViewById(R.id.btnViewAll);
        Button btnAnalytics = findViewById(R.id.btnAnalytics);
        Button btnReminders = findViewById(R.id.btnReminders);

        tvUserName.setText(currentUser.getName());

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            startActivity(intent);
        });

        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpensesListActivity.class);
            startActivity(intent);
        });

        btnAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalyticsActivity.class);
            startActivity(intent);
        });

        btnReminders.setOnClickListener(v -> {
            Intent intent = new Intent(this, RemindersActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(this);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvRecentExpenses.setAdapter(adapter);
    }

    private void observeViewModel() {
        progressBar.setVisibility(View.VISIBLE);

        viewModel.getRecentExpenses().observe(this, expenses -> {
            progressBar.setVisibility(View.GONE);
            if (expenses != null && !expenses.isEmpty()) {
                adapter.setExpenses(expenses);
                emptyView.setVisibility(View.GONE);
                rvRecentExpenses.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                rvRecentExpenses.setVisibility(View.GONE);
            }
        });

        viewModel.getTotalMonthlyExpenses().observe(this, total -> {
            if (total != null) {
                tvTotalMonth.setText(formatCurrency(total));
            } else {
                tvTotalMonth.setText(formatCurrency(0.0));
            }
        });
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        return format.format(amount);
    }

    @Override
    public void onExpenseClick(ExpenseEntity expense) {
        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra("expense_id", expense.getId());
        startActivity(intent);
    }
}
