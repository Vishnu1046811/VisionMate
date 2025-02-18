package com.example.visionmate.chatbot

import android.content.Context
import android.util.Log
import com.example.visionmate.room.LogDatabase
import com.example.visionmate.room.entity.LogEntry
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

class ChatBot(private val context: Context) {

    private val logDao = LogDatabase.getDatabase(context).logDao()
    private lateinit var tflite: Interpreter

    // Assuming you have a vocabulary file for DistilBERT
    private val vocabulary: Map<String, Int> = loadVocabulary()

    // Max input length for the model, typically 128 for DistilBERT
    private val MAX_INPUT_LENGTH = 1

    init {
        loadModel()
    }

    private fun loadModel() {
        val modelFile = "chat/distilbert_model.tflite" // Ensure this file is in assets folder
        val option = Interpreter.Options()
        tflite = Interpreter(loadModelFile(modelFile), option)

        val inputShape = tflite.getInputTensor(0).shape()  // Get the correct input tensor shape
        Log.e("TAG", "loadModel: ${inputShape.contentToString()}")
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun loadVocabulary(): Map<String, Int> {
        // Load the vocabulary from a file (assume it's in the assets folder)
        val vocabulary = mutableMapOf<String, Int>()
        context.assets.open("vocab.txt").bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                vocabulary[line] = index
            }
        }
        return vocabulary
    }

    fun reset() {
        logDao.clearAllLogs()
    }

    // Store user input in SQLite
    fun storeConversation(text: String) {
        logDao.insertLog(LogEntry(text = text, tokenized = ""))
    }

    // Get response using DistilBERT
    fun getResponse(query: String): String {
        val conversations = logDao.getAllLogs()

        if (conversations.isEmpty()) {
            return "I don't know the answer to that yet."
        }

        var bestMatch: LogEntry? = null
        var highestScore = Float.MIN_VALUE

        for (conversation in conversations) {
            val score = computeSimilarity(query, conversation.text)
            if (score > highestScore) {
                highestScore = score
                bestMatch = conversation
            }
        }

        return bestMatch?.text ?: "I don't have enough information to answer that."
    }

    private fun computeSimilarity(query: String, storedText: String): Float {
        val queryTokens = tokenize(query)
        val storedTokens = tokenize(storedText)

        // Pad tokens to MAX_INPUT_LENGTH
        val paddedQueryTokens = padTokens(queryTokens, MAX_INPUT_LENGTH)
        val paddedStoredTokens = padTokens(storedTokens, MAX_INPUT_LENGTH)

        // Ensure the input tensor is of the correct shape
        val inputTensor = Array(1) { IntArray(MAX_INPUT_LENGTH) } // Shape [1, 128] for DistilBERT
        inputTensor[0] = paddedQueryTokens

        // Output tensor should be of shape [1, 768] for embedding vector
        val outputTensor = Array(1) { FloatArray(768) } // Shape [1, 768] for embedding vector

        // Run the model with the correctly shaped input tensor
        tflite.run(inputTensor, outputTensor)

        val queryEmbedding = outputTensor[0] // The query's embedding vector

        // Now process the stored conversation in a similar way
        val storedInputTensor = Array(1) { IntArray(MAX_INPUT_LENGTH) }
        storedInputTensor[0] = paddedStoredTokens

        // Get stored conversation's embedding
        val storedOutputTensor = Array(1) { FloatArray(768) }
        tflite.run(storedInputTensor, storedOutputTensor)
        val storedEmbedding = storedOutputTensor[0] // The stored conversation's embedding vector

        // Calculate cosine similarity between query and stored conversation
        return cosineSimilarity(queryEmbedding, storedEmbedding)
    }
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val dotProduct = a.zip(b).sumByDouble { (it.first * it.second).toDouble() }.toFloat()
        val magnitudeA = Math.sqrt(a.sumByDouble { (it * it).toDouble() }.toDouble()).toFloat()
        val magnitudeB = Math.sqrt(b.sumByDouble { (it * it).toDouble() }.toDouble()).toFloat()
        return dotProduct / (magnitudeA * magnitudeB)
    }

    // Tokenization logic using vocabulary (simplified version)
    private fun tokenize(text: String): IntArray {
        // Split text into tokens (here, we're simply splitting by spaces)
        val tokens = text.split(" ")
        // Convert tokens into token IDs using the vocabulary
        return tokens.mapNotNull { vocabulary[it] }.toIntArray()
    }

    // Pad tokens to a fixed length (e.g., 128)
    private fun padTokens(tokens: IntArray, length: Int): IntArray {
        return if (tokens.size < length) {
            tokens + IntArray(length - tokens.size) { 0 } // Pad with zeros
        } else {
            tokens.copyOfRange(0, length) // Truncate if necessary
        }
    }
}