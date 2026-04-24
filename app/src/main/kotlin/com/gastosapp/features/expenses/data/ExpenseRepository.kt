package com.gastosapp.features.expenses.data

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

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

    fun getExpenseByIdFlow(id: Int): Flow<ExpenseEntity> {
        return expenseDao.getExpenseByIdFlow(id)
    }

    fun getAllExpenses(): LiveData<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpensesFlow()
    }

    fun getRecentExpenses(): LiveData<List<ExpenseEntity>> {
        return expenseDao.getRecentExpenses()
    }

    fun getRecentExpensesFlow(): Flow<List<ExpenseEntity>> {
        return expenseDao.getRecentExpensesFlow()
    }

    fun getTotalMonthlyExpenses(start: Long, end: Long): LiveData<Double> {
        return expenseDao.getTotalMonthlyExpenses(start, end)
    }
}
