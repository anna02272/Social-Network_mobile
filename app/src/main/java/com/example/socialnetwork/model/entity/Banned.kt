package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class Banned(
    val id: Long?,
    val timeStamp: LocalDate,
    val isBlocked: Boolean,
    val groupAdmin: GroupAdmin?,
    val group: Group?,
    val bannedUser: User?,
    val user: User?
)