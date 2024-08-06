package com.example.socialnetwork.clients

import com.example.socialnetwork.BuildConfig
import com.example.socialnetwork.services.GroupService
import com.example.socialnetwork.services.PostService
import com.example.socialnetwork.services.UserService
import com.example.socialnetwork.utils.LocalDateTimeDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ClientUtils {

    private const val SERVICE_API_PATH = "http://${BuildConfig.IP_ADDR}:8080/api/"

    private fun createOkHttpClient(token: String? = null): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(interceptor)

        if (token != null) {
            builder.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                val request = requestBuilder.build()
                chain.proceed(request)
            }
        }

        return builder.build()
    }

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SERVICE_API_PATH)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(createOkHttpClient())
            .build()
    }

    private fun createRetrofit(token: String? = null): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVICE_API_PATH)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(createOkHttpClient(token))
            .build()
    }

    fun getPostService(token: String? = null): PostService {
        return createRetrofit(token).create(PostService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    fun getGroupService(token: String? = null): GroupService {
        return createRetrofit(token).create(GroupService::class.java)
    }
}
