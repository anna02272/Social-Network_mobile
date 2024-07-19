package com.example.socialnetwork.model

class Comment(
    private val profileImageResource: Int,
    private val username: String,
    private val date: String,
    private val content: String) {

    fun getProfileImageResource(): Int {
        return profileImageResource
    }

    fun getUsername(): String {
        return username
    }

    fun getDate(): String {
        return date
    }

    fun getContent(): String {
        return content
    }
}
