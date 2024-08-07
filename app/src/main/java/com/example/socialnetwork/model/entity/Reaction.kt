package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class Reaction(
    val id: Long?,
    val type: EReactionType,
    val timeStamp: LocalDate,
    val user: User,
    val post: Post?,
    val comment: Comment?
)

