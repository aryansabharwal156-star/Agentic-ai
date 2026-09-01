package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BusinessEntity
import com.example.data.local.CallLogEntity
import com.example.data.local.OutreachLogEntity
import com.example.data.local.SeedData
import com.example.data.remote.CallAnalysisResult
import com.example.data.remote.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BusinessRepository(
    private val database: AppDatabase,
    private val geminiService: GeminiService = GeminiService()
) {
    private val businessDao = database.businessDao()
    private val callLogDao = database.callLogDao()
    private val outreachDao = database.outreachDao()

    val allBusinesses: Flow<List<BusinessEntity>> = businessDao.getAllBusinesses()
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()
    val allOutreachLogs: Flow<List<OutreachLogEntity>> = outreachDao.getAllOutreachLogs()

    suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
        if (businessDao.getCount() == 0) {
            val initial = SeedData.getInitialBusinesses()
            businessDao.insertAll(initial)
            val sampleCalls = SeedData.getInitialSampleCallLogs()
            callLogDao.insertAll(sampleCalls)
        }
    }

    fun getBusinessFlow(id: Long): Flow<BusinessEntity?> = businessDao.getBusinessByIdFlow(id)

    suspend fun getBusinessById(id: Long): BusinessEntity? = withContext(Dispatchers.IO) {
        businessDao.getBusinessById(id)
    }

    suspend fun getBusinessByDemoCode(code: String): BusinessEntity? = withContext(Dispatchers.IO) {
        businessDao.getBusinessByDemoCode(code)
    }

    fun getCallLogsForBusiness(businessId: Long): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogsForBusiness(businessId)

    fun getOutreachForBusiness(businessId: Long): Flow<List<OutreachLogEntity>> =
        outreachDao.getOutreachForBusiness(businessId)

    suspend fun insertBusiness(business: BusinessEntity): Long = withContext(Dispatchers.IO) {
        businessDao.insert(business)
    }

    suspend fun updateBusiness(business: BusinessEntity) = withContext(Dispatchers.IO) {
        businessDao.update(business)
    }

    suspend fun deleteBusiness(business: BusinessEntity) = withContext(Dispatchers.IO) {
        businessDao.delete(business)
    }

    suspend fun deleteBusinessById(id: Long) = withContext(Dispatchers.IO) {
        businessDao.deleteById(id)
    }

    suspend fun recordCallLog(callLog: CallLogEntity): Long = withContext(Dispatchers.IO) {
        callLogDao.insert(callLog)
    }

    suspend fun recordOutreachEmail(outreach: OutreachLogEntity) = withContext(Dispatchers.IO) {
        outreachDao.insert(outreach)
        businessDao.recordEmailSent(
            businessId = outreach.businessId,
            newStatus = "demo_sent",
            timestamp = outreach.sentTimestamp
        )
    }

    // Gemini Integrations
    suspend fun generateVoiceBotResponse(
        business: BusinessEntity,
        conversationHistory: List<Pair<String, String>>,
        customerMessage: String
    ): String = withContext(Dispatchers.IO) {
        geminiService.generateVoiceBotResponse(
            businessName = business.name,
            industry = business.industry,
            location = business.countryCity,
            personaName = business.botPersonaName,
            systemPrompt = business.systemPrompt,
            services = business.servicesList,
            pricing = business.pricingGuide,
            conversationHistory = conversationHistory,
            customerMessage = customerMessage
        )
    }

    suspend fun generateOutreachPitch(
        business: BusinessEntity,
        demoLink: String,
        tone: String = "ROI & Missed Revenue",
        senderName: String = "VoiceNexus AI Team",
        customOffer: String = "5-minute preview chat"
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        geminiService.generateOutreachEmail(
            businessName = business.name,
            industry = business.industry,
            location = business.countryCity,
            whyNeed = business.whyNeedVoiceBot,
            estMissedRev = business.estMonthlyMissedRev,
            demoLink = demoLink,
            tone = tone,
            senderName = senderName,
            customOffer = customOffer
        )
    }

    suspend fun analyzeCall(
        business: BusinessEntity,
        transcript: String,
        durationSeconds: Int
    ): CallAnalysisResult = withContext(Dispatchers.IO) {
        geminiService.analyzeCallTranscript(
            businessName = business.name,
            industry = business.industry,
            transcript = transcript,
            durationSeconds = durationSeconds
        )
    }

    suspend fun generateAgentPersona(
        name: String,
        industry: String,
        services: String,
        pricing: String,
        location: String
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        geminiService.generateCustomAgentPersona(name, industry, services, pricing, location)
    }

    companion object {
        @Volatile
        private var INSTANCE: BusinessRepository? = null

        fun getInstance(context: Context, scope: CoroutineScope): BusinessRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context, scope)
                val instance = BusinessRepository(db)
                INSTANCE = instance
                instance
            }
        }
    }
}
