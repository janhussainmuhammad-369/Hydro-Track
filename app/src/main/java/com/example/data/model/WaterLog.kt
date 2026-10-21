package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class WaterLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val amountMl: Long = 250L,
    val beverageType: String = "Water",
    val container: String = "Glass",
    val dateString: String = "", // e.g., "2026-10-21"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
