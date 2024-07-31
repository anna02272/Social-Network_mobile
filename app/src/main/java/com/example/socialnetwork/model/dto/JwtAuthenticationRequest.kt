package com.example.socialnetwork.model.dto

data class JwtAuthenticationRequest(
    var username: String,
    var password: String
)