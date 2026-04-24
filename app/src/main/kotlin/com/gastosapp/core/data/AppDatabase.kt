package com.gastosapp.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gastosapp.features.expenses.data.ExpenseDao
import com.gastosapp.features.expenses.data.ExpenseEntity
import com.gastosapp.features.auth.data.UserDao
import com.gastosapp.features.auth.data.UserEntity
import com.gastosapp.features.reminders.data.ReminderDao
import com.gastosapp.features.reminders.data.ReminderEntity
import com.gastosapp.features.analytics.data.BudgetDao
import com.gastosapp.features.analytics.data.BudgetEntity

@Database(entities = [ExpenseEntity::class, ReminderEntity::class, UserEntity::class, BudgetEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao
}
