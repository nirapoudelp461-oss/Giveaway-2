package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Announcement
import com.example.data.model.Giveaway
import com.example.data.model.GiveawayEntry
import com.example.data.model.UserProfile
import com.example.data.model.WinnerRecord
import com.example.ui.components.FeaturedGiveawayCard
import com.example.ui.components.StandardGiveawayCard
import com.example.ui.components.UserStatsGrid
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.MainBackground
import com.example.ui.theme.MainText
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryBackground
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    giveaways: List<Giveaway>,
    joinedEntries: List<GiveawayEntry>,
    recentWinners: List<WinnerRecord>,
    announcements: List<Announcement>,
    onJoinGiveaway: (Giveaway) -> Unit,
    onSelectGiveaway: (Giveaway) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToWin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Popular") }

    val categories = listOf("All", "Tech & Gaming", "Gift Cards", "Cash")
    val sortOptions = listOf("Popular", "Ending Soon", "Newest")

    // Filter and sort active giveaways
    val filteredGiveaways = giveaways.filter { g ->
        val matchesCategory = selectedCategory == "All" || g.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || g.title.contains(searchQuery, ignoreCase = true) || g.prize.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.let { list ->
        when (selectedSort) {
            "Ending Soon" -> list.sortedBy { it.endDate }
            "Newest" -> list.sortedByDescending { it.startDate }
            else -> list.sortedByDescending { it.totalParticipants }
        }
    }

    val featured = giveaways.find { it.isFeatured } ?: giveaways.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Section 01 B: 4 Attractive Stat Cards
        UserStatsGrid(
            points = userProfile?.points ?: 0,
            joinedCount = joinedEntries.size,
            winsCount = userProfile?.totalWins ?: 0,
            referralsCount = 5
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Access Action Banners
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Claim daily bonus banner
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, GoldRewards.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToTasks() }
                    .padding(12.dp)
                    .testTag("quick_access_tasks_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldRewards.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Bonus",
                            tint = GoldRewards,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Daily Missions", color = MainText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("+15 PTS Ready", color = GoldRewards, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Winners banner
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBackground)
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToWin() }
                    .padding(12.dp)
                    .testTag("quick_access_win_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Wins",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Claim Prizes", color = MainText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Winner Portal", color = SecondaryBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Section 01 C: Featured Giveaway Promotional Card
        if (featured != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Giveaway",
                    color = MainText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "LIVE",
                    color = GoldRewards,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FeaturedGiveawayCard(
                giveaway = featured,
                isJoined = joinedEntries.any { it.giveawayId == featured.id },
                onJoinClick = { onJoinGiveaway(featured) },
                onDetailsClick = { onSelectGiveaway(featured) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section 01 D: Active Giveaways with Category Filters & Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Giveaways",
                color = MainText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${filteredGiveaways.size} Drops Available",
                color = SecondaryText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search gaming gear, cards, cash...", color = SecondaryText, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryPurple, modifier = Modifier.size(20.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("giveaway_search_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = MainText,
                unfocusedTextColor = MainText,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) PrimaryPurple else CardBackground)
                        .border(1.dp, if (isSelected) PrimaryPurple else BorderColor, RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else SecondaryText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active giveaway cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredGiveaways.forEach { g ->
                StandardGiveawayCard(
                    giveaway = g,
                    isJoined = joinedEntries.any { it.giveawayId == g.id },
                    onJoinClick = { onJoinGiveaway(g) },
                    onDetailsClick = { onSelectGiveaway(g) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 01 E: Recent Winners Carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = GoldRewards,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recent Winners",
                    color = MainText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "Verified Draws",
                color = SuccessGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(recentWinners) { winner ->
                WinnerCarouselCard(winner)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 01 F: Announcements
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = SecondaryBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Official Announcements",
                color = MainText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            announcements.forEach { ann ->
                AnnouncementCard(ann)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun WinnerCarouselCard(winner: WinnerRecord) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GoldRewards.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = winner.winnerName.take(1).uppercase(),
                        color = GoldRewards,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = winner.winnerName,
                        color = MainText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Claim: ${winner.claimStatus}",
                        color = SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = winner.prize,
                color = GoldRewards,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = winner.giveawayTitle,
                color = SecondaryText,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AnnouncementCard(ann: Announcement) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SecondaryBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ann.tag,
                        color = SecondaryBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = ann.date,
                    color = SecondaryText,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ann.title,
                color = MainText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ann.content,
                color = SecondaryText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
