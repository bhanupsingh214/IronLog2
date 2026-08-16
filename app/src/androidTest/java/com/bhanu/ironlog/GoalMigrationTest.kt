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
    fun migrate22To23CreatesEmptyGoalsTable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration-test-goals")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(22) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`id` INTEGER PRIMARY KEY NOT NULL, `sex` TEXT, `dateOfBirth` INTEGER, `heightCm` REAL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val db = factory.create(config).writableDatabase
        AppDatabase.MIGRATION_22_23.migrate(db)

        var cursor = db.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'goals'")
        assertTrue(cursor.moveToFirst())
        cursor.close()

        db.execSQL("INSERT INTO goals (type, targetValue, startingValue, startDate) VALUES ('WEIGHT', 80.0, 85.0, 1000)")
        cursor = db.query("SELECT type, targetValue, startingValue FROM goals")
        assertTrue(cursor.moveToFirst())
        assertEquals("WEIGHT", cursor.getString(cursor.getColumnIndexOrThrow("type")))
        assertEquals(80.0, cursor.getDouble(cursor.getColumnIndexOrThrow("targetValue")), 0.01)
        assertEquals(85.0, cursor.getDouble(cursor.getColumnIndexOrThrow("startingValue")), 0.01)
        cursor.close()
        db.close()
    }
}
