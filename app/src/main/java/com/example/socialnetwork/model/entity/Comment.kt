package com.example.socialnetwork.model.entity

import java.time.LocalDate

data class Comment(
    val id: Long,
    val text: String,
    val timeStamp: LocalDate,
    val isDeleted: Boolean,
    val user: User?,
    val post: Post,
    val replies: List<Comment> = emptyList(),
    val parentComment: Comment,
    val reactions: List<Reaction> = emptyList(),
    val report: List<Report> = emptyList()
)

data class CreateCommentRequest(
    val text: String
)