package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class Banned(
    var id: Long?,
    var timeStamp: LocalDate,
    var isBlocked: Boolean,
    val groupAdmin: GroupAdmin?,
    val group: Group?,
    var bannedUser: User?,
    var user: User?
)
