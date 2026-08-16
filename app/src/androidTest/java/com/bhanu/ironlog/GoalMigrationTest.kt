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
class GoalMigrationTest {
    @Test
    fun migrate22To23PreservesDataAndCreatesGoals() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration-test-goals-comprehensive")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(22) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // v22 schema
                    db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`id` INTEGER PRIMARY KEY NOT NULL, `sex` TEXT, `dateOfBirth` INTEGER, `heightCm` REAL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `body_weight_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `weightKg` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `waist_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `circumferenceCm` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `personal_records` (`exerciseTemplateId` INTEGER NOT NULL, `libraryExerciseId` INTEGER NOT NULL, `weightPR` REAL NOT NULL, `weightPRDate` INTEGER NOT NULL, `weightPRSessionId` INTEGER NOT NULL, `estimated1RM` REAL NOT NULL, `estimated1RMDate` INTEGER NOT NULL, `estimated1RMSessionId` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`exerciseTemplateId`, `libraryExerciseId`))")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val db = factory.create(config).writableDatabase

        // Insert existing data
        db.execSQL("INSERT INTO user_profile (id, sex, createdAt, updatedAt) VALUES (1, 'MALE', 1000, 1000)")
        db.execSQL("INSERT INTO body_weight_history (weightKg, timestamp, notes) VALUES (85.0, 1000, 'Baseline')")
        db.execSQL("INSERT INTO waist_history (circumferenceCm, timestamp, notes) VALUES (90.0, 1000, 'Baseline')")
        db.execSQL("INSERT INTO personal_records (exerciseTemplateId, libraryExerciseId, weightPR, weightPRDate, weightPRSessionId, estimated1RM, estimated1RMDate, estimated1RMSessionId, createdAt, updatedAt) VALUES (0, 10, 100.0, 1000, 1, 120.0, 1000, 1, 1000, 1000)")

        // RUN MIGRATION
        AppDatabase.MIGRATION_22_23.migrate(db)

        // VERIFY PRESERVATION
        var cursor = db.query("SELECT sex FROM user_profile WHERE id = 1")
        assertTrue("user_profile data should survive", cursor.moveToFirst())
        assertEquals("MALE", cursor.getString(0))
        cursor.close()

        cursor = db.query("SELECT weightKg FROM body_weight_history")
        assertTrue("body_weight_history data should survive", cursor.moveToFirst())
        assertEquals(85.0, cursor.getDouble(0), 0.01)
        cursor.close()

        cursor = db.query("SELECT circumferenceCm FROM waist_history")
        assertTrue("waist_history data should survive", cursor.moveToFirst())
        assertEquals(90.0, cursor.getDouble(0), 0.01)
        cursor.close()

        cursor = db.query("SELECT weightPR FROM personal_records WHERE libraryExerciseId = 10")
        assertTrue("personal_records data should survive", cursor.moveToFirst())
        assertEquals(100.0, cursor.getDouble(0), 0.01)
        cursor.close()

        // VERIFY NEW TABLE
        cursor = db.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'goals'")
        assertTrue("goals table should be created", cursor.moveToFirst())
        cursor.close()

        // VERIFY INSERTION INTO NEW TABLE
        db.execSQL("INSERT INTO goals (type, targetValue, startingValue, startDate) VALUES ('WEIGHT', 80.0, 85.0, 2000)")
        cursor = db.query("SELECT targetValue FROM goals WHERE type = 'WEIGHT'")
        assertTrue("Should be able to insert and query from goals table", cursor.moveToFirst())
        assertEquals(80.0, cursor.getDouble(0), 0.01)
        cursor.close()

        db.close()
    }
}
