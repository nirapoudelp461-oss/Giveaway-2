package com.example.data.repository

import android.content.Context
import com.example.data.model.Announcement
import com.example.data.model.Giveaway
import com.example.data.model.GiveawayEntry
import com.example.data.model.NotificationItem
import com.example.data.model.RewardTask
import com.example.data.model.TaskSubmission
import com.example.data.model.UserProfile
import com.example.data.model.WinnerRecord
import com.example.data.remote.AuthResult
import com.example.data.remote.FirebaseAuthService
import com.example.data.remote.FirebaseDatabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GiveawayRepository(private val context: Context) {
    val authService = FirebaseAuthService(context)
    val dbService = FirebaseDatabaseService(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _giveaways = MutableStateFlow<List<Giveaway>>(emptyList())
    val giveaways: StateFlow<List<Giveaway>> = _giveaways.asStateFlow()

    private val _joinedEntries = MutableStateFlow<List<GiveawayEntry>>(emptyList())
    val joinedEntries: StateFlow<List<GiveawayEntry>> = _joinedEntries.asStateFlow()

    private val _allWinners = MutableStateFlow<List<WinnerRecord>>(emptyList())
    val allWinners: StateFlow<List<WinnerRecord>> = _allWinners.asStateFlow()

    private val _myWinnings = MutableStateFlow<List<WinnerRecord>>(emptyList())
    val myWinnings: StateFlow<List<WinnerRecord>> = _myWinnings.asStateFlow()

    private val _tasks = MutableStateFlow<List<RewardTask>>(emptyList())
    val tasks: StateFlow<List<RewardTask>> = _tasks.asStateFlow()

    private val _submissions = MutableStateFlow<List<TaskSubmission>>(emptyList())
    val submissions: StateFlow<List<TaskSubmission>> = _submissions.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        // Initialize with default announcements & notifications
        _announcements.value = dbService.getDefaultAnnouncements()
        _notifications.value = listOf(
            NotificationItem(
                id = "notif_welcome",
                title = "Welcome to Giveaway Hub! 🎁",
                message = "You received 250 welcome points. Explore active drops to join now!",
                type = "points",
                isRead = false
            ),
            NotificationItem(
                id = "notif_iphone",
                title = "Mega Drop Active: iPhone 16 Pro Max",
                message = "The grand prize giveaway is now taking free entries.",
                type = "giveaway",
                isRead = false
            )
        )

        // If previously logged in, restore session
        if (authService.isLoggedIn()) {
            val uid = authService.getStoredUid() ?: ""
            val email = authService.getStoredEmail() ?: ""
            val username = authService.getStoredUsername() ?: "Gamer"
            val fallbackProfile = UserProfile(
                uid = uid,
                username = username,
                fullName = username,
                email = email,
                points = 250,
                referralCode = "GIVE-${uid.takeLast(4).uppercase()}"
            )
            _userProfile.value = fallbackProfile
        }
        refreshData()
    }

    fun refreshData() {
        scope.launch {
            _isLoading.value = true
            val token = authService.getStoredToken()
            val uid = authService.getStoredUid()

            // Fetch giveaways
            val fetchedGiveaways = dbService.fetchGiveaways(token)
            _giveaways.value = fetchedGiveaways

            // Fetch winners
            val fetchedWinners = dbService.fetchWinners(token)
            _allWinners.value = fetchedWinners

            // Fetch tasks
            val fetchedTasks = dbService.fetchTasks(token)
            _tasks.value = fetchedTasks

            // Fetch announcements
            val fetchedAnn = dbService.fetchAnnouncements(token)
            if (fetchedAnn.isNotEmpty()) {
                _announcements.value = fetchedAnn
            }

            // User-specific data
            if (!uid.isNullOrEmpty()) {
                val profile = dbService.getUserProfile(uid, token)
                if (profile != null) {
                    _userProfile.value = profile
                } else if (_userProfile.value == null) {
                    val defaultProf = UserProfile(
                        uid = uid,
                        username = authService.getStoredUsername() ?: "Gamer",
                        fullName = authService.getStoredUsername() ?: "Gamer",
                        email = authService.getStoredEmail() ?: "",
                        points = 250,
                        referralCode = "GIVE-${uid.takeLast(4).uppercase()}"
                    )
                    _userProfile.value = defaultProf
                    dbService.saveUserProfile(defaultProf, token)
                }

                // Fetch entries
                val userEntries = dbService.fetchUserEntries(uid, token)
                _joinedEntries.value = userEntries

                // Filter user's winnings
                _myWinnings.value = _allWinners.value.filter { it.winnerUID == uid }

                // Fetch task submissions
                val subs = dbService.fetchUserSubmissions(uid, token)
                _submissions.value = subs
            }
            _isLoading.value = false
        }
    }

    suspend fun login(email: String, pass: String): AuthResult {
        _isLoading.value = true
        val result = authService.signIn(email, pass)
        if (result is AuthResult.Success) {
            val profile = dbService.getUserProfile(result.uid, result.token) ?: UserProfile(
                uid = result.uid,
                username = result.displayName ?: email.substringBefore("@"),
                fullName = result.displayName ?: email.substringBefore("@"),
                email = result.email,
                points = 250,
                referralCode = "GIVE-${result.uid.takeLast(4).uppercase()}"
            )
            _userProfile.value = profile
            dbService.saveUserProfile(profile, result.token)
            refreshData()
            _toastEvent.emit("Welcome back, ${profile.username}!")
        }
        _isLoading.value = false
        return result
    }

    suspend fun register(email: String, pass: String, username: String): AuthResult {
        _isLoading.value = true
        val result = authService.signUp(email, pass, username)
        if (result is AuthResult.Success) {
            val newProfile = UserProfile(
                uid = result.uid,
                username = username,
                fullName = username,
                email = email,
                points = 250,
                referralCode = "GIVE-${result.uid.takeLast(4).uppercase()}",
                createdAt = System.currentTimeMillis()
            )
            _userProfile.value = newProfile
            dbService.saveUserProfile(newProfile, result.token)
            refreshData()
            _toastEvent.emit("Account created! 250 bonus points awarded.")
        }
        _isLoading.value = false
        return result
    }

    fun quickDemoLogin(username: String = "CyberAce") {
        val success = authService.loginAsDemoUser(username)
        val profile = UserProfile(
            uid = success.uid,
            username = username,
            fullName = "$username Player",
            email = success.email,
            points = 350,
            referralCode = "GIVE-${success.uid.takeLast(4).uppercase()}",
            totalJoined = 1,
            totalWins = 1
        )
        _userProfile.value = profile
        // Add a demo joined entry and demo win for instant rich review
        val featuredGW = _giveaways.value.firstOrNull() ?: dbService.getDefaultGiveaways().first()
        val demoEntry = GiveawayEntry(
            giveawayId = featuredGW.id,
            uid = profile.uid,
            entryId = "GW-IPH-89214",
            joinedAt = System.currentTimeMillis() - 3600000L * 3,
            status = "verified",
            giveawayTitle = featuredGW.title,
            giveawayImage = featuredGW.image,
            prize = featuredGW.prize,
            prizeValue = featuredGW.prizeValue,
            endDate = featuredGW.endDate,
            totalParticipants = featuredGW.totalParticipants + 1,
            giveawayStatus = featuredGW.status
        )
        _joinedEntries.value = listOf(demoEntry)

        val demoWin = WinnerRecord(
            giveawayId = "win_demo_mystery",
            giveawayTitle = "Razer Kraken V3 HyperSense Headset",
            prize = "Razer Kraken V3 Pro",
            prizeValue = "$199",
            winnerUID = profile.uid,
            winnerName = profile.username,
            winnerAvatar = "",
            selectedAt = System.currentTimeMillis() - 86400000L * 1,
            claimStatus = "Pending"
        )
        _myWinnings.value = listOf(demoWin)
        _allWinners.value = listOf(demoWin) + dbService.getDefaultWinners()

        scope.launch {
            _toastEvent.emit("Logged in as $username (Demo Mode)")
            refreshData()
        }
    }

    fun logout() {
        authService.clearSession()
        _userProfile.value = null
        _joinedEntries.value = emptyList()
        _myWinnings.value = emptyList()
        _submissions.value = emptyList()
        scope.launch {
            _toastEvent.emit("Logged out successfully")
        }
    }

    fun joinGiveaway(giveaway: Giveaway) {
        val user = _userProfile.value
        if (user == null) {
            scope.launch { _toastEvent.emit("Please log in to enter giveaways") }
            return
        }

        // Check if already joined
        if (_joinedEntries.value.any { it.giveawayId == giveaway.id }) {
            scope.launch { _toastEvent.emit("You are already entered in this giveaway!") }
            return
        }

        // Check points if required
        if (giveaway.entryCostPoints > 0 && user.points < giveaway.entryCostPoints) {
            scope.launch {
                _toastEvent.emit("Insufficient points (${user.points}/${giveaway.entryCostPoints}). Complete tasks to earn more!")
            }
            return
        }

        scope.launch {
            _isLoading.value = true
            val token = authService.getStoredToken()
            val (success, entryId) = dbService.joinGiveaway(giveaway, user.uid, token)

            if (success) {
                val newPoints = user.points - giveaway.entryCostPoints
                val updatedProfile = user.copy(
                    points = newPoints,
                    totalJoined = user.totalJoined + 1
                )
                _userProfile.value = updatedProfile
                dbService.saveUserProfile(updatedProfile, token)

                val newEntry = GiveawayEntry(
                    giveawayId = giveaway.id,
                    uid = user.uid,
                    entryId = entryId,
                    joinedAt = System.currentTimeMillis(),
                    status = "verified",
                    giveawayTitle = giveaway.title,
                    giveawayImage = giveaway.image,
                    prize = giveaway.prize,
                    prizeValue = giveaway.prizeValue,
                    endDate = giveaway.endDate,
                    totalParticipants = giveaway.totalParticipants + 1,
                    giveawayStatus = giveaway.status
                )
                _joinedEntries.value = listOf(newEntry) + _joinedEntries.value

                // Update giveaway participant count in memory
                _giveaways.value = _giveaways.value.map {
                    if (it.id == giveaway.id) it.copy(totalParticipants = it.totalParticipants + 1) else it
                }

                // Add notification
                val notif = NotificationItem(
                    id = "notif_joined_${System.currentTimeMillis()}",
                    title = "Giveaway Entry Confirmed! 🎟️",
                    message = "You joined ${giveaway.title}. Entry ID: $entryId",
                    type = "giveaway"
                )
                _notifications.value = listOf(notif) + _notifications.value

                _toastEvent.emit("🎉 Entry confirmed! Ticket #$entryId")
            } else {
                _toastEvent.emit(entryId)
            }
            _isLoading.value = false
        }
    }

    fun claimPrize(
        giveawayId: String,
        name: String,
        email: String,
        address: String,
        handle: String
    ) {
        scope.launch {
            _isLoading.value = true
            val token = authService.getStoredToken()
            dbService.submitPrizeClaim(giveawayId, name, email, address, handle, token)

            // Update local state
            _myWinnings.value = _myWinnings.value.map { win ->
                if (win.giveawayId == giveawayId) {
                    win.copy(
                        claimStatus = "Under Review",
                        contactName = name,
                        contactEmail = email,
                        shippingAddress = address,
                        handleSocial = handle,
                        claimedAt = System.currentTimeMillis()
                    )
                } else win
            }

            _allWinners.value = _allWinners.value.map { win ->
                if (win.giveawayId == giveawayId) {
                    win.copy(claimStatus = "Under Review", contactName = name)
                } else win
            }

            _toastEvent.emit("📦 Prize claim submitted! Our team will review within 24h.")
            _isLoading.value = false
        }
    }

    fun submitTask(task: RewardTask, proofText: String) {
        val user = _userProfile.value ?: return
        scope.launch {
            _isLoading.value = true
            val token = authService.getStoredToken()
            val submission = dbService.submitTask(task, user.uid, proofText, token)

            _submissions.value = listOf(submission) + _submissions.value

            if (submission.status == "Approved") {
                val updated = user.copy(
                    points = user.points + task.reward,
                    tasksCompleted = user.tasksCompleted + 1
                )
                _userProfile.value = updated
                dbService.saveUserProfile(updated, token)
                _toastEvent.emit("✨ Verified! +${task.reward} points credited.")
            } else {
                _toastEvent.emit("Proof submitted! Under review by moderator.")
            }
            _isLoading.value = false
        }
    }

    fun claimDailyCheckIn() {
        val user = _userProfile.value ?: return
        val todayTaskId = "task_daily_login"
        if (_submissions.value.any { it.taskId == todayTaskId && (System.currentTimeMillis() - it.submittedAt < 86400000L) }) {
            scope.launch { _toastEvent.emit("Already claimed today's check-in bonus! Come back tomorrow.") }
            return
        }

        val dailyTask = _tasks.value.find { it.taskId == todayTaskId } ?: RewardTask(
            taskId = todayTaskId,
            title = "Daily Check-In",
            reward = 15,
            category = "Daily"
        )
        submitTask(dailyTask, "Daily streak check-in")
    }

    fun redeemReferralCode(code: String) {
        val user = _userProfile.value ?: return
        if (code.isBlank() || code.equals(user.referralCode, ignoreCase = true)) {
            scope.launch { _toastEvent.emit("Invalid referral code") }
            return
        }
        scope.launch {
            val updated = user.copy(points = user.points + 50)
            _userProfile.value = updated
            dbService.saveUserProfile(updated, authService.getStoredToken())
            _toastEvent.emit("🎁 Referral code applied! +50 bonus points added.")
        }
    }

    fun updateProfile(fullName: String, bio: String) {
        val user = _userProfile.value ?: return
        scope.launch {
            val updated = user.copy(fullName = fullName, bio = bio)
            _userProfile.value = updated
            dbService.saveUserProfile(updated, authService.getStoredToken())
            _toastEvent.emit("Profile saved successfully!")
        }
    }

    // Admin testing simulator: allows user to simulate a win or approve a task
    fun simulateWinForUser(giveaway: Giveaway) {
        val user = _userProfile.value ?: return
        val win = WinnerRecord(
            giveawayId = giveaway.id,
            giveawayTitle = giveaway.title,
            prize = giveaway.prize,
            prizeValue = giveaway.prizeValue,
            prizeImage = giveaway.image,
            winnerUID = user.uid,
            winnerName = user.username,
            winnerAvatar = user.photoURL,
            selectedAt = System.currentTimeMillis(),
            claimStatus = "Pending"
        )
        _myWinnings.value = listOf(win) + _myWinnings.value.filter { it.giveawayId != giveaway.id }
        _allWinners.value = listOf(win) + _allWinners.value.filter { it.giveawayId != giveaway.id }
        val updatedUser = user.copy(totalWins = user.totalWins + 1)
        _userProfile.value = updatedUser
        scope.launch {
            _toastEvent.emit("🏆 CONGRATULATIONS! You were selected as winner for ${giveaway.title}!")
        }
    }

    fun adminApproveAllSubmissions() {
        val user = _userProfile.value ?: return
        val pending = _submissions.value.filter { it.status == "Under Review" }
        if (pending.isEmpty()) {
            scope.launch { _toastEvent.emit("No pending task submissions to approve.") }
            return
        }
        val bonus = pending.sumOf { it.reward }
        _submissions.value = _submissions.value.map {
            if (it.status == "Under Review") it.copy(status = "Approved", reviewedAt = System.currentTimeMillis()) else it
        }
        val updatedUser = user.copy(
            points = user.points + bonus,
            tasksCompleted = user.tasksCompleted + pending.size
        )
        _userProfile.value = updatedUser
        scope.launch {
            _toastEvent.emit("Admin Approved ${pending.size} tasks! Credited +$bonus points.")
        }
    }

    fun markNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}
