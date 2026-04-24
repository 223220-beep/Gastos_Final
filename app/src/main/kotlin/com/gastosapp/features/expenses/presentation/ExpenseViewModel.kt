package com.gastosapp.features.expenses.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastosapp.features.expenses.data.ExpenseEntity
import com.gastosapp.features.expenses.data.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ExpenseUiState(
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val allExpenses: List<ExpenseEntity> = emptyList(),
    val totalMonthly: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        observeExpenses()
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Combinar flujos para actualizar el estado
            repository.getAllExpensesFlow().combine(repository.getRecentExpensesFlow()) { all, recent ->
                all to recent
            }.collect { (all, recent) ->
                val total = calculateMonthlyTotal(all)
                _uiState.update { 
                    it.copy(
                        allExpenses = all,
                        recentExpenses = recent,
                        totalMonthly = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateMonthlyTotal(expenses: List<ExpenseEntity>): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return expenses.filter { exp ->
            val expCal = Calendar.getInstance().apply { timeInMillis = exp.timestamp }
            expCal.get(Calendar.MONTH) == currentMonth && expCal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
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
