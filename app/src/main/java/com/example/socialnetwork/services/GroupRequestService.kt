package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.Group
import com.example.socialnetwork.model.entity.GroupRequest
import com.example.socialnetwork.model.entity.User
import retrofit2.Call
import retrofit2.http.*

interface GroupRequestService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("groupRequests/create/{id}")
    fun createGroupRequest(
        @Path("id") groupId: Long,
        @Body groupRequest: GroupRequest
    ): Call<GroupRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("groupRequests/approve/{id}")
    fun approveGroupRequest(
        @Path("id") id: Long
    ): Call<GroupRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("groupRequests/decline/{id}")
    fun declineGroupRequest(
        @Path("id") id: Long
    ): Call<GroupRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupRequests/find/{id}")
    fun getGroupRequestById(
        @Path("id") id: Long
    ): Call<GroupRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupRequests/all")
    fun getAllGroupRequests(): Call<List<GroupRequest>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupRequests/all/{groupId}")
    fun getGroupRequestsByGroup(
        @Path("groupId") groupId: Long
    ): Call<List<GroupRequest>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupRequests/find/{userId}/{groupId}")
    fun getGroupRequestByUserAndGroup(
        @Path("userId") userId: Long,
        @Path("groupId") groupId: Long
    ): Call<GroupRequest>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupRequests/approvedGroups/{userId}")
    fun getApprovedGroupsForUser(
        @Path("userId") userId: Long
    ): Call<Set<Group>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groupRequests/approvedUsers/{groupId}")
    fun getApprovedUsersForGroup(
        @Path("groupId") groupId: Long
    ): Call<Set<User>>
}
