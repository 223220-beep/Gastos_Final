package com.gastosapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(ExpenseEntity expense);

    @androidx.room.Update
    void update(ExpenseEntity expense);

    @androidx.room.Delete
    void delete(ExpenseEntity expense);

    @Query("SELECT * FROM expenses WHERE id = :id")
    LiveData<ExpenseEntity> getExpenseById(int id);

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses();

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC LIMIT 5")
    LiveData<List<ExpenseEntity>> getRecentExpenses();

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    LiveData<Double> getTotalMonthlyExpenses(long startTimestamp, long endTimestamp);
}
