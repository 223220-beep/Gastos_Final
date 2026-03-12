package com.gastosapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    fun getRecentExpenses(): LiveData<List<ExpenseEntity>> {
        return repository.getRecentExpenses()
    }

    fun getAllExpenses(): LiveData<List<ExpenseEntity>> {
        return repository.getAllExpenses()
    }

    fun getTotalMonthlyExpenses(): LiveData<Double> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis

        return repository.getTotalMonthlyExpenses(start, end)
    }

    fun getExpenseById(id: Int): LiveData<ExpenseEntity> {
        return repository.getExpenseById(id)
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.insert(expense)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.update(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }
}
