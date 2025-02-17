package com.example.visionmate

import ChatBot
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.visionmate.databinding.ActivityHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch (Dispatchers.IO){
        val chatBot = ChatBot(this@HomeActivity)

        val conversationLogs = listOf(
            "I saw a beautiful lady in the morning.",
            "I went to the park to play cricket.",
            "I had a great lunch with my friends.",
            "I bought a new phone today.",
            "I watched a fantastic movie last night.",
            "I was reading a book in the evening.",
            "I met an old friend after many years.",
            "I spent the afternoon working on my project.",
            "I felt happy after receiving good news.",
            "I went for a run in the park today.",
            "I had a quiet evening at home.",
            "I was thinking about my childhood memories.",
            "I went shopping and bought a gift for my sister.",
            "I had coffee with my colleague in the morning.",
            "I visited the museum and learned a lot of history.",
            "I saw a movie with my friends.",
            "I enjoyed the weather while walking outside.",
            "I woke up early to go to work.",
            "I had a discussion with my boss at work.",
            "I took a nap in the afternoon."
        )

        conversationLogs.forEach {
            chatBot.storeConversation(it)
        }

        val exampleQueries = listOf(
            "Did I see a lady today?",
            "What did I do in the afternoon?",
            "What did I buy recently?",
            "What did I do yesterday?",
            "Did I go for a run today?",
            "What did I eat today?",
            "Did I talk to anyone today?",
            "How did I spend my evening?",
            "What did I do this morning?",
            "Did I go to work today?"
        )

        val sb = StringBuilder()
        exampleQueries.forEach {
            sb.append("Q.$it")
            val answer = chatBot.getResponse(it)
            sb.append("\n")
            sb.append("A.$answer")
            sb.append("\n\n")
        }

        Log.e("TAG", "onCreate: ${sb.toString()}", )

        }
        //startListening()
//        startActivity(Intent(this, MainActivity::class.java))
//        PorcupineManager().startListening(this,{ result->
//            Log.e("","")
//            if(result){
//                navigateChatbot()
//            }else{
//                navigateCamera()
//            }
//        })
    }
/*

    private fun startListening() {
        SpeechRecognizer(this).startListening(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Called when the user is ready to speak
                Log.e("SpeechRecognition", "")
            }

            override fun onBeginningOfSpeech() {
                // Called when the user starts to speak
                Log.e("SpeechRecognition", "")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Called when the energy of the speech changes
                Log.e("SpeechRecognition", "")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Called when a buffer of audio is received
                Log.e("SpeechRecognition", "")
            }

            override fun onEndOfSpeech() {
                // Called when the user stops speaking
                Log.e("SpeechRecognition", "")
            }

            override fun onError(error: Int) {
                // Called when an error occurs
                Log.e("SpeechRecognition", "Error: $error")
                if (error == 7)
                // speakOut("REPEAT")
                    startListening()
            }

            override fun onResults(results: Bundle?) {
                val data =
                    results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null && data.isNotEmpty()) {
                    val spokenText = data[0]
                    //textView.text = textView.text.toString() + "\n $spokenText"
                    startListening()

                    checkCommands(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Called when partial results are available
                Log.e("SpeechRecognition", "")
                //speakOut("REPEAT")
                startListening()
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Called when a platform-specific event occurs
            }
        })

        //speechRecognizer.startListening(speechRecognizerIntent)
    }
*/

    private fun checkCommands(spokenText: String) {

        if(Commands.openCameraRegex.matches(spokenText)){
            navigateCamera()
        }else if(Commands.openChatBotRegex.matches(spokenText)){
            navigateChatbot()
        }
    }

    private fun navigateChatbot() {

        startActivity(Intent(this,ChatBotActivity::class.java))
    }

    fun navigateCamera(){
        startActivity(Intent(this,MainActivity::class.java))
    }

}