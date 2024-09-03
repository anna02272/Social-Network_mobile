package com.example.socialnetwork.services

import com.example.socialnetwork.model.dto.JwtAuthenticationRequest
import com.example.socialnetwork.model.dto.UserDTO
import com.example.socialnetwork.model.dto.UserTokenState
import com.example.socialnetwork.model.entity.ChangePassword
import com.example.socialnetwork.model.entity.User
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

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
    fun logout(): Call<Void>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("users/whoami")
    fun whoAmI(): Call<User>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("users/find/{id}")
    fun getUserById(@Path("id") id: Long): Call<User>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("users/all")
    fun loadAll(): Call<List<User>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("users/changePassword")
    fun changePassword(@Body changePasswordRequest: ChangePassword): Call<Void>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("users/update/{id}")
    fun update(@Path("id") id: Long, @Body user: User): Call<User>

    @Multipart
    @PUT("users/updateProfilePicture/{id}")
    fun updateProfilePicture(@Path("id") id: Long, @Part profilePicture: MultipartBody.Part): Call<User>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @DELETE("users/deleteProfilePicture/{id}")
    fun deleteProfilePicture(@Path("id") id: Long): Call<User>
}
