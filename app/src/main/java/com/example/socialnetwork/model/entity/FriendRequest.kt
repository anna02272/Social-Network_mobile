package com.example.socialnetwork.model.entity

import java.time.LocalDateTime

data class FriendRequest (
    val id: Long?,
    val approved: Boolean,
    val created_at: LocalDateTime,
    val at: LocalDateTime,
    val fromUser: User,
    val forUser: User
)