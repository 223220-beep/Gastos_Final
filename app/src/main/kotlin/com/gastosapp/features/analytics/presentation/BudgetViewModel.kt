package com.gastosapp.features.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastosapp.features.analytics.data.BudgetDao
import com.gastosapp.features.analytics.data.BudgetEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class BudgetUiState(
    val budgets: List<BudgetEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetDao: BudgetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val currentMonth: String
        get() = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            budgetDao.getBudgetsForMonth(currentMonth).collect { list ->
                _uiState.value = _uiState.value.copy(
                    budgets = list,
                    isLoading = false
                )
            }
        }
    }

    fun addBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                category = category,
                limitAmount = amount,
                month = currentMonth
            )
            budgetDao.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }
}
