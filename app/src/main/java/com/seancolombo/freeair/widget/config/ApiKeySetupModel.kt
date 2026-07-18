package com.seancolombo.freeair.widget.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class ApiKeySetupStatus {
    data object Idle : ApiKeySetupStatus()
    data object Verifying : ApiKeySetupStatus()
    data object Success : ApiKeySetupStatus()
    data class Failure(val message: String) : ApiKeySetupStatus()
}

/**
 * Plain state holder for the "add your PurpleAir API key" screen -- not an Android ViewModel,
 * just observable state plus verify/save logic, so it's testable with fakes and no
 * Activity/Context. Verifying and persisting are separate injected functions so a test can check
 * each independently (e.g. that a failed check never calls [onVerified]).
 */
class ApiKeySetupModel(
    private val checkApiKey: suspend (String) -> Result<Unit>,
    private val onVerified: suspend (String) -> Unit,
) {
    var apiKey by mutableStateOf("")
        private set

    var status by mutableStateOf<ApiKeySetupStatus>(ApiKeySetupStatus.Idle)
        private set

    val canVerify: Boolean
        get() = apiKey.isNotBlank() && status !is ApiKeySetupStatus.Verifying

    fun onApiKeyChanged(value: String) {
        apiKey = value
        // Clear a stale error as soon as they start fixing it, rather than leaving last
        // attempt's failure message sitting under a key they've already changed.
        if (status is ApiKeySetupStatus.Failure) {
            status = ApiKeySetupStatus.Idle
        }
    }

    suspend fun verify() {
        if (!canVerify) return
        status = ApiKeySetupStatus.Verifying
        val trimmed = apiKey.trim()
        status = checkApiKey(trimmed).fold(
            onSuccess = {
                onVerified(trimmed)
                ApiKeySetupStatus.Success
            },
            onFailure = {
                ApiKeySetupStatus.Failure(
                    "That API key didn't work. Double-check it, or go back to PurpleAir to get a new one.",
                )
            },
        )
    }
}
