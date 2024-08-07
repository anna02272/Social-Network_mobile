package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class Report (
    val id: Long?,
    val reason: EReportReason,
    val timestamp: LocalDate,
    val accepted: Boolean,
    val isDeleted: Boolean,
    val user: User,
    val post: Post,
    val comment: Comment,
    val reportedUser: User
)