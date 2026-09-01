package com.example.ui.screens.demo

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.BusinessEntity
import com.example.data.local.CallLogEntity
import com.example.data.remote.CallAnalysisResult
import com.example.data.repository.BusinessRepository
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.components.SentimentChip
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryViolet
import com.example.ui.theme.WarningAmber
import com.example.voice.VoiceAgentManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class CallState {
    IDLE,
    RINGING,
    CONNECTED,
    ENDED_ANALYZING,
    CALL_SUMMARY
}

data class ChatTurn(val speaker: String, val text: String)

@Composable
fun VoiceAgentDemoScreen(
    business: BusinessEntity,
    repository: BusinessRepository,
    onNavigateToOutreach: (BusinessEntity) -> Unit,
    onNavigateToDashboard: (BusinessEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val voiceManager = remember {
        VoiceAgentManager(context, coroutineScope)
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.release()
        }
    }

    var callState by remember { mutableStateOf(CallState.IDLE) }
    var callDurationSeconds by remember { mutableIntStateOf(0) }
    val conversationHistory = remember { mutableStateListOf<ChatTurn>() }
    var textInput by remember { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }
    var isAiGenerating by remember { mutableStateOf(false) }
    var callAnalysis by remember { mutableStateOf<CallAnalysisResult?>(null) }
    val transcriptListState = rememberLazyListState()

    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val amplitudes by voiceManager.audioAmplitudes.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission needed for live voice", Toast.LENGTH_SHORT).show()
        }
    }

    // Connect voiceManager callback
    LaunchedEffect(business) {
        voiceManager.onSpeechRecognized = { spokenQuery ->
            if (spokenQuery.isNotBlank() && callState == CallState.CONNECTED) {
                coroutineScope.launch {
                    handleCustomerQuery(
                        query = spokenQuery,
                        business = business,
                        history = conversationHistory,
                        repository = repository,
                        voiceManager = voiceManager,
                        onLoadingChange = { isAiGenerating = it }
                    )
                }
            }
        }
    }

    // Call Duration timer
    LaunchedEffect(callState) {
        if (callState == CallState.CONNECTED) {
            callDurationSeconds = 0
            while (callState == CallState.CONNECTED) {
                delay(1000)
                callDurationSeconds++
            }
        }
    }

    // Auto-scroll transcript
    LaunchedEffect(conversationHistory.size) {
        if (conversationHistory.isNotEmpty()) {
            transcriptListState.animateScrollToItem(conversationHistory.size - 1)
        }
    }

    val demoLink = remember(business.id) { "voicebiz://demo?id=${business.id}" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Business Top Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${business.industry} • ${business.countryCity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Demo Code & Link Share
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryTeal.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Demo Link", demoLink))
                        Toast.makeText(context, "Demo link copied: $demoLink", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = business.demoCode.ifBlank { "DEMO-LINK" },
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        when (callState) {
            CallState.IDLE -> {
                IdleDemoStartView(
                    business = business,
                    demoLink = demoLink,
                    onStartCall = {
                        callState = CallState.RINGING
                        coroutineScope.launch {
                            delay(1500)
                            callState = CallState.CONNECTED
                            conversationHistory.clear()
                            val initialGreeting = "Hello! Thank you for calling ${business.name}. I'm ${business.botPersonaName}. How can I help you today?"
                            conversationHistory.add(ChatTurn(business.botPersonaName, initialGreeting))
                            voiceManager.speak(
                                text = initialGreeting,
                                pitch = business.botVoicePitch,
                                speed = business.botVoiceSpeed
                            )
                        }
                    },
                    onNavigateToOutreach = { onNavigateToOutreach(business) },
                    onNavigateToDashboard = { onNavigateToDashboard(business) }
                )
            }

            CallState.RINGING -> {
                RingingCallView(
                    business = business,
                    onAnswer = {
                        callState = CallState.CONNECTED
                        conversationHistory.clear()
                        val initialGreeting = "Hello! Thank you for calling ${business.name}. I'm ${business.botPersonaName}. How can I assist you?"
                        conversationHistory.add(ChatTurn(business.botPersonaName, initialGreeting))
                        voiceManager.speak(
                            text = initialGreeting,
                            pitch = business.botVoicePitch,
                            speed = business.botVoiceSpeed
                        )
                    },
                    onDecline = { callState = CallState.IDLE }
                )
            }

            CallState.CONNECTED -> {
                ActiveCallConversationView(
                    business = business,
                    durationSeconds = callDurationSeconds,
                    conversationHistory = conversationHistory,
                    transcriptListState = transcriptListState,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    isAiGenerating = isAiGenerating,
                    amplitudes = amplitudes,
                    textInput = textInput,
                    onTextInputChange = { textInput = it },
                    isMuted = isMuted,
                    onToggleMute = { isMuted = !isMuted },
                    onMicClick = {
                        val hasMic = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasMic) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) voiceManager.stopListening()
                            else voiceManager.startListening()
                        }
                    },
                    onSendTextQuery = { query ->
                        if (query.isNotBlank()) {
                            textInput = ""
                            coroutineScope.launch {
                                handleCustomerQuery(
                                    query = query,
                                    business = business,
                                    history = conversationHistory,
                                    repository = repository,
                                    voiceManager = voiceManager,
                                    onLoadingChange = { isAiGenerating = it }
                                )
                            }
                        }
                    },
                    onEndCall = {
                        voiceManager.stopSpeaking()
                        voiceManager.stopListening()
                        callState = CallState.ENDED_ANALYZING

                        coroutineScope.launch {
                            val transcriptText = conversationHistory.joinToString("\n") { "${it.speaker}: ${it.text}" }
                            val analysis = repository.analyzeCall(
                                business = business,
                                transcript = transcriptText,
                                durationSeconds = callDurationSeconds
                            )

                            // Save call log into Room Database
                            val transcriptJson = JSONArray().apply {
                                conversationHistory.forEach { turn ->
                                    put(JSONObject().apply {
                                        put("speaker", turn.speaker)
                                        put("text", turn.text)
                                    })
                                }
                            }.toString()

                            val callLog = CallLogEntity(
                                businessId = business.id,
                                callerName = analysis.capturedLeadName.ifBlank { "Demo Caller" },
                                callerPhone = analysis.capturedLeadPhone.ifBlank { "+1 (555) 345-6789" },
                                callDate = System.currentTimeMillis(),
                                durationSeconds = callDurationSeconds,
                                status = "completed",
                                inquiryType = analysis.inquiryType,
                                sentiment = analysis.sentiment,
                                summary = analysis.summary,
                                actionItems = analysis.actionItems,
                                capturedLeadName = analysis.capturedLeadName,
                                capturedLeadPhone = analysis.capturedLeadPhone,
                                leadValueEstimate = analysis.leadValueEstimate,
                                fullTranscriptJson = transcriptJson
                            )
                            repository.recordCallLog(callLog)
                            callAnalysis = analysis
                            callState = CallState.CALL_SUMMARY
                        }
                    }
                )
            }

            CallState.ENDED_ANALYZING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = PrimaryTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing Call Transcript with AI...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Extracting customer intent, sentiment & lead details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            CallState.CALL_SUMMARY -> {
                CallSummaryView(
                    business = business,
                    durationSeconds = callDurationSeconds,
                    analysis = callAnalysis,
                    onMakeAnotherCall = {
                        callState = CallState.IDLE
                    },
                    onViewDashboard = {
                        onNavigateToDashboard(business)
                    },
                    onSendOutreach = {
                        onNavigateToOutreach(business)
                    }
                )
            }
        }
    }
}

