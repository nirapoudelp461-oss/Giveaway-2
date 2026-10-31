package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palette according to master prompt specification
val MainBackground = Color(0xFF0B1020)
val SecondaryBackground = Color(0xFF121A2D)
val CardBackground = Color(0xFF18233A)
val CardBackgroundElevated = Color(0xFF1F2E4D)

val PrimaryPurple = Color(0xFF8B5CF6)
val PrimaryPurpleDark = Color(0xFF7C3AED)
val SecondaryBlue = Color(0xFF3B82F6)
val SecondaryBlueDark = Color(0xFF2563EB)
val CyanAccent = Color(0xFF06B6D4)

val GoldRewards = Color(0xFFFBBF24)
val GoldRewardsDark = Color(0xFFD97706)
val SuccessGreen = Color(0xFF22C55E)
val DangerRed = Color(0xFFEF4444)

val MainText = Color(0xFFFFFFFF)
val SecondaryText = Color(0xFF94A3B8)
val MutedText = Color(0xFF64748B)
val BorderColor = Color(0xFF283858)

// Gradients
val PurpleBlueGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryPurple, SecondaryBlue)
)

val GoldRewardsGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFDE047), GoldRewards, GoldRewardsDark)
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0x338B5CF6), Color(0x000B1020))
)

val WinnerCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1F2E4D), Color(0xFF241E38))
)
