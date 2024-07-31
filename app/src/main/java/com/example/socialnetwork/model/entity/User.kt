package com.example.socialnetwork.model.entity

import com.example.socialnetwork.model.entity.Comment
import com.example.socialnetwork.model.entity.FriendRequest
import com.example.socialnetwork.model.entity.Post
import com.example.socialnetwork.model.entity.Report
import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val password: String,
    val email: String,
    val lastLogin: LocalDateTime? = null,
    val firstName: String,
    val lastName: String,
//    private var _type: EUserType,
    val description: String? = null,
    val profileName: String? = null,
//    val image: Image? = null,
//    val groupAdmins: List<GroupAdmin> = emptyList(),
    val posts: List<Post> = emptyList(),
//    val reactions: List<Reaction> = emptyList(),
    val reports: List<Report> = emptyList(),
    val comments: List<Comment> = emptyList(),
//    val groupRequests: List<GroupRequest> = emptyList(),
    val sentFriendRequests: List<FriendRequest> = emptyList(),
    val receivedFriendRequests: List<FriendRequest> = emptyList()
)
