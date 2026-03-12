package com.gastosapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Int): LiveData<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): LiveData<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC LIMIT 5")
    fun getRecentExpenses(): LiveData<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    fun getTotalMonthlyExpenses(startTimestamp: Long, endTimestamp: Long): LiveData<Double>
}
