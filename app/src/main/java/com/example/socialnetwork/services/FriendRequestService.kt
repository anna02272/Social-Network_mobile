package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.FriendRequest
import com.example.socialnetwork.model.entity.User
import retrofit2.Call
import retrofit2.http.*

interface FriendRequestService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("friendRequests/create/{id}")
    fun create(
        @Path("id") userId: Long,
        @Body friendRequest: FriendRequest
    ): Call<FriendRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("friendRequests/approve/{id}")
    fun approve(
        @Path("id") id: Long
    ): Call<FriendRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("friendRequests/decline/{id}")
    fun decline(
        @Path("id") id: Long
    ): Call<FriendRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("friendRequests/friends/{userId}")
    fun getApprovedFriendsForUser(
        @Path("userId") userId: Long
    ): Call<Set<User>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("friendRequests/find/{id}")
    fun getFriendRequestById(
        @Path("id") id: Long
    ): Call<FriendRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("friendRequests/all/{userId}")
    fun getByUser(
        @Path("userId") userId: Long
    ): Call<List<FriendRequest>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("friendRequests/all")
    fun getAll(): Call<List<FriendRequest>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("friendRequests/search")
    fun searchUsers(
        @Query("keyword") keyword: String
    ): Call<List<User>>
}
