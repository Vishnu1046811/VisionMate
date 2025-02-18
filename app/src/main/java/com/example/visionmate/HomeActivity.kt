package com.example.visionmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.visionmate.databinding.ActivityHomeBinding
import com.example.visionmate.model.SpeechModel
import com.google.gson.Gson
import org.vosk.LibVosk
import org.vosk.LogLevel
import util.Commands

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    val PERMISSIONS_REQUEST_RECORD_AUDIO: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LibVosk.setLogLevel(LogLevel.INFO)

        //startListening()
/*
        PorcupineManager().startListening(this,{ result->
            Log.e("","")
            if(result){
                navigateChatbot()
            }else{
                navigateCamera()
            }
        })*/
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

    override fun onResume() {
        super.onResume()

        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            startListening()
        }
        //startListening()
    }

    fun startListening(){
        com.example.visionmate.speech_listener.SpeechRecognizer().startListening(this@HomeActivity, object:
            org.vosk.android.RecognitionListener{
            override fun onPartialResult(hypothesis: String?) {

            }

            override fun onResult(hypothesis: String?) {
                var speechModel = Gson().fromJson(hypothesis, SpeechModel::class.java)
                speechModel?.let { checkCommands(speechModel.text) }
            }

            override fun onFinalResult(hypothesis: String?) {

            }

            override fun onError(exception: Exception?) {

            }

            override fun onTimeout() {

            }

        })
    }

}