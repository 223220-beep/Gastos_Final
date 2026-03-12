package com.gastosapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY date ASC")
    fun getAllReminders(): LiveData<List<ReminderEntity>>
}
