package com.example.visionmate.speech_listener

import android.R
import android.content.Context
import android.util.Log
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import java.io.IOException
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechSettings


class SpeechClient {
    companion object
    private  val HOSTNAME = "speech.googleapis.com"
    private  val PORT = 443

    private fun initializeSpeechClient(context: Context) {
        /*try {
            val credentials =
                GoogleCredentials.fromStream(context.getResources().openRawResource(R.raw.credentials))
            val credentialsProvider: FixedCredentialsProvider =
                FixedCredentialsProvider.create(credentials)
            speechClient = SpeechClient.create(
                SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()
            )
        } catch (e: IOException) {
            Log.e("kya", "InitException" + e.message)
        }*/
    }
}