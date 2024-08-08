package com.example.socialnetwork.model.entity

data class Image(
    val id: Long?,
    val path: String?,
    val post: Post?,
    val user: User?
)