package com.example.ui.screens.directory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.local.BusinessEntity
import com.example.ui.components.BusinessCard
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.HighDensityAccentPurple
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityDarkConsole
import com.example.ui.theme.HighDensityOnPrimaryContainer
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySubBorder
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensitySurfaceVariant
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusAmber

@Composable
fun DirectoryScreen(
    businesses: List<BusinessEntity>,
    onLaunchDemo: (BusinessEntity) -> Unit,
    onSendEmail: (BusinessEntity) -> Unit,
    onViewLogs: (BusinessEntity) -> Unit,
    onSaveBusiness: (BusinessEntity) -> Unit,
    onDeleteBusiness: (BusinessEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingBusiness by remember { mutableStateOf<BusinessEntity?>(null) }

    val industries = remember(businesses) {
        listOf("All") + businesses.map { it.industry }.distinct().sorted()
    }

    val filteredBusinesses by remember(businesses, searchQuery, selectedIndustry, selectedStatus) {
        derivedStateOf {
            businesses.filter { b ->
                val matchesSearch = searchQuery.isBlank() ||
                        b.name.contains(searchQuery, ignoreCase = true) ||
                        b.industry.contains(searchQuery, ignoreCase = true) ||
                        b.countryCity.contains(searchQuery, ignoreCase = true) ||
                        b.whyNeedVoiceBot.contains(searchQuery, ignoreCase = true)
                val matchesIndustry = selectedIndustry == "All" || b.industry.equals(selectedIndustry, ignoreCase = true)
                val matchesStatus = selectedStatus == "All" || b.status.equals(selectedStatus, ignoreCase = true)
                matchesSearch && matchesIndustry && matchesStatus
            }
        }
    }

    val totalMissedRev by remember(businesses) {
        derivedStateOf { businesses.sumOf { it.estMonthlyMissedRev } }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBusiness = null
                    showAddEditDialog = true
                },
                containerColor = HighDensityPrimaryContainer,
                contentColor = HighDensityOnPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_business_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Business",
                        tint = HighDensityOnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Add Business",
                        fontWeight = FontWeight.Bold,
                        color = HighDensityOnPrimaryContainer
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Platform Header (Dark High Density Console style)
            item {
                PlatformHeroBanner(
                    totalBusinesses = businesses.size,
                    totalMissedRev = totalMissedRev,
                    activeDemos = businesses.count { it.status != "new" }
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_businesses_input"),
                    placeholder = {
                        Text(
                            "Search business, industry, city...",
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

            // Industry Filter Horizontal Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "FILTER BY INDUSTRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityTextSecondary,
                        fontWeight = FontWeight.Bold,
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
                        industries.forEach { industry ->
                            val isSelected = selectedIndustry == industry
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) HighDensityPrimaryContainer else HighDensitySurface,
                                border = BorderStroke(1.dp, if (isSelected) HighDensityPrimary else HighDensityBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { selectedIndustry = industry }
                                    .testTag("filter_industry_$industry")
                            ) {
                                Text(
                                    text = industry,
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

            // Status Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROSPECTIVE BUSINESSES (${filteredBusinesses.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("All", "new", "demo_sent", "demo_booked").forEach { status ->
                            val label = when (status) {
                                "All" -> "All"
                                "new" -> "New"
                                "demo_sent" -> "Sent"
                                "demo_booked" -> "Booked"
                                else -> status
                            }
                            val isSelected = selectedStatus == status
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) HighDensityPrimaryContainer else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, HighDensityPrimary) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedStatus = status }
                            ) {
                                Text(
                                    text = label,
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

            // Businesses List
            if (filteredBusinesses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = HighDensityTextSecondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No businesses match your filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HighDensityTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredBusinesses, key = { it.id }) { business ->
                    BusinessCard(
                        business = business,
                        onLaunchDemo = onLaunchDemo,
                        onSendEmail = onSendEmail,
                        onViewLogs = onViewLogs,
                        onEdit = {
                            editingBusiness = it
                            showAddEditDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditBusinessDialog(
            existingBusiness = editingBusiness,
            onDismiss = { showAddEditDialog = false },
            onSave = {
                onSaveBusiness(it)
                showAddEditDialog = false
            },
            onDelete = {
                if (editingBusiness != null) {
                    onDeleteBusiness(editingBusiness!!)
                }
                showAddEditDialog = false
            }
        )
    }
}

@Composable
private fun PlatformHeroBanner(
    totalBusinesses: Int,
    totalMissedRev: Double,
    activeDemos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HighDensityDarkConsole),
        border = BorderStroke(1.dp, Color(0xFF332D41)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HighDensityPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsVoice,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Voice Agent Console",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Claude & Gemini Multi-Modal Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityAccentPurple,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }

                // Decorative wave bars in hero
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(18.dp)
                ) {
                    listOf(4, 10, 16, 8, 14, 11, 5).forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(h.dp)
                                .clip(RoundedCornerShape(50))
                                .background(HighDensityAccentPurple)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniCard(
                    label = "DIRECTORY",
                    value = "$totalBusinesses",
                    color = HighDensityAccentPurple,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    label = "MISSED REV/MO",
                    value = "$${(totalMissedRev / 1000).toInt()}k",
                    color = StatusAmber,
                    modifier = Modifier.weight(1.2f)
                )
                MetricMiniCard(
                    label = "ACTIVE DEMOS",
                    value = "$activeDemos",
                    color = StatusActiveGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF2B2930),
        border = BorderStroke(1.dp, Color(0xFF3B3844))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = DarkTextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
