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
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    fun speakOut(text: String) {
        TextToSpeechManager.speakOut(text)

    }

    override fun onDestroy() {
        super.onDestroy()

    }


    override fun onInit(status: Int) {

    }
}