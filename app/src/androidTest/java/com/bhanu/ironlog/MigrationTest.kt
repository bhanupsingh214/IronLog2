package com.bhanu.ironlog

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bhanu.ironlog.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @Test
    fun migrate19To20() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration-test")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(19) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create v19 schema
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `exercises` (
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
                            `createdAt` INTEGER NOT NULL
                        )
                    """)
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

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val db = factory.create(config).writableDatabase

        // 1. SIMPLE LIBRARY EXERCISE (Temp 1 -> Lib 10)
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (1, 1, 10, 1, 1, 1000)")
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (1, 100.0, 1000, 101, 120.0, 1000, 101, 1000, 1000)")

        // 2. MULTI-TEMPLATE SAME LIBRARY EXERCISE (Temp 2, 3 -> Lib 20)
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (2, 1, 20, 2, 1, 2000)")
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (3, 1, 20, 3, 1, 3000)")
        // Temp 2 has better weight
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (2, 150.0, 2000, 201, 170.0, 2000, 201, 2000, 2000)")
        // Temp 3 has better e1RM
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (3, 140.0, 3000, 301, 180.0, 3000, 301, 3000, 3000)")

        // 3. INDEPENDENT METRIC WINNERS (Handled in 2 above)

        // 4. CUSTOM/UNRESOLVED EXERCISES (Temp 4, 5 -> Lib 0)
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (4, 1, 0, 4, 1, 4000)")
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (5, 1, 0, 5, 1, 5000)")
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (4, 50.0, 4000, 401, 60.0, 4000, 401, 4000, 4000)")
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (5, 55.0, 5000, 501, 65.0, 5000, 501, 5000, 5000)")

        // 5. DELETED TEMPLATE (Temp 99 -> No exercise)
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (99, 80.0, 9900, 991, 90.0, 9900, 991, 9900, 9900)")

        // 6. DIFFERENT LIBRARY EXERCISES
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (6, 1, 60, 6, 1, 6000)")
        db.execSQL("INSERT INTO exercises (id, dayId, libraryExerciseId, `order`, enabled, createdAt) VALUES (7, 1, 70, 7, 1, 7000)")
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (6, 60.0, 6000, 601, 61.0, 6000, 601, 6000, 6000)")
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (7, 70.0, 7000, 701, 71.0, 7000, 701, 7000, 7000)")

        // RUN MIGRATION
        AppDatabase.MIGRATION_19_20.migrate(db)

        // VERIFY RESULTS

        // Scenario 1: Simple Library
        var cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 10 AND exerciseTemplateId = 0")
        assertTrue("Simple Library PR not found", cursor.moveToFirst())
        assertEquals(100.0, cursor.getDouble(cursor.getColumnIndexOrThrow("weightPR")), 0.01)
        assertEquals(101L, cursor.getLong(cursor.getColumnIndexOrThrow("weightPRSessionId")))
        cursor.close()

        // Scenario 2 & 3: Multi-Template Merge + Independent Winners
        cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 20 AND exerciseTemplateId = 0")
        assertTrue("Merged Library PR not found", cursor.moveToFirst())
        // Weight winner was Temp 2 (150.0 @ Session 201)
        assertEquals(150.0, cursor.getDouble(cursor.getColumnIndexOrThrow("weightPR")), 0.01)
        assertEquals(201L, cursor.getLong(cursor.getColumnIndexOrThrow("weightPRSessionId")))
        // e1RM winner was Temp 3 (180.0 @ Session 301)
        assertEquals(180.0, cursor.getDouble(cursor.getColumnIndexOrThrow("estimated1RM")), 0.01)
        assertEquals(301L, cursor.getLong(cursor.getColumnIndexOrThrow("estimated1RMSessionId")))
        cursor.close()

        // Scenario 4: Custom Exercises remain separate
        cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 0 AND exerciseTemplateId = 4")
        assertTrue("Custom PR 4 not found", cursor.moveToFirst())
        assertEquals(50.0, cursor.getDouble(cursor.getColumnIndexOrThrow("weightPR")), 0.01)
        cursor.close()

        cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 0 AND exerciseTemplateId = 5")
        assertTrue("Custom PR 5 not found", cursor.moveToFirst())
        assertEquals(55.0, cursor.getDouble(cursor.getColumnIndexOrThrow("weightPR")), 0.01)
        cursor.close()

        // Scenario 5: Deleted Template
        cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 0 AND exerciseTemplateId = 99")
        assertTrue("Deleted template PR not found", cursor.moveToFirst())
        assertEquals(80.0, cursor.getDouble(cursor.getColumnIndexOrThrow("weightPR")), 0.01)
        cursor.close()

        // Scenario 6: Different Library Exercises
        cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 60 AND exerciseTemplateId = 0")
        assertTrue("Lib 60 PR not found", cursor.moveToFirst())
        cursor.close()
        cursor = db.query("SELECT * FROM personal_records WHERE libraryExerciseId = 70 AND exerciseTemplateId = 0")
        assertTrue("Lib 70 PR not found", cursor.moveToFirst())
        cursor.close()

        db.close()
    }

    @Test
    fun migrate21To22() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration-test-22")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(21) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create v21 schema (minimal for this test)
                    db.execSQL("CREATE TABLE IF NOT EXISTS `workout_settings` (`id` INTEGER PRIMARY KEY NOT NULL)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val db = factory.create(config).writableDatabase
        db.execSQL("INSERT INTO workout_settings (id) VALUES (1)")

        // RUN MIGRATION
        AppDatabase.MIGRATION_21_22.migrate(db)

        // VERIFY RESULTS
        // Check user_profile table exists and has initial record
        var cursor = db.query("SELECT * FROM user_profile")
        assertTrue("UserProfile table should have 1 record", cursor.moveToFirst())
        assertEquals(1L, cursor.getLong(cursor.getColumnIndexOrThrow("id")))
        cursor.close()

        // Check history tables exist
        db.execSQL("INSERT INTO body_weight_history (weightKg, timestamp, notes) VALUES (75.0, 1000, 'Test')")
        cursor = db.query("SELECT * FROM body_weight_history")
        assertTrue(cursor.moveToFirst())
        assertEquals(75.0, cursor.getDouble(cursor.getColumnIndexOrThrow("weightKg")), 0.01)
        cursor.close()

        db.execSQL("INSERT INTO waist_history (circumferenceCm, timestamp, notes) VALUES (85.0, 1000, 'Test')")
        cursor = db.query("SELECT * FROM waist_history")
        assertTrue(cursor.moveToFirst())
        assertEquals(85.0, cursor.getDouble(cursor.getColumnIndexOrThrow("circumferenceCm")), 0.01)
        cursor.close()

        db.close()
    }
}
