package com.example.socialnetwork.model.entity

data class Report (
    val fromUser: String,
    val createdAt: String,
    val content: String? = null,
    val reason: String? = null
)