package com.example.visionmate

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object TextToSpeechManager : TextToSpeech.OnInitListener {

    // Initialize tts as a lazy property
    private val tts: TextToSpeech by lazy {
        TextToSpeech(context, this)
    }

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
        tts // Accessing tts will trigger its lazy initialization
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language for the TTS engine
            val result = tts.setLanguage(Locale.US) // Change Locale.US to your desired language

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            }


            welcomeSpeech()
        } else {
            Log.e("TTS", "Initialization failed!")
        }
    }

    fun speakOut(text: String, param: Bundle? = null) {
        if (!tts.isSpeaking) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, param, "")
        }
    }

    private fun welcomeSpeech() {
        val text = "Welcome to vision mate"
        speakOut(text)
    }

    fun stopSpeak() {
        if (tts.isSpeaking) {
            tts.stop()
        }
    }

    fun stopSpeechListener() {
        if (tts.isSpeaking) {
            tts.stop()
            tts.shutdown()
        }
    }

    fun isSpeaking(): Boolean {
        return tts.isSpeaking
    }

    fun getSpeakObj() = tts
}
