package com.bhanu.ironlog.data.service

import android.content.Context
import com.bhanu.ironlog.data.local.backup.BackupPayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Converts the payload to JSON, calculates checksum, and creates a ZIP archive.
     */
    fun createBackupZip(payload: BackupPayload): File {
        val tempDir = File(context.cacheDir, "backups").apply {
            if (!exists()) mkdirs()
        }
        val dataFile = File(tempDir, "data.json")
        val metadataFile = File(tempDir, "metadata.json")
        val zipFile = File(
            tempDir,
            "ironlog_backup_${System.currentTimeMillis()}.ironlog"
        )

        // 1. Serialize Data
        val dataJson = json.encodeToString(payload)
        dataFile.writeText(dataJson)

        // 2. Calculate Checksum
        val checksum = calculateChecksum(dataJson)

        // 3. Create Metadata
        val metadata = payload.metadata.copy(checksum = checksum)
        val metadataJson = json.encodeToString(metadata)
        metadataFile.writeText(metadataJson)

        // 4. Create ZIP
        ZipOutputStream(
            BufferedOutputStream(FileOutputStream(zipFile))
        ).use { zos ->
            addFileToZip(zos, dataFile, "data.json")
            addFileToZip(zos, metadataFile, "metadata.json")
        }

        // Cleanup temporary files
        dataFile.delete()
        metadataFile.delete()

        return zipFile
    }

    private fun calculateChecksum(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun addFileToZip(
        zos: ZipOutputStream,
        file: File,
        entryName: String
    ) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        file.inputStream().use { it.copyTo(zos) }
        zos.closeEntry()
    }
}
