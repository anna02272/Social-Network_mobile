package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.Banned
import retrofit2.Call
import retrofit2.http.*

interface BannedService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("banned/blockUser/{id}")
    fun blockUser(
        @Path("id") userId: Long,
        @Body banned: Banned
    ): Call<Banned>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("banned/unblockUser/{id}")
    fun unblockUser(
        @Path("id") id: Long
    ): Call<Banned>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("banned/allBlockedUsers")
    fun getAllBlockedUsers(): Call<List<Banned>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("banned/blockGroupUser/{userId}/{groupId}")
    fun blockGroupUser(
        @Path("userId") userId: Long,
        @Path("groupId") groupId: Long,
        @Body banned: Banned
    ): Call<Banned>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("banned/allBlockedGroupUsers/{groupId}")
    fun getAllBlockedGroupUsers(
        @Path("groupId") groupId: Long
    ): Call<List<Banned>>
}
