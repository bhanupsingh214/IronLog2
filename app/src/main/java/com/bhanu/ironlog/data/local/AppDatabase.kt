package com.bhanu.ironlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bhanu.ironlog.data.local.dao.PlaceholderDao
import com.bhanu.ironlog.data.local.dao.ProgramDao
import com.bhanu.ironlog.data.local.dao.SessionDao
import com.bhanu.ironlog.data.local.dao.WorkoutSessionDao
import com.bhanu.ironlog.data.local.dao.PersonalRecordDao
import com.bhanu.ironlog.data.local.dao.WorkoutSettingsDao
import com.bhanu.ironlog.data.local.entity.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlaceholderEntity::class,
        ProgramEntity::class,
        WorkoutDayEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSession::class,
        SessionExercise::class,
        SessionSet::class,
        PersonalRecordEntity::class,
        WorkoutSettingsEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
    abstract fun programDao(): ProgramDao
    abstract fun sessionDao(): SessionDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun workoutSettingsDao(): WorkoutSettingsDao

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_session_logs` (
                        `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `programId` INTEGER NOT NULL, 
                        `workoutDayId` INTEGER NOT NULL, 
                        `startTime` INTEGER NOT NULL, 
                        `endTime` INTEGER, 
                        `status` TEXT NOT NULL, 
                        `notes` TEXT, 
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `session_exercises` (
                        `sessionExerciseId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `sessionId` INTEGER NOT NULL, 
                        `exerciseTemplateId` INTEGER NOT NULL, 
                        `exerciseOrder` INTEGER NOT NULL, 
                        `isSwapped` INTEGER NOT NULL, 
                        `originalExerciseId` INTEGER, 
                        FOREIGN KEY(`sessionId`) REFERENCES `workout_session_logs`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercises_sessionId` ON `session_exercises` (`sessionId`)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `session_sets` (
                        `sessionSetId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `sessionExerciseId` INTEGER NOT NULL, 
                        `setNumber` INTEGER NOT NULL, 
                        `weight` REAL NOT NULL, 
                        `reps` INTEGER NOT NULL, 
                        `rpe` REAL, 
                        `completed` INTEGER NOT NULL, 
                        `notes` TEXT, 
                        `createdAt` INTEGER NOT NULL, 
                        FOREIGN KEY(`sessionExerciseId`) REFERENCES `session_exercises`(`sessionExerciseId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_sets_sessionExerciseId` ON `session_sets` (`sessionExerciseId`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `dayName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `programName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `completedExerciseIds` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `session_sets` ADD COLUMN `setType` TEXT NOT NULL DEFAULT 'Working'")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `durationSeconds` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_records` (
                        `exerciseTemplateId` INTEGER PRIMARY KEY NOT NULL, 
                        `weightPR` REAL NOT NULL, 
                        `weightPRDate` INTEGER NOT NULL, 
                        `weightPRSessionId` INTEGER NOT NULL, 
                        `estimated1RM` REAL NOT NULL, 
                        `estimated1RMDate` INTEGER NOT NULL, 
                        `estimated1RMSessionId` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `currentExerciseId` INTEGER")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `currentSetNumber` INTEGER")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `completedSetsCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `hasShownBackgroundDialog` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `session_exercises` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'PLANNED'")
                db.execSQL("ALTER TABLE `session_exercises` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `session_exercises` ADD COLUMN `restTimerSeconds` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ExerciseEntity changes
                db.execSQL("ALTER TABLE `exercises` ADD COLUMN `restTimerSeconds` INTEGER NOT NULL DEFAULT 90")
                db.execSQL("ALTER TABLE `exercises` ADD COLUMN `useDefaultRestTimer` INTEGER NOT NULL DEFAULT 1")
                
                // WorkoutSession changes
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerStartTime` INTEGER")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerDurationSeconds` INTEGER")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerState` TEXT NOT NULL DEFAULT 'IDLE'")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerPausedRemainingSeconds` INTEGER")

                // New WorkoutSettings table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_settings` (
                        `id` INTEGER PRIMARY KEY NOT NULL, 
                        `defaultRestTimerSeconds` INTEGER NOT NULL DEFAULT 90, 
                        `autoStartTimer` INTEGER NOT NULL DEFAULT 1, 
                        `hapticFeedback` INTEGER NOT NULL DEFAULT 1, 
                        `soundAlert` INTEGER NOT NULL DEFAULT 1
                    )
                """)
                db.execSQL("INSERT OR IGNORE INTO `workout_settings` (id) VALUES (1)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `session_exercises` ADD COLUMN `exerciseName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `session_exercises` ADD COLUMN `muscleGroup` TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
