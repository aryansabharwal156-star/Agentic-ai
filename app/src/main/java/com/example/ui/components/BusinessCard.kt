package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessEntity
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
import com.example.ui.theme.HighDensityTextTertiary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusBlueBg
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGreenText

@Composable
fun BusinessCard(
    business: BusinessEntity,
    onLaunchDemo: (BusinessEntity) -> Unit,
    onSendEmail: (BusinessEntity) -> Unit,
    onViewLogs: (BusinessEntity) -> Unit,
    onEdit: (BusinessEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("business_card_${business.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HighDensitySurface
        ),
        border = BorderStroke(1.dp, HighDensityBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Active Listing Badge + More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = HighDensityPrimaryContainer
                    ) {
                        Text(
                            text = business.industry.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    StatusBadge(status = business.status)
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = HighDensityTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Business Details") },
                            onClick = {
                                showMenu = false
                                onEdit(business)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Analytics & Logs") },
                            onClick = {
                                showMenu = false
                                onViewLogs(business)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Assessment, contentDescription = null)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Business Title & Address
            Text(
                text = business.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HighDensityTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${business.countryCity} • ${business.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HighDensityTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // High Density 2-Column Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Column 1: Agent Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HighDensitySurfaceVariant,
                    border = BorderStroke(1.dp, HighDensitySubBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text(
                            text = "AGENT STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusActiveGreen)
                            )
                            Text(
                                text = "Live AI Demo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HighDensityTextPrimary
                            )
                        }
                    }
                }

                // Column 2: Missed Revenue / Why Need
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HighDensitySurfaceVariant,
                    border = BorderStroke(1.dp, HighDensitySubBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Text(
                            text = "EST. MISSED REV",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "$${business.estMonthlyMissedRev.toInt()}/mo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onLaunchDemo(business) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("launch_demo_button_${business.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HighDensityPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Agent", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { onSendEmail(business) },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("send_email_button_${business.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HighDensityBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HighDensityTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = HighDensityTextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Email", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                IconButton(
                    onClick = { onViewLogs(business) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HighDensitySurfaceVariant)
                        .border(1.dp, HighDensitySubBorder, RoundedCornerShape(10.dp))
                        .testTag("view_logs_button_${business.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assessment,
                        contentDescription = "View Call Logs",
                        tint = HighDensityPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "new" -> Triple(StatusBlueBg, StatusBlue, "New")
        "demo_sent" -> Triple(HighDensityPrimaryContainer, HighDensityPrimary, "Demo Sent")
        "demo_opened" -> Triple(StatusAmberBg, StatusAmber, "Opened")
        "demo_booked" -> Triple(StatusGreenBg, StatusGreenText, "Booked")
        "closed_won" -> Triple(StatusGreenBg, StatusActiveGreen, "Won")
        else -> Triple(HighDensitySurfaceVariant, HighDensityTextSecondary, status.replace("_", " ").capitalize())
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
