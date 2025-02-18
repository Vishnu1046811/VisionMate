package com.example.visionmate.diary_logger.api.request

import com.google.gson.annotations.SerializedName

data class ChatGptRequest(
    val model: String,
    val messages: List<Message>,
    @SerializedName("max_tokens")
    val maxTokens: Int
)

data class Message(val role: String, val content: List<Content>)

data class Content(
    val type: String,  // "text" or "image_url"
    val text: String? = null, // For text content
    @SerializedName("image_url")
    val imageUrl: ImageUrl? = null // Now it's an object, not a string
)

data class ImageUrl(
    val url: String
)