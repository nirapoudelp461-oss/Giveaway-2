package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.MainBackground
import com.example.ui.theme.MainText
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryBackground
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onUpdateProfile: (name: String, bio: String) -> Unit,
    onRedeemReferral: (String) -> Unit,
    onOpenAdminSim: () -> Unit,
    onLogout: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf<String?>(null) } // "terms" or "privacy"
    var referralCodeInput by remember { mutableStateOf("") }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var emailAlertsEnabled by remember { mutableStateOf(true) }

    val user = userProfile ?: UserProfile()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Section 05: Profile Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with Trophy/Gamer Badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.2f))
                            .border(2.dp, PrimaryPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.giveaway_logo_1793446619929),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.username,
                                color = MainText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Member",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = user.email.ifBlank { "member@giveaway.gg" },
                            color = SecondaryText,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "UID: ${user.uid.take(12)}...",
                            color = SecondaryText.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryBackground)
                            .testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "“${user.bio}”",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Statistics Row
        Text(
            text = "Account Statistics",
            color = MainText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatPill(
                title = "Entries",
                value = "${user.totalJoined}",
                icon = Icons.Default.CardGiftcard,
                accentColor = PrimaryPurple,
                modifier = Modifier.weight(1f)
            )
            ProfileStatPill(
                title = "Wins",
                value = "${user.totalWins}",
                icon = Icons.Default.EmojiEvents,
                accentColor = GoldRewards,
                modifier = Modifier.weight(1f)
            )
            ProfileStatPill(
                title = "Missions",
                value = "${user.tasksCompleted}",
                icon = Icons.Default.TaskAlt,
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            ProfileStatPill(
                title = "Points",
                value = "${user.points}",
                icon = Icons.Default.Stars,
                accentColor = GoldRewards,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Referral System Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardBackground)
                .border(1.dp, SecondaryBlue.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondaryBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Referral",
                                tint = SecondaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Referral Program",
                            color = MainText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "+30 PTS / friend",
                        color = GoldRewards,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Share your unique referral code. Earn 30 Points whenever a friend joins Giveaway Hub!",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Referral Code Box with Copy & Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryBackground)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("YOUR UNIQUE CODE", color = SecondaryText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = user.referralCode.ifBlank { "GIVE-ACE99" },
                            color = PrimaryPurple,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Giveaway Referral Code", user.referralCode)
                                clipboard.setPrimaryClip(clip)
                                onShowToast("Referral code copied to clipboard!")
                            },
                            modifier = Modifier.testTag("copy_referral_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                tint = MainText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Join Giveaway Hub with my referral code ${user.referralCode} to get 50 free bonus points: https://giveaway-5569c.firebaseapp.com")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Referral Code")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.testTag("share_referral_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = SecondaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Redeem referral input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = referralCodeInput,
                        onValueChange = { referralCodeInput = it },
                        placeholder = { Text("Have a referral code?", fontSize = 12.sp, color = SecondaryText) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("redeem_code_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = MainText,
                            unfocusedTextColor = MainText
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (referralCodeInput.isNotBlank()) {
                                onRedeemReferral(referralCodeInput)
                                referralCodeInput = ""
                            }
                        },
                        modifier = Modifier.height(48.dp).testTag("redeem_code_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Settings & Preferences
        Text(
            text = "Settings & Preferences",
            color = MainText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                // Push Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Giveaway Drop Notifications", color = MainText, fontSize = 13.sp)
                    }
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurple.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Email Alerts Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = GoldRewards, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Winner Email Alerts", color = MainText, fontSize = 13.sp)
                    }
                    Switch(
                        checked = emailAlertsEnabled,
                        onCheckedChange = { emailAlertsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldRewards, checkedTrackColor = GoldRewards.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Admin simulation drawer trigger (Section 10)
                SettingsRowItem(
                    title = "Admin Testing & Winner Simulator",
                    subtitle = "Simulate winning draws & task verification",
                    icon = Icons.Default.AdminPanelSettings,
                    iconColor = PrimaryPurple,
                    onClick = onOpenAdminSim
                )

                // Terms and Conditions
                SettingsRowItem(
                    title = "Terms & Rules of Participation",
                    subtitle = "Fair play and draw rules",
                    icon = Icons.Default.Policy,
                    iconColor = SecondaryBlue,
                    onClick = { showLegalDialog = "terms" }
                )

                // Privacy Policy
                SettingsRowItem(
                    title = "Privacy Policy",
                    subtitle = "Zero-share data policy",
                    icon = Icons.Default.HelpOutline,
                    iconColor = SecondaryBlue,
                    onClick = { showLegalDialog = "privacy" }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        OutlinedButton(
            onClick = { showLogoutConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f))
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out from Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(user.fullName) }
        var editBio by remember { mutableStateOf(user.bio) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SecondaryBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Edit Profile Details", color = MainText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name / Gamer Tag") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = MainText,
                            unfocusedTextColor = MainText
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio / Status") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_bio_input"),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = MainText,
                            unfocusedTextColor = MainText
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("Cancel", color = SecondaryText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateProfile(editName, editBio)
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log Out?", color = MainText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out from Giveaway Hub?", color = SecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = SecondaryBackground
        )
    }

    // Terms or Privacy dialog
    if (showLegalDialog != null) {
        Dialog(onDismissRequest = { showLegalDialog = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SecondaryBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (showLegalDialog == "terms") "Terms & Conditions" else "Privacy Policy",
                        color = MainText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (showLegalDialog == "terms")
                            "1. All giveaway draws are transparent and audited.\n2. One entry per user per giveaway unless specified.\n3. Winners must submit shipping details within 14 days of selection.\n4. Reward points hold no cash conversion outside platform redemption.\n5. Fraudulent activity or multi-accounting results in immediate forfeiture."
                        else
                            "We respect your privacy. Delivery addresses and contact handles provided for prize fulfillment are used strictly for shipping physical rewards or dispatching digital gift codes. Your data is encrypted and never sold to third parties.",
                        color = SecondaryText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showLegalDialog = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Understood", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatPill(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = MainText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                color = SecondaryText,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MainText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = SecondaryText, fontSize = 10.sp)
        }
    }
}
