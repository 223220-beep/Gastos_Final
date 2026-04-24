package com.gastosapp.features.reminders.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastosapp.features.reminders.data.ReminderEntity
import com.gastosapp.features.reminders.data.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    val allReminders: LiveData<List<ReminderEntity>> = repository.getAllReminders()

    fun addReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.insert(reminder)
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.update(reminder)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.delete(reminder)
        }
    }
}
