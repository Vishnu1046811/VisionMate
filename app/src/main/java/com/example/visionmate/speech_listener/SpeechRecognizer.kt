package com.example.visionmate.speech_listener

import android.content.Context
import android.util.Log
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.File
import java.io.IOException

class SpeechRecognizer {
    private lateinit var speechService: SpeechService
    private lateinit var recognizer: Recognizer
    private lateinit var model: Model


    init{
        // Initialize Vosk Library
        LibVosk.setLogLevel(LogLevel.INFO)



    }

    fun startListening(context: Context,recognitionListener: RecognitionListener) {
        StorageService.unpack(
            context, "vosk-model-small-en-in-0.4",  "model",
            { model: Model? ->
                this.model =
                    model!!
                //setUiState(org.vosk.demo.VoskActivity.STATE_READY)
                recognizer = Recognizer(model, 16000f)
                speechService = SpeechService(recognizer, 16000f)
                recognitionListener?.let {
                    speechService.startListening(recognitionListener)
                }
            },
            { exception: IOException ->
                Log.e("",
                    "Failed to unpack the model" + exception.message
                )
            })
    }

    fun pause() {
        speechService.setPause(true)
    }
    fun resume() {
        speechService.setPause(false)
    }

  /*  override fun onPartialResult(hypothesis: String?) {
        Log.e("","")
    }

    override fun onResult(hypothesis: String?) {
        Log.e("","")
    }

    override fun onFinalResult(hypothesis: String?) {
        Log.e("","")
    }

    override fun onError(exception: Exception?) {
        Log.e("","")
    }

    override fun onTimeout() {
        Log.e("","")
    }*/
}