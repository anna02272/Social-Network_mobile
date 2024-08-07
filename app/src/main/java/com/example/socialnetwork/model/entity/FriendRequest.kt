package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class FriendRequest (
    val id: Long?,
    val approved: Boolean,
    val created_at: LocalDate,
    val at: LocalDate,
    val fromUser: User,
    val forUser: User
)