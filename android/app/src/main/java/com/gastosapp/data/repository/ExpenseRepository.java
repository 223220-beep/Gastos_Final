package com.gastosapp.data.repository;

import androidx.lifecycle.LiveData;
import com.gastosapp.data.local.ExpenseDao;
import com.gastosapp.data.local.ExpenseEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public ExpenseRepository(ExpenseDao expenseDao) {
        this.expenseDao = expenseDao;
    }

    public void insert(ExpenseEntity expense) {
        executorService.execute(() -> expenseDao.insert(expense));
    }

    public void update(ExpenseEntity expense) {
        executorService.execute(() -> expenseDao.update(expense));
    }

    public void delete(ExpenseEntity expense) {
        executorService.execute(() -> expenseDao.delete(expense));
    }

    public LiveData<ExpenseEntity> getExpenseById(int id) {
        return expenseDao.getExpenseById(id);
    }

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    public LiveData<List<ExpenseEntity>> getRecentExpenses() {
        return expenseDao.getRecentExpenses();
    }

    public LiveData<Double> getTotalMonthlyExpenses(long start, long end) {
        return expenseDao.getTotalMonthlyExpenses(start, end);
    }
}
