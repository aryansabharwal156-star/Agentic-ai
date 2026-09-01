package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessEntity
import com.example.data.repository.BusinessRepository
import com.example.ui.screens.dashboard.OwnerDashboardScreen
import com.example.ui.screens.demo.VoiceAgentDemoScreen
import com.example.ui.screens.directory.DirectoryScreen
import com.example.ui.screens.outreach.OutreachEmailScreen
import com.example.ui.theme.HighDensityAccentPurple
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityNavBg
import com.example.ui.theme.HighDensityOnPrimaryContainer
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.VoiceBizTheme
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    DIRECTORY("Businesses", Icons.Default.Business, "tab_directory"),
    VOICE_DEMO("Voice Demo", Icons.Default.GraphicEq, "tab_voice_demo"),
    OUTREACH("Outreach", Icons.Default.Email, "tab_outreach"),
    DASHBOARD("Call Logs", Icons.Default.Assessment, "tab_dashboard")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDeepLinkId = parseDeepLinkId(intent)

        setContent {
            VoiceBizTheme {
                VoiceBizApp(initialDeepLinkId = initialDeepLinkId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun parseDeepLinkId(intent: Intent?): Long? {
        val data = intent?.data
        if (data != null && data.scheme == "voicebiz") {
            val idStr = data.getQueryParameter("id")
            return idStr?.toLongOrNull()
        }
        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceBizApp(initialDeepLinkId: Long?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { BusinessRepository.getInstance(context, coroutineScope) }

    // Seed DB if first launch
    LaunchedEffect(Unit) {
        repository.initializeIfEmpty()
    }

    val businesses by repository.allBusinesses.collectAsState(initial = emptyList())
    val callLogs by repository.allCallLogs.collectAsState(initial = emptyList())

    var currentTab by remember {
        mutableStateOf(if (initialDeepLinkId != null) AppTab.VOICE_DEMO else AppTab.DIRECTORY)
    }
    var selectedBusinessId by remember {
        mutableLongStateOf(initialDeepLinkId ?: -1L)
    }

    // Default to first business if none selected
    val currentSelectedBusiness = remember(businesses, selectedBusinessId) {
        if (selectedBusinessId > 0) {
            businesses.find { it.id == selectedBusinessId } ?: businesses.firstOrNull()
        } else {
            businesses.firstOrNull()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsVoice,
                                contentDescription = "VoiceBiz",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "VoiceNexus",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityTextPrimary,
                                lineHeight = 20.sp
                            )
                            Text(
                                text = "AI Business Concierge",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(HighDensityPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = HighDensityOnPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                HorizontalDivider(
                    color = HighDensityBorder.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HighDensityNavBg)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(
                    color = HighDensityBorder,
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = HighDensityNavBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HighDensityOnPrimaryContainer,
                                unselectedIconColor = HighDensityTextSecondary,
                                selectedTextColor = HighDensityPrimary,
                                unselectedTextColor = HighDensityTextSecondary,
                                indicatorColor = HighDensityPrimaryContainer
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    AppTab.DIRECTORY -> {
                        DirectoryScreen(
                            businesses = businesses,
                            onLaunchDemo = { business ->
                                selectedBusinessId = business.id
                                currentTab = AppTab.VOICE_DEMO
                            },
                            onSendEmail = { business ->
                                selectedBusinessId = business.id
                                currentTab = AppTab.OUTREACH
                            },
                            onViewLogs = { business ->
                                selectedBusinessId = business.id
                                currentTab = AppTab.DASHBOARD
                            },
                            onSaveBusiness = { business ->
                                coroutineScope.launch {
                                    if (business.id == 0L) {
                                        repository.insertBusiness(business)
                                    } else {
                                        repository.updateBusiness(business)
                                    }
                                }
                            },
                            onDeleteBusiness = { business ->
                                coroutineScope.launch {
                                    repository.deleteBusiness(business)
                                }
                            }
                        )
                    }

                    AppTab.VOICE_DEMO -> {
                        if (currentSelectedBusiness != null) {
                            VoiceAgentDemoScreen(
                                business = currentSelectedBusiness,
                                repository = repository,
                                onNavigateToOutreach = { business ->
                                    selectedBusinessId = business.id
                                    currentTab = AppTab.OUTREACH
                                },
                                onNavigateToDashboard = { business ->
                                    selectedBusinessId = business.id
                                    currentTab = AppTab.DASHBOARD
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No business available. Add one in the Directory tab.")
                            }
                        }
                    }

                    AppTab.OUTREACH -> {
                        OutreachEmailScreen(
                            businesses = businesses,
                            selectedBusinessId = selectedBusinessId.takeIf { it > 0 },
                            repository = repository,
                            onLaunchDemoForBusiness = { business ->
                                selectedBusinessId = business.id
                                currentTab = AppTab.VOICE_DEMO
                            }
                        )
                    }

                    AppTab.DASHBOARD -> {
                        OwnerDashboardScreen(
                            businesses = businesses,
                            callLogs = callLogs,
                            selectedBusinessId = selectedBusinessId.takeIf { it > 0 }
                        )
                    }
                }
            }
        }
    }
}
