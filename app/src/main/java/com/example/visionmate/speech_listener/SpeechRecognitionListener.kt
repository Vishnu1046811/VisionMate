package com.example.visionmate.speech_listener

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale
//
//class SpeechRecognitionListener() {
//    private var speechRecognizer: SpeechRecognizer? = null
//    private val REQUEST_RECORD_AUDIO = 123
//    lateinit var context: Context
//
//    constructor(context: Context) : this() {
//        this.context = context
//        checkPermission()
//    }
//    fun checkPermission(){
//        // Check for audio recording permission
//        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(context as Activity, arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
//        } else {
//            startSpeechRecognition()
//        }
//    }
//
//    private fun startSpeechRecognition() {
//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
//        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//
//        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
//            override fun onReadyForSpeech(params: Bundle?) {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onBeginningOfSpeech() {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onRmsChanged(rmsdB: Float) {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onBufferReceived(buffer: ByteArray?) {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onEndOfSpeech() {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onError(error: Int) {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onResults(results: Bundle?) {
//                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                if (matches != null) {
//
//                }
//            }
//
//            override fun onPartialResults(partialResults: Bundle?) {
//                Log.e("","Not yet implemented")
//            }
//
//            override fun onEvent(eventType: Int, params: Bundle?) {
//                Log.e("","Not yet implemented")
//            }
//
//        })
//
//        speechRecognizer?.startListening(speechIntent)
//    }
//
//    //Example of using the Google Cloud Speech API (requires setup and credentials)
//    private fun processAudioWithGoogleCloudSpeech(audioData: ByteArray) {
//        try {
//            val speechClient = SpeechClient.create()
//            val config = RecognitionConfig.newBuilder()
//                .setEncoding(AudioEncoding.LINEAR16)
//                .setSampleRateHertz(16000) // Adjust as needed
//                .setLanguageCode("en-US") // Set your language code
//                .build()
//
//            val audio = RecognitionAudio.newBuilder()
//                .setContent(ByteArrayInputStream(audioData))
//                .build()
//
//            val response = speechClient.recognize(config, audio)
//
//            for (result in response.resultsList) {
//                for (alternative in result.alternativesList) {
//                    textView.text = alternative.transcript
//                    println("Transcription: ${alternative.transcript}")
//                }
//            }
//            speechClient.close()
//        } catch (e: Exception) {
//            println("Error processing audio: ${e.message}")
//        }
//    }
//
//
//}