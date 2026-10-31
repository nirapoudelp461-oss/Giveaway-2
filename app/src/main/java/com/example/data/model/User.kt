package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val photoURL: String = "",
    val points: Int = 100,
    val referralCode: String = "",
    val totalJoined: Int = 0,
    val totalWins: Int = 0,
    val tasksCompleted: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = true,
    val bio: String = "Rewards enthusiast & gaming fan"
)
