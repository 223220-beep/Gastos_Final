package com.gastosapp.data.repository;

import com.gastosapp.data.local.ReminderDao;
import com.gastosapp.data.local.ReminderEntity;

import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReminderRepository {
    private final ReminderDao reminderDao;
    private final ExecutorService executorService;

    @Inject
    public ReminderRepository(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ReminderEntity>> getAllReminders() {
        return reminderDao.getAllReminders();
    }

    public void insert(ReminderEntity reminder) {
        executorService.execute(() -> reminderDao.insert(reminder));
    }

    public void update(ReminderEntity reminder) {
        executorService.execute(() -> reminderDao.update(reminder));
    }

    public void delete(ReminderEntity reminder) {
        executorService.execute(() -> reminderDao.delete(reminder));
    }
}
