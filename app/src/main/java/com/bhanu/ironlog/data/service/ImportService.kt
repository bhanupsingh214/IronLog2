package com.bhanu.ironlog.data.service

import android.content.Context
import android.net.Uri
import android.util.Log
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
     * Validates and parses the .ironlog backup file.
     */
    fun parseBackup(uri: Uri): BackupPayload {
        Log.d("IronLogImportDebug", "3. ImportService.parseBackup() entered")
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Failed to open backup file")

        var dataJson: String? = null
        var metadataJson: String? = null

        Log.d("IronLogImportDebug", "4. ZIP validation started")
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.getNextEntry()
            while (entry != null) {
                Log.d("IronLogImportDebug", "Reading entry: ${entry.name}")
                when (entry.name) {
                    "data.json" -> dataJson = zis.bufferedReader().readText()
                    "metadata.json" -> metadataJson = zis.bufferedReader().readText()
                }
                zis.closeEntry()
                entry = zis.getNextEntry()
            }
        }

        if (dataJson == null || metadataJson == null) {
            throw Exception("Invalid backup file: missing data or metadata")
        }

        Log.d("IronLogImportDebug", "5. Attempting to parse metadata.json")
        val metadata = json.decodeFromString<BackupMetadata>(metadataJson!!)
        Log.d("IronLogImportDebug", "6. metadata.json successfully parsed. Version: ${metadata.version}")

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
        Log.d("IronLogImportDebug", "7. Attempting to parse data.json payload")
        val payload = json.decodeFromString<BackupPayload>(dataJson!!)
        Log.d("IronLogImportDebug", "7. parseBackup() successfully returned BackupPayload. Library size: ${payload.library.size}")
        return payload
    }
}
