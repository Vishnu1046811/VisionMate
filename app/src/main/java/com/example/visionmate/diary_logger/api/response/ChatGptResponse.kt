package com.example.visionmate.diary_logger.api.response

data class ChatGptResponse(val choices: List<Choice>)

data class Choice(val message: ResponseMessage)

data class ResponseMessage(
    val content: String
)
