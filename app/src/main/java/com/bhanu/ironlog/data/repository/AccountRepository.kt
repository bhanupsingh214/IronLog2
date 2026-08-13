package com.bhanu.ironlog.data.repository

import android.content.Context
import android.content.Intent
import com.bhanu.ironlog.data.model.cloud.CloudResult
import com.google.android.gms.auth.api.identity.AuthorizationResult
import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    val accountState: StateFlow<AccountState>
    suspend fun signIn(activityContext: Context): CloudResult<Unit>
    suspend fun authorizeDrive(): CloudResult<AuthorizationResult>
    suspend fun processAuthorizationResult(intent: Intent?): CloudResult<Unit>
    suspend fun getAccessToken(): String?
    suspend fun signOut()
    suspend fun refreshAuth(): CloudResult<Unit>
}

sealed class AccountState {
    object SignedOut : AccountState()
    data class SignedIn(
        val email: String,
        val displayName: String?,
        val isDriveAuthorized: Boolean = false
    ) : AccountState()
    object Loading : AccountState()
}
