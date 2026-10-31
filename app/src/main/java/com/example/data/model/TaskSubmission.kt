package com.example.data.model

data class TaskSubmission(
    val submissionId: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val reward: Int = 0,
    val uid: String = "",
    val proofText: String = "",
    val status: String = "Under Review", // Under Review, Approved, Rejected
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L
)
