package com.example.visionmate

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerWrapper(private val context: Context) {

    private val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    private var speechRecognizer: SpeechRecognizer? = null  // Make nullable
    private var audioManager: AudioManager? = null

    init {
        initSpeechRecognizer()
        initAudioManager()
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizerIntent.apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            // Consider these extras for better control:
            putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.packageName
            ) // Crucial for proper intent handling
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Get results as they come
            // You can specify the language if needed
            // putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
    }

    private fun initAudioManager() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    }

    // Start speech recognition
    fun startListening(listener: RecognitionListener) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speechRecognizer?.startListening(intent)
    }


    /*    fun startListening(listener: RecognitionListener) {
            speechRecognizer?.let { recognizer ->
                // 1. Check if speech recognition is available:
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    listener.onError(SpeechRecognizer.ERROR_CLIENT)//ERROR_SPEECH_UNAVAILABLE)
                    Log.e("SpeechRecognizer", "Speech recognition is not available on this device.")
                    return
                }

                val streamType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    AudioManager.STREAM_VOICE_CALL
                } else {
                    AudioManager.STREAM_MUSIC
                }

                // 2. Set up audio focus (important for noise suppression):
                audioManager?.requestAudioFocus(
                    { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            // Audio focus granted, start listening
                            recognizer.setRecognitionListener(listener)
                            recognizer.startListening(speechRecognizerIntent)
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            // Audio focus lost, stop listening
                            recognizer.stopListening()
                        }
                    },
                    streamType,
                    //AudioManager.STREAM_VOICE_COMMUNICATION, // Or STREAM_MUSIC
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK // Or AUDIOFOCUS_GAIN_TRANSIENT
                )


            } ?: run {
                Log.e("SpeechRecognizer", "SpeechRecognizer is not initialized!")
                listener.onError(SpeechRecognizer.ERROR_CLIENT)
            }
        }*/


    fun stopListening() {
        speechRecognizer?.let { recognizer ->
            recognizer.stopListening()
            recognizer.setRecognitionListener(null) // Important: Clear the listener
            audioManager?.abandonAudioFocus(null) // Release audio focus
        }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        audioManager = null
    }
}

