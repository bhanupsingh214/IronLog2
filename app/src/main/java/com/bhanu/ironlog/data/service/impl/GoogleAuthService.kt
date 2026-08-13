package com.bhanu.ironlog.data.service.impl

import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.bhanu.ironlog.R
import com.bhanu.ironlog.data.local.PreferenceStorage
import com.bhanu.ironlog.data.model.cloud.CloudResult
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceStorage: PreferenceStorage
) {
    private val credentialManager = CredentialManager.create(context)
    private val authorizationClient = Identity.getAuthorizationClient(context)

    suspend fun signIn(activityContext: Context): CloudResult<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.google_drive_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    preferenceStorage.setUserEmail(googleIdTokenCredential.id)
                    preferenceStorage.setUserDisplayName(googleIdTokenCredential.displayName)
                    CloudResult.Success(googleIdTokenCredential.id)
                } catch (e: GoogleIdTokenParsingException) {
                    CloudResult.Error("Failed to parse Google ID token", e)
                }
            } else {
                CloudResult.Error("Unsupported credential type: ${credential.type}")
            }
        } catch (e: Exception) {
            CloudResult.Error(e.message ?: "Sign in failed", e)
        }
    }

    /**
     * Checks if the app is already authorized for Drive access.
     * Uses authorize() which is non-interactive if scopes are already granted.
     */
    suspend fun checkDriveAuthorization(): CloudResult<AuthorizationResult> {
        return try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                .build()

            val result = authorizationClient.authorize(request).await()
            if (result.accessToken != null) {
                CloudResult.Success(result)
            } else {
                CloudResult.Error("Authorization required")
            }
        } catch (e: Exception) {
            CloudResult.Error("Authorization required", e)
        }
    }

    /**
     * Initiates the authorization flow.
     * May return a result with a pendingIntent if user interaction is required.
     */
    suspend fun authorizeDrive(): CloudResult<AuthorizationResult> {
        return try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                .build()

            val result = authorizationClient.authorize(request).await()
            // Return success even if pendingIntent is present so the caller can launch it.
            CloudResult.Success(result)
        } catch (e: Exception) {
            CloudResult.Error(e.message ?: "Authorization failed", e)
        }
    }

    fun getAuthorizationResultFromIntent(intent: Intent?): AuthorizationResult {
        return authorizationClient.getAuthorizationResultFromIntent(intent)
    }

    suspend fun signOut(accessToken: String?) {
        accessToken?.let {
            try {
                // Use the explicit AuthorizationClient to clear the token from Google Play Services cache
                val request = ClearTokenRequest.builder().setToken(it).build()
                authorizationClient.clearToken(request).await()
            } catch (e: Exception) {
                // Ignore failure to clear token during sign out
            }
        }
        credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
    }
}
