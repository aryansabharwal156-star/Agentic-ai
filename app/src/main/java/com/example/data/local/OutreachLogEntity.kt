package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outreach_logs",
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
data class OutreachLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val businessId: Long,
    val recipientEmail: String,
    val subject: String,
    val emailBody: String,
    val demoLink: String,
    val status: String = "Sent", // Draft, Sent, Delivered, Opened, Demo Clicked
    val sentTimestamp: Long = System.currentTimeMillis()
)
