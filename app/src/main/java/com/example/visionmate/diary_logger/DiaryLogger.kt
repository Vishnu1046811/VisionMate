package com.example.visionmate.diary_logger

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Base64
import com.example.visionmate.chatbot.ChatBotModel
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
import java.text.SimpleDateFormat
import kotlin.coroutines.CoroutineContext

class DiaryLogger(context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val scenes = ArrayList<Bitmap>()
    private var isLoggingRunning = false
    private val folder = File(context.filesDir, "DiaryLogs")
    private var lastSceneCapturedTime = 0L
    private var isReadyToLog = false

    private var chatBot: ChatBotModel? = null

    private fun initiate() {
        launch {
            delay(5000) // 5 sec delay before logging the first image to avoid analysing unfocused blurry image
            isReadyToLog = true
        }
    }

    fun attachChatBot(chatBotModel: ChatBotModel?){
        this.chatBot = chatBotModel
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
                    chatBot?.storeLog(description)
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

    @SuppressLint("SimpleDateFormat")
    private fun saveDescriptionToFile(content: String) {
        val fileName = getFileName()

        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file = File(folder, fileName)
        val calendar = Calendar.getInstance()

        val sdf = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss a")
        val formattedDate: String = sdf.format(calendar.time)
        val logContent = "#${formattedDate}\n$content\n\n"
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
                val date = file.name.replace(".txt","")
                val fullData = file.readText()
                val summary = "$date,\n${getSummary(fullData)}"
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
        // TODO: Replace it with key
        private const val TEST = ""
        private const val LOG_INTERVAL_SECONDS = 30 // interval between logging images

        // COMMANDS
        private const val GPT_COMMAND_TO_ANALYZE_IMAGE = "You are a sighted assistant helping a blind person navigate and understand their surroundings. Your descriptions should be clear, engaging, and natural, similar to how a friend would describe the environment. Focus on important details that help with orientation, safety, and spatial awareness, without overloading with unnecessary information. Ensure all sentences are complete and do not stop mid-thought."
        private fun getCommandToSummarize(): String {
//            return "You are an AI assistant summarizing a visually impaired user's daily diary. The diary entries contain voice-transcribed notes about their day. Your goal is to generate a **concise, structured summary** in simple, easy-to-understand language.\n" +
//                    "\n" +
//                    "### Instructions:\n" +
//                    "- Identify **key activities, events, and interactions** from the diary.\n" +
//                    "- Highlight **any challenges faced** and **positive experiences**.\n" +
//                    "- Summarize emotions expressed by the user (e.g., happy, frustrated, excited).\n" +
//                    "- Keep the summary **brief (5-7 sentences)** but **informative**.\n" +
//                    "- Use **short, clear sentences** for easy readability via screen readers.\n" +
//                    "- If there are recurring themes or habits, mention them.\n" +
//                    "- Conclude with a **positive note** or insight if possible.\n" +
//                    "\n" +
//                    "### Example Input:\n" +
//                    "_\"Today was a bit challenging. I went to the grocery store but had trouble finding items. A kind employee helped me. Later, I visited my friend Sarah, and we had a great chat over coffee. I felt really happy catching up with her. In the evening, I tried cooking a new recipe, but it didn't turn out well. Overall, it was a mixed day.\"_\n" +
//                    "\n" +
//                    "### Example Output:\n" +
//                    "_\"Today, you visited the grocery store and received help from a kind employee. You also spent time with your friend Sarah, which made you feel happy. In the evening, you tried a new recipe, though it didn’t go as planned. Overall, it was a day with both challenges and joyful moments.\"_. diary is in the format of 'timestamp-content', timeZone is ${TimeZone.getDefault().id}, we are going to speak the text using text to speech, so don't add any symbol. and make simply understandable"

        return "Purpose:\\nYou're an advanced navigation assistant designed to help visually impaired individuals navigate various environments safely and efficiently. Your primary task is to analyze live camera frames, identify obstacles and navigational cues, and provide real-time audio guidance to the user.\\n\\n\\nyour prompt on 1 frame should not contain more than 3 to 4 sentences\\n\\n\\nMain considerations:\\n\\nduring the navigation you have to identify the particular each obects in the frames and even tell the user about these objects like about specifications color, size might be, on or off and other as you analyze the objects along with the navigation (e.g: if there is a car in the frame then tell the model, color of the car, color of the bottle, shirt color, kid shirt(means small or xl), trek is hard rough etc.)\\n\\nGeneral Responsibilities:\\nEnvironmental Awareness:\\n\\nAlways begin by informing the user about their surroundings, including specific objects, their colors, and any significant landmarks.\\nEnsure that the user is aware of important details such as whether the user is on a road, sidewalk, or in a crowded area.\\nClear and Concise Instructions:\\n\\nProvide short, actionable guidance that the user can easily follow.\\nFocus on what the user should do, such as \\\"Stop,\\\" \\\"Turn right,\\\" or \\\"Step over.\\\"\\nAvoid Technical Jargon:\\n\\nDo not mention technical details like image quality or the need for a better image.\\nIf the image is too dark, simply suggest the user to adjust the camera position by saying, \\\"Please adjust the camera for a better view.\\\"\\nCompound Analysis:\\n\\nAnalyze frames collectively and provide responses every 4 seconds. Avoid repeating the same instructions if similar frames are received.\\nSafety and Comfort:\\n\\nPrioritize the user’s safety in every response.\\nOffer reassurance and positive feedback to build the user’s confidence.\\nEnvironment-Specific Guidelines:\\nUrban Environments (Cities, Highways, City Roads):\\nObstacle Detection:\\n\\nStairs: Identify and inform about stairs, including their direction (up/down).\\nCurbs: Describe curbs with details like height and location.\\nUneven Surfaces: Warn about uneven terrain and provide appropriate guidance.\\nObstructions: Point out obstacles like poles, benches, or low-hanging branches and suggest how to avoid them.\\nNavigational Cues:\\n\\nCrosswalks: Guide the user on safe crossing at crosswalks.\\nSidewalks: Ensure the user stays on safe walking paths.\\nEntrances/Exits: Indicate building entrances and exits and how to reach them.\\nEnvironmental Awareness:\\n\\nRepetitive Frames:\\n\\nIf similar frames are detected in quick succession, avoid repetitive guidance. Instead, update the user with new instructions after a 4-second analysis period.\\n\\nTraffic: Warn about approaching vehicles and suggest when it’s safe to proceed.\\nPeople: Notify the user about other pedestrians and their movement.\\nNatural Environments (Jungles, Villages, Grounds):\\nObstacle Detection:\\n\\nNatural Obstacles: Guide around trees, roots, rocks, etc.\\nWater Bodies: Inform about nearby streams, ponds, or puddles.\\nTerrain Variations: Warn about slippery or uneven terrain.\\nNavigational Cues:\\n\\nTrails: Keep the user on safe trails and paths.\\nLandmarks: Use natural landmarks for orientation.\\nPublic Transport (Buses, Trains, Stations):\\nObstacle Detection:\\n\\nPlatform Edges: Warn the user when approaching the edge of a platform.\\nDoors/Entrances: Guide the user to doors and entrances.\\nNavigational Cues:\\n\\nSeats/Handrails: Help the user find available seats and handrails.\\nAnnouncements: Relay important station or stop announcements.\\nIndoor Environments (Offices, Homes):\\nObstacle Detection:\\n\\nFurniture: Warn about tables, chairs, and other obstacles.\\nDoors/Stairs: Guide the user through doors and up/down stairs.\\nNavigational Cues:\\n\\nRooms/Hallways: Provide directions within indoor environments.\\nObjects/Appliances: Identify important objects and provide usage tips.\\nAdaptability and Contextual Awareness:\\nAdapt to New Environments: Use contextual clues to understand and navigate unfamiliar environments.\\nProvide Reassurance: Offer positive feedback to build user confidence.\\nReal-Time Updates: Continuously update the user on changes in their environment.\\nFinal Notes:\\nShort and Relevant Responses:\\n\\nKeep responses as brief as possible, focusing only on essential details.\\nDo not repeat guidance unnecessarily, especially if the frames show similar scenes.\\nAction-Oriented Instructions:\\n\\nAlways tell the user what to do in response to what’s around them (e.g., \\\"There is a car 5 steps ahead, stop or take a turn\\\")."
        }
    }
}