package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CallLogEntity
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityDarkPurpleText
import com.example.ui.theme.HighDensityDivider
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySubBorder
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensitySurfaceVariant
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusErrorText
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGreenText
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallLogCard(
    callLog: CallLogEntity,
    businessName: String,
    onPlayTTS: (String) -> Unit,
    isPlaying: Boolean = false,
    onStopTTS: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("call_log_card_${callLog.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
        border = BorderStroke(1.dp, HighDensityBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Caller icon avatar, Caller Name, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (callLog.sentiment.contains("Pos", true)) HighDensityPrimaryContainer
                                else StatusErrorBg
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (callLog.sentiment.contains("Pos", true)) Icons.Default.CallReceived else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (callLog.sentiment.contains("Pos", true)) HighDensityDarkPurpleText else StatusErrorText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = callLog.callerName.ifBlank { "Unknown Caller" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = HighDensityTextPrimary
                        )
                        Text(
                            text = "$businessName • ${dateFormat.format(Date(callLog.callDate))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                SentimentChip(sentiment = callLog.sentiment)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-row: Duration, Inquiry Tag, Lead Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HighDensitySurfaceVariant,
                        border = BorderStroke(1.dp, HighDensitySubBorder)
                    ) {
                        Text(
                            text = "Inquiry: ${callLog.inquiryType}",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HighDensitySurfaceVariant,
                        border = BorderStroke(1.dp, HighDensitySubBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = HighDensityTextSecondary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${callLog.durationSeconds}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (callLog.leadValueEstimate > 0) {
                    Text(
                        text = "Lead: $${callLog.leadValueEstimate.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatusActiveGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Summary box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = HighDensitySurfaceVariant,
                border = BorderStroke(1.dp, HighDensitySubBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "AI Call Summary",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = callLog.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextPrimary,
                        lineHeight = 18.sp
                    )
                    if (callLog.actionItems.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusActiveGreen,
                                modifier = Modifier
                                    .size(13.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Action: ${callLog.actionItems}",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusActiveGreen,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expand Transcript & Audio Playback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Transcript" else "View Full Transcript",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = HighDensityPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Audio Playback pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isPlaying) StatusErrorBg else HighDensityPrimaryContainer,
                    modifier = Modifier.clickable {
                        if (isPlaying) onStopTTS() else onPlayTTS(callLog.summary)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Listen Summary",
                            tint = if (isPlaying) StatusErrorText else HighDensityDarkPurpleText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "Stop" else "Listen",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPlaying) StatusErrorText else HighDensityDarkPurpleText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Expanded Transcript View
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(
                        color = HighDensityDivider,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Recorded Call Dialogue:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val turns = parseTranscriptTurns(callLog.fullTranscriptJson)
                    if (turns.isEmpty()) {
                        Text(
                            text = "No detailed turns stored.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HighDensityTextSecondary
                        )
                    } else {
                        turns.forEach { (speaker, text) ->
                            val isBot = speaker.contains("AI", true) || speaker.contains("Bot", true) || speaker.contains("Assistant", true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp,
                                        bottomStart = if (isBot) 2.dp else 10.dp,
                                        bottomEnd = if (isBot) 10.dp else 2.dp
                                    ),
                                    color = if (isBot) HighDensityPrimaryContainer else HighDensitySurfaceVariant,
                                    border = BorderStroke(1.dp, if (isBot) HighDensityPrimary.copy(alpha = 0.2f) else HighDensitySubBorder),
                                    modifier = Modifier.fillMaxWidth(0.92f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = speaker,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBot) HighDensityPrimary else HighDensityTextSecondary,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HighDensityTextPrimary,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
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

@Composable
fun SentimentChip(sentiment: String) {
    val (bgColor, textColor) = when {
        sentiment.contains("Pos", true) -> Pair(StatusGreenBg, StatusGreenText)
        sentiment.contains("Neu", true) -> Pair(HighDensityPrimaryContainer, HighDensityPrimary)
        else -> Pair(StatusAmberBg, StatusAmber)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = sentiment,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

private fun parseTranscriptTurns(jsonStr: String): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val speaker = obj.optString("speaker", "Speaker")
            val text = obj.optString("text", "")
            result.add(Pair(speaker, text))
        }
    } catch (e: Exception) {
        // Ignored
    }
    return result
}
