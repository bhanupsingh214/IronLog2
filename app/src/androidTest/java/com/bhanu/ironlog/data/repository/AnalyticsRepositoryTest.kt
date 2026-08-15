package com.bhanu.ironlog.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bhanu.ironlog.data.local.AppDatabase
import com.bhanu.ironlog.data.local.entity.WorkoutSession
import com.bhanu.ironlog.data.model.WorkoutSessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class AnalyticsRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var analyticsRepository: AnalyticsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        val historyRepository = HistoryRepository(db.workoutSessionDao())
        val programRepository = ProgramRepository(db.programDao(), db.sessionDao(), ExerciseLibraryRepository(db.libraryExerciseDao()))
        val prRepository = PersonalRecordRepository(db.personalRecordDao(), db.workoutSessionDao())

        analyticsRepository = AnalyticsRepository(
            historyRepository,
            programRepository,
            prRepository,
            db.workoutSessionDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getMonthlyRecap_calculatesCorrectStats() = runBlocking {
        // Given 2 workouts in August 2026
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.AUGUST, 10, 10, 0)
        val startTime1 = calendar.timeInMillis

        db.workoutSessionDao().insertSession(
            WorkoutSession(
                programId = 1,
                workoutDayId = 1,
                dayName = "Push",
                programName = "Test",
                status = WorkoutSessionStatus.COMPLETED,
                startTime = startTime1,
                createdAt = startTime1,
                durationSeconds = 3600 // 60 mins
            )
        )

        calendar.set(2026, Calendar.AUGUST, 15, 10, 0)
        val startTime2 = calendar.timeInMillis
        db.workoutSessionDao().insertSession(
            WorkoutSession(
                programId = 1,
                workoutDayId = 1,
                dayName = "Pull",
                programName = "Test",
                status = WorkoutSessionStatus.COMPLETED,
                startTime = startTime2,
                createdAt = startTime2,
                durationSeconds = 1800 // 30 mins
            )
        )

        // When
        val recap = analyticsRepository.getMonthlyRecap(2026, Calendar.AUGUST).first()

        // Then
        assertNotNull(recap)
        assertEquals(2, recap?.workoutCount)
        assertEquals(5400L, recap?.totalDurationSeconds)
        assertEquals(45, recap?.averageWorkoutDurationMinutes)
    }

    @Test
    fun getYearlyRecap_calculatesCorrectStats() = runBlocking {
        // Given 1 workout in 2026
        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.AUGUST, 10, 10, 0)
        val startTime = calendar.timeInMillis

        db.workoutSessionDao().insertSession(
            WorkoutSession(
                programId = 1,
                workoutDayId = 1,
                dayName = "Push",
                programName = "Test",
                status = WorkoutSessionStatus.COMPLETED,
                startTime = startTime,
                createdAt = startTime,
                durationSeconds = 3600
            )
        )

        // When
        val recap = analyticsRepository.getYearlyRecap(2026).first()

        // Then
        assertNotNull(recap)
        assertEquals(1, recap?.workoutCount)
        assertEquals("2026", recap?.periodName)
    }

    @Test
    fun getProgressSummary_handlesEmptyHistory() = runBlocking {
        val summary = analyticsRepository.getProgressSummary().first()
        assertEquals(0, summary.totalWorkouts)
        assertEquals(0.0, summary.totalVolume, 0.0)
    }
}
