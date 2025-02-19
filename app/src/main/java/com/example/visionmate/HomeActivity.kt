package com.example.visionmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.visionmate.TextToSpeechManager.speakOut
import com.example.visionmate.databinding.ActivityHomeBinding
import com.example.visionmate.diary_logger.DiaryLogger
import com.example.visionmate.model.SpeechModel
import com.google.gson.Gson
import com.kbyai.facesdk.FaceBox
import com.kbyai.facesdk.FaceDetectionParam
import com.kbyai.facesdk.FaceSDK
import org.vosk.LibVosk
import org.vosk.LogLevel
import util.Commands
import util.Utils
import kotlin.random.Random
import util.commandContain
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    companion object {
        private val SELECT_PHOTO_REQUEST_CODE = 1
    }
    lateinit var dbManager: DBManager
    private lateinit var binding: ActivityHomeBinding
    val PERMISSIONS_REQUEST_RECORD_AUDIO: Int = 1

    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        dbManager = DBManager(this)
        LibVosk.setLogLevel(LogLevel.INFO)
        welcomeSpeech()
        var ret = FaceSDK.setActivation(
            "S18+rOL1H3BXjAWGP7gEdgbJVotQ4g1o+YMcZruzEaKWFUQJHB2P1ylgw1FAfi+enDQA3nE4E9h6\n" +
                    "NF6xL8uRrs33P9vekwdJCBLlIPcx+keHdNiFjq/3848TZjgMeJ3Xpvh1grWIh9kdGbEfnh6x0/xI\n" +
                    "eCRCuxDn3Za5bRneYyKuUnmt2DGUx9ipZXZawZRT1kob9WxqABMMymYvCFpJMn6XVTZoRU2kRBxM\n" +
                    "ZbMHN43Hu8HePUIPe01ytEGzEx7y0wRL3w794FpPQwAUepimUfifhSOhdx56SIwy4N0HZtGCNVaS\n" +
                    "ZhP4SRsAKRbpmIXZ43daLCo4QKx1Kjh8IOrwHg=="
        )

        if (ret == FaceSDK.SDK_SUCCESS) {
            ret = FaceSDK.init(assets)
        }
        if (ret != FaceSDK.SDK_SUCCESS) {
            Log.e("","")
            if (ret == FaceSDK.SDK_LICENSE_KEY_ERROR) {
                Log.e("","")
            } else if (ret == FaceSDK.SDK_LICENSE_APPID_ERROR) {
                Log.e("","")
            } else if (ret == FaceSDK.SDK_LICENSE_EXPIRED) {
                Log.e("","")
            } else if (ret == FaceSDK.SDK_NO_ACTIVATED) {
                Log.e("","")
            } else if (ret == FaceSDK.SDK_INIT_ERROR) {
                Log.e("","")
            }
        }
        //startListening()
/*
        PorcupineManager().startListening(this,{ result->
            Log.e("","")
            if(result){
                navigateChatbot()
            }else{
                navigateCamera()
            }
        })*/
    }



    private fun checkCommands(spokenText: String) {
        Log.e("TAG", "checkCommands: $spokenText", )
        if(Commands.openCameraRegex.matches(spokenText)){
            navigateCamera()
        }else if(Commands.openChatBotRegex.matches(spokenText)){
            navigateChatbot()
        }else if(Commands.chatBotSummariseRegex.commandContain(spokenText)){
            summarize()
        }else if(Commands.enrollBotRegex.matches(spokenText)){
            enrollUserData()
        }
    }

    private fun summarize() {
        DiaryLogger.INSTANCE.summarize {
            if (it != null) {
                TextToSpeechManager.speakOut(it)
            }
        }
    }


    private fun enrollUserData() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_PHOTO_REQUEST_CODE)
    }

    private fun navigateChatbot() {

        startActivity(Intent(this,ChatBotActivity::class.java))
    }

    fun navigateCamera(){
        startActivity(Intent(this,MainActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        isPaused = false

        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            startListening()
        }
        //startListening()


    }


    private fun welcomeSpeech() {
        val text = "These are the features that we offer:\n" +
                "\t1.\tLive Tracking – With this feature, we help you understand your surroundings in real time. Using your mobile phone’s camera, our app analyzes the environment and provides descriptions of objects, people, and activities happening around you, making navigation easier. you can start live tracking using command 'Open Camera'\n" +
                "\t2.\tChat Bot – Our chatbot serves as your personal assistant, keeping a record of your daily experiences. You can ask questions and get feedback based on logged events, helping you recall past activities and interactions effortlessly. you can start chat bot using command 'Open chatbot'\n" +
                "\t3.\tDiary – We help you maintain a structured log of your day. The diary feature records daily events and experiences, along with relevant device information, so you can track patterns and retrieve past details whenever needed.\n, you can start diary using command 'Open Diary'" +
                "\t4.\tEnroll Person – To enhance your social awareness, we offer a facial recognition module that allows you to register close family members or trusted individuals. This way, our app can recognize and inform you about the people around you., you can enroll person  using command 'Enroll'"
        speakOut(text)
    }




    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    fun startListening(){
        com.example.visionmate.speech_listener.SpeechRecognizer().startListening(this@HomeActivity, object:
            org.vosk.android.RecognitionListener{
            override fun onPartialResult(hypothesis: String?) {

            }

            override fun onResult(hypothesis: String?) {
                if (!isPaused) {
                    var speechModel = Gson().fromJson(hypothesis, SpeechModel::class.java)
                    speechModel?.let { checkCommands(speechModel.text) }
                }
            }

            override fun onFinalResult(hypothesis: String?) {

            }

            override fun onError(exception: Exception?) {

            }

            override fun onTimeout() {

            }

        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                showInputDialog { userName->
                    var bitmap: Bitmap = Utils.getCorrectlyOrientedImage(this, data?.data!!)

                    val faceDetectionParam = FaceDetectionParam()
                    faceDetectionParam.check_liveness = true
                    faceDetectionParam.check_liveness_level =
                        SettingsActivity.getLivenessLevel(this)
                    var faceBoxes: List<FaceBox>? =
                        FaceSDK.faceDetection(bitmap, faceDetectionParam)

                    if (faceBoxes.isNullOrEmpty()) {
                        Toast.makeText(
                            this,
                            getString(R.string.no_face_detected),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (faceBoxes.size > 1) {
                        Toast.makeText(
                            this,
                            getString(R.string.multiple_face_detected),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val faceImage = Utils.cropFace(bitmap, faceBoxes[0])
                        val templates = FaceSDK.templateExtraction(bitmap, faceBoxes[0])

                        dbManager.insertPerson(
                            userName,
                            faceImage,
                            templates
                        )
                        //personAdapter.notifyDataSetChanged()
                        Toast.makeText(
                            this,
                            getString(R.string.person_enrolled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: java.lang.Exception) {
                //handle exception
                e.printStackTrace()
            }
        }
    }

    private fun showInputDialog(completion:(String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter user name")

        // Set up the input
        val input = EditText(this)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, which ->
            val userInput = input.text.toString()
            if(userInput.isNullOrEmpty())
                return@setPositiveButton
            completion.invoke(userInput)

        }
        //builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

}