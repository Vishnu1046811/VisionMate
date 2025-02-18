package com.example.visionmate.chatbot.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id

@Entity
data class Chunk(
    @Id var chunkId: Long = 0,
    var chunkData: String = "",
    @HnswIndex(dimensions = 384) var chunkEmbedding: FloatArray = floatArrayOf(),
)

data class RetrievedContext(
    val context: String,
)
