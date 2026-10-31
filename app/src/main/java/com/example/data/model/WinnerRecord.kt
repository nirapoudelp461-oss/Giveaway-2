package com.example.data.model

data class WinnerRecord(
    val giveawayId: String = "",
    val giveawayTitle: String = "",
    val prize: String = "",
    val prizeValue: String = "$0",
    val prizeImage: String = "",
    val winnerUID: String = "",
    val winnerName: String = "",
    val winnerAvatar: String = "",
    val selectedAt: Long = System.currentTimeMillis(),
    val claimStatus: String = "Pending", // Pending, Under Review, Approved, Shipped, Delivered, Rejected
    val contactName: String = "",
    val contactEmail: String = "",
    val shippingAddress: String = "",
    val handleSocial: String = "",
    val trackingCode: String = "",
    val claimedAt: Long = 0L
)