private suspend fun handleCustomerQuery(
    query: String,
    business: BusinessEntity,
    history: MutableList<ChatTurn>,
    repository: BusinessRepository,
    voiceManager: VoiceAgentManager,
    onLoadingChange: (Boolean) -> Unit
) {
    history.add(ChatTurn("Customer", query))
    onLoadingChange(true)

    val historyPairs = history.map { Pair(it.speaker, it.text) }
    val botResponse = repository.generateVoiceBotResponse(
        business = business,
        conversationHistory = historyPairs,
        customerMessage = query
    )

    onLoadingChange(false)
    history.add(ChatTurn(business.botPersonaName, botResponse))

    voiceManager.speak(
        text = botResponse,
        pitch = business.botVoicePitch,
        speed = business.botVoiceSpeed
    )
}

@Composable
private fun IdleDemoStartView(
    business: BusinessEntity,
    demoLink: String,
    onStartCall: () -> Unit,
    onNavigateToOutreach: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Glowing Bot Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(PrimaryTeal, Color(0xFF1E1B4B))
                        )
                    )
                    .border(3.dp, SecondaryCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = business.botPersonaName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Voice Assistant for ${business.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Knowledge Highlights Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configured Bot Capabilities:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    KnowledgeBullet("Services Knowledge", business.servicesList.ifBlank { "Full catalog coverage" })
                    KnowledgeBullet("Pricing Rules", business.pricingGuide.ifBlank { "Custom quote handling" })
                    KnowledgeBullet("Business Hours", business.customHours)
                    KnowledgeBullet("Goal", "Capture leads, book appointments & solve inquiries automatically")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive demo deep link card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Demo Link:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = demoLink,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "AI Voice Bot Demo for ${business.name}")
                                putExtra(Intent.EXTRA_TEXT, "Test the custom AI Voice Bot for ${business.name}: $demoLink")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Demo Link"))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Demo Link", tint = PrimaryTeal)
                    }
                }
            }
        }

        // Bottom CTA Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStartCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_demo_call_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Live Voice Demo Call", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToOutreach,
                    modifier = Modifier.weight(1f).testTag("demo_send_email_cta"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pitch Email")
                }

                OutlinedButton(
                    onClick = onNavigateToDashboard,
                    modifier = Modifier.weight(1f).testTag("demo_view_logs_cta"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Logs")
                }
            }
        }
    }
}

