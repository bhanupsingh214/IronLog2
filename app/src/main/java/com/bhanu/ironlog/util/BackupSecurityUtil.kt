package com.bhanu.ironlog.util

import java.security.MessageDigest

object BackupSecurityUtil {
    fun calculateChecksum(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
