package com.example.socialnetwork.services

import com.example.socialnetwork.model.dto.JwtAuthenticationRequest
import com.example.socialnetwork.model.dto.UserDTO
import com.example.socialnetwork.model.dto.UserTokenState
import com.example.socialnetwork.model.entity.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("users/signup")
    fun create(@Body user: UserDTO): Call<UserDTO>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("users/login")
    fun createAuthenticationToken(@Body request: JwtAuthenticationRequest): Call<UserTokenState>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("users/logout")
    fun logout(): Call<String>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("users/whoami")
    fun whoAmI(): Call<User>
}
