/*
 * Copyright (C) 2025 FUJIFILM Corporation. All rights reserved.
 *
 * Created on : 18-02-2025
 * Author     : Suhail.CP
 *
 * com.example.visionmate
 *
 * This file contains the implementation of BaseActivity.kt class.
 */
package com.example.visionmate

import android.os.Bundle
import android.os.PersistableBundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

abstract class BaseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {



    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        // Initialize TextToSpeech object
        //tts = TextToSpeech(this, this)
        //
         }

    fun speakOut(text: String) {
        TextToSpeechManager.speakOut(text)

    }

    override fun onDestroy() {
        super.onDestroy()
        /*if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }*/
    }


    override fun onInit(status: Int) {
/*        if (status == TextToSpeech.SUCCESS) {
            // Set the language for the TTS engine
            val result = tts!!.setLanguage(Locale.US) // Change Locale.US to your desired language

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            } else {
                //btnSpeak!!.isEnabled = true
            }
        }*/
    }
}