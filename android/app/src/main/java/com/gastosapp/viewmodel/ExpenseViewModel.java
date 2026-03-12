package com.gastosapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.gastosapp.data.local.ExpenseEntity;
import com.gastosapp.data.repository.ExpenseRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Calendar;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ExpenseViewModel extends ViewModel {
    private final ExpenseRepository repository;

    @Inject
    public ExpenseViewModel(ExpenseRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<ExpenseEntity>> getRecentExpenses() {
        return repository.getRecentExpenses();
    }

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return repository.getAllExpenses();
    }

    public LiveData<Double> getTotalMonthlyExpenses() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long start = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long end = calendar.getTimeInMillis();

        return repository.getTotalMonthlyExpenses(start, end);
    }

    public LiveData<ExpenseEntity> getExpenseById(int id) {
        return repository.getExpenseById(id);
    }

    public void addExpense(ExpenseEntity expense) {
        repository.insert(expense);
    }

    public void updateExpense(ExpenseEntity expense) {
        repository.update(expense);
    }

    public void deleteExpense(ExpenseEntity expense) {
        repository.delete(expense);
    }
}
