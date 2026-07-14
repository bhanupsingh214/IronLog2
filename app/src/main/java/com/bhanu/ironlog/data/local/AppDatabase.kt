package com.bhanu.ironlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bhanu.ironlog.data.local.dao.PlaceholderDao
import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.SessionDao
import com.bhanu.ironlog.data.local.entity.*

@Database(
    entities = [
        PlaceholderEntity::class,
        ProgramEntity::class,
        WorkoutDayEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
    abstract fun programDao(): ProgramDao
    abstract fun sessionDao(): SessionDao
}
