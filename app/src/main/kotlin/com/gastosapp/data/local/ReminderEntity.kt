package com.gastosapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val amount: Double,
    val category: String,
    val date: String,
    val isCompleted: Boolean
)
