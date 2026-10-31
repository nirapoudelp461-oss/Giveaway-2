package com.example.data.remote

import android.content.Context
import com.example.data.model.Announcement
import com.example.data.model.Giveaway
import com.example.data.model.GiveawayEntry
import com.example.data.model.NotificationItem
import com.example.data.model.RewardTask
import com.example.data.model.TaskSubmission
import com.example.data.model.UserProfile
import com.example.data.model.WinnerRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseDatabaseService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = FirebaseConfig.DATABASE_URL.trimEnd('/')

    private fun buildUrl(path: String, token: String? = null): String {
        val cleanPath = path.trimStart('/')
        return if (!token.isNullOrEmpty()) {
            "$baseUrl/$cleanPath.json?auth=$token"
        } else {
            "$baseUrl/$cleanPath.json"
        }
    }

    // Helper: Execute GET request and return JSON string
    private suspend fun httpGet(path: String, token: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(path, token)
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (_: Exception) {
            null
        }
    }

    // Helper: Execute PUT/PATCH request
    private suspend fun httpPut(path: String, jsonString: String, token: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(path, token)
            val body = jsonString.toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).put(body).build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun httpPatch(path: String, jsonString: String, token: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(path, token)
            val body = jsonString.toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).patch(body).build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    // --- USERS ---
    suspend fun getUserProfile(uid: String, token: String? = null): UserProfile? = withContext(Dispatchers.IO) {
        val json = httpGet("giveaway/users/$uid", token)
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val obj = JSONObject(json)
                UserProfile(
                    uid = uid,
                    username = obj.optString("username", "Gamer"),
                    fullName = obj.optString("fullName", ""),
                    email = obj.optString("email", ""),
                    photoURL = obj.optString("photoURL", ""),
                    points = obj.optInt("points", 250),
                    referralCode = obj.optString("referralCode", "GIVE-${uid.takeLast(4).uppercase()}"),
                    totalJoined = obj.optInt("totalJoined", 0),
                    totalWins = obj.optInt("totalWins", 0),
                    tasksCompleted = obj.optInt("tasksCompleted", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    isVerified = obj.optBoolean("isVerified", true),
                    bio = obj.optString("bio", "Rewards enthusiast & gaming fan")
                )
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile, token: String? = null): Boolean {
        val json = JSONObject().apply {
            put("uid", profile.uid)
            put("username", profile.username)
            put("fullName", profile.fullName)
            put("email", profile.email)
            put("photoURL", profile.photoURL)
            put("points", profile.points)
            put("referralCode", profile.referralCode)
            put("totalJoined", profile.totalJoined)
            put("totalWins", profile.totalWins)
            put("tasksCompleted", profile.tasksCompleted)
            put("createdAt", profile.createdAt)
            put("isVerified", profile.isVerified)
            put("bio", profile.bio)
        }.toString()
        return httpPut("giveaway/users/${profile.uid}", json, token)
    }

    // --- GIVEAWAYS ---
    suspend fun fetchGiveaways(token: String? = null): List<Giveaway> = withContext(Dispatchers.IO) {
        val json = httpGet("giveaway/giveaways", token)
        val list = mutableListOf<Giveaway>()
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val root = JSONObject(json)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = root.optJSONObject(key) ?: continue
                    list.add(
                        Giveaway(
                            id = key,
                            title = item.optString("title", "Giveaway"),
                            description = item.optString("description", ""),
                            image = item.optString("image", ""),
                            prize = item.optString("prize", ""),
                            prizeValue = item.optString("prizeValue", "$0"),
                            maxParticipants = item.optInt("maxParticipants", 1000),
                            totalParticipants = item.optInt("totalParticipants", 0),
                            startDate = item.optLong("startDate", System.currentTimeMillis()),
                            endDate = item.optLong("endDate", System.currentTimeMillis() + 86400000L * 7),
                            status = item.optString("status", "active"),
                            category = item.optString("category", "Tech & Gaming"),
                            entryCostPoints = item.optInt("entryCostPoints", 0),
                            isFeatured = item.optBoolean("isFeatured", false),
                            rules = item.optString("rules", "Open worldwide. Fair draw verified.")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        if (list.isEmpty()) {
            val defaults = getDefaultGiveaways()
            // Try seeding to remote
            seedGiveaways(defaults, token)
            defaults
        } else {
            list
        }
    }

    private suspend fun seedGiveaways(giveaways: List<Giveaway>, token: String? = null) {
        val root = JSONObject()
        giveaways.forEach { g ->
            val obj = JSONObject().apply {
                put("title", g.title)
                put("description", g.description)
                put("image", g.image)
                put("prize", g.prize)
                put("prizeValue", g.prizeValue)
                put("maxParticipants", g.maxParticipants)
                put("totalParticipants", g.totalParticipants)
                put("startDate", g.startDate)
                put("endDate", g.endDate)
                put("status", g.status)
                put("category", g.category)
                put("entryCostPoints", g.entryCostPoints)
                put("isFeatured", g.isFeatured)
                put("rules", g.rules)
            }
            root.put(g.id, obj)
        }
        httpPut("giveaway/giveaways", root.toString(), token)
    }

    // --- ENTRIES ---
    suspend fun joinGiveaway(
        giveaway: Giveaway,
        uid: String,
        token: String? = null
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val checkJson = httpGet("giveaway/entries/${giveaway.id}/$uid", token)
        if (!checkJson.isNullOrBlank() && checkJson != "null") {
            return@withContext Pair(false, "You have already joined this giveaway!")
        }

        val entryId = "GW-${giveaway.id.take(3).uppercase()}-${(10000..99999).random()}"
        val entryObj = JSONObject().apply {
            put("entryId", entryId)
            put("joinedAt", System.currentTimeMillis())
            put("status", "verified")
            put("giveawayTitle", giveaway.title)
            put("giveawayImage", giveaway.image)
            put("prize", giveaway.prize)
            put("prizeValue", giveaway.prizeValue)
            put("endDate", giveaway.endDate)
            put("totalParticipants", giveaway.totalParticipants + 1)
            put("giveawayStatus", giveaway.status)
        }

        val success = httpPut("giveaway/entries/${giveaway.id}/$uid", entryObj.toString(), token)
        if (success) {
            // Increment giveaway participant count
            val patch = JSONObject().put("totalParticipants", giveaway.totalParticipants + 1).toString()
            httpPatch("giveaway/giveaways/${giveaway.id}", patch, token)
        }
        Pair(true, entryId)
    }

    suspend fun fetchUserEntries(uid: String, token: String? = null): List<GiveawayEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<GiveawayEntry>()
        val json = httpGet("giveaway/entries", token)
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val root = JSONObject(json)
                val giveawayKeys = root.keys()
                while (giveawayKeys.hasNext()) {
                    val gId = giveawayKeys.next()
                    val gObj = root.optJSONObject(gId) ?: continue
                    val userEntry = gObj.optJSONObject(uid) ?: continue
                    list.add(
                        GiveawayEntry(
                            giveawayId = gId,
                            uid = uid,
                            entryId = userEntry.optString("entryId", "GW-$gId"),
                            joinedAt = userEntry.optLong("joinedAt", System.currentTimeMillis()),
                            status = userEntry.optString("status", "verified"),
                            giveawayTitle = userEntry.optString("giveawayTitle", "Giveaway Event"),
                            giveawayImage = userEntry.optString("giveawayImage", ""),
                            prize = userEntry.optString("prize", "Prize"),
                            prizeValue = userEntry.optString("prizeValue", "$0"),
                            endDate = userEntry.optLong("endDate", 0L),
                            totalParticipants = userEntry.optInt("totalParticipants", 1),
                            giveawayStatus = userEntry.optString("giveawayStatus", "active")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        list
    }

    // --- WINNERS & CLAIMS ---
    suspend fun fetchWinners(token: String? = null): List<WinnerRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WinnerRecord>()
        val json = httpGet("giveaway/winners", token)
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val root = JSONObject(json)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = root.optJSONObject(key) ?: continue
                    list.add(
                        WinnerRecord(
                            giveawayId = key,
                            giveawayTitle = item.optString("giveawayTitle", "Winner Event"),
                            prize = item.optString("prize", "Grand Prize"),
                            prizeValue = item.optString("prizeValue", "$500"),
                            prizeImage = item.optString("prizeImage", ""),
                            winnerUID = item.optString("winnerUID", ""),
                            winnerName = item.optString("winnerName", "LuckyWinner"),
                            winnerAvatar = item.optString("winnerAvatar", ""),
                            selectedAt = item.optLong("selectedAt", System.currentTimeMillis()),
                            claimStatus = item.optString("claimStatus", "Pending"),
                            contactName = item.optString("contactName", ""),
                            contactEmail = item.optString("contactEmail", ""),
                            shippingAddress = item.optString("shippingAddress", ""),
                            handleSocial = item.optString("handleSocial", ""),
                            trackingCode = item.optString("trackingCode", ""),
                            claimedAt = item.optLong("claimedAt", 0L)
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        if (list.isEmpty()) {
            val defaults = getDefaultWinners()
            seedWinners(defaults, token)
            defaults
        } else {
            list
        }
    }

    private suspend fun seedWinners(winners: List<WinnerRecord>, token: String? = null) {
        val root = JSONObject()
        winners.forEach { w ->
            val obj = JSONObject().apply {
                put("giveawayTitle", w.giveawayTitle)
                put("prize", w.prize)
                put("prizeValue", w.prizeValue)
                put("prizeImage", w.prizeImage)
                put("winnerUID", w.winnerUID)
                put("winnerName", w.winnerName)
                put("winnerAvatar", w.winnerAvatar)
                put("selectedAt", w.selectedAt)
                put("claimStatus", w.claimStatus)
                put("contactName", w.contactName)
                put("contactEmail", w.contactEmail)
                put("shippingAddress", w.shippingAddress)
                put("handleSocial", w.handleSocial)
                put("trackingCode", w.trackingCode)
                put("claimedAt", w.claimedAt)
            }
            root.put(w.giveawayId, obj)
        }
        httpPut("giveaway/winners", root.toString(), token)
    }

    suspend fun submitPrizeClaim(
        giveawayId: String,
        contactName: String,
        contactEmail: String,
        shippingAddress: String,
        handleSocial: String,
        token: String? = null
    ): Boolean {
        val patch = JSONObject().apply {
            put("contactName", contactName)
            put("contactEmail", contactEmail)
            put("shippingAddress", shippingAddress)
            put("handleSocial", handleSocial)
            put("claimStatus", "Under Review")
            put("claimedAt", System.currentTimeMillis())
        }.toString()
        return httpPatch("giveaway/winners/$giveawayId", patch, token)
    }

    // --- TASKS & SUBMISSIONS ---
    suspend fun fetchTasks(token: String? = null): List<RewardTask> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RewardTask>()
        val json = httpGet("giveaway/tasks", token)
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val root = JSONObject(json)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = root.optJSONObject(key) ?: continue
                    list.add(
                        RewardTask(
                            taskId = key,
                            title = item.optString("title", "Reward Task"),
                            description = item.optString("description", ""),
                            reward = item.optInt("reward", 10),
                            category = item.optString("category", "Daily"),
                            iconName = item.optString("iconName", "star"),
                            requiresProof = item.optBoolean("requiresProof", false),
                            proofPlaceholder = item.optString("proofPlaceholder", "Proof description"),
                            externalUrl = item.optString("externalUrl", ""),
                            maxProgress = item.optInt("maxProgress", 1),
                            currentProgress = item.optInt("currentProgress", 0),
                            status = item.optString("status", "available")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        if (list.isEmpty()) {
            val defaults = getDefaultTasks()
            seedTasks(defaults, token)
            defaults
        } else {
            list
        }
    }

    private suspend fun seedTasks(tasks: List<RewardTask>, token: String? = null) {
        val root = JSONObject()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("title", t.title)
                put("description", t.description)
                put("reward", t.reward)
                put("category", t.category)
                put("iconName", t.iconName)
                put("requiresProof", t.requiresProof)
                put("proofPlaceholder", t.proofPlaceholder)
                put("externalUrl", t.externalUrl)
                put("maxProgress", t.maxProgress)
                put("currentProgress", t.currentProgress)
                put("status", t.status)
            }
            root.put(t.taskId, obj)
        }
        httpPut("giveaway/tasks", root.toString(), token)
    }

    suspend fun submitTask(
        task: RewardTask,
        uid: String,
        proofText: String,
        token: String? = null
    ): TaskSubmission = withContext(Dispatchers.IO) {
        val subId = "sub_${System.currentTimeMillis()}"
        val status = if (task.requiresProof) "Under Review" else "Approved"
        val sub = TaskSubmission(
            submissionId = subId,
            taskId = task.taskId,
            taskTitle = task.title,
            reward = task.reward,
            uid = uid,
            proofText = proofText,
            status = status,
            submittedAt = System.currentTimeMillis(),
            reviewedAt = if (status == "Approved") System.currentTimeMillis() else 0L
        )

        val obj = JSONObject().apply {
            put("submissionId", sub.submissionId)
            put("taskId", sub.taskId)
            put("taskTitle", sub.taskTitle)
            put("reward", sub.reward)
            put("uid", sub.uid)
            put("proofText", sub.proofText)
            put("status", sub.status)
            put("submittedAt", sub.submittedAt)
            put("reviewedAt", sub.reviewedAt)
        }
        httpPut("giveaway/taskSubmissions/$uid/$subId", obj.toString(), token)
        sub
    }

    suspend fun fetchUserSubmissions(uid: String, token: String? = null): List<TaskSubmission> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TaskSubmission>()
        val json = httpGet("giveaway/taskSubmissions/$uid", token)
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val root = JSONObject(json)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = root.optJSONObject(key) ?: continue
                    list.add(
                        TaskSubmission(
                            submissionId = key,
                            taskId = item.optString("taskId", ""),
                            taskTitle = item.optString("taskTitle", ""),
                            reward = item.optInt("reward", 0),
                            uid = uid,
                            proofText = item.optString("proofText", ""),
                            status = item.optString("status", "Under Review"),
                            submittedAt = item.optLong("submittedAt", 0L),
                            reviewedAt = item.optLong("reviewedAt", 0L)
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        list
    }

    // --- ANNOUNCEMENTS ---
    suspend fun fetchAnnouncements(token: String? = null): List<Announcement> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Announcement>()
        val json = httpGet("giveaway/announcements", token)
        if (!json.isNullOrBlank() && json != "null") {
            try {
                val root = JSONObject(json)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = root.optJSONObject(key) ?: continue
                    list.add(
                        Announcement(
                            id = key,
                            title = item.optString("title", ""),
                            content = item.optString("content", ""),
                            date = item.optString("date", "Today"),
                            tag = item.optString("tag", "Update"),
                            timestamp = item.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        if (list.isEmpty()) {
            val defaults = getDefaultAnnouncements()
            seedAnnouncements(defaults, token)
            defaults
        } else {
            list
        }
    }

    private suspend fun seedAnnouncements(announcements: List<Announcement>, token: String? = null) {
        val root = JSONObject()
        announcements.forEach { a ->
            val obj = JSONObject().apply {
                put("title", a.title)
                put("content", a.content)
                put("date", a.date)
                put("tag", a.tag)
                put("timestamp", a.timestamp)
            }
            root.put(a.id, obj)
        }
        httpPut("giveaway/announcements", root.toString(), token)
    }

    // --- DEFAULT CATALOG GENERATORS ---
    fun getDefaultGiveaways(): List<Giveaway> {
        val now = System.currentTimeMillis()
        val day = 86400000L
        return listOf(
            Giveaway(
                id = "gw_iphone16",
                title = "iPhone 16 Pro Max 1TB",
                description = "Unleash extreme mobile power with Apple's titanium flagship. Complete with ProMotion OLED and A18 Pro chip.",
                image = "res://giveaway_iphone_1793446725071",
                prize = "iPhone 16 Pro Max (Black Titanium)",
                prizeValue = "$1,599",
                maxParticipants = 5000,
                totalParticipants = 2418,
                startDate = now - day * 2,
                endDate = now + day * 5 + 3600000L * 14,
                status = "active",
                category = "Tech & Gaming",
                entryCostPoints = 0,
                isFeatured = true,
                rules = "Open worldwide. Free entry. Must have completed profile."
            ),
            Giveaway(
                id = "gw_ps5pro",
                title = "PlayStation 5 Pro Console Bundle",
                description = "Next-gen ray tracing, PSSR 4K resolution, dual DualSense Wireless Controllers, plus 12 Months PS Plus Deluxe.",
                image = "res://giveaway_ps5_1793446737535",
                prize = "PS5 Pro + 2 DualSense Controllers",
                prizeValue = "$899",
                maxParticipants = 3500,
                totalParticipants = 1840,
                startDate = now - day * 1,
                endDate = now + day * 3 + 3600000L * 8,
                status = "active",
                category = "Tech & Gaming",
                entryCostPoints = 0,
                isFeatured = false,
                rules = "Verified members only. Instant winner notification upon draw."
            ),
            Giveaway(
                id = "gw_steam500",
                title = "$500 Steam & Amazon Gift Cards",
                description = "Upgrade your gaming library! Instant digital code redeemable on Steam or Amazon anywhere in the world.",
                image = "",
                prize = "$500 Digital Gift Card of Choice",
                prizeValue = "$500",
                maxParticipants = 2000,
                totalParticipants = 982,
                startDate = now - day * 3,
                endDate = now + day * 2 + 3600000L * 5,
                status = "active",
                category = "Gift Cards",
                entryCostPoints = 10,
                isFeatured = false,
                rules = "Costs 10 Reward Points. Winner receives digital code instantly."
            ),
            Giveaway(
                id = "gw_razer_blade",
                title = "Razer Blade 16 Gaming Laptop",
                description = "Intel Core i9-14900HX, RTX 4090, 32GB DDR5, Dual-Mode Mini-LED Display.",
                image = "",
                prize = "Razer Blade 16 Ultra Beast",
                prizeValue = "$3,299",
                maxParticipants = 10000,
                totalParticipants = 4520,
                startDate = now - day * 4,
                endDate = now + day * 9,
                status = "active",
                category = "Tech & Gaming",
                entryCostPoints = 0,
                isFeatured = false,
                rules = "Shipped worldwide with tracking & insurance."
            ),
            Giveaway(
                id = "gw_cash1000",
                title = "$1,000 PayPal or Crypto Bounty",
                description = "Pure cash reward sent directly to your verified PayPal address or USDT/ETH wallet.",
                image = "",
                prize = "$1,000 Direct Cash Transfer",
                prizeValue = "$1,000",
                maxParticipants = 8000,
                totalParticipants = 3120,
                startDate = now - day * 5,
                endDate = now + day * 1 + 3600000L * 18,
                status = "active",
                category = "Cash",
                entryCostPoints = 15,
                isFeatured = false,
                rules = "18+ only. Instant transfer within 24 hours of claim."
            )
        )
    }

    fun getDefaultTasks(): List<RewardTask> {
        return listOf(
            RewardTask(
                taskId = "task_daily_login",
                title = "Daily Check-In",
                description = "Claim your daily attendance points and build up your streak reward multiplier.",
                reward = 5,
                category = "Daily",
                iconName = "calendar",
                requiresProof = false,
                status = "available"
            ),
            RewardTask(
                taskId = "task_profile",
                title = "Complete Your Profile",
                description = "Fill in your full name, gamer bio, and social handle for 100% account verification.",
                reward = 20,
                category = "Profile",
                iconName = "badge",
                requiresProof = false,
                status = "available"
            ),
            RewardTask(
                taskId = "task_invite",
                title = "Invite Friends & Squad",
                description = "Share your unique referral code with friends. Earn 30 points for each friend that joins.",
                reward = 30,
                category = "Referral",
                iconName = "share",
                requiresProof = false,
                status = "available"
            ),
            RewardTask(
                taskId = "task_twitter",
                title = "Follow @GiveawayHub on X",
                description = "Follow our official announcement channel on X / Twitter and submit your handle.",
                reward = 10,
                category = "Social",
                iconName = "social",
                requiresProof = true,
                proofPlaceholder = "Enter your @X_username",
                externalUrl = "https://twitter.com",
                status = "available"
            ),
            RewardTask(
                taskId = "task_discord",
                title = "Join Discord Community",
                description = "Hop into our Discord server to participate in flash drops, secret codes, and voice events.",
                reward = 15,
                category = "Social",
                iconName = "discord",
                requiresProof = true,
                proofPlaceholder = "Enter your Discord Username#0000",
                externalUrl = "https://discord.com",
                status = "available"
            ),
            RewardTask(
                taskId = "task_promo_video",
                title = "Watch Partner Teaser",
                description = "Watch the 30-second gaming hardware showcase video to claim instant reward points.",
                reward = 5,
                category = "Video",
                iconName = "play",
                requiresProof = false,
                status = "available"
            ),
            RewardTask(
                taskId = "task_share_giveaway",
                title = "Share Active Giveaway",
                description = "Share the iPhone 16 Pro Max giveaway link to any social media or community group.",
                reward = 10,
                category = "Social",
                iconName = "share",
                requiresProof = true,
                proofPlaceholder = "Paste link to your post or tweet",
                status = "available"
            )
        )
    }

    fun getDefaultWinners(): List<WinnerRecord> {
        val now = System.currentTimeMillis()
        val day = 86400000L
        return listOf(
            WinnerRecord(
                giveawayId = "win_iphone15_past",
                giveawayTitle = "iPhone 15 Pro Max 512GB",
                prize = "iPhone 15 Pro Max",
                prizeValue = "$1,399",
                winnerUID = "usr_alex_88",
                winnerName = "Alex Rivera",
                winnerAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                selectedAt = now - day * 3,
                claimStatus = "Delivered",
                trackingCode = "FEDEX-882941094",
                contactName = "Alex Rivera",
                shippingAddress = "Austin, TX, USA",
                claimedAt = now - day * 2
            ),
            WinnerRecord(
                giveawayId = "win_ps5_spiderman",
                giveawayTitle = "PS5 Spider-Man 2 Limited Edition",
                prize = "PS5 Limited Edition Console",
                prizeValue = "$649",
                winnerUID = "usr_elena_22",
                winnerName = "Elena Rostova",
                winnerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                selectedAt = now - day * 7,
                claimStatus = "Shipped",
                trackingCode = "DHL-559102834",
                contactName = "Elena Rostova",
                shippingAddress = "Berlin, Germany",
                claimedAt = now - day * 6
            ),
            WinnerRecord(
                giveawayId = "win_steam250",
                giveawayTitle = "$250 Steam Digital Gift Card",
                prize = "$250 Steam Card",
                prizeValue = "$250",
                winnerUID = "usr_marcus_4",
                winnerName = "Marcus Chen",
                winnerAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                selectedAt = now - day * 12,
                claimStatus = "Delivered",
                trackingCode = "DIGITAL-CODE-REDEEMED",
                contactName = "Marcus Chen",
                shippingAddress = "Digital Delivery",
                claimedAt = now - day * 12
            )
        )
    }

    fun getDefaultAnnouncements(): List<Announcement> {
        return listOf(
            Announcement(
                id = "ann_1",
                title = "Welcome to Giveaway Hub v2.0",
                content = "Our official rewards ecosystem is live! Enter verified drops, complete daily missions, and climb the leaderboard.",
                date = "Oct 2026",
                tag = "Announcement"
            ),
            Announcement(
                id = "ann_2",
                title = "Mega Drop: iPhone 16 Pro Max 1TB Live",
                content = "The featured iPhone 16 Pro Max drop is open to all members with zero entry fees. Winner drawn transparently at countdown completion.",
                date = "Live Now",
                tag = "Featured"
            ),
            Announcement(
                id = "ann_3",
                title = "Weekly Winner Draw Security Notice",
                content = "All prize draws are computed with verifiable pseudo-random seeds. Never share passwords or payment details with anyone.",
                date = "Notice",
                tag = "Security"
            )
        )
    }
}
