package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class GroupRequest(
    val id: Long?,
    val approved: Boolean,
    val created_at: LocalDate,
    val at: LocalDate,
    val user: User,
    val group: Group
)
