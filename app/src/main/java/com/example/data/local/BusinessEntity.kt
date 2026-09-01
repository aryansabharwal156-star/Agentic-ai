package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val industry: String,
    val countryCity: String,
    val phone: String,
    val email: String,
    val website: String,
    val whyNeedVoiceBot: String,
    val rating: Int = 5,
    val status: String = "new", // new, demo_sent, demo_opened, demo_booked, closed_won
    val estMonthlyMissedRev: Double = 5000.0,
    val accountNotes: String = "",
    val customHours: String = "Mon-Fri: 9:00 AM - 6:00 PM, Sat: 10:00 AM - 4:00 PM, Sun: Closed",
    val servicesList: String = "",
    val pricingGuide: String = "",
    val botPersonaName: String = "Alex (AI Receptionist)",
    val botVoicePitch: Float = 1.0f,
    val botVoiceSpeed: Float = 1.0f,
    val systemPrompt: String = "",
    val demoCode: String = "",
    val emailSentCount: Int = 0,
    val lastContactedDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
