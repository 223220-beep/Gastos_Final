package com.gastosapp.features.analytics.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val limitAmount: Double,
    val currentSpent: Double = 0.0,
    val month: String // Formato YYYY-MM
)
