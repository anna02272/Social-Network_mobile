package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.Post
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PostService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/all")
    fun getAll(): Call<ArrayList<Post>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/find/{id}")
    fun getById(@Path("id") id: Long): Call<Post>

    @Multipart
    @POST("posts/create")
    fun create(
        @Part("content") content: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Call<Post>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @Multipart
    @POST("posts/create/{id}")
    fun createGroupPost(
        @Path("id") groupId: Long,
        @Part("content") content: String,
        @Part images: List<MultipartBody.Part>? = null
    ): Call<Post>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @Multipart
    @PUT("posts/update/{id}")
    fun update(
        @Path("id") id: Long,
        @Part("content") content: String,
        @Part images: List<MultipartBody.Part>? = null
    ): Call<Post>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("posts/delete/{id}")
    fun delete(@Path("id") id: Long): Call<Post>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @DELETE("posts/delete/{id}")
    fun deleteImage(@Path("id") id: Long): Call<ResponseBody>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/all/{groupId}")
    fun getByGroup(@Path("groupId") groupId: Long): Call<List<Post>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/ascendingAll")
    fun getAllAscending(): Call<List<Post>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/descendingAll")
    fun getAllDescending(): Call<List<Post>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/allAsc/{groupId}")
    fun getByGroupAscending(@Path("groupId") groupId: Long): Call<List<Post>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("posts/allDesc/{groupId}")
    fun getByGroupDescending(@Path("groupId") groupId: Long): Call<List<Post>>
}
