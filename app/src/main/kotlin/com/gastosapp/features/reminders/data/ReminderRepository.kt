package com.gastosapp.features.reminders.data

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    fun getAllReminders(): LiveData<List<ReminderEntity>> {
        return reminderDao.getAllReminders()
    }

    suspend fun insert(reminder: ReminderEntity) {
        reminderDao.insert(reminder)
    }

    suspend fun update(reminder: ReminderEntity) {
        reminderDao.update(reminder)
    }

    suspend fun delete(reminder: ReminderEntity) {
        reminderDao.delete(reminder)
    }
}
