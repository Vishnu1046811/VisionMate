package com.example.visionmate

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.visionmate.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //startListening()
        startActivity(Intent(this, MainActivity::class.java))
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