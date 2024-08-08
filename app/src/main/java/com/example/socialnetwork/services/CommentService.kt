package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.Comment
import com.example.socialnetwork.model.entity.CreateCommentRequest
import retrofit2.Call
import retrofit2.http.*

interface CommentService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("comments/create/{id}")
    fun create(
        @Path("id") postId: Long,
        @Body comment: CreateCommentRequest
    ): Call<Comment>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("comments/update/{id}")
    fun update(
        @Path("id") id: Long,
        @Body comment: Comment
    ): Call<Comment>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("comments/delete/{id}")
    fun delete(
        @Path("id") id: Long
    ): Call<Comment>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/find/{id}")
    fun getById(
        @Path("id") id: Long
    ): Call<Comment>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/findByPost/{postId}")
    fun getCommentsByPostId(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/all")
    fun getAll(): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/ascendingAll/{postId}")
    fun getAllAscending(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/descendingAll/{postId}")
    fun getAllDescending(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/likesAscendingAll/{postId}")
    fun getAllAscendingLikes(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/likesDescendingAll/{postId}")
    fun getAllDescendingLikes(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/dislikesAscendingAll/{postId}")
    fun getAllByAscendingDislikes(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/dislikesDescendingAll/{postId}")
    fun getAllByDescendingDislikes(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/heartsAscendingAll/{postId}")
    fun getAllByAscendingHearts(
        @Path("postId") postId: Long
    ): Call<List<Comment>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("comments/heartsDescendingAll/{postId}")
    fun getAllByDescendingHearts(
        @Path("postId") postId: Long
    ): Call<List<Comment>>
}
