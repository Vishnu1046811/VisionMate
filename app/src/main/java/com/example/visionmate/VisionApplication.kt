/*
 * Copyright (C) 2025 FUJIFILM Corporation. All rights reserved.
 *
 * Created on : 18-02-2025
 * Author     : Suhail.CP
 *
 * com.example.visionmate
 *
 * This file contains the implementation of Application.kt class.
 */
package com.example.visionmate

import android.app.Application
import com.example.visionmate.chatbot.data.ObjectBoxStore
import com.example.visionmate.diary_logger.DiaryLogger

class VisionApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        TextToSpeechManager.init(this)
        ObjectBoxStore.init(this)
        DiaryLogger.init(this)
    }
}