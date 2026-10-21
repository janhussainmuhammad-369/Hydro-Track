package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WaterRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before performing Firestore operations.")
    }

    /**
     * Real-time observation of user profile and hydration preferences.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
    }

    /**
     * Real-time observation of water intake logs for the specified user.
     */
    fun observeWaterLogs(userId: String): Flow<List<WaterLog>> {
        val path = "users/$userId/waterLogs"
        return db.collection("users").document(userId).collection("waterLogs")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WaterLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp.now() }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Creates or updates the user's profile and reminder preferences.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val data = mapOf(
                    "displayName" to profile.displayName,
                    "email" to profile.email,
                    "dailyGoalMl" to profile.dailyGoalMl,
                    "unit" to profile.unit,
                    "reminderEnabled" to profile.reminderEnabled,
                    "reminderIntervalMinutes" to profile.reminderIntervalMinutes,
                    "reminderStartTime" to profile.reminderStartTime,
                    "reminderEndTime" to profile.reminderEndTime,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                docRef.update(data).await()
            } else {
                val data = mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName,
                    "email" to profile.email,
                    "dailyGoalMl" to profile.dailyGoalMl,
                    "unit" to profile.unit,
                    "reminderEnabled" to profile.reminderEnabled,
                    "reminderIntervalMinutes" to profile.reminderIntervalMinutes,
                    "reminderStartTime" to profile.reminderStartTime,
                    "reminderEndTime" to profile.reminderEndTime,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                docRef.set(data).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Logs a new water or beverage intake entry.
     */
    suspend fun addWaterLog(
        userId: String,
        amountMl: Long,
        beverageType: String,
        container: String,
        dateString: String
    ): Result<String> {
        val uid = requireUserId()
        require(uid == userId) { "Cannot log water for a different user." }
        val collectionRef = db.collection("users").document(uid).collection("waterLogs")
        return try {
            val payload = mapOf(
                "userId" to uid,
                "amountMl" to amountMl,
                "beverageType" to beverageType,
                "container" to container,
                "dateString" to dateString,
                "createdAt" to FieldValue.serverTimestamp()
            )
            val docRef = collectionRef.add(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, collectionRef.path)
            Result.failure(e)
        }
    }

    /**
     * Deletes a water intake log entry.
     */
    suspend fun deleteWaterLog(userId: String, logId: String): Result<Unit> {
        val uid = requireUserId()
        require(uid == userId) { "Cannot delete water log for another user." }
        val docRef = db.collection("users").document(uid).collection("waterLogs").document(logId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
