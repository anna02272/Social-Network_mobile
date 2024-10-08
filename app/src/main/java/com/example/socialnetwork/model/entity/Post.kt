package com.example.socialnetwork.model.entity

import java.time.LocalDateTime

data class Post(
    val id: Long,
    val content: String,
    val creationDate: LocalDateTime,
    val isDeleted: Boolean,
    val user: User?,
    val images: List<Image> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val report: List<Report> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val group: Group?
)

