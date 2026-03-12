package com.gastosapp.di;

import android.content.Context;
import androidx.room.Room;
import com.gastosapp.data.local.AppDatabase;
import com.gastosapp.data.local.ExpenseDao;
import com.gastosapp.data.local.ReminderDao;
import com.gastosapp.data.local.UserDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "gastos_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public static ExpenseDao provideExpenseDao(AppDatabase database) {
        return database.expenseDao();
    }

    @Provides
    public static ReminderDao provideReminderDao(AppDatabase database) {
        return database.reminderDao();
    }

    @Provides
    public static UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }
}
