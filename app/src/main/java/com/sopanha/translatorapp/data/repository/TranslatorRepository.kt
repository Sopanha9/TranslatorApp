package com.sopanha.translatorapp.data.repository

import com.sopanha.translatorapp.BuildConfig
import com.sopanha.translatorapp.data.model.TranslateResponse
import com.sopanha.translatorapp.utils.ApiResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TranslatorApiService {
    @POST("external-api/free-google-translator")
    suspend fun translate(
        @Header("x-rapidapi-host") host: String = "free-google-translator.p.rapidapi.com",
        @Header("x-rapidapi-key") apiKey: String = BuildConfig.RAPID_API_KEY,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("query") query: String,
        @Body body: Map<String, String> = mapOf("translate" to "rapidapi")
    ): TranslateResponse
}

class TranslatorRepository {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://free-google-translator.p.rapidapi.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(TranslatorApiService::class.java)

    suspend fun translate(from: String, to: String, text: String): ApiResult<TranslateResponse> {
        return try {
            val response = api.translate(from = from, to = to, query = text)
            if (response.translation != null) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(response.message ?: "Translation failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }
}
