/*
 * Copyright (C) 2025 FUJIFILM Corporation. All rights reserved.
 *
 * Created on : 18-02-2025
 * Author     : Suhail.CP
 *
 * com.example.visionmate.chatbot
 *
 * This file contains the implementation of ChatBotModel.kt class.
 */
package com.example.visionmate.chatbot

import android.content.Context
import android.util.Log
import com.example.visionmate.chatbot.data.Chunk
import com.example.visionmate.chatbot.data.ChunksDB
import com.example.visionmate.chatbot.data.RetrievedContext
import com.example.visionmate.chatbot.embeddings.SentenceEmbeddingProvider
import com.ml.shubham0204.docqa.domain.llm.GeminiRemoteAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatBotModel(context: Context) {

    companion object {
        private const val TAG = "ChatBotModel"

        private const val QUERY = "Here is the retrieved context\n" +
                "--------------------------------------------------\n" +
                "<CONTEXT>\n" +
                "--------------------------------------------------\n" +
                "Here is the user\\'s query: <QUERY>"
    }
    private val chunkDb = ChunksDB()
    private val encoder = SentenceEmbeddingProvider(context)



    fun storeLog(text: String) {
        val embedded = encoder.encodeText(text)
        chunkDb.addChunk(Chunk(
            chunkData = text,
            chunkEmbedding = embedded
        ))
        Log.i(TAG, "Logged: $text")
    }

    fun getAnswer(
        query: String,
        onAnswer: (answer: String)-> Unit
    ) {
        val apiKey = "---"
        val geminiRemoteAPI = GeminiRemoteAPI(apiKey)
        try {
            var jointContext = ""
            val retrievedContextList = ArrayList<RetrievedContext>()
            val queryEmbedding = encoder.encodeText(query)
            chunkDb.getSimilarChunks(queryEmbedding, n = 5).forEach {
                jointContext += " " + it.second.chunkData
                retrievedContextList.add(RetrievedContext(/*it.second.docFileName, */it.second.chunkData))
            }
            val inputPrompt = QUERY.replace("<CONTEXT>", jointContext).replace("<QUERY>", query)
            CoroutineScope(Dispatchers.IO).launch {
                geminiRemoteAPI.getResponse(inputPrompt)?.let { llmResponse ->
                    withContext(Dispatchers.Main) {
                        onAnswer(llmResponse)
                    }
                }
            }
        } catch (e: Exception) {

            throw e
        }
    }

}