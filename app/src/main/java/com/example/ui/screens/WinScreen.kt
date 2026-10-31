package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WinnerRecord
import com.example.ui.components.ClaimPrizeDialog
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.GoldRewardsGradient
import com.example.ui.theme.MainBackground
import com.example.ui.theme.MainText
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PurpleBlueGradient
import com.example.ui.theme.SecondaryBackground
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen

@Composable
fun WinScreen(
    myWinnings: List<WinnerRecord>,
    communityWinners: List<WinnerRecord>,
    onSubmitClaim: (giveawayId: String, name: String, email: String, address: String, handle: String) -> Unit,
    onOpenAdminSim: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWinningToClaim by remember { mutableStateOf<WinnerRecord?>(null) }
    var activeTab by remember { mutableStateOf("My Winnings") } // My Winnings, Hall of Fame

    val pendingClaims = myWinnings.count { it.claimStatus == "Pending" || it.claimStatus == "Under Review" }
    val claimedPrizes = myWinnings.count { it.claimStatus == "Approved" || it.claimStatus == "Shipped" || it.claimStatus == "Delivered" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Page Title & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Winner Portal",
                    color = MainText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Transparent Draws & Prize Fulfillment",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldRewards.copy(alpha = 0.2f))
                    .border(1.dp, GoldRewards.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = GoldRewards,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${myWinnings.size} Wins",
                        color = GoldRewards,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 03 A: My Winnings Summary Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WinStatItem(
                title = "Total Wins",
                value = "${myWinnings.size}",
                accentColor = GoldRewards,
                modifier = Modifier.weight(1f)
            )
            WinStatItem(
                title = "Pending",
                value = "$pendingClaims",
                accentColor = SecondaryBlue,
                modifier = Modifier.weight(1f)
            )
            WinStatItem(
                title = "Claimed",
                value = "$claimedPrizes",
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Selector: My Winnings vs Hall of Fame
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == "My Winnings") PrimaryPurple else Color.Transparent)
                    .clickable { activeTab = "My Winnings" }
                    .testTag("win_tab_my_winnings"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "My Winnings (${myWinnings.size})",
                    color = if (activeTab == "My Winnings") Color.White else SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == "Hall of Fame") PrimaryPurple else Color.Transparent)
                    .clickable { activeTab = "Hall of Fame" }
                    .testTag("win_tab_hall_of_fame"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hall of Fame (${communityWinners.size})",
                    color = if (activeTab == "Hall of Fame") Color.White else SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Content
        if (activeTab == "My Winnings") {
            if (myWinnings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(CardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "No wins",
                                tint = GoldRewards,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Prizes Won Yet",
                            color = MainText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter active drops and complete daily missions to boost your chances! Or open the Admin Simulator to test claiming a prize.",
                            color = SecondaryText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onOpenAdminSim,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.testTag("simulate_win_trigger_button")
                        ) {
                            Text("Simulate a Win (Test Claim Flow)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myWinnings) { win ->
                        MyWinningCard(
                            winner = win,
                            onClaimClick = { selectedWinningToClaim = win }
                        )
                    }
                }
            }
        } else {
            // Hall of Fame
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(communityWinners) { winner ->
                    HallOfFameCard(winner)
                }
            }
        }
    }

    if (selectedWinningToClaim != null) {
        ClaimPrizeDialog(
            winner = selectedWinningToClaim!!,
            onDismiss = { selectedWinningToClaim = null },
            onSubmitClaim = { name, email, address, handle ->
                onSubmitClaim(selectedWinningToClaim!!.giveawayId, name, email, address, handle)
            }
        )
    }
}

@Composable
fun WinStatItem(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = title, color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun MyWinningCard(
    winner: WinnerRecord,
    onClaimClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.5.dp, GoldRewards.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Won",
                        tint = GoldRewards,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WINNER SELECTION",
                        color = GoldRewards,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (winner.claimStatus) {
                                "Approved", "Delivered" -> SuccessGreen.copy(alpha = 0.2f)
                                "Shipped" -> SecondaryBlue.copy(alpha = 0.2f)
                                "Under Review" -> PrimaryPurple.copy(alpha = 0.2f)
                                else -> GoldRewards.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = winner.claimStatus,
                        color = when (winner.claimStatus) {
                            "Approved", "Delivered" -> SuccessGreen
                            "Shipped" -> SecondaryBlue
                            "Under Review" -> PrimaryPurple
                            else -> GoldRewards
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = winner.prize,
                color = MainText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${winner.giveawayTitle} • Retail: ${winner.prizeValue}",
                color = SecondaryText,
                fontSize = 12.sp
            )

            if (winner.trackingCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Tracking",
                        tint = SecondaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tracking: ${winner.trackingCode}",
                        color = SecondaryBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action button
            Button(
                onClick = onClaimClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("claim_prize_button_${winner.giveawayId}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (winner.claimStatus == "Pending") GoldRewards else PrimaryPurple
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = if (winner.claimStatus == "Pending") MainBackground else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (winner.claimStatus == "Pending") "Claim Prize Now" else "View / Update Delivery Details",
                    color = if (winner.claimStatus == "Pending") MainBackground else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun HallOfFameCard(winner: WinnerRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = winner.winnerName.take(1).uppercase(),
                    color = PrimaryPurple,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = winner.winnerName,
                    color = MainText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Won: ${winner.prize}",
                    color = GoldRewards,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = winner.giveawayTitle,
                    color = SecondaryText,
                    fontSize = 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = winner.claimStatus,
                    color = SuccessGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
