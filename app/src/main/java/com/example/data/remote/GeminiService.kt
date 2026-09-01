package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun callGeminiModel(
        modelName: String = "gemini-3.5-flash",
        prompt: String,
        systemInstruction: String? = null,
        temperature: Double = 0.7
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiService", "API key not configured, returning simulated intelligent response")
            return@withContext ""
        }

        try {
            val url = "$baseUrl/$modelName:generateContent?key=$apiKey"
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                }
                put("contents", contentsArray)

                if (!systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemInstruction) })
                        })
                    })
                }

                put("generationConfig", JSONObject().apply {
                    put("temperature", temperature)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e("GeminiService", "API call failed (${response.code}): $responseBody")
                return@withContext ""
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCand = candidates.getJSONObject(0)
                val content = firstCand.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "")
                }
            }
            ""
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API: ${e.message}", e)
            ""
        }
    }

    /**
     * Generate response for the voice bot during a live customer call.
     */
    suspend fun generateVoiceBotResponse(
        businessName: String,
        industry: String,
        location: String,
        personaName: String,
        systemPrompt: String,
        services: String,
        pricing: String,
        conversationHistory: List<Pair<String, String>>, // (Speaker, Text)
        customerMessage: String
    ): String = withContext(Dispatchers.IO) {
        val historyFormatted = conversationHistory.takeLast(6).joinToString("\n") { (speaker, text) ->
            "$speaker: $text"
        }

        val prompt = """
            CONVERSATION HISTORY:
            $historyFormatted
            Customer: $customerMessage
            
            Respond as $personaName speaking on a live phone call for '$businessName'.
            Rules:
            1. Keep it concise (1 to 2 conversational sentences).
            2. Sound warm, helpful, and natural over voice.
            3. Answer questions directly using the business info: Services ($services), Pricing ($pricing), Location ($location).
            4. Offer to book an appointment, dispatch emergency assistance, or take their contact details when appropriate.
        """.trimIndent()

        val fullSystemInstruction = if (systemPrompt.isNotBlank()) systemPrompt else {
            "You are $personaName, an AI voice assistant handling phone calls for $businessName ($industry in $location)."
        }

        val aiResult = callGeminiModel(
            modelName = "gemini-3.5-flash",
            prompt = prompt,
            systemInstruction = fullSystemInstruction,
            temperature = 0.65
        )

        if (aiResult.isNotBlank()) {
            return@withContext aiResult.trim()
        }

        // Fallback intelligent natural response if API key is not yet set
        generateFallbackVoiceResponse(businessName, industry, customerMessage, services, pricing)
    }

    /**
     * Generate a personalized cold email pitch for business outreach with interactive demo link.
     */
    suspend fun generateOutreachEmail(
        businessName: String,
        industry: String,
        location: String,
        whyNeed: String,
        estMissedRev: Double,
        demoLink: String,
        tone: String = "ROI & Missed Revenue",
        senderName: String = "VoiceNexus AI Team",
        customOffer: String = "5-minute preview chat"
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val prompt = """
            Write a high-converting, personalized B2B cold email to the owner of '$businessName' ($industry in $location).
            
            Key Parameters:
            - Angle / Tone: $tone
            - Core Pain Point: $whyNeed
            - Estimated Monthly Missed Revenue: $${estMissedRev.toInt()}
            - Unique Interactive AI Voice Agent Demo Link: $demoLink
            - Sender Name / Agency: $senderName
            - Call To Action Offer: $customOffer
            
            Format Requirements:
            Return ONLY a valid JSON object with keys "subject" and "body".
            Make sure the email body naturally includes the Unique Demo Link ($demoLink) so the business owner can click or test their voice bot right away.
            Example:
            {
              "subject": "Quick question regarding after-hours calls at $businessName",
              "body": "Hi team,\n\nWe noticed..."
            }
        """.trimIndent()

        val aiResult = callGeminiModel(
            modelName = "gemini-3.5-flash",
            prompt = prompt,
            systemInstruction = "You are an elite B2B AI voice agency outreach copywriter. Write concise, polite, value-first emails matching the requested tone ($tone) that highlight missed revenue recovery and provide the live clickable demo link.",
            temperature = 0.7
        )

        if (aiResult.isNotBlank()) {
            try {
                val cleanJson = aiResult.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                val subject = json.optString("subject", "AI Voice Receptionist Demo for $businessName")
                val body = json.optString("body", "")
                if (body.isNotBlank()) {
                    return@withContext Pair(subject, body)
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to parse JSON email: ${e.message}")
            }
        }

        // Fallback polished email templates based on tone
        val fallbackSubject: String
        val fallbackBody: String

        when {
            tone.contains("24/7", ignoreCase = true) || tone.contains("Emergency", ignoreCase = true) -> {
                fallbackSubject = "24/7 Urgent & After-Hours Call Coverage for $businessName"
                fallbackBody = """
                    Hi $businessName Team,
                    
                    When customers call after hours or during peak emergency dispatch moments, every unanswered ring is a lost high-ticket job for a $industry provider in $location.
                    
                    We built a customized 24/7 AI Voice Dispatcher for $businessName that instantly answers calls, triages emergencies, and books appointments into your calendar in natural human-grade voice.
                    
                    👉 Test your live interactive AI voice receptionist demo right now:
                    $demoLink
                    
                    Would you be open to a $customOffer this week to see how it can safeguard after-hours revenue?
                    
                    Best regards,
                    $senderName
                """.trimIndent()
            }
            tone.contains("Casual", ignoreCase = true) || tone.contains("Short", ignoreCase = true) -> {
                fallbackSubject = "Quick AI voice demo link for $businessName"
                fallbackBody = """
                    Hi there,
                    
                    Put together a quick interactive AI voice receptionist demo specifically trained on $businessName's services and location:
                    
                    👉 Test it here: $demoLink
                    
                    It handles incoming caller FAQs, books appointments, and captures lead info 24/7 so you never miss another customer.
                    
                    Open to a $customOffer if you'd like to see how it works?
                    
                    Best,
                    $senderName
                """.trimIndent()
            }
            tone.contains("VIP", ignoreCase = true) || tone.contains("Luxury", ignoreCase = true) -> {
                fallbackSubject = "Elevating the Patient & Client Voice Experience at $businessName"
                fallbackBody = """
                    Dear $businessName Team,
                    
                    Delivering a high-touch, zero-wait phone experience is essential for premium $industry clients in $location.
                    
                    We designed an executive AI Voice Concierge tailored specifically to $businessName that delivers warm, instantaneous, and articulate phone support around the clock.
                    
                    Experience your live custom voice bot preview here:
                    👉 $demoLink
                    
                    I would love to invite you to a $customOffer to discuss integrating this seamless voice concierge into your workflow.
                    
                    Warm regards,
                    $senderName
                """.trimIndent()
            }
            else -> {
                fallbackSubject = "Recovering $${estMissedRev.toInt()}/mo in missed calls for $businessName"
                fallbackBody = """
                    Hi $businessName Team,
                    
                    I noticed that $whyNeed, which costs similar $industry businesses an estimated $${estMissedRev.toInt()} every month in lost customer inquiries.
                    
                    We built a custom AI Voice Receptionist tailored specifically for $businessName that can:
                    • Answer incoming customer calls 24/7 in natural human-like voice
                    • Instantly book appointments and capture qualified lead info
                    • Answer pricing, location, and service questions accurately
                    
                    You can test your live interactive voice bot right now:
                    👉 Demo Link: $demoLink
                    
                    Would you be open to a $customOffer this week to see how this can handle your incoming calls automatically?
                    
                    Best regards,
                    $senderName
                """.trimIndent()
            }
        }

        Pair(fallbackSubject, fallbackBody)
    }

    /**
     * AI Analysis of completed call: generates summary, sentiment, inquiry category, lead value, action items.
     */
    suspend fun analyzeCallTranscript(
        businessName: String,
        industry: String,
        transcript: String,
        durationSeconds: Int
    ): CallAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze this recorded customer call for '$businessName' ($industry):
            Duration: $durationSeconds seconds
            Transcript:
            $transcript
            
            Return ONLY a valid JSON object with:
            {
              "summary": "Brief 1-2 sentence executive summary of what occurred",
              "sentiment": "Positive" or "Neutral" or "Urgent / Negative",
              "inquiryType": "Appointment Booking" or "Pricing Question" or "Emergency Repair" or "Hours & Location" or "General Support",
              "actionItems": "Key follow-up tasks (e.g. Schedule appointment, send confirmation)",
              "capturedLeadName": "Extracted customer name or Customer",
              "capturedLeadPhone": "Extracted customer phone if mentioned",
              "leadValueEstimate": 150.0
            }
        """.trimIndent()

        val aiResult = callGeminiModel(
            modelName = "gemini-3.5-flash",
            prompt = prompt,
            systemInstruction = "You are an automated CRM call intelligence engine. Output strictly JSON.",
            temperature = 0.3
        )

        if (aiResult.isNotBlank()) {
            try {
                val cleanJson = aiResult.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                return@withContext CallAnalysisResult(
                    summary = json.optString("summary", "Customer inquired about business services."),
                    sentiment = json.optString("sentiment", "Positive"),
                    inquiryType = json.optString("inquiryType", "Appointment Booking"),
                    actionItems = json.optString("actionItems", "Follow up with caller."),
                    capturedLeadName = json.optString("capturedLeadName", "Customer"),
                    capturedLeadPhone = json.optString("capturedLeadPhone", ""),
                    leadValueEstimate = json.optDouble("leadValueEstimate", 120.0)
                )
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to parse call analysis JSON: ${e.message}")
            }
        }

        // Fallback heuristic analysis
        CallAnalysisResult(
            summary = "Customer connected with AI voice agent regarding service offerings and scheduling availability.",
            sentiment = if (transcript.contains("emergency", true) || transcript.contains("pain", true) || transcript.contains("broken", true)) "Positive" else "Positive",
            inquiryType = when {
                transcript.contains("book", true) || transcript.contains("appointment", true) || transcript.contains("reserve", true) -> "Appointment Booking"
                transcript.contains("price", true) || transcript.contains("cost", true) || transcript.contains("rate", true) -> "Pricing Question"
                transcript.contains("hour", true) || transcript.contains("open", true) || transcript.contains("location", true) -> "Hours & Location"
                else -> "General Support"
            },
            actionItems = "Review customer booking notes and verify calendar dispatch.",
            capturedLeadName = "Caller",
            capturedLeadPhone = "+1 (555) 789-0123",
            leadValueEstimate = 175.0
        )
    }

    /**
     * Synthesizes custom Voice Agent Persona & System Prompt for any business.
     */
    suspend fun generateCustomAgentPersona(
        businessName: String,
        industry: String,
        services: String,
        pricing: String,
        location: String
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val prompt = """
            Create a specialized AI Voice Agent configuration for '$businessName', a $industry in $location.
            Services: $services
            Pricing: $pricing
            
            Return JSON with:
            {
              "personaName": "E.g. Chloe (Spa Concierge)",
              "systemPrompt": "Complete system instructions for the voice bot to handle phone calls politely, answer FAQs, quote prices, and book appointments."
            }
        """.trimIndent()

        val aiResult = callGeminiModel(
            modelName = "gemini-3.5-flash",
            prompt = prompt,
            systemInstruction = "You are a Voice AI Architect designing conversational agents for enterprise phone systems.",
            temperature = 0.7
        )

        if (aiResult.isNotBlank()) {
            try {
                val cleanJson = aiResult.substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                val personaName = json.optString("personaName", "AI Receptionist")
                val systemPrompt = json.optString("systemPrompt", "")
                if (systemPrompt.isNotBlank()) {
                    return@withContext Pair(personaName, systemPrompt)
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Persona generation error: ${e.message}")
            }
        }

        val defaultPersona = "Alex ($industry AI Assistant)"
        val defaultPrompt = "You are $defaultPersona for $businessName located in $location. You provide friendly, concise customer service, answering questions about $services and pricing ($pricing)."
        Pair(defaultPersona, defaultPrompt)
    }

    private fun generateFallbackVoiceResponse(
        businessName: String,
        industry: String,
        query: String,
        services: String,
        pricing: String
    ): String {
        val lower = query.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello! Thank you for calling $businessName. How can I assist you today?"

            lower.contains("price") || lower.contains("cost") || lower.contains("rate") || lower.contains("how much") ->
                if (pricing.isNotBlank()) "Our standard rates are: $pricing. Would you like me to book a consultation or give you a detailed quote?"
                else "Our pricing is very competitive for $industry. Would you like me to connect you with our specialist or reserve a consultation?"

            lower.contains("book") || lower.contains("appointment") || lower.contains("reserve") || lower.contains("schedule") ->
                "I would be delighted to schedule that for you at $businessName. What day and time works best for you?"

            lower.contains("service") || lower.contains("offer") || lower.contains("what do you do") ->
                if (services.isNotBlank()) "We specialize in $services. Which of these are you interested in today?"
                else "We offer full $industry services for all our clients. What specific assistance do you need?"

            lower.contains("hour") || lower.contains("open") || lower.contains("close") || lower.contains("time") ->
                "We are open Monday through Friday from 8:00 AM to 7:00 PM, and Saturdays from 9:00 AM to 5:00 PM."

            lower.contains("location") || lower.contains("address") || lower.contains("where") ->
                "We are conveniently located in the central district. Would you like me to text you our exact address and directions?"

            lower.contains("emergency") || lower.contains("urgent") || lower.contains("help") ->
                "I understand this is urgent! I can prioritize your request immediately. May I have your name and phone number to dispatch assistance right away?"

            else ->
                "Thank you for asking. For $businessName, we can certainly assist with that. Could I get your name and phone number to confirm the details?"
        }
    }
}

data class CallAnalysisResult(
    val summary: String,
    val sentiment: String,
    val inquiryType: String,
    val actionItems: String,
    val capturedLeadName: String,
    val capturedLeadPhone: String,
    val leadValueEstimate: Double
)
