package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.CreateGroupRequest
import com.example.socialnetwork.model.entity.Group
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface GroupService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("groups/create")
    fun create(@Body group: CreateGroupRequest): Call<Group>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("groups/update/{id}")
    fun update(@Path("id") id: Long, @Body group: CreateGroupRequest): Call<Group>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("groups/delete/{id}")
    fun delete(@Path("id") id: Long, @Body suspendedReason: RequestBody): Call<Group>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groups/find/{id}")
    fun getById(@Path("id") id: Long): Call<Group>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("groups/all")
    fun getAll(): Call<ArrayList<Group>>
}
