package by.bsu.newsly.data.remote.api

import by.bsu.newsly.data.remote.model.NewsResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

val BASE_URL = "https://newsapi.org/"

interface ApiService {

    @GET("v2/everything")
    suspend fun getArticles(
        @Query("q") query: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("sortBy") sortBy: String,
        @Query("language") language: String,
        @Query("apiKey") apiKey: String = "1c79a28b8b294748aff63eb4aaee2eed"
    ): Response<NewsResponse>

    companion object Factory {
        fun create(): ApiService {
            val gson = GsonBuilder()
                .create()

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()


            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}