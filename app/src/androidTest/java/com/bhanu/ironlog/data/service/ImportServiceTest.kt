package com.bhanu.ironlog.data.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bhanu.ironlog.data.local.backup.BackupMetadata
import com.bhanu.ironlog.data.local.backup.BackupPayload
import com.bhanu.ironlog.data.local.backup.WorkoutSettingsDto
import com.bhanu.ironlog.util.BackupSecurityUtil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class ImportServiceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var importService: ImportService
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Before
    fun setup() {
        // Use real instrumentation context provided by AndroidTest environment
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        importService = ImportService(context)
    }

    @Test
    fun parseBackupFromFile_succeedsWithValidBackup() {
        val payload = createValidPayload()
        val backupFile = createZipBackup(payload)

        val result = importService.parseBackup(backupFile)

        assertEquals(payload.metadata.appVersion, result.metadata.appVersion)
        assertEquals(payload.settings.defaultRestTimerSeconds, result.settings.defaultRestTimerSeconds)
    }

    @Test(expected = Exception::class)
    fun parseBackupFromFile_failsWithChecksumMismatch() {
        val payload = createValidPayload()
        val backupFile = createZipBackup(payload, corruptChecksum = true)

        importService.parseBackup(backupFile)
    }

    @Test(expected = Exception::class)
    fun parseBackupFromFile_failsWithUnsupportedVersion() {
        val payload = createValidPayload().let {
            it.copy(metadata = it.metadata.copy(version = 99))
        }
        val backupFile = createZipBackup(payload)

        importService.parseBackup(backupFile)
    }

    private fun createValidPayload() = BackupPayload(
        metadata = BackupMetadata(
            version = 1,
            timestamp = System.currentTimeMillis(),
            appVersion = "1.0.0",
            programCount = 0,
            sessionCount = 0
        ),
        library = emptyList(),
        programs = emptyList(),
        history = emptyList(),
        records = emptyList(),
        settings = WorkoutSettingsDto(0, 60, true, true, true)
    )

    private fun createZipBackup(payload: BackupPayload, corruptChecksum: Boolean = false): File {
        val zipFile = tempFolder.newFile("test.ironlog")
        val dataJson = json.encodeToString(payload)
        val checksum = if (corruptChecksum) "wrong" else BackupSecurityUtil.calculateChecksum(dataJson)
        val metadata = payload.metadata.copy(checksum = checksum)
        val metadataJson = json.encodeToString(metadata)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            zos.putNextEntry(ZipEntry("data.json"))
            zos.write(dataJson.toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("metadata.json"))
            zos.write(metadataJson.toByteArray())
            zos.closeEntry()
        }
        return zipFile
    }
}
