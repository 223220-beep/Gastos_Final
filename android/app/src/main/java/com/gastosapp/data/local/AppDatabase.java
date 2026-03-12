package com.gastosapp.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Assuming UserEntity is also in the same package, otherwise it would need an import.
// import com.gastosapp.data.local.UserEntity;
// import com.gastosapp.data.local.UserDao; // If UserDao is not in the same package

@Database(entities = { ExpenseEntity.class, ReminderEntity.class, UserEntity.class }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExpenseDao expenseDao();

    public abstract ReminderDao reminderDao();

    public abstract UserDao userDao();
}
