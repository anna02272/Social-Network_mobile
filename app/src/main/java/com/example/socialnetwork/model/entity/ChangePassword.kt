package com.example.socialnetwork.model.entity

data class ChangePassword (
    val id: Long = 0L,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
    )