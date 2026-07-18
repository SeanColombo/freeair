package com.seancolombo.freeair.widget.config

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeySetupModelTest {
    @Test
    fun `starts idle with a blank key`() {
        val model = ApiKeySetupModel(checkApiKey = { Result.success(Unit) }, onVerified = {})

        assertEquals("", model.apiKey)
        assertEquals(ApiKeySetupStatus.Idle, model.status)
    }

    @Test
    fun `a blank key cannot be verified`() {
        val model = ApiKeySetupModel(checkApiKey = { Result.success(Unit) }, onVerified = {})

        model.onApiKeyChanged("   ")

        assertFalse(model.canVerify)
    }

    @Test
    fun `a successful check trims the key, persists it, and reports success`() = runTest {
        var verifiedWith: String? = null
        val model = ApiKeySetupModel(
            checkApiKey = { Result.success(Unit) },
            onVerified = { verifiedWith = it },
        )
        model.onApiKeyChanged("  my-key  ")

        model.verify()

        assertEquals("my-key", verifiedWith)
        assertEquals(ApiKeySetupStatus.Success, model.status)
    }

    @Test
    fun `a failed check does not persist the key and reports failure`() = runTest {
        var onVerifiedCalled = false
        val model = ApiKeySetupModel(
            checkApiKey = { Result.failure(RuntimeException("HTTP 403")) },
            onVerified = { onVerifiedCalled = true },
        )
        model.onApiKeyChanged("bad-key")

        model.verify()

        assertFalse(onVerifiedCalled)
        assertTrue(model.status is ApiKeySetupStatus.Failure)
    }

    @Test
    fun `editing the key after a failure clears the error`() = runTest {
        val model = ApiKeySetupModel(
            checkApiKey = { Result.failure(RuntimeException("HTTP 403")) },
            onVerified = {},
        )
        model.onApiKeyChanged("bad-key")
        model.verify()
        assertTrue(model.status is ApiKeySetupStatus.Failure)

        model.onApiKeyChanged("bad-key-2")

        assertEquals(ApiKeySetupStatus.Idle, model.status)
    }

    @Test
    fun `verify does nothing when the key is blank`() = runTest {
        var checkCallCount = 0
        val model = ApiKeySetupModel(
            checkApiKey = {
                checkCallCount++
                Result.success(Unit)
            },
            onVerified = {},
        )

        model.verify()

        assertEquals(0, checkCallCount)
        assertEquals(ApiKeySetupStatus.Idle, model.status)
    }
}
