package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.Post
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("posts/create")
    fun create(@Body post: Post): Call<Post>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @DELETE("posts/delete/{id}")
    fun deleteById(@Path("id") id: Long): Call<ResponseBody>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("posts/update/{id}")
    fun update(@Body post: Post): Call<Post>
}
