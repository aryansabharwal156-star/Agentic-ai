package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_logs",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("businessId")]
)
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val businessId: Long,
    val callerName: String = "Customer",
    val callerPhone: String = "+1 (555) 234-5678",
    val callDate: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 65,
    val status: String = "completed", // completed, missed, transferred, voicemail
    val inquiryType: String = "Appointment Booking", // Booking, Pricing, Hours, Emergency, FAQs
    val sentiment: String = "Positive", // Positive, Neutral, Urgent / Negative
    val summary: String = "Customer inquired about appointment availability and pricing.",
    val actionItems: String = "Follow up with booking confirmation.",
    val capturedLeadName: String = "",
    val capturedLeadPhone: String = "",
    val capturedLeadEmail: String = "",
    val capturedLeadNotes: String = "",
    val leadValueEstimate: Double = 150.0,
    val fullTranscriptJson: String = "[]" // JSON string of speaker turns
)
