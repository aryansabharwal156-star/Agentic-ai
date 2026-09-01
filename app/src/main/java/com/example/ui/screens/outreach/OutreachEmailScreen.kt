package com.example.ui.screens.outreach

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessEntity
import com.example.data.local.OutreachLogEntity
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityDarkPurpleText
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySubBorder
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensitySurfaceVariant
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGreenText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OutreachViewMode(val title: String) {
    FORM("Edit & Customize"),
    PREVIEW("Live Email Preview")
}

@Composable
fun OutreachEmailScreen(
    businesses: List<BusinessEntity>,
    selectedBusinessId: Long?,
    repository: BusinessRepository,
    onLaunchDemoForBusiness: (BusinessEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeBusiness by remember(businesses, selectedBusinessId) {
        mutableStateOf(
            businesses.find { it.id == selectedBusinessId }
                ?: businesses.firstOrNull()
        )
    }

    var selectedViewMode by remember { mutableStateOf(OutreachViewMode.FORM) }

    // Generation settings
    var selectedTone by remember { mutableStateOf("ROI & Missed Revenue") }
    val toneOptions = remember {
        listOf(
            "ROI & Missed Revenue",
            "24/7 Emergency & Urgent",
            "Short & Casual",
            "VIP Experience"
        )
    }

    var senderName by remember { mutableStateOf("Alex Morgan | VoiceNexus AI") }
    var customOffer by remember { mutableStateOf("5-minute preview call") }
    var campaignTag by remember { mutableStateOf("direct_cold_pitch") }

    var subject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var isGeneratingEmail by remember { mutableStateOf(false) }
    var isEmailSentSuccess by remember { mutableStateOf(false) }
    var showHistorySection by remember { mutableStateOf(false) }

    val activeBusinessSafe = activeBusiness

    // Dynamic unique demo link with campaign tracking code
    val uniqueDemoLink = remember(activeBusinessSafe?.id, campaignTag, activeBusinessSafe?.demoCode) {
        val baseCode = if (!activeBusinessSafe?.demoCode.isNullOrBlank()) {
            activeBusinessSafe?.demoCode
        } else {
            "DEMO-${activeBusinessSafe?.id ?: 1}"
        }
        "voicebiz://demo?id=${activeBusinessSafe?.id ?: 1}&code=$baseCode&utm_campaign=$campaignTag"
    }

    // Outreach logs for current active business
    val outreachLogs: List<OutreachLogEntity> by repository
        .getOutreachForBusiness(activeBusinessSafe?.id ?: -1L)
        .collectAsState(initial = emptyList())

    // Auto-generate or update email whenever active business changes or tone changes
    fun triggerGeneration(forcedTone: String = selectedTone) {
        if (activeBusinessSafe != null) {
            isGeneratingEmail = true
            isEmailSentSuccess = false
            coroutineScope.launch {
                val (genSubject, genBody) = repository.generateOutreachPitch(
                    business = activeBusinessSafe,
                    demoLink = uniqueDemoLink,
                    tone = forcedTone,
                    senderName = senderName,
                    customOffer = customOffer
                )
                subject = genSubject
                emailBody = genBody
                isGeneratingEmail = false
            }
        }
    }

    LaunchedEffect(activeBusinessSafe?.id) {
        if (activeBusinessSafe != null) {
            triggerGeneration()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title & Description Header
        item {
            Column {
                Text(
                    text = "Outreach Email Generator & Preview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary
                )
                Text(
                    text = "Generate and preview high-converting cold pitches with unique interactive voice demo links",
                    style = MaterialTheme.typography.bodySmall,
                    color = HighDensityTextSecondary
                )
            }
        }

        // Business Selector Horizontal Strip
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT PROSPECTIVE BUSINESS:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${businesses.size} Prospects",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(businesses) { b ->
                        val isSelected = activeBusiness?.id == b.id
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) HighDensityPrimaryContainer else HighDensitySurface,
                            border = BorderStroke(1.dp, if (isSelected) HighDensityPrimary else HighDensityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    activeBusiness = b
                                }
                                .testTag("select_outreach_biz_${b.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = b.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                                if (b.status == "demo_sent" || b.status == "demo_booked") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sent",
                                        tint = StatusActiveGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeBusinessSafe != null) {
            // Prospect Quick Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                    border = BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeBusinessSafe.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityTextPrimary
                                )
                                Text(
                                    text = "${activeBusinessSafe.industry} • ${activeBusinessSafe.countryCity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HighDensityTextSecondary
                                )
                            }

                            // Revenue Lost Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HighDensityPrimaryContainer,
                                border = BorderStroke(1.dp, HighDensityPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = HighDensityPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "~$${activeBusinessSafe.estMonthlyMissedRev.toInt()}/mo missed",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pain point highlight
                        Text(
                            text = "Pain Point: ${activeBusinessSafe.whyNeedVoiceBot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HighDensityTextPrimary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Unique Demo Link Station
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurfaceVariant),
                    border = BorderStroke(1.dp, HighDensitySubBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = HighDensityPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "UNIQUE INTERACTIVE DEMO LINK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HighDensityPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Copy Link button
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HighDensitySurface,
                                border = BorderStroke(1.dp, HighDensityBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Unique Demo Link", uniqueDemoLink))
                                        Toast.makeText(context, "Copied Demo Link to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = HighDensityPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy Link",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HighDensityPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // URL Display Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HighDensitySurface,
                            border = BorderStroke(1.dp, HighDensityBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uniqueDemoLink,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = HighDensityDarkPurpleText,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Test Demo Button
                            Button(
                                onClick = { onLaunchDemoForBusiness(activeBusinessSafe) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("test_voice_demo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Live Demo Bot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Share Link Button
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Interactive Voice Bot Demo for ${activeBusinessSafe.name}")
                                        putExtra(Intent.EXTRA_TEXT, "Test your custom AI Voice Receptionist for ${activeBusinessSafe.name}:\n$uniqueDemoLink")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Demo Link via"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityPrimary),
                                border = BorderStroke(1.dp, HighDensityPrimary),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // View Mode Tab Switcher (Form vs Live Preview)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HighDensitySurfaceVariant,
                    border = BorderStroke(1.dp, HighDensitySubBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutreachViewMode.values().forEach { mode ->
                            val isSelected = selectedViewMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) HighDensitySurface else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, HighDensityBorder) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedViewMode = mode }
                                    .testTag("tab_outreach_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (mode == OutreachViewMode.FORM) Icons.Default.Edit else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FORM VIEW: Customization Controls & Editable Inputs
            if (selectedViewMode == OutreachViewMode.FORM) {
                // Pitch Angles / Tones
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                        border = BorderStroke(1.dp, HighDensityBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PITCH STRATEGY & ANGLE:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                toneOptions.take(2).forEach { tone ->
                                    val isSelected = selectedTone == tone
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) HighDensityPrimaryContainer else HighDensitySurfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) HighDensityPrimary else HighDensitySubBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedTone = tone
                                                triggerGeneration(tone)
                                            }
                                            .testTag("tone_${tone.take(4)}")
                                    ) {
                                        Text(
                                            text = tone,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                toneOptions.drop(2).forEach { tone ->
                                    val isSelected = selectedTone == tone
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) HighDensityPrimaryContainer else HighDensitySurfaceVariant,
                                        border = BorderStroke(1.dp, if (isSelected) HighDensityPrimary else HighDensitySubBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedTone = tone
                                                triggerGeneration(tone)
                                            }
                                            .testTag("tone_${tone.take(4)}")
                                    ) {
                                        Text(
                                            text = tone,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Agency & Campaign Personalization Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                        border = BorderStroke(1.dp, HighDensityBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SENDER & OFFER PERSONALIZATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Sender Identity
                            OutlinedTextField(
                                value = senderName,
                                onValueChange = { senderName = it },
                                label = { Text("Sender Name & Agency", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = HighDensitySurface,
                                    unfocusedContainerColor = HighDensitySurface,
                                    focusedBorderColor = HighDensityPrimary,
                                    unfocusedBorderColor = HighDensityBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Call to Action Offer
                            OutlinedTextField(
                                value = customOffer,
                                onValueChange = { customOffer = it },
                                label = { Text("Call to Action Offer", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = HighDensitySurface,
                                    unfocusedContainerColor = HighDensitySurface,
                                    focusedBorderColor = HighDensityPrimary,
                                    unfocusedBorderColor = HighDensityBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Campaign tracking code
                            OutlinedTextField(
                                value = campaignTag,
                                onValueChange = { campaignTag = it },
                                label = { Text("Campaign Tag (UTM)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = HighDensitySurface,
                                    unfocusedContainerColor = HighDensitySurface,
                                    focusedBorderColor = HighDensityPrimary,
                                    unfocusedBorderColor = HighDensityBorder
                                )
                            )
                        }
                    }
                }

                // Email Form Editor (Subject & Body)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                        border = BorderStroke(1.dp, HighDensityBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EMAIL CONTENT GENERATOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityTextSecondary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HighDensityPrimaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { triggerGeneration() }
                                        .testTag("regenerate_pitch_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = HighDensityPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Regenerate AI Draft",
                                            fontSize = 11.sp,
                                            color = HighDensityPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isGeneratingEmail) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            color = HighDensityPrimary,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Gemini AI crafting personalized outreach pitch...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HighDensityTextSecondary
                                        )
                                    }
                                }
                            } else {
                                // To Field
                                OutlinedTextField(
                                    value = activeBusinessSafe.email,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("To (Owner Email)", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = HighDensitySurfaceVariant,
                                        unfocusedContainerColor = HighDensitySurfaceVariant,
                                        focusedBorderColor = HighDensityBorder,
                                        unfocusedBorderColor = HighDensitySubBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Subject Field
                                OutlinedTextField(
                                    value = subject,
                                    onValueChange = { subject = it },
                                    label = { Text("Subject Line", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("outreach_subject_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = HighDensitySurface,
                                        unfocusedContainerColor = HighDensitySurface,
                                        focusedBorderColor = HighDensityPrimary,
                                        unfocusedBorderColor = HighDensityBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Body Field
                                OutlinedTextField(
                                    value = emailBody,
                                    onValueChange = { emailBody = it },
                                    label = { Text("Email Message Body", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .testTag("outreach_body_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = HighDensitySurface,
                                        unfocusedContainerColor = HighDensitySurface,
                                        focusedBorderColor = HighDensityPrimary,
                                        unfocusedBorderColor = HighDensityBorder
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // LIVE EMAIL PREVIEW VIEW: Realistic Inbox Email Rendering
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                        border = BorderStroke(1.dp, HighDensityBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Mock Email Client Header
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HighDensitySurfaceVariant,
                                border = BorderStroke(1.dp, HighDensitySubBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Email Client Preview",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = HighDensityPrimaryContainer
                                    ) {
                                        Text(
                                            text = "INBOX",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email Headers (From, To, Subject, Date)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(HighDensityPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = senderName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = senderName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityTextPrimary
                                        )
                                        Text(
                                            text = "Today, 10:30 AM",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Text(
                                        text = "to ${activeBusinessSafe.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HighDensityTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = HighDensitySubBorder, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Subject in Preview
                            Text(
                                text = subject.ifBlank { "AI Voice Receptionist Demo for ${activeBusinessSafe.name}" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityTextPrimary,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Formatted Email Body Text
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HighDensitySurfaceVariant,
                                border = BorderStroke(1.dp, HighDensitySubBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = emailBody.ifBlank { "Generating preview message..." },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HighDensityTextPrimary,
                                        lineHeight = 20.sp,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Embedded Interactive Demo CTA Card in Email Preview
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onLaunchDemoForBusiness(activeBusinessSafe) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                                        border = BorderStroke(1.dp, HighDensityPrimary.copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = null,
                                                    tint = HighDensityPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Interactive Voice Receptionist Demo",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = HighDensityPrimary
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = HighDensityPrimary,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "▶ Click to Launch Interactive Demo",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Direct Link: $uniqueDemoLink",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = HighDensityTextSecondary,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dispatch & Record Actions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                    border = BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DISPATCH & LOG OUTREACH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // Main Email Dispatch Button
                        Button(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${activeBusinessSafe.email}")
                                    putExtra(Intent.EXTRA_SUBJECT, subject)
                                    putExtra(Intent.EXTRA_TEXT, emailBody)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Send Demo Email via..."))
                                    coroutineScope.launch {
                                        val outreachLog = OutreachLogEntity(
                                            businessId = activeBusinessSafe.id,
                                            recipientEmail = activeBusinessSafe.email,
                                            subject = subject,
                                            emailBody = emailBody,
                                            demoLink = uniqueDemoLink,
                                            status = "Sent",
                                            sentTimestamp = System.currentTimeMillis()
                                        )
                                        repository.recordOutreachEmail(outreachLog)
                                        isEmailSentSuccess = true
                                    }
                                } catch (e: Exception) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Pitch Email", "Subject: $subject\n\n$emailBody"))
                                    Toast.makeText(context, "No email app found. Pitch copied to clipboard.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("send_pitch_email_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dispatch Demo Pitch Email", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Action Buttons: Copy All & Mark as Sent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val fullContent = "Subject: $subject\n\n$emailBody"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Outreach Pitch", fullContent))
                                    Toast.makeText(context, "Copied full subject & email pitch!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, HighDensityBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("copy_pitch_button")
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Pitch", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val outreachLog = OutreachLogEntity(
                                            businessId = activeBusinessSafe.id,
                                            recipientEmail = activeBusinessSafe.email,
                                            subject = subject,
                                            emailBody = emailBody,
                                            demoLink = uniqueDemoLink,
                                            status = "Sent",
                                            sentTimestamp = System.currentTimeMillis()
                                        )
                                        repository.recordOutreachEmail(outreachLog)
                                        isEmailSentSuccess = true
                                        Toast.makeText(context, "Logged outreach & updated status to Demo Sent", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, HighDensityBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("log_outreach_manual_button")
                            ) {
                                Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log as Sent", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Success confirmation banner
                        if (isEmailSentSuccess) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusGreenBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusActiveGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Outreach logged in database & status set to 'Demo Sent'!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusGreenText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Past Outreach Activity History for this Prospect
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                    border = BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHistorySection = !showHistorySection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = HighDensityPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OUTREACH LOG HISTORY (${outreachLogs.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityTextSecondary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Icon(
                                imageVector = if (showHistorySection) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle",
                                tint = HighDensityTextSecondary
                            )
                        }

                        if (showHistorySection) {
                            Spacer(modifier = Modifier.height(10.dp))
                            if (outreachLogs.isEmpty()) {
                                Text(
                                    text = "No previous emails logged for this prospect yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HighDensityTextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                                outreachLogs.forEach { log ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = HighDensitySurfaceVariant,
                                        border = BorderStroke(1.dp, HighDensitySubBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = dateFormat.format(Date(log.sentTimestamp)),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = HighDensityTextSecondary,
                                                    fontSize = 10.sp
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = StatusGreenBg
                                                ) {
                                                    Text(
                                                        text = log.status.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = StatusGreenText,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = log.subject,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityTextPrimary,
                                                fontSize = 12.sp
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = log.emailBody,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = HighDensityTextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
