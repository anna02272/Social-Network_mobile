package com.example.socialnetwork.model.entity

import java.time.LocalDateTime

data class GroupRequest(
    val id: Long?,
    val approved: Boolean,
    val created_at: LocalDateTime,
    val at: LocalDateTime? = null,
    val user: User,
    val group: Group
)
