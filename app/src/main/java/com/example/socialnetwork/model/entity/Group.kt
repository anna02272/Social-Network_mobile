package com.example.socialnetwork.model.entity

import java.time.LocalDateTime

data class Group (
    val id: Long?,
    val name: String,
    val description: String,
    val creationDate: LocalDateTime,
    val isSuspended: Boolean,
    val suspendedReason: String,
    val groupAdmin: List<GroupAdmin> = emptyList(),
    val post: List<Post> = emptyList(),
    val groupRequest: List<GroupRequest> = emptyList(),
)
data class CreateGroupRequest(
    val name: String,
    val description: String
)
