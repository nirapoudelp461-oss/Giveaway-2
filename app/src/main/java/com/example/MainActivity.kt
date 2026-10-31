package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Giveaway
import com.example.data.model.GiveawayEntry
import com.example.data.repository.GiveawayRepository
import com.example.ui.components.AdminSimulatorDialog
import com.example.ui.components.GiveawayDetailBottomSheet
import com.example.ui.components.NotificationDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JoinedScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WinScreen
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.MainBackground
import com.example.ui.theme.MainText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryBackground
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SecondaryText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String) {
    HOME("Home"),
    JOINED("Joined"),
    WIN("Win"),
    TASKS("Tasks"),
    PROFILE("Profile")
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: GiveawayRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = GiveawayRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                GiveawayApp(repository = repository)
            }
        }
    }
}

@Composable
fun GiveawayApp(repository: GiveawayRepository) {
    val userProfile by repository.userProfile.collectAsState()
    val giveaways by repository.giveaways.collectAsState()
    val joinedEntries by repository.joinedEntries.collectAsState()
    val allWinners by repository.allWinners.collectAsState()
    val myWinnings by repository.myWinnings.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val submissions by repository.submissions.collectAsState()
    val announcements by repository.announcements.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var selectedGiveawayForDetails by remember { mutableStateOf<Giveaway?>(null) }
    var selectedEntryForDetails by remember { mutableStateOf<GiveawayEntry?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAdminSimulatorDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Hardware back button navigation handling
    if (currentTab != ScreenTab.HOME) {
        BackHandler {
            currentTab = ScreenTab.HOME
        }
    }

    // If user is not authenticated, show AuthScreen
    if (userProfile == null) {
        AuthScreen(
            repository = repository,
            onAuthSuccess = {
                currentTab = ScreenTab.HOME
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MainBackground,
            topBar = {
                TopHeaderBar(
                    userProfile = userProfile,
                    unreadNotifCount = notifications.count { !it.isRead },
                    onNotificationsClick = {
                        showNotificationDialog = true
                    },
                    onProfileClick = {
                        currentTab = ScreenTab.PROFILE
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SecondaryBackground,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor.copy(alpha = 0.5f))
                        .navigationBarsPadding()
                ) {
                    ScreenTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        ScreenTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                        ScreenTab.JOINED -> if (isSelected) Icons.Filled.ConfirmationNumber else Icons.Outlined.ConfirmationNumber
                                        ScreenTab.WIN -> if (isSelected) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents
                                        ScreenTab.TASKS -> if (isSelected) Icons.Filled.TaskAlt else Icons.Outlined.TaskAlt
                                        ScreenTab.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                                    },
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryPurple,
                                selectedTextColor = PrimaryPurple,
                                indicatorColor = PrimaryPurple.copy(alpha = 0.15f),
                                unselectedIconColor = SecondaryText,
                                unselectedTextColor = SecondaryText
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        ScreenTab.HOME -> {
                            HomeScreen(
                                userProfile = userProfile,
                                giveaways = giveaways,
                                joinedEntries = joinedEntries,
                                recentWinners = allWinners,
                                announcements = announcements,
                                onJoinGiveaway = { g -> repository.joinGiveaway(g) },
                                onSelectGiveaway = { g ->
                                    selectedGiveawayForDetails = g
                                    selectedEntryForDetails = joinedEntries.find { it.giveawayId == g.id }
                                },
                                onNavigateToTasks = { currentTab = ScreenTab.TASKS },
                                onNavigateToWin = { currentTab = ScreenTab.WIN }
                            )
                        }
                        ScreenTab.JOINED -> {
                            JoinedScreen(
                                entries = joinedEntries,
                                giveaways = giveaways,
                                onSelectEntry = { g, entry ->
                                    selectedGiveawayForDetails = g
                                    selectedEntryForDetails = entry
                                },
                                onExploreGiveaways = { currentTab = ScreenTab.HOME }
                            )
                        }
                        ScreenTab.WIN -> {
                            WinScreen(
                                myWinnings = myWinnings,
                                communityWinners = allWinners,
                                onSubmitClaim = { gId, name, email, addr, handle ->
                                    repository.claimPrize(gId, name, email, addr, handle)
                                },
                                onOpenAdminSim = { showAdminSimulatorDialog = true }
                            )
                        }
                        ScreenTab.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                submissions = submissions,
                                userPoints = userProfile?.points ?: 0,
                                onClaimDailyCheckIn = { repository.claimDailyCheckIn() },
                                onSubmitTask = { task, proof -> repository.submitTask(task, proof) }
                            )
                        }
                        ScreenTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                onUpdateProfile = { name, bio -> repository.updateProfile(name, bio) },
                                onRedeemReferral = { code -> repository.redeemReferralCode(code) },
                                onOpenAdminSim = { showAdminSimulatorDialog = true },
                                onLogout = { repository.logout() },
                                onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryPurple,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }

    // Giveaway Details Bottom Sheet
    if (selectedGiveawayForDetails != null) {
        GiveawayDetailBottomSheet(
            giveaway = selectedGiveawayForDetails!!,
            userEntry = selectedEntryForDetails,
            onDismiss = {
                selectedGiveawayForDetails = null
                selectedEntryForDetails = null
            },
            onJoinClick = {
                repository.joinGiveaway(selectedGiveawayForDetails!!)
            }
        )
    }

    // Notification Center Dialog
    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationDialog = false },
            onMarkAllRead = { repository.markNotificationsAsRead() }
        )
    }

    // Admin Simulator Dialog (Section 10)
    if (showAdminSimulatorDialog) {
        AdminSimulatorDialog(
            giveaways = giveaways,
            onDismiss = { showAdminSimulatorDialog = false },
            onSimulateWin = { g -> repository.simulateWinForUser(g) },
            onApproveTasks = { repository.adminApproveAllSubmissions() },
            onAddBonusPoints = {
                val current = userProfile
                if (current != null) {
                    val updated = current.copy(points = current.points + 100)
                    scope.launch {
                        repository.dbService.saveUserProfile(updated, repository.authService.getStoredToken())
                        repository.refreshData()
                    }
                }
            }
        )
    }
}
