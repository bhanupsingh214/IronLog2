package com.bhanu.ironlog.data.service

import android.content.Context
import android.net.Uri
import com.bhanu.ironlog.data.local.backup.BackupMetadata
import com.bhanu.ironlog.data.local.backup.BackupPayload
import com.bhanu.ironlog.util.BackupSecurityUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Validates and parses the .ironlog backup file from a Uri.
     */
    fun parseBackup(uri: Uri): BackupPayload {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Failed to open backup file")
        return parseBackup(inputStream)
    }

    /**
     * Validates and parses the .ironlog backup file from a File.
     */
    fun parseBackup(file: File): BackupPayload {
        return parseBackup(file.inputStream())
    }

    /**
     * Validates and parses the .ironlog backup file from an InputStream.
     */
    private fun parseBackup(inputStream: InputStream): BackupPayload {
        var dataJson: String? = null
        var metadataJson: String? = null

        inputStream.use { stream ->
            ZipInputStream(stream).use { zis ->
                var entry = zis.getNextEntry()
                while (entry != null) {
                    when (entry.name) {
                        "data.json" -> dataJson = zis.bufferedReader().readText()
                        "metadata.json" -> metadataJson = zis.bufferedReader().readText()
                    }
                    zis.closeEntry()
                    entry = zis.getNextEntry()
                }
            }
        }

        if (dataJson == null || metadataJson == null) {
            throw Exception("Invalid backup file: missing data or metadata")
        }

        val metadata = json.decodeFromString<BackupMetadata>(metadataJson!!)

        // 1. Version Check
        if (metadata.version != 1) {
            throw Exception("Unsupported backup version: ${metadata.version}")
        }

        // 2. Checksum Verification
        val calculatedChecksum = BackupSecurityUtil.calculateChecksum(dataJson!!)
        if (calculatedChecksum != metadata.checksum) {
            throw Exception("Backup integrity check failed: checksum mismatch")
        }

        // 3. Parse Payload
        return json.decodeFromString<BackupPayload>(dataJson!!)
    }
}
