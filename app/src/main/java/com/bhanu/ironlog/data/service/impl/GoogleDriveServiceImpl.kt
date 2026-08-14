package com.bhanu.ironlog.data.service.impl

import com.bhanu.ironlog.data.local.PreferenceStorage
import com.bhanu.ironlog.data.model.cloud.CloudResult
import com.bhanu.ironlog.data.repository.AccountRepository
import com.bhanu.ironlog.data.service.CloudStorageService
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.GenericData
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream

@Singleton
class GoogleDriveServiceImpl @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val accountRepository: AccountRepository
) : CloudStorageService {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport = NetHttpTransport()

    private suspend fun getDriveService(): Drive? {
        val accessToken = accountRepository.getAccessToken() ?: return null
        val googleCredentials = GoogleCredentials.create(AccessToken(accessToken, null))

        return Drive.Builder(transport, jsonFactory, HttpCredentialsAdapter(googleCredentials))
            .setApplicationName("IronLog")
            .build()
    }

    override suspend fun isAuthorized(): Boolean {
        return accountRepository.getAccessToken() != null
    }

    override suspend fun uploadBackup(file: File): CloudResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService() ?: return@withContext CloudResult.Error("Google Drive is not authorized")

            // 1. Prepare metadata
            val metadata = com.google.api.services.drive.model.File()
            metadata.set("name", file.name)
            metadata.set("parents", listOf("appDataFolder"))

            // 2. Prepare content
            val content = FileContent("application/octet-stream", file)

            // 3. Search for existing backup to replace or create new one
            val fileList: FileList = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '${file.name}' and trashed = false")
                .execute()

            val files = fileList.files
            val existingFile = files?.firstOrNull()

            if (existingFile != null) {
                val existingFileId = (existingFile as GenericData).get("id").toString()
                drive.files().update(existingFileId, null, content).execute()
            } else {
                drive.files().create(metadata, content).execute()
            }

            preferenceStorage.setLastCloudBackupTimestamp(System.currentTimeMillis())
            CloudResult.Success(Unit)
        } catch (e: Exception) {
            CloudResult.Error(e.message ?: "Cloud upload failed", e)
        }
    }

    override suspend fun downloadBackup(fileName: String, targetFile: File): CloudResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService() ?: return@withContext CloudResult.Error("Google Drive is not authorized")

            // 1. Search for the backup file
            val fileList: FileList = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$fileName' and trashed = false")
                .execute()

            val files = fileList.files
            val backupFile = files?.firstOrNull() ?: return@withContext CloudResult.Error("No cloud backup found")
            val fileId = (backupFile as GenericData).get("id").toString()

            // 2. Download the file
            FileOutputStream(targetFile).use { outputStream ->
                drive.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream)
            }

            CloudResult.Success(Unit)
        } catch (e: Exception) {
            CloudResult.Error(e.message ?: "Cloud download failed", e)
        }
    }
}
