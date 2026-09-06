package com.example.kartavya.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Handles Google Sign-In via Credential Manager and Firebase Authentication.
 *
 * Uses the modern androidx.credentials API instead of the deprecated
 * GoogleSignInClient approach.
 */
object AuthRepository {

    private const val TAG = "AuthRepository"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /** Currently authenticated Firebase user, or null. */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Perform Google Sign-In using Credential Manager, then authenticate
     * with Firebase using the resulting ID token.
     *
     * @param context Activity or Application context.
     * @return The authenticated [FirebaseUser] on success.
     * @throws GetCredentialCancellationException if user cancelled the picker.
     * @throws GetCredentialException on credential retrieval failure.
     * @throws Exception on Firebase authentication failure.
     */
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Resolve the web client ID that google-services.json provides
            val webClientId = resolveWebClientId(context)
            if (webClientId.isNullOrBlank()) {
                return Result.failure(
                    IllegalStateException(
                        "Web Client ID not found. Please add your SHA-1 fingerprint in Firebase Console, " +
                        "re-download google-services.json, and replace the file in app/."
                    )
                )
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In")
            Result.failure(Exception("Sign-in was cancelled."))
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google credentials available", e)
            Result.failure(Exception("No eligible Google account found. Please ensure this machine's debug SHA-1 is added in Firebase Console."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error", e)
            Result.failure(Exception("Could not sign in with Google. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign-in error", e)
            Result.failure(Exception("Authentication failed. Please check your internet connection and try again."))
        }
    }

    /**
     * Process the credential response and authenticate with Firebase.
     */
    private suspend fun handleSignInResult(result: GetCredentialResponse): Result<FirebaseUser> {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase returned null user after authentication."))

            Log.i(TAG, "Signed in as ${user.displayName} (${user.uid})")
            return Result.success(user)
        }

        return Result.failure(IllegalStateException("Unexpected credential type received."))
    }

    /**
     * Sign out from Firebase and clear Credential Manager state.
     */
    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing credential state", e)
        }
    }

    /**
     * Register a listener for authentication state changes.
     * Returns the listener so it can be removed later.
     */
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    /**
     * Resolve the web client ID from the auto-generated string resource.
     * The google-services plugin generates R.string.default_web_client_id
     * from the oauth_client entries in google-services.json.
     */
    private fun resolveWebClientId(context: Context): String? {
        val resId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        return if (resId != 0) {
            val value = context.getString(resId)
            if (value.isNotBlank()) value else null
        } else {
            null
        }
    }
}
