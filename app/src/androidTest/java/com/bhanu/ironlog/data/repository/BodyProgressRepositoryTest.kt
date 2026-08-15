package com.bhanu.ironlog.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bhanu.ironlog.data.local.AppDatabase
import com.bhanu.ironlog.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BodyProgressRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: BodyProgressRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = BodyProgressRepository(db.userProfileDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun profile_canBeSavedAndRetrieved() = runBlocking {
        val profile = UserProfileEntity(sex = "Male", heightCm = 180.0)
        repository.saveProfile(profile)

        val retrieved = repository.getProfile().first()
        assertNotNull(retrieved)
        assertEquals("Male", retrieved?.sex)
        assertEquals(180.0, retrieved?.heightCm!!, 0.01)
    }

    @Test
    fun weightHistory_canBeManaged() = runBlocking {
        repository.addWeightEntry(75.0, 1000L)
        repository.addWeightEntry(76.0, 2000L)

        val history = repository.getWeightHistory().first()
        assertEquals(2, history.size)
        assertEquals(76.0, history[0].weightKg, 0.01) // Sorted by timestamp DESC

        val latest = repository.getLatestWeight().first()
        assertEquals(76.0, latest?.weightKg!!, 0.01)

        repository.deleteWeightEntry(history[0])
        val afterDelete = repository.getWeightHistory().first()
        assertEquals(1, afterDelete.size)
    }

    @Test
    fun waistHistory_canBeManaged() = runBlocking {
        repository.addWaistEntry(85.0, 1000L)

        val latest = repository.getLatestWaist().first()
        assertEquals(85.0, latest?.circumferenceCm!!, 0.01)
    }
}
