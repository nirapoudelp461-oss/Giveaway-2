package com.example.data.model

data class RewardTask(
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val reward: Int = 10,
    val category: String = "Daily", // Daily, Social, Referral, Video, Profile
    val iconName: String = "star",
    val requiresProof: Boolean = false,
    val proofPlaceholder: String = "Enter username or screenshot link",
    val externalUrl: String = "",
    val maxProgress: Int = 1,
    val currentProgress: Int = 0,
    val status: String = "available" // available, submitted, completed
)
