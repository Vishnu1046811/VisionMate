import android.content.Context
import com.example.visionmate.chatbot.TextVectorizer
import com.example.visionmate.room.LogDatabase
import com.example.visionmate.room.entity.LogEntry

class ChatBot(private val context: Context) {

    private val logDao = LogDatabase.getDatabase(context).logDao()
    private val textVectorizer = TextVectorizer()

    // Store user input and its TF-IDF vector in SQLite
    fun storeConversation(text: String) {
        // Retrieve all conversations for IDF calculation
        val conversations = logDao.getAllLogs()

        // Calculate TF-IDF vector for the new input
        val tfidfScores = textVectorizer.computeTFIDF(text, conversations.map { textVectorizer.computeBagOfWords(textVectorizer.tokenizeText(it.text)) })

        // Convert the TF-IDF map to a string
        val tfidfString = textVectorizer.convertToString(tfidfScores)

        // Insert into the database
        logDao.insertLog(LogEntry(text = text, tokenized = tfidfString))
    }

    // Retrieve and compare input to stored conversations using TF-IDF
    fun getResponse(userInput: String): String {
        // Retrieve all conversations from the database
        val conversations = logDao.getAllLogs()

        // Compute TF-IDF for the user input
        val userTfidfScores = textVectorizer.computeTFIDF(userInput, conversations.map { textVectorizer.computeBagOfWords(textVectorizer.tokenizeText(it.text)) })

        // Calculate cosine similarity or other metric to compare the vectors (not implemented here)
        // For simplicity, we'll just return the most similar conversation's text (you can use cosine similarity here)
        val bestMatch = conversations.maxByOrNull { compareTFIDF(userTfidfScores, it.tokenized) }

        return bestMatch?.text ?: "Sorry, I didn't understand that."
    }

    // A simple function to compare TF-IDF scores (you can replace this with a cosine similarity function)
    private fun compareTFIDF(userTfidf: Map<String, Double>, storedVector: String): Double {
        val storedScores = storedVector.split(",").associate {
            val parts = it.split(":")
            parts[0] to parts[1].toDouble()
        }

        // Compare logic to return the "similarity" between vectors (simplified here)
        var similarity = 0.0
        for (entry in userTfidf) {
            similarity += storedScores.getOrDefault(entry.key, 0.0) * entry.value
        }
        return similarity
    }
}
