package com.example.data.model

data class Giveaway(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val prize: String = "",
    val prizeValue: String = "$0",
    val maxParticipants: Int = 1000,
    val totalParticipants: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 86400000L * 7,
    val status: String = "active", // active, drawing, completed, upcoming
    val category: String = "Tech & Gaming", // Tech & Gaming, Gift Cards, Cash, Merch
    val entryCostPoints: Int = 0,
    val isFeatured: Boolean = false,
    val rules: String = "Open worldwide. Winner drawn transparently on end date."
)
