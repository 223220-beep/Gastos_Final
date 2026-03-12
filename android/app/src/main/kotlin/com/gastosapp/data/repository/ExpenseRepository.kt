package com.gastosapp.data.repository

import androidx.lifecycle.LiveData
import com.gastosapp.data.local.ExpenseDao
import com.gastosapp.data.local.ExpenseEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun insert(expense: ExpenseEntity) {
        expenseDao.insert(expense)
    }

    suspend fun update(expense: ExpenseEntity) {
        expenseDao.update(expense)
    }

    suspend fun delete(expense: ExpenseEntity) {
        expenseDao.delete(expense)
    }

    fun getExpenseById(id: Int): LiveData<ExpenseEntity> {
        return expenseDao.getExpenseById(id)
    }

    fun getAllExpenses(): LiveData<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    fun getRecentExpenses(): LiveData<List<ExpenseEntity>> {
        return expenseDao.getRecentExpenses()
    }

    fun getTotalMonthlyExpenses(start: Long, end: Long): LiveData<Double> {
        return expenseDao.getTotalMonthlyExpenses(start, end)
    }
}
