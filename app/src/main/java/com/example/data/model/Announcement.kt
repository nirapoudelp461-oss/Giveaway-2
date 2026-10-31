package com.example.data.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val tag: String = "Update", // Update, Event, Notice, Winner
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "general", // winner, points, giveaway, system
    val isRead: Boolean = false
)
