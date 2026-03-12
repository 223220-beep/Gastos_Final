package com.gastosapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    void insert(ReminderEntity reminder);

    @Update
    void update(ReminderEntity reminder);

    @Delete
    void delete(ReminderEntity reminder);

    @Query("SELECT * FROM reminders ORDER BY date ASC")
    LiveData<List<ReminderEntity>> getAllReminders();
}
