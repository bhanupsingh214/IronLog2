package com.bhanu.ironlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bhanu.ironlog.data.local.dao.PlaceholderDao
import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.entity.*

@Database(
    entities = [
        PlaceholderEntity::class,
        ProgramEntity::class,
        WorkoutDayEntity::class,
        ExerciseEntity::class,
        SetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
    abstract fun programDao(): ProgramDao
}
