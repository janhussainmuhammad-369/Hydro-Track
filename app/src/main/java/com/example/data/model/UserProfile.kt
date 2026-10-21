package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val dailyGoalMl: Long = 2000L,
    val unit: String = "ml",
    val reminderEnabled: Boolean = true,
    val reminderIntervalMinutes: Long = 60L,
    val reminderStartTime: String = "08:00",
    val reminderEndTime: String = "22:00",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
