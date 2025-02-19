package com.example.visionmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.visionmate.TextToSpeechManager.speakOut
import com.example.visionmate.databinding.ActivityHomeBinding
import com.example.visionmate.diary_logger.DiaryLogger
import com.example.visionmate.model.SpeechModel
import com.google.gson.Gson
import org.vosk.LibVosk
import org.vosk.LogLevel
import util.Commands
import util.commandContain
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    val PERMISSIONS_REQUEST_RECORD_AUDIO: Int = 1

    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        LibVosk.setLogLevel(LogLevel.INFO)
        welcomeSpeech()
    }



    private fun checkCommands(spokenText: String) {
        Log.e("TAG", "checkCommands: $spokenText", )
        if(Commands.openCameraRegex.matches(spokenText)){
            navigateCamera()
        }else if(Commands.openChatBotRegex.matches(spokenText)){
            navigateChatbot()
        }else if(Commands.chatBotSummariseRegex.commandContain(spokenText)){
            summarize()
        }
    }

    private fun summarize() {
        DiaryLogger.INSTANCE.summarize {
            if (it != null) {
                TextToSpeechManager.speakOut(it)
            }
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
        isPaused = false

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


    private fun welcomeSpeech() {
        val text = "These are the features that we offer:\n" +
                "\t1.\tLive Tracking – With this feature, we help you understand your surroundings in real time. Using your mobile phone’s camera, our app analyzes the environment and provides descriptions of objects, people, and activities happening around you, making navigation easier. you can start live tracking using command 'Open Camera'\n" +
                "\t2.\tChat Bot – Our chatbot serves as your personal assistant, keeping a record of your daily experiences. You can ask questions and get feedback based on logged events, helping you recall past activities and interactions effortlessly. you can start chat bot using command 'Open chatbot'\n" +
                "\t3.\tDiary – We help you maintain a structured log of your day. The diary feature records daily events and experiences, along with relevant device information, so you can track patterns and retrieve past details whenever needed.\n, you can start diary using command 'Open Diary'" +
                "\t4.\tEnroll Person – To enhance your social awareness, we offer a facial recognition module that allows you to register close family members or trusted individuals. This way, our app can recognize and inform you about the people around you., you can enroll person  using command 'Enroll'"
        speakOut(text)
    }




    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    fun startListening(){
        com.example.visionmate.speech_listener.SpeechRecognizer().startListening(this@HomeActivity, object:
            org.vosk.android.RecognitionListener{
            override fun onPartialResult(hypothesis: String?) {

            }

            override fun onResult(hypothesis: String?) {
                if (!isPaused) {
                    var speechModel = Gson().fromJson(hypothesis, SpeechModel::class.java)
                    speechModel?.let { checkCommands(speechModel.text) }
                }
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