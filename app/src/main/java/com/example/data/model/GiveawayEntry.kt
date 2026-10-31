package com.example.data.model

data class GiveawayEntry(
    val giveawayId: String = "",
    val uid: String = "",
    val entryId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val status: String = "verified", // verified, pending, won, ended
    val giveawayTitle: String = "",
    val giveawayImage: String = "",
    val prize: String = "",
    val prizeValue: String = "$0",
    val endDate: Long = 0L,
    val totalParticipants: Int = 0,
    val giveawayStatus: String = "active"
)
