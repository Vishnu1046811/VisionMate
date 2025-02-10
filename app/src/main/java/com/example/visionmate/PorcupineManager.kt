package com.example.visionmate

import ai.picovoice.porcupine.*
import ai.picovoice.porcupine.PorcupineManager
import android.content.Context
import android.util.Log


class PorcupineManager {



    fun startListening(context: Context, callback: (Boolean) -> Unit) {
        val wakeWordcallback =
            PorcupineManagerCallback { keywordIndex ->
                if (keywordIndex == 0) {
                    // porcupine detected
                    callback.invoke(true)
                    Log.e("","")
                } else if (keywordIndex == 1) {
                    // bumblebee detected
                    callback.invoke(false)
                    Log.e("","")
                }
            }
        var array = ArrayList< Porcupine.BuiltInKeyword>()
        array.add(Porcupine.BuiltInKeyword.PORCUPINE)
        array.add(Porcupine.BuiltInKeyword.BUMBLEBEE)
        val porcupineManager = PorcupineManager.Builder()
            .setAccessKey(Constants.PICO_VOICE_KEY) // Assuming ACCESS_KEY is defined elsewhere
            //.setKeywords(array.toTypedArray())
            .setKeywordPaths(arrayOf("open-chatbot_en_android_v3_0_0.ppn","open-camera_en_android_v3_0_0.ppn","Hey-Vision-Mate_en_android_v3_0_0.ppn"))
            .build(context, wakeWordcallback)

        porcupineManager.start();

    }

    /*fun stopListening() {
        porcupine.stop()
    }*/

}