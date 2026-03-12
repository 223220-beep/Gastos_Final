package com.gastosapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.gastosapp.data.local.ExpenseEntity;
import com.gastosapp.viewmodel.ExpenseViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpenseDetailActivity extends AppCompatActivity {

    private ExpenseEntity expense;
    private int expenseId = -1;
    private boolean isEditing = false;
    private boolean isLoading = false;
    private Calendar selectedDate;
    private ExpenseViewModel viewModel;

    // View mode views
    private LinearLayout viewModeLayout;
    private TextView tvDescription;
    private TextView tvAmount;
    private TextView tvCategory;
    private TextView tvDate;
    private Button btnEdit;
    private Button btnDelete;

    // Edit mode views
    private LinearLayout editModeLayout;
    private EditText etDescription;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private EditText etDate;
    private Button btnSave;
    private Button btnCancel;

    private static final String[] CATEGORIES = {
            "Alimentos",
            "Transporte",
            "Entretenimiento",
            "Salud",
            "Servicios",
            "Otros"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        expenseId = getIntent().getIntExtra("expense_id", -1);
        if (expenseId == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        initViews();
        setupCategorySpinner();
        setupDatePicker();
        observeViewModel();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // View mode
        viewModeLayout = findViewById(R.id.viewModeLayout);
        tvDescription = findViewById(R.id.tvDescription);
        tvAmount = findViewById(R.id.tvAmount);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        // Edit mode
        editModeLayout = findViewById(R.id.editModeLayout);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        btnEdit.setOnClickListener(v -> switchToEditMode());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnSave.setOnClickListener(v -> handleUpdate());
        btnCancel.setOnClickListener(v -> switchToViewMode());

        selectedDate = Calendar.getInstance();
    }

    private void observeViewModel() {
        viewModel.getExpenseById(expenseId).observe(this, expenseEntity -> {
            if (expenseEntity != null) {
                this.expense = expenseEntity;
                if (!isEditing) {
                    displayExpense();
                }
            } else if (!isEditing) {
                // If it was deleted
                finish();
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        etDate.setText(sdf.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        etDate.setFocusable(false);
    }

    private void displayExpense() {
        tvDescription.setText(expense.getDescription());
        tvAmount.setText(formatCurrency(expense.getAmount()));
        tvCategory.setText(expense.getCategory());
        tvDate.setText(formatDateLong(expense.getTimestamp()));
    }

    private void switchToEditMode() {
        isEditing = true;
        viewModeLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);

        // Populate edit fields
        etDescription.setText(expense.getDescription());
        etAmount.setText(String.valueOf(expense.getAmount()));

        // Set category
        int categoryIndex = Arrays.asList(CATEGORIES).indexOf(expense.getCategory());
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex);
        }

        // Set date
        selectedDate.setTimeInMillis(expense.getTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void switchToViewMode() {
        isEditing = false;
        editModeLayout.setVisibility(View.GONE);
        viewModeLayout.setVisibility(View.VISIBLE);
    }

    private void handleUpdate() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = CATEGORIES[spinnerCategory.getSelectedItemPosition()];

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setTimestamp(selectedDate.getTimeInMillis());

        viewModel.updateExpense(expense);

        Toast.makeText(this, "¡Gasto actualizado exitosamente!", Toast.LENGTH_SHORT).show();
        switchToViewMode();
        setLoading(false);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar gasto?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> handleDelete())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void handleDelete() {
        setLoading(true);
        viewModel.deleteExpense(expense);
        Toast.makeText(this, "¡Gasto eliminado exitosamente!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnSave.setEnabled(!loading);
        btnSave.setText(loading ? "Guardando..." : "Guardar");
        btnDelete.setEnabled(!loading);
        btnEdit.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        return format.format(amount);
    }

    private String formatDateLong(long timestamp) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
        return outputFormat.format(new Date(timestamp));
    }
}
