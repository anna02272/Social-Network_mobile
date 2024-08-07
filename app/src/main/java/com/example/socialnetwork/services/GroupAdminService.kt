package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.GroupAdmin
import com.example.socialnetwork.model.entity.User
import retrofit2.Call
import retrofit2.http.*

interface GroupAdminService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupadmins/all")
    fun getAllGroupAdmins(): Call<List<GroupAdmin>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupadmins/findBy/{groupId}/{userId}")
    fun findByGroupAndUser(
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long
    ): Call<GroupAdmin>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @DELETE("groupadmins/removeGroupAdmin/{groupId}")
    fun removeGroupAdmin(
        @Path("groupId") groupId: Long
    ): Call<GroupAdmin>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("groupadmins/createGroupAdmin/{groupId}/{userId}")
    fun createGroupAdmin(
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long
    ): Call<GroupAdmin>
}
