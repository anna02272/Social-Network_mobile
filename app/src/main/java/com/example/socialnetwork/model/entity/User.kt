package com.example.socialnetwork.model.entity

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val password: String? = null,
    val email: String,
    val lastLogin: LocalDateTime? = null,
    val firstName: String,
    val lastName: String,
    val type: EUserType = EUserType.USER,
    val description: String? = null,
    val profileName: String? = null,
    val image: Image? = null,
    val groupAdmins: List<GroupAdmin> = emptyList(),
    val post: List<Post> = emptyList(),
    val reaction: List<Reaction> = emptyList(),
    val report: List<Report> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val groupRequest: List<GroupRequest> = emptyList(),
    val sentFriendRequests: List<FriendRequest> = emptyList(),
    val receivedFriendRequests: List<FriendRequest> = emptyList()
)
