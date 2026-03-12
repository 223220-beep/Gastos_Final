package com.gastosapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gastosapp.adapter.ExpenseAdapter;
import com.gastosapp.data.local.ExpenseEntity;
import com.gastosapp.viewmodel.ExpenseViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpensesListActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {

    private EditText etSearch;
    private TextView tvTotal;
    private TextView tvCount;
    private RecyclerView rvExpenses;
    private View emptyView;
    private ProgressBar progressBar;
    private ExpenseAdapter adapter;
    private ExpenseViewModel viewModel;
    private List<ExpenseEntity> allExpenses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_list);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        initViews();
        setupRecyclerView();
        observeViewModel();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        tvTotal = findViewById(R.id.tvTotal);
        tvCount = findViewById(R.id.tvCount);
        rvExpenses = findViewById(R.id.rvExpenses);
        emptyView = findViewById(R.id.emptyView);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        btnBack.setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(this);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);
    }

    private void observeViewModel() {
        progressBar.setVisibility(View.VISIBLE);

        viewModel.getAllExpenses().observe(this, expenses -> {
            progressBar.setVisibility(View.GONE);
            if (expenses != null) {
                allExpenses = expenses;
                adapter.setExpenses(expenses);
                updateSummary(expenses);
                updateEmptyState(expenses.isEmpty());
            }
        });
    }

    private void filterExpenses(String query) {
        if (allExpenses == null)
            return;

        List<ExpenseEntity> filtered = new ArrayList<>();
        if (query.trim().isEmpty()) {
            filtered = allExpenses;
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (ExpenseEntity expense : allExpenses) {
                if (expense.getDescription().toLowerCase().contains(lowerQuery) ||
                        expense.getCategory().toLowerCase().contains(lowerQuery)) {
                    filtered.add(expense);
                }
            }
        }
        adapter.setExpenses(filtered);
        updateSummary(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void updateSummary(List<ExpenseEntity> expenses) {
        double total = 0;
        for (ExpenseEntity expense : expenses) {
            total += expense.getAmount();
        }
        tvTotal.setText(formatCurrency(total));

        int count = expenses.size();
        tvCount.setText(count + " " + (count == 1 ? "gasto" : "gastos"));
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyView.setVisibility(View.VISIBLE);
            rvExpenses.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            rvExpenses.setVisibility(View.VISIBLE);
        }
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
