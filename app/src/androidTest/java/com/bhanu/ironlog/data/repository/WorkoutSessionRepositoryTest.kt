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

@RunWith(AndroidJUnit4::class)
class WorkoutSessionRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkoutSessionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = WorkoutSessionRepository(
            db.workoutSessionDao(),
            db.programDao(),
            db.workoutSettingsDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getWorkoutCompletionSummary_ActiveSession_ReturnsLiveDuration() = runBlocking {
        // Given an active session started 10 seconds ago
        val startTime = System.currentTimeMillis() - 10000
        val sessionId = db.workoutSessionDao().insertSession(
            WorkoutSession(
                programId = 1,
                workoutDayId = 1,
                dayName = "Test Day",
                programName = "Test Program",
                status = WorkoutSessionStatus.IN_PROGRESS,
                startTime = startTime,
                durationSeconds = 0 // Not yet persisted
            )
        )

        // When fetching the summary
        val summary = repository.getWorkoutCompletionSummary(sessionId).first()

        // Then duration should be approximately 10 seconds (non-zero)
        assertNotNull(summary)
        assertTrue("Duration should be at least 10s, was ${summary?.durationSeconds}", (summary?.durationSeconds ?: 0) >= 10)
    }

    @Test
    fun getWorkoutCompletionSummary_CompletedSession_ReturnsPersistedDuration() = runBlocking {
        // Given a completed session with a persisted duration
        val persistedDuration = 3600L // 1 hour
        val sessionId = db.workoutSessionDao().insertSession(
            WorkoutSession(
                programId = 1,
                workoutDayId = 1,
                dayName = "Test Day",
                programName = "Test Program",
                status = WorkoutSessionStatus.COMPLETED,
                startTime = System.currentTimeMillis() - 7200000,
                endTime = System.currentTimeMillis() - 3600000,
                durationSeconds = persistedDuration
            )
        )

        // When fetching the summary
        val summary = repository.getWorkoutCompletionSummary(sessionId).first()

        // Then duration should match persisted value exactly
        assertNotNull(summary)
        assertEquals(persistedDuration, summary?.durationSeconds)
    }
}
