package com.example.socialnetwork.model.entity

class Report(
    private val fromUser: String,
    private val createdAt: String,
    private val content: String? = null,
    private val reason: String? = null) {

    fun getFromUser(): String {
        return fromUser
    }

    fun getCreatedAt(): String {
        return createdAt
    }

    fun getContent(): String? {
        return content
    }

    fun getReason(): String? {
        return reason
    }
}
