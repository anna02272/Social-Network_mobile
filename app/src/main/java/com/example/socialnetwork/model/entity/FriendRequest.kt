package com.example.socialnetwork.model.entity

class FriendRequest(
    private val fromUser: String,
    private val createdAt: String) {

    fun getFromUser(): String {
        return fromUser
    }

    fun getCreatedAt(): String {
        return createdAt
    }

}
