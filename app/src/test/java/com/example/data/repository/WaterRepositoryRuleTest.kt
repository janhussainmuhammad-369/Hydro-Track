package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class WaterRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticated_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = WaterRepository(firestore)

        val profile = UserProfile(
            userId = uid,
            displayName = "Alice Test",
            dailyGoalMl = 2200L,
            unit = "ml"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(profile)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun addWaterLog_authenticated_createsDocumentAndReturnsId() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = WaterRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addWaterLog(
                userId = uid,
                amountMl = 300L,
                beverageType = "Water",
                container = "Glass",
                dateString = "2026-10-21"
            )
        }
        assertTrue(result.isSuccess)
        val logId = result.getOrThrow()
        assertTrue(logId.isNotBlank())
    }

    @Test
    fun observeWaterLogs_authenticated_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = WaterRepository(firestore)

        val logId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addWaterLog(
                userId = uid,
                amountMl = 500L,
                beverageType = "Water",
                container = "Bottle",
                dateString = "2026-10-21"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeWaterLogs(uid).first { list -> list.any { it.id == logId } }
        }
        assertTrue(emitted.any { it.id == logId })
    }

    @Test
    fun observeWaterLogs_unauthenticated_failsWithPermissionDenied() = runBlocking {
        val uid = UUID.randomUUID().toString()
        auth.signOut()
        val repository = WaterRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeWaterLogs(uid).first()
            }
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            // Unauthenticated callers or repository fail-fast checks
            assertTrue(e is IllegalStateException || e is FirebaseFirestoreException)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_water@test.com"
        const val BOB_EMAIL = "bob_water@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
