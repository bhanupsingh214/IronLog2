package com.bhanu.ironlog.data.repository.impl

import android.content.Context
import android.content.Intent
import com.bhanu.ironlog.data.local.PreferenceStorage
import com.bhanu.ironlog.data.model.cloud.CloudResult
import com.bhanu.ironlog.data.repository.AccountRepository
import com.bhanu.ironlog.data.repository.AccountState
import com.bhanu.ironlog.data.service.impl.GoogleAuthService
import com.google.android.gms.auth.api.identity.AuthorizationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val preferenceStorage: PreferenceStorage,
    private val googleAuthService: GoogleAuthService
) : AccountRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isDriveAuthorized = MutableStateFlow(false)
    private var currentAccessToken: String? = null

    override val accountState: StateFlow<AccountState> = combine(
        preferenceStorage.userEmail,
        preferenceStorage.userDisplayName,
        _isDriveAuthorized
    ) { email, displayName, authorized ->
        if (email != null) {
            AccountState.SignedIn(email, displayName, authorized)
        } else {
            AccountState.SignedOut
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountState.Loading
    )

    init {
        repositoryScope.launch {
            if (preferenceStorage.getUserEmailOnce() != null) {
                refreshDriveAuthorization()
            }
        }
    }

    override suspend fun signIn(activityContext: Context): CloudResult<Unit> {
        val result = googleAuthService.signIn(activityContext)
        return when (result) {
            is CloudResult.Success -> {
                preferenceStorage.setUserEmail(result.data)
                refreshDriveAuthorization()
                CloudResult.Success(Unit)
            }
            is CloudResult.Error -> {
                CloudResult.Error(result.message, result.throwable)
            }
        }
    }

    override suspend fun authorizeDrive(): CloudResult<AuthorizationResult> {
        val result = googleAuthService.authorizeDrive()
        if (result is CloudResult.Success) {
            updateAuthorization(result.data)
        }
        return result
    }

    override suspend fun processAuthorizationResult(intent: Intent?): CloudResult<Unit> {
        return try {
            val result = googleAuthService.getAuthorizationResultFromIntent(intent)
            updateAuthorization(result)
            if (currentAccessToken != null) {
                CloudResult.Success(Unit)
            } else {
                CloudResult.Error("Authorization failed: no access token")
            }
        } catch (e: Exception) {
            _isDriveAuthorized.value = false
            currentAccessToken = null
            CloudResult.Error(e.message ?: "Authorization process failed", e)
        }
    }

    private fun updateAuthorization(result: AuthorizationResult) {
        currentAccessToken = result.accessToken
        _isDriveAuthorized.value = currentAccessToken != null
    }

    override suspend fun getAccessToken(): String? {
        return currentAccessToken
    }

    private suspend fun refreshDriveAuthorization() {
        val result = googleAuthService.checkDriveAuthorization()
        if (result is CloudResult.Success) {
            updateAuthorization(result.data)
        } else {
            currentAccessToken = null
            _isDriveAuthorized.value = false
        }
    }

    override suspend fun signOut() {
        googleAuthService.signOut(currentAccessToken)
        preferenceStorage.setUserEmail(null)
        preferenceStorage.setUserDisplayName(null)
        currentAccessToken = null
        _isDriveAuthorized.value = false
    }

    override suspend fun refreshAuth(): CloudResult<Unit> {
        refreshDriveAuthorization()
        return CloudResult.Success(Unit)
    }
}
