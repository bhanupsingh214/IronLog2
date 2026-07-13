package com.bhanu.ironlog.di

import android.content.Context
import androidx.room.Room
import com.bhanu.ironlog.data.local.AppDatabase
import com.bhanu.ironlog.data.local.dao.PlaceholderDao
import com.bhanu.ironlog.data.local.dao.ProgramDao
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
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun providePlaceholderDao(database: AppDatabase): PlaceholderDao {
        return database.placeholderDao()
    }

    @Provides
    fun provideProgramDao(database: AppDatabase): ProgramDao {
        return database.programDao()
    }
}
