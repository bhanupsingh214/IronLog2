package com.bhanu.ironlog.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bhanu.ironlog.data.local.AppDatabase
import com.bhanu.ironlog.data.local.dao.*
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
            AppDatabase.MIGRATION_18_19,
            AppDatabase.MIGRATION_19_20,
            AppDatabase.MIGRATION_20_21,
            AppDatabase.MIGRATION_21_22,
            AppDatabase.MIGRATION_22_23
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT OR IGNORE INTO workout_settings (id, defaultRestTimerSeconds, autoStartTimer, hapticFeedback, soundAlert) VALUES (1, 90, 1, 1, 1)")
                db.execSQL("INSERT OR IGNORE INTO user_profile (id, createdAt, updatedAt) VALUES (1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})")
            }
        }).build()
    }

    @Provides fun providePlaceholderDao(database: AppDatabase): PlaceholderDao = database.placeholderDao()
    @Provides fun provideProgramDao(database: AppDatabase): ProgramDao = database.programDao()
    @Provides fun provideSessionDao(database: AppDatabase): SessionDao = database.sessionDao()
    @Provides fun provideWorkoutSessionDao(database: AppDatabase): WorkoutSessionDao = database.workoutSessionDao()
    @Provides fun providePersonalRecordDao(database: AppDatabase): PersonalRecordDao = database.personalRecordDao()
    @Provides fun provideWorkoutSettingsDao(database: AppDatabase): WorkoutSettingsDao = database.workoutSettingsDao()
    @Provides fun provideLibraryExerciseDao(database: AppDatabase): LibraryExerciseDao = database.libraryExerciseDao()
    @Provides fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()
    @Provides fun provideGoalDao(database: AppDatabase): GoalDao = database.goalDao()
}