package com.example.visionmate

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.visionmate.chatbot.ChatBotModel
import com.example.visionmate.databinding.ActivityChatBotBinding
import com.example.visionmate.diary_logger.DiaryLogger
import com.example.visionmate.model.SpeechModel
import com.example.visionmate.speech_listener.SpeechRecognizer
import com.google.gson.Gson

class ChatBotActivity : BaseActivity() {
    private var chatBot: ChatBotModel? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var binding: ActivityChatBotBinding
    private val animatorSet = AnimatorSet()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)


        speakOut("ChatBot Ready to assist you")
        chatBot = ChatBotModel(this)
        DiaryLogger.INSTANCE.attachChatBot(chatBot)

        speechRecognizer = SpeechRecognizer()

        TextToSpeechManager.getSpeakObj().setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                pauseListening(true)
            }

            override fun onDone(utteranceId: String?) {
                pauseListening(false)
            }

            override fun onError(utteranceId: String?) {
                pauseListening(false)
            }
        })



        val animator1 = ObjectAnimator.ofFloat(binding.ivMic,"ScaleX",1f,0.7f)
        val animator2 = ObjectAnimator.ofFloat(binding.ivMic,"ScaleY",1f,0.7f)
        animator2.repeatMode = ObjectAnimator.REVERSE
        animator2.repeatCount = ObjectAnimator.INFINITE
        animator1.repeatMode = ObjectAnimator.REVERSE
        animator1.repeatCount = ObjectAnimator.INFINITE
        animatorSet.playTogether(animator1,animator2)
        animatorSet.duration = 1500
        animatorSet.start()

    }

    override fun onResume() {
        super.onResume()
        startListening()
    }



    fun startListening() {

        speechRecognizer.startListening(this@ChatBotActivity, object :
                org.vosk.android.RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {

                }

                override fun onResult(hypothesis: String?) {
                    if (chatBot?.isProcessing != true && !TextToSpeechManager.isSpeaking()) {
                        pauseListening(false)
                        val speechModel = Gson().fromJson(hypothesis, SpeechModel::class.java)
                        Log.e("TAG", "onResult: ${speechModel.text}", )
                        if (speechModel.text.trim().isNotEmpty()) {
                            chatBot?.getAnswer(speechModel.text, onAnswer = {
                                binding.tvMessage.text = it
                                TextToSpeechManager.speakOut(it)
                            })
                        } else {
                            pauseListening(false)
                        }

                    }

                }

                override fun onFinalResult(hypothesis: String?) {

                }

                override fun onError(exception: java.lang.Exception?) {

                }

                override fun onTimeout() {

                }

            })
    }


    private fun pauseListening(pause: Boolean) {
        if (pause) {
            speechRecognizer.pause()
            listeningAnimation(false)
        } else {
            speechRecognizer.resume()
            listeningAnimation(true)
        }
    }

    private fun listeningAnimation(listening: Boolean) {
        if (listening) {
            animatorSet.resume()
        } else {
            animatorSet.pause()
        }
    }

    fun checkCommad(text: String) {
//        Log.e("TAG", "checkCommad: $text ", )
//        if(Commands.chatBotSummariseRegex.commandContain(text)){
//            DiaryLogger.INSTANCE.summarize{
//                if (it != null) {
//                    //tts?.stop()
//                    speakOut(it)
//                }
//            }
//        }
    }
}