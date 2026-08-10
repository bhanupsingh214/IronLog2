package com.bhanu.ironlog.di

import android.content.Context
import androidx.room.Room
import com.bhanu.ironlog.data.local.AppDatabase
import com.bhanu.ironlog.data.local.dao.PlaceholderDao
import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.SessionDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.dao.PersonalRecordDao
import com.bhanu.ironlog.data.local.dao.WorkoutSettingsDao
import com.bhanu.ironlog.data.local.dao.LibraryExerciseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "iron_log_db"
        ).addMigrations(
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12,
            AppDatabase.MIGRATION_12_13,
            AppDatabase.MIGRATION_13_14,
            AppDatabase.MIGRATION_14_15,
            AppDatabase.MIGRATION_15_16,
            AppDatabase.MIGRATION_16_17,
            AppDatabase.MIGRATION_17_18,
            AppDatabase.MIGRATION_18_19
        ).build()
    }

    @Provides
    fun providePlaceholderDao(database: AppDatabase): PlaceholderDao {
        return database.placeholderDao()
    }

    @Provides
    fun provideProgramDao(database: AppDatabase): ProgramDao {
        return database.programDao()
    }

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun provideWorkoutSessionDao(database: AppDatabase): WorkoutSessionDao {
        return database.workoutSessionDao()
    }

    @Provides
    fun providePersonalRecordDao(database: AppDatabase): PersonalRecordDao {
        return database.personalRecordDao()
    }

    @Provides
    fun provideWorkoutSettingsDao(database: AppDatabase): WorkoutSettingsDao {
        return database.workoutSettingsDao()
    }

    @Provides
    fun provideLibraryExerciseDao(database: AppDatabase): LibraryExerciseDao {
        return database.libraryExerciseDao()
    }
}