@Composable
private fun KnowledgeBullet(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = AccentEmerald,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RingingCallView(
    business: BusinessEntity,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = "INCOMING DEMO CALL",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = business.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = business.botPersonaName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulsing Avatar
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(PrimaryTeal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryTeal, TertiaryViolet)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Simulating incoming customer call...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Answer & Decline Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decline
            IconButton(
                onClick = onDecline,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ErrorRose)
                    .testTag("decline_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Decline",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Answer
            IconButton(
                onClick = onAnswer,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(AccentEmerald)
                    .testTag("answer_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Answer",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveCallConversationView(
    business: BusinessEntity,
    durationSeconds: Int,
    conversationHistory: List<ChatTurn>,
    transcriptListState: androidx.compose.foundation.lazy.LazyListState,
    isSpeaking: Boolean,
    isListening: Boolean,
    isAiGenerating: Boolean,
    amplitudes: List<Float>,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onMicClick: () -> Unit,
    onSendTextQuery: (String) -> Unit,
    onEndCall: () -> Unit
) {
    val quickQuestions = remember(business.industry) {
        when {
            business.industry.contains("Dental", true) -> listOf(
                "I have a toothache, do you have emergency slots?",
                "How much is routine teeth cleaning?",
                "Are you open on Saturdays?",
                "Where is your clinic located?"
            )
            business.industry.contains("Restaurant", true) || business.industry.contains("Café", true) -> listOf(
                "Can I book a table for 4 tonight at 7:30 PM?",
                "Do you have vegetarian or gluten-free options?",
                "What is your chef's tasting menu price?",
                "Can we host a private party here?"
            )
            business.industry.contains("Salon", true) || business.industry.contains("Spa", true) -> listOf(
                "Can I book a haircut and color for tomorrow?",
                "What are your massage packages and prices?",
                "Do you accept walk-ins?",
                "What are your opening hours?"
            )
            business.industry.contains("HVAC", true) || business.industry.contains("Plumb", true) -> listOf(
                "Our heating stopped working, can a tech come today?",
                "What is your diagnostic service fee?",
                "Do you provide emergency 24/7 dispatch?",
                "Can I schedule maintenance for next week?"
            )
            business.industry.contains("Real Estate", true) -> listOf(
                "I'm interested in viewing a property this weekend.",
                "What are your property management commission rates?",
                "Can I get a free home valuation estimate?",
                "Do you have rental listings available?"
            )
            else -> listOf(
                "What services do you offer?",
                "How much do you charge for a consultation?",
                "Can I schedule an appointment for this week?",
                "What are your operating hours and location?"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Call Status & Timer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Voice Call • ${formatDuration(durationSeconds)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSpeaking) PrimaryTeal.copy(alpha = 0.2f) else if (isListening) SecondaryCyan.copy(alpha = 0.2f) else Color.Transparent
            ) {
                Text(
                    text = if (isSpeaking) "AI Speaking..." else if (isListening) "Listening..." else if (isAiGenerating) "Thinking..." else "Connected",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSpeaking) PrimaryTeal else if (isListening) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Soundwave visualizer bar
        AudioWaveVisualizer(
            amplitudes = amplitudes,
            isActive = isSpeaking || isListening,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        )

        // Live Transcript Conversation Box
        LazyColumn(
            state = transcriptListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversationHistory) { turn ->
                val isBot = turn.speaker == business.botPersonaName || turn.speaker.contains("AI", true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isBot) 2.dp else 16.dp,
                            bottomEnd = if (isBot) 16.dp else 2.dp
                        ),
                        color = if (isBot) Color(0xFF1E293B) else PrimaryTeal,
                        border = if (isBot) BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f)) else null,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = turn.speaker,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isBot) SecondaryCyan else Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = turn.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (isAiGenerating) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryTeal,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${business.botPersonaName} is responding...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Customer Inquiries Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quick Customer Prompts:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickQuestions) { question ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSendTextQuery(question) }
                    ) {
                        Text(
                            text = question,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input & Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text Input Field
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextInputChange,
                placeholder = { Text("Speak or type inquiry...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("call_text_input"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    if (textInput.isNotBlank()) {
                        IconButton(onClick = { onSendTextQuery(textInput) }) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = PrimaryTeal
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Voice Mic Button
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListening) AccentEmerald else PrimaryTeal)
                    .testTag("live_mic_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                    contentDescription = if (isListening) "Listening..." else "Tap to Speak",
                    tint = Color.White
                )
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ErrorRose)
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun CallSummaryView(
    business: BusinessEntity,
    durationSeconds: Int,
    analysis: CallAnalysisResult?,
    onMakeAnotherCall: () -> Unit,
    onViewDashboard: () -> Unit,
    onSendOutreach: () -> Unit
) {
    val summary = analysis?.summary ?: "Call ended. Customer received assistance."
    val sentiment = analysis?.sentiment ?: "Positive"
    val inquiryType = analysis?.inquiryType ?: "Appointment Booking"
    val actionItems = analysis?.actionItems ?: "Follow up with customer confirmation."
    val leadValue = analysis?.leadValueEstimate ?: 150.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Call Completed & Saved",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentEmerald
                    )
                    Text(
                        text = "Duration: ${formatDuration(durationSeconds)} • ${business.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SentimentChip(sentiment = sentiment)
            }
        }

        item {
            // AI Analysis Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI CALL SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                        Text(
                            text = "Est. Lead Value: $${leadValue.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Inquiry Category: $inquiryType",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Action Required: $actionItems",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✓ Automatically synchronized to Owner's Call Logs & Analytics Dashboard",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Action Navigation CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onViewDashboard,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View in Owner Call Logs Dashboard", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSendOutreach,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCyan)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Email Demo Link to Business Owner", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMakeAnotherCall,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate Another Customer Call")
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
