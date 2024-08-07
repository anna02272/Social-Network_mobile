package com.example.socialnetwork.services

import com.example.socialnetwork.model.entity.Report
import retrofit2.Call
import retrofit2.http.*

interface ReportService {

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("reports/reportPost/{id}")
    fun reportPost(
        @Path("id") postId: Long,
        @Body report: Report
    ): Call<Report>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("reports/reportComment/{id}")
    fun reportComment(
        @Path("id") commentId: Long,
        @Body report: Report
    ): Call<Report>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @POST("reports/reportUser/{id}")
    fun reportUser(
        @Path("id") reportedUserId: Long,
        @Body report: Report
    ): Call<Report>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("reports/approve/{id}")
    fun approveReport(
        @Path("id") reportId: Long
    ): Call<Report>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @PUT("reports/decline/{id}")
    fun declineReport(
        @Path("id") reportId: Long
    ): Call<Report>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reports/allPosts")
    fun getAllReportsForPosts(): Call<List<Report>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reports/allComments")
    fun getAllReportsForComments(): Call<List<Report>>

    @Headers(
        "User-Agent: Mobile-Android",
        "Content-Type: application/json"
    )
    @GET("reports/allUsers")
    fun getAllReportsForUsers(): Call<List<Report>>
}
