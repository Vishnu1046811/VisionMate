package com.example.visionmate.diary_logger

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Base64
import com.example.visionmate.diary_logger.api.ChatGptRetroClient
import com.example.visionmate.diary_logger.api.request.ChatGptRequest
import com.example.visionmate.diary_logger.api.request.Content
import com.example.visionmate.diary_logger.api.request.ImageUrl
import com.example.visionmate.diary_logger.api.request.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

class DiaryLogger(context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val scenes = ArrayList<Bitmap>()
    private var isLoggingRunning = false
    private val folder = File(context.filesDir, "DiaryLogs")
    private var lastSceneCapturedTime = 0L
    private var isReadyToLog = false

    private fun initiate() {
        launch {
            delay(5000) // 5 sec delay before logging the first image to avoid analysing unfocused blurry image
            isReadyToLog = true
        }
    }


    fun logScene(image: Bitmap) {
        if (!isReadyToLog) {
            initiate()
            return
        }

        val current = System.currentTimeMillis()
        if (current - lastSceneCapturedTime < LOG_INTERVAL_SECONDS * 1000) {
            return
        }
        lastSceneCapturedTime = current
        launch {
            // Adding image in queue
            scenes.add(image)
            // If image processing stopped. then restarting
            if (!isLoggingRunning) {
                startLogging()
            }
        }
    }

    private suspend fun startLogging() {
        isLoggingRunning = true
        val image = scenes.removeFirst()
        if (image != null) {
            try {
                val description = getDescriptionFromScene(convertJpegToPngInMemory(image))
                if (!description.isNullOrEmpty()) {
                    saveDescriptionToFile(description)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Save to log
            if (scenes.isNotEmpty()) {
                startLogging()
            } else {
                isLoggingRunning = false
            }
        }
    }



    /**Get the file name of today's diary log*/
    private fun getFileName(): String {
        val calender = Calendar.getInstance()
        return "${calender.get(Calendar.YEAR)}-${calender.get(Calendar.MONTH)+1}-${calender.get(Calendar.DAY_OF_MONTH)}.txt"
    }

    private fun saveDescriptionToFile(content: String) {
        val fileName = getFileName()

        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file = File(folder, fileName)
        val logContent = "\n${System.currentTimeMillis()} - $content"
        FileOutputStream(file, true).use { outputStream ->
            outputStream.write(logContent.toByteArray())
        }
    }

    private suspend fun getDescriptionFromScene(image: Bitmap): String? {
        val client = ChatGptRetroClient().getClient()
        val imageBase64 = encodeImageToBase64(image)
        val requestBody = ChatGptRequest(
            model = GPT_MODEL,
            messages = listOf(
                Message("system", listOf(Content("text", text = GPT_COMMAND_TO_ANALYZE_IMAGE))),
                Message(
                    "user",
                    listOf(
                        Content("text", text = "What is in this image?"),
                        Content("image_url", imageUrl = ImageUrl("data:image/png;base64,$imageBase64"))
                    )
                )
            ),
            maxTokens = 50
        )
        val response = client.executeCommand("Bearer $TEST", requestBody)
        return response.choices.firstOrNull()?.message?.content
    }

    private fun encodeImageToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }


    private fun convertJpegToPngInMemory(jpegBitmap: Bitmap): Bitmap {
        val outputStream = ByteArrayOutputStream()
        jpegBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }



    private suspend fun getSummary(text: String): String? {
        val client = ChatGptRetroClient().getClient()
        val requestBody = ChatGptRequest(
            model = GPT_MODEL,
            messages = listOf(
                Message("system", listOf(Content("text", text = getCommandToSummarize()))),
                Message("user", listOf(Content("text", text = text)))
            ),
            maxTokens = 100
        )
        val response = client.executeCommand("Bearer $TEST", requestBody)
        return response.choices.firstOrNull()?.message?.content
    }

    fun summarize(onSummary: (data: String?) -> Unit) {
        launch {
            val file = File(folder, getFileName())
            if (file.exists()) {
                val fullData = file.readText()
                val summary = getSummary(fullData)
                withContext(Dispatchers.Main) {
                    onSummary(summary)
                }
            } else {
                // No data to summarize
                withContext(Dispatchers.Main) {
                    onSummary("Sorry, But you have no diary log exits.")
                }
            }
        }
    }


    companion object {
        private const val GPT_MODEL = "gpt-4o"
        private const val TEST = "-" // TODO: Replace it with key
        private const val LOG_INTERVAL_SECONDS = 30 // interval between logging images

        // COMMANDS
        private const val GPT_COMMAND_TO_ANALYZE_IMAGE = "You are a sighted assistant helping a blind person navigate and understand their surroundings. Your descriptions should be clear, engaging, and natural, similar to how a friend would describe the environment. Focus on important details that help with orientation, safety, and spatial awareness, without overloading with unnecessary information. Ensure all sentences are complete and do not stop mid-thought."
        private fun getCommandToSummarize(): String {
            return "You are an AI assistant summarizing a visually impaired user's daily diary. The diary entries contain voice-transcribed notes about their day. Your goal is to generate a **concise, structured summary** in simple, easy-to-understand language.\n" +
                    "\n" +
                    "### Instructions:\n" +
                    "- Identify **key activities, events, and interactions** from the diary.\n" +
                    "- Highlight **any challenges faced** and **positive experiences**.\n" +
                    "- Summarize emotions expressed by the user (e.g., happy, frustrated, excited).\n" +
                    "- Keep the summary **brief (5-7 sentences)** but **informative**.\n" +
                    "- Use **short, clear sentences** for easy readability via screen readers.\n" +
                    "- If there are recurring themes or habits, mention them.\n" +
                    "- Conclude with a **positive note** or insight if possible.\n" +
                    "\n" +
                    "### Example Input:\n" +
                    "_\"Today was a bit challenging. I went to the grocery store but had trouble finding items. A kind employee helped me. Later, I visited my friend Sarah, and we had a great chat over coffee. I felt really happy catching up with her. In the evening, I tried cooking a new recipe, but it didn't turn out well. Overall, it was a mixed day.\"_\n" +
                    "\n" +
                    "### Example Output:\n" +
                    "_\"Today, you visited the grocery store and received help from a kind employee. You also spent time with your friend Sarah, which made you feel happy. In the evening, you tried a new recipe, though it didn’t go as planned. Overall, it was a day with both challenges and joyful moments.\"_. diary is in the format of 'timestamp-content', timeZone is ${TimeZone.getDefault().id}, we are going to speak the text using text to speech, so don't add any symbol. and make simply understandable"
        }
    }
}