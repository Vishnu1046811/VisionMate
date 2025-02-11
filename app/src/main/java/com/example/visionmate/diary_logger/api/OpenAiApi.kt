package com.example.visionmate.diary_logger.api

import com.example.visionmate.diary_logger.api.request.ChatGptRequest
import com.example.visionmate.diary_logger.api.response.ChatGptResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun executeCommand(
        @Header("Authorization") authHeader: String,
        @Body request: ChatGptRequest
    ): ChatGptResponse
}