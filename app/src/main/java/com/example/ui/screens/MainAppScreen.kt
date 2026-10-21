package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthManager
import com.example.data.model.UserProfile
import com.example.ui.components.ProfileSettingsDialog
import kotlinx.coroutines.launch

enum class AppTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TRACKER("Tracker", Icons.Default.WaterDrop),
    HISTORY("Analytics", Icons.Default.BarChart),
    REMINDERS("Reminders", Icons.Default.Alarm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    onSignOutComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(AppTab.TRACKER) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val userProfileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val todayTotalMl by viewModel.todayTotalMl.collectAsStateWithLifecycle()
    val past7Days by viewModel.past7DaysData.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val allLogsState by viewModel.waterLogs.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val profile = (userProfileState as? UiState.Success)?.data ?: UserProfile()
    val allLogs = (allLogsState as? UiState.Success)?.data ?: emptyList()

    if (showSettingsDialog) {
        ProfileSettingsDialog(
            profile = profile,
            onDismiss = { showSettingsDialog = false },
            onSaveGoal = { newGoal -> viewModel.updateGoal(newGoal) },
            onSignOut = {
                AuthManager.signOut(context, credentialManager, onSignOutComplete, coroutineScope)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HydroTrack",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.TRACKER -> {
                    DashboardScreen(
                        userProfile = profile,
                        todayTotalMl = todayTotalMl,
                        todayLogs = todayLogs,
                        streakDays = streakDays,
                        onQuickLog = { amount, beverage, container ->
                            viewModel.quickLog(amount, beverage, container)
                        },
                        onDeleteLog = { logId -> viewModel.deleteLog(logId) },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }
                AppTab.HISTORY -> {
                    HistoryScreen(
                        userProfile = profile,
                        past7Days = past7Days,
                        allLogs = allLogs
                    )
                }
                AppTab.REMINDERS -> {
                    RemindersScreen(
                        userProfile = profile,
                        onUpdateReminders = { enabled, interval, start, end ->
                            viewModel.updateReminders(enabled, interval, start, end)
                        }
                    )
                }
            }
        }
    }
}
