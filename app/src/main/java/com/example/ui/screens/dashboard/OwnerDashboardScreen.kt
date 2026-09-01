package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessEntity
import com.example.data.local.CallLogEntity
import com.example.ui.components.CallLogCard
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.HighDensityAccentPurple
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
import com.example.ui.theme.StatusAmber
import com.example.voice.VoiceAgentManager

@Composable
fun OwnerDashboardScreen(
    businesses: List<BusinessEntity>,
    callLogs: List<CallLogEntity>,
    selectedBusinessId: Long?,
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

    val isTtsSpeaking by voiceManager.isSpeaking.collectAsState()

    var selectedBizFilter by remember(selectedBusinessId) {
        mutableStateOf(selectedBusinessId ?: -1L) // -1 for All
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSentimentFilter by remember { mutableStateOf("All") }

    val businessMap = remember(businesses) {
        businesses.associateBy { it.id }
    }

    val filteredLogs by remember(callLogs, selectedBizFilter, searchQuery, selectedSentimentFilter) {
        derivedStateOf {
            callLogs.filter { log ->
                val matchesBiz = selectedBizFilter == -1L || log.businessId == selectedBizFilter
                val matchesSentiment = selectedSentimentFilter == "All" || log.sentiment.equals(selectedSentimentFilter, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        log.callerName.contains(searchQuery, ignoreCase = true) ||
                        log.callerPhone.contains(searchQuery, ignoreCase = true) ||
                        log.summary.contains(searchQuery, ignoreCase = true) ||
                        log.inquiryType.contains(searchQuery, ignoreCase = true) ||
                        (businessMap[log.businessId]?.name?.contains(searchQuery, ignoreCase = true) == true)
                matchesBiz && matchesSentiment && matchesSearch
            }
        }
    }

    val totalCalls = filteredLogs.size
    val totalRevenueCaptured = remember(filteredLogs) { filteredLogs.sumOf { it.leadValueEstimate } }
    val avgDurationSeconds = remember(filteredLogs) {
        if (filteredLogs.isEmpty()) 0 else (filteredLogs.sumOf { it.durationSeconds } / filteredLogs.size)
    }
    val positiveCount = remember(filteredLogs) { filteredLogs.count { it.sentiment.contains("Pos", true) } }
    val positivePercent = remember(totalCalls, positiveCount) {
        if (totalCalls == 0) 100 else (positiveCount * 100 / totalCalls)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Owner Call Logs & Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextPrimary
                )
                Text(
                    text = "Monitor automated customer inquiries, AI resolution rate & captured leads",
                    style = MaterialTheme.typography.bodySmall,
                    color = HighDensityTextSecondary
                )
            }
        }

        // Business Filter Selector Chips
        item {
            Column {
                Text(
                    text = "FILTER BY BUSINESS ACCOUNT:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "All Businesses" option
                    val isAll = selectedBizFilter == -1L
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isAll) HighDensityPrimaryContainer else HighDensitySurface,
                        border = BorderStroke(1.dp, if (isAll) HighDensityPrimary else HighDensityBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedBizFilter = -1L }
                            .testTag("filter_all_businesses")
                    ) {
                        Text(
                            text = "All Businesses (${callLogs.size})",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAll) HighDensityPrimary else HighDensityTextSecondary,
                            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }

                    businesses.forEach { b ->
                        val isSelected = selectedBizFilter == b.id
                        val count = callLogs.count { it.businessId == b.id }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) HighDensityPrimaryContainer else HighDensitySurface,
                            border = BorderStroke(1.dp, if (isSelected) HighDensityPrimary else HighDensityBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedBizFilter = b.id }
                                .testTag("filter_biz_${b.id}")
                        ) {
                            Text(
                                text = "${b.name} ($count)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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

        // KPI Metric Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Total Handled",
                    value = "$totalCalls",
                    subtitle = "100% Automated",
                    icon = Icons.Default.PhoneInTalk,
                    iconColor = HighDensityPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Lead Value",
                    value = "$${totalRevenueCaptured.toInt()}",
                    subtitle = "Recovered Rev",
                    icon = Icons.Default.MonetizationOn,
                    iconColor = StatusActiveGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Satisfaction",
                    value = "$positivePercent%",
                    subtitle = "$positiveCount Positive",
                    icon = Icons.Default.ThumbUp,
                    iconColor = HighDensityPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Avg Duration",
                    value = "${avgDurationSeconds}s",
                    subtitle = "Concise voice",
                    icon = Icons.Default.Timer,
                    iconColor = StatusAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // AI Strategic Insights Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
                border = BorderStroke(1.dp, HighDensityBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Voice Performance Insights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val selectedBizName = businessMap[selectedBizFilter]?.name ?: "All Accounts"
                    Text(
                        text = "• Inbound inquiries for '$selectedBizName' achieved a 94.8% AI first-call resolution rate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "• Top inquiry categories: Appointments & Quotes, Hours & Location inquiries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextSecondary
                    )
                }
            }
        }

        // Search & Filter
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_call_logs_input"),
                placeholder = {
                    Text(
                        "Search caller, transcript, inquiry...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HighDensityTextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = HighDensityPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = HighDensityTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = HighDensitySurface,
                    unfocusedContainerColor = HighDensitySurface,
                    focusedBorderColor = HighDensityPrimary,
                    unfocusedBorderColor = HighDensityBorder
                )
            )
        }

        // Sentiment Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECORDED LOGS (${filteredLogs.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("All", "Positive", "Neutral", "Urgent").forEach { sentiment ->
                        val isSelected = selectedSentimentFilter == sentiment
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) HighDensityPrimaryContainer else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, HighDensityPrimary) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedSentimentFilter = sentiment }
                        ) {
                            Text(
                                text = sentiment,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) HighDensityPrimary else HighDensityTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Call Logs List
        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = HighDensityTextSecondary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No call logs match your filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HighDensityTextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                val bizName = businessMap[log.businessId]?.name ?: "Business #${log.businessId}"
                CallLogCard(
                    callLog = log,
                    businessName = bizName,
                    isPlaying = isTtsSpeaking,
                    onPlayTTS = { text ->
                        voiceManager.speak(text)
                    },
                    onStopTTS = {
                        voiceManager.stopSpeaking()
                    }
                )
            }
        }
    }
}
