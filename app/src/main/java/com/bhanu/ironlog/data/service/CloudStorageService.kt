package com.bhanu.ironlog.data.service

import com.bhanu.ironlog.data.model.cloud.CloudResult
import java.io.File

interface CloudStorageService {
    suspend fun uploadBackup(file: File): CloudResult<Unit>
    suspend fun downloadBackup(fileName: String, targetFile: File): CloudResult<Unit>
    suspend fun isAuthorized(): Boolean
}
