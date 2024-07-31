package com.example.socialnetwork.model.dto

data class UserTokenState(
    var accessToken: String? = null,
    var expiresIn: Long? = null,
//    var response: HttpServletResponse? = null
)
