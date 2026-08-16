package com.bhanu.ironlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.bhanu.ironlog.data.local.dao.*
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
        WorkoutSettingsEntity::class,
        LibraryExerciseEntity::class,
        UserProfileEntity::class,
        BodyWeightEntry::class,
        WaistEntry::class,
        GoalEntity::class
    ],
    version = 23,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao
    abstract fun programDao(): ProgramDao
    abstract fun sessionDao(): SessionDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun workoutSettingsDao(): WorkoutSettingsDao
    abstract fun libraryExerciseDao(): LibraryExerciseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun goalDao(): GoalDao

    /**
     * Clears all user-owned training data from the database.
     * Order of operations respects foreign key constraints (leaf to root).
     */
    suspend fun clearAllUserData() {
        withTransaction {
            val db = openHelper.writableDatabase
            // Sessions & History
            db.execSQL("DELETE FROM session_sets")
            db.execSQL("DELETE FROM session_exercises")
            db.execSQL("DELETE FROM workout_session_logs")

            // Programs & Blueprints
            db.execSQL("DELETE FROM exercise_sets")
            db.execSQL("DELETE FROM exercises")
            db.execSQL("DELETE FROM workout_days")
            db.execSQL("DELETE FROM programs")

            // Body & Progress
            db.execSQL("DELETE FROM body_weight_history")
            db.execSQL("DELETE FROM waist_history")
            db.execSQL("DELETE FROM user_profile")

            // Global State & Identity
            db.execSQL("DELETE FROM personal_records")
            db.execSQL("DELETE FROM exercise_library")
            db.execSQL("DELETE FROM workout_settings")
            db.execSQL("DELETE FROM goals")

            // Legacy Table (Phase 2 engine)
            db.execSQL("DELETE FROM workout_sessions")
        }
    }

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
                db.execSQL("ALTER TABLE `exercises` ADD COLUMN `restTimerSeconds` INTEGER NOT NULL DEFAULT 90")
                db.execSQL("ALTER TABLE `exercises` ADD COLUMN `useDefaultRestTimer` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerStartTime` INTEGER")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerDurationSeconds` INTEGER")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerState` TEXT NOT NULL DEFAULT 'IDLE'")
                db.execSQL("ALTER TABLE `workout_session_logs` ADD COLUMN `timerPausedRemainingSeconds` INTEGER")
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

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exercise_library` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `systemKey` TEXT, 
                        `name` TEXT NOT NULL, 
                        `normalizedName` TEXT NOT NULL, 
                        `muscleGroup` TEXT NOT NULL, 
                        `equipment` TEXT NOT NULL DEFAULT 'None', 
                        `exerciseType` TEXT NOT NULL DEFAULT 'Compound', 
                        `createdBy` TEXT NOT NULL DEFAULT 'System', 
                        `isActive` INTEGER NOT NULL DEFAULT 1, 
                        `createdAt` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_library_normalizedName` ON `exercise_library` (`normalizedName`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_library_systemKey` ON `exercise_library` (`systemKey`)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exercises_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `dayId` INTEGER NOT NULL, 
                        `libraryExerciseId` INTEGER NOT NULL DEFAULT 0, 
                        `order` INTEGER NOT NULL, 
                        `enabled` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        `restTimerSeconds` INTEGER NOT NULL DEFAULT 90, 
                        `useDefaultRestTimer` INTEGER NOT NULL DEFAULT 1, 
                        `targetSets` INTEGER NOT NULL DEFAULT 3, 
                        `targetRepMin` INTEGER NOT NULL DEFAULT 8, 
                        `targetRepMax` INTEGER NOT NULL DEFAULT 12, 
                        `targetRPE` REAL, 
                        `name` TEXT NOT NULL DEFAULT '', 
                        `muscleGroup` TEXT NOT NULL DEFAULT '', 
                        `equipment` TEXT NOT NULL DEFAULT '', 
                        `exerciseType` TEXT NOT NULL DEFAULT 'Compound', 
                        `createdAt` INTEGER NOT NULL, 
                        FOREIGN KEY(`dayId`) REFERENCES `workout_days`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`libraryExerciseId`) REFERENCES `exercise_library`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """)
                db.execSQL("""
                    INSERT INTO `exercises_new` (id, dayId, `order`, enabled, notes, restTimerSeconds, useDefaultRestTimer, name, muscleGroup, equipment, exerciseType, createdAt)
                    SELECT id, dayId, `order`, enabled, notes, restTimerSeconds, useDefaultRestTimer, name, muscleGroup, equipment, exerciseType, createdAt FROM `exercises`
                """)
                db.execSQL("DROP TABLE `exercises`")
                db.execSQL("ALTER TABLE `exercises_new` RENAME TO `exercises`")
                db.execSQL("""
                    INSERT OR IGNORE INTO exercise_library (name, normalizedName, muscleGroup, equipment, exerciseType, createdBy, isActive, createdAt, updatedAt)
                    SELECT DISTINCT name, LOWER(name), muscleGroup, equipment, exerciseType, 'User', 1, 0, 0 FROM exercises
                """)
                db.execSQL("""
                    UPDATE exercises SET libraryExerciseId = (
                        SELECT id FROM exercise_library WHERE exercise_library.name = exercises.name LIMIT 1
                    )
                """)
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_session_logs_new` (
                        `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `programId` INTEGER NOT NULL, `workoutDayId` INTEGER NOT NULL,
                        `dayName` TEXT NOT NULL, `programName` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER,
                        `status` TEXT NOT NULL DEFAULT 'CREATED', `notes` TEXT, `createdAt` INTEGER NOT NULL, `completedExerciseIds` TEXT NOT NULL,
                        `durationSeconds` INTEGER NOT NULL, `currentExerciseId` INTEGER, `currentSetNumber` INTEGER, `completedSetsCount` INTEGER NOT NULL,
                        `lastActiveTimestamp` INTEGER NOT NULL, `hasShownBackgroundDialog` INTEGER NOT NULL, `timerStartTime` INTEGER,
                        `timerDurationSeconds` INTEGER, `timerState` TEXT NOT NULL, `timerPausedRemainingSeconds` INTEGER
                    )
                """)
                db.execSQL("""
                    INSERT INTO `workout_session_logs_new` (
                        sessionId, programId, workoutDayId, dayName, programName, startTime, endTime, status, notes, createdAt,
                        completedExerciseIds, durationSeconds, currentExerciseId, currentSetNumber, completedSetsCount, lastActiveTimestamp,
                        hasShownBackgroundDialog, timerStartTime, timerDurationSeconds, timerState, timerPausedRemainingSeconds
                    ) SELECT sessionId, programId, workoutDayId, dayName, programName, startTime, endTime,
                        CASE WHEN status = 'ACTIVE' THEN 'IN_PROGRESS' ELSE status END, notes, createdAt, completedExerciseIds, durationSeconds,
                        currentExerciseId, currentSetNumber, completedSetsCount, createdAt, hasShownBackgroundDialog, timerStartTime,
                        timerDurationSeconds, timerState, timerPausedRemainingSeconds FROM `workout_session_logs`
                """)
                db.execSQL("DROP TABLE `workout_session_logs`")
                db.execSQL("ALTER TABLE `workout_session_logs_new` RENAME TO `workout_session_logs`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `session_exercises_new` (
                        `sessionExerciseId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `exerciseTemplateId` INTEGER NOT NULL,
                        `exerciseName` TEXT NOT NULL DEFAULT '', `muscleGroup` TEXT NOT NULL DEFAULT '', `equipment` TEXT NOT NULL DEFAULT '',
                        `exerciseType` TEXT NOT NULL DEFAULT 'Compound', `targetSets` INTEGER NOT NULL DEFAULT 3, `targetRepMin` INTEGER NOT NULL DEFAULT 8,
                        `targetRepMax` INTEGER NOT NULL DEFAULT 12, `targetRPE` REAL, `restTimerSeconds` INTEGER NOT NULL DEFAULT 90,
                        `exerciseOrder` INTEGER NOT NULL, `isSwapped` INTEGER NOT NULL, `originalExerciseId` INTEGER, `status` TEXT NOT NULL,
                        `notes` TEXT NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `workout_session_logs`(`sessionId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO `session_exercises_new` (
                        sessionExerciseId, sessionId, exerciseTemplateId, exerciseName, muscleGroup, exerciseOrder, isSwapped,
                        originalExerciseId, status, notes, restTimerSeconds
                    ) SELECT sessionExerciseId, sessionId, exerciseTemplateId, exerciseName, muscleGroup, exerciseOrder, isSwapped,
                        originalExerciseId, status, notes, restTimerSeconds FROM `session_exercises`
                """)
                db.execSQL("DROP TABLE `session_exercises`")
                db.execSQL("ALTER TABLE `session_exercises_new` RENAME TO `session_exercises`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercises_sessionId` ON `session_exercises` (`sessionId`)")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `session_exercises` ADD COLUMN `libraryExerciseId` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    UPDATE session_exercises SET libraryExerciseId = (
                        SELECT libraryExerciseId FROM exercises WHERE exercises.id = session_exercises.exerciseTemplateId
                    ) WHERE EXISTS (SELECT 1 FROM exercises WHERE exercises.id = session_exercises.exerciseTemplateId)
                """)
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    UPDATE session_exercises SET libraryExerciseId = (
                        SELECT libraryExerciseId FROM exercises WHERE exercises.id = session_exercises.exerciseTemplateId
                    ) WHERE (libraryExerciseId = 0 OR libraryExerciseId IS NULL) AND exerciseTemplateId > 0
                    AND EXISTS (SELECT 1 FROM exercises WHERE exercises.id = session_exercises.exerciseTemplateId AND libraryExerciseId > 0)
                """)
                val cleanSql = "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(%s, ' ', ''), '(', ''), ')', ''), '-', ''), ',', ''), '.', ''), '/', ''), '''', ''), '[', ''), ']', ''), '&', ''), '+', ''))"
                db.execSQL("""
                    UPDATE session_exercises SET libraryExerciseId = (
                        SELECT id FROM exercise_library WHERE LOWER(TRIM(name)) = LOWER(TRIM(session_exercises.exerciseName))
                        GROUP BY LOWER(TRIM(name)) HAVING COUNT(*) = 1
                    ) WHERE (libraryExerciseId = 0 OR libraryExerciseId IS NULL) AND exerciseName IS NOT NULL AND exerciseName != ''
                    AND EXISTS (SELECT 1 FROM exercise_library WHERE LOWER(TRIM(name)) = LOWER(TRIM(session_exercises.exerciseName)) GROUP BY LOWER(TRIM(name)) HAVING COUNT(*) = 1)
                """)
                db.execSQL("""
                    UPDATE exercises SET libraryExerciseId = (
                        SELECT id FROM exercise_library WHERE LOWER(TRIM(name)) = LOWER(TRIM(exercises.name))
                        GROUP BY LOWER(TRIM(name)) HAVING COUNT(*) = 1
                    ) WHERE (libraryExerciseId = 0 OR libraryExerciseId IS NULL) AND name IS NOT NULL AND name != ''
                    AND EXISTS (SELECT 1 FROM exercise_library WHERE LOWER(TRIM(name)) = LOWER(TRIM(exercises.name)) GROUP BY LOWER(TRIM(name)) HAVING COUNT(*) = 1)
                """)
                db.execSQL("""
                    UPDATE session_exercises SET libraryExerciseId = (
                        SELECT id FROM exercise_library WHERE ${cleanSql.format("name")} = ${cleanSql.format("session_exercises.exerciseName")}
                        GROUP BY ${cleanSql.format("name")} HAVING COUNT(*) = 1
                    ) WHERE (libraryExerciseId = 0 OR libraryExerciseId IS NULL) AND exerciseName IS NOT NULL AND exerciseName != ''
                    AND EXISTS (SELECT 1 FROM exercise_library WHERE ${cleanSql.format("name")} = ${cleanSql.format("session_exercises.exerciseName")} GROUP BY ${cleanSql.format("name")} HAVING COUNT(*) = 1)
                """)
                db.execSQL("""
                    UPDATE exercises SET libraryExerciseId = (
                        SELECT id FROM exercise_library WHERE ${cleanSql.format("name")} = ${cleanSql.format("exercises.name")}
                        GROUP BY ${cleanSql.format("name")} HAVING COUNT(*) = 1
                    ) WHERE (libraryExerciseId = 0 OR libraryExerciseId IS NULL) AND name IS NOT NULL AND name != ''
                    AND EXISTS (SELECT 1 FROM exercise_library WHERE ${cleanSql.format("name")} = ${cleanSql.format("exercises.name")} GROUP BY ${cleanSql.format("name")} HAVING COUNT(*) = 1)
                """)
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_records_new` (
                        `libraryExerciseId` INTEGER NOT NULL, `exerciseTemplateId` INTEGER NOT NULL, `weightPR` REAL NOT NULL,
                        `weightPRDate` INTEGER NOT NULL, `weightPRSessionId` INTEGER NOT NULL, `estimated1RM` REAL NOT NULL,
                        `estimated1RMDate` INTEGER NOT NULL, `estimated1RMSessionId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`libraryExerciseId`, `exerciseTemplateId`)
                    )
                """)
                db.execSQL("""
                    CREATE TEMP TABLE `pr_resolved` AS SELECT pr.*, IFNULL(e.libraryExerciseId, 0) as resolvedLibId
                    FROM personal_records pr LEFT JOIN exercises e ON pr.exerciseTemplateId = e.id
                """)
                db.execSQL("""
                    INSERT INTO personal_records_new (
                        libraryExerciseId, exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId,
                        estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt
                    ) SELECT resolvedLibId, 0,
                        (SELECT weightPR FROM pr_resolved r2 WHERE r2.resolvedLibId = r1.resolvedLibId ORDER BY weightPR DESC, weightPRDate DESC LIMIT 1),
                        (SELECT weightPRDate FROM pr_resolved r2 WHERE r2.resolvedLibId = r1.resolvedLibId ORDER BY weightPR DESC, weightPRDate DESC LIMIT 1),
                        (SELECT weightPRSessionId FROM pr_resolved r2 WHERE r2.resolvedLibId = r1.resolvedLibId ORDER BY weightPR DESC, weightPRDate DESC LIMIT 1),
                        (SELECT estimated1RM FROM pr_resolved r2 WHERE r2.resolvedLibId = r1.resolvedLibId ORDER BY estimated1RM DESC, estimated1RMDate DESC LIMIT 1),
                        (SELECT estimated1RMDate FROM pr_resolved r2 WHERE r2.resolvedLibId = r1.resolvedLibId ORDER BY estimated1RM DESC, estimated1RMDate DESC LIMIT 1),
                        (SELECT estimated1RMSessionId FROM pr_resolved r2 WHERE r2.resolvedLibId = r1.resolvedLibId ORDER BY estimated1RM DESC, estimated1RMDate DESC LIMIT 1),
                        MIN(createdAt), MAX(updatedAt) FROM pr_resolved r1 WHERE resolvedLibId > 0 GROUP BY resolvedLibId
                """)
                db.execSQL("""
                    INSERT INTO personal_records_new (
                        libraryExerciseId, exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId,
                        estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt
                    ) SELECT 0, exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM,
                        estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt FROM pr_resolved WHERE resolvedLibId = 0
                """)
                db.execSQL("DROP TABLE `personal_records`")
                db.execSQL("ALTER TABLE `personal_records_new` RENAME TO `personal_records`")
                db.execSQL("DROP TABLE `pr_resolved`")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercises_identity` ON `session_exercises` (`libraryExerciseId`, `exerciseTemplateId`)")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER PRIMARY KEY NOT NULL, `sex` TEXT, `dateOfBirth` INTEGER, `heightCm` REAL,
                        `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `body_weight_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `weightKg` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `waist_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `circumferenceCm` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL
                    )
                """)
                db.execSQL("INSERT OR IGNORE INTO `user_profile` (id, createdAt, updatedAt) VALUES (1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `goals` (
                        `goalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `type` TEXT NOT NULL,
                        `targetValue` REAL NOT NULL,
                        `startingValue` REAL NOT NULL,
                        `libraryExerciseId` INTEGER,
                        `frequencyCount` INTEGER,
                        `frequencyPeriod` TEXT,
                        `startDate` INTEGER NOT NULL,
                        `deadline` INTEGER
                    )
                """)
            }
        }
    }
}
