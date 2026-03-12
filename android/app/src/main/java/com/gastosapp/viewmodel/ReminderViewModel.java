package com.gastosapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.gastosapp.data.local.ReminderEntity;
import com.gastosapp.data.repository.ReminderRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ReminderViewModel extends ViewModel {
    private final ReminderRepository repository;
    private final LiveData<List<ReminderEntity>> allReminders;

    @Inject
    public ReminderViewModel(ReminderRepository repository) {
        this.repository = repository;
        this.allReminders = repository.getAllReminders();
    }

    public LiveData<List<ReminderEntity>> getAllReminders() {
        return allReminders;
    }

    public void addReminder(ReminderEntity reminder) {
        repository.insert(reminder);
    }

    public void updateReminder(ReminderEntity reminder) {
        repository.update(reminder);
    }

    public void deleteReminder(ReminderEntity reminder) {
        repository.delete(reminder);
    }
}
