package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.*
import retrofit2.Call
import retrofit2.http.*

interface ReactionService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("reactions/reactToPost/{id}")
    fun reactToPost(
        @Path("id") postId: Long,
        @Body reaction: Reaction
    ): Call<Reaction>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reactions/count/post/{postId}")
    fun countReactionsByPost(
        @Path("postId") postId: Long
    ): Call<Map<EReactionType, Integer>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reactions/find/post/{postId}/user/{userId}")
    fun findReactionByPostAndUser(
        @Path("postId") postId: Long,
        @Path("userId") userId: Long
    ): Call<Reaction>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("reactions/reactToComment/{id}")
    fun reactToComment(
        @Path("id") commentId: Long,
        @Body reaction: Reaction
    ): Call<Reaction>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reactions/count/comment/{commentId}")
    fun countReactionsByComment(
        @Path("commentId") commentId: Long
    ): Call<Map<EReactionType, Integer>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reactions/find/comment/{commentId}/user/{userId}")
    fun findReactionByCommentAndUser(
        @Path("commentId") commentId: Long,
        @Path("userId") userId: Long
    ): Call<Reaction>
}
