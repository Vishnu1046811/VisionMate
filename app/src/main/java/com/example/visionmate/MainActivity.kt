package com.example.visionmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.visionmate.Constants.LABELS_PATH
import com.example.visionmate.Constants.MODEL_PATH
import com.example.visionmate.SettingsActivity.Companion.getLivenessLevel
import com.example.visionmate.chatbot.ChatBotModel
import com.example.visionmate.component.DoubleTapView
import com.example.visionmate.databinding.ActivityMainBinding
import com.example.visionmate.diary_logger.DiaryLogger
import com.example.visionmate.model.FaceModel
import com.example.visionmate.model.SpeechModel
import com.google.gson.Gson
import com.kbyai.facesdk.FaceBox
import com.kbyai.facesdk.FaceDetectionParam
import com.kbyai.facesdk.FaceSDK
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.preview.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import util.Commands
import util.Helper
import util.Utils
import java.io.ByteArrayOutputStream
import util.commandContain
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

class MainActivity : AppCompatActivity(), Detector.DetectorListener, TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()

        private const val CAMERA_DETECTION_DELAY = 1000L
    }

    private lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: Detector

//    private var tts: TextToSpeech? = null
    private lateinit var textView: TextView

    private lateinit var cameraExecutor: ExecutorService
    var speechRecognizerWrapper :SpeechRecognizerWrapper?= null

    private var chatBot: ChatBotModel? = null

    lateinit var dbManager: DBManager

    var isFaceDetectionCoolOfTime = false

    private var captureAndSummarize = false
    private var pauseObjDetector = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbManager = DBManager(this)
        textView = binding.speechText

        // Initialize TextToSpeech object
//        tts = TextToSpeech(this, this)

        detector = Detector(this, MODEL_PATH, LABELS_PATH, this)
        detector.setup()



        //diaryLogger = DiaryLogger(this)
        chatBot = ChatBotModel(this)
        DiaryLogger.INSTANCE.attachChatBot(chatBot)

        if (allPermissionsGranted()) {
            //startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        //initListener()


        binding.tapDetector.setOnLongClickListener {
            TextToSpeechManager.stopSpeak()
            TextToSpeechManager.speakOut("Processing image, please wait.")
            pauseObjDetector = true
            captureAndSummarize = true
            true
        }

        dbManager = DBManager(this)
        dbManager.loadPerson()

    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        // Compress the bitmap to a byte array (PNG format with 100% quality)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    fun detectUser(image: Bitmap, completion : (isDetected: Boolean, facemodel: FaceModel) -> Unit ){

        if(isFaceDetectionCoolOfTime){
            return
        }
        enableCoolOfTime()
        val frame:Frame = Frame(Resolution(image.width,image.height),bitmapToByteArray(image),0)

        //val bitmap = FaceSDK.yuv2Bitmap(frame.image, frame.size.width, frame.size.height, CameraSelector.LENS_FACING_FRONT)

      val faceDetectionParam = FaceDetectionParam()
        faceDetectionParam.check_liveness = true
        faceDetectionParam.check_liveness_level = getLivenessLevel(this)
        val faceBoxes = FaceSDK.faceDetection(image, faceDetectionParam)

        runOnUiThread {
            Log.e("","")
            //faceView.setFrameSize(Size(bitmap.width, bitmap.height))
            //faceView.setFaceBoxes(faceBoxes)
        }

        if(faceBoxes.size > 0) {
            val faceBox = faceBoxes[0]
            if (faceBox.liveness > SettingsActivity.getLivenessThreshold(this)) {
                val templates = FaceSDK.templateExtraction(image, faceBox)

                var maxSimiarlity = 0f
                var maximiarlityPerson: Person? = null
                for (person in DBManager.personList) {
                    val similarity = FaceSDK.similarityCalculation(templates, person.templates)
                    if (similarity > maxSimiarlity) {
                        maxSimiarlity = similarity
                        maximiarlityPerson = person
                    }
                }
                if (maxSimiarlity > SettingsActivity.getIdentifyThreshold(this)) {
                    //recognized = true
                    val identifiedPerson = maximiarlityPerson
                    val identifiedSimilarity = maxSimiarlity

                    runOnUiThread {
                        val faceImage = Utils.cropFace(image, faceBox)
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra("identified_face", faceImage)
                        intent.putExtra("enrolled_face", identifiedPerson!!.face)
                        intent.putExtra("identified_name", identifiedPerson!!.name)
                        intent.putExtra("similarity", identifiedSimilarity)
                        intent.putExtra("liveness", faceBox.liveness)
                        intent.putExtra("yaw", faceBox.yaw)
                        intent.putExtra("roll", faceBox.roll)
                        intent.putExtra("pitch", faceBox.pitch)
                        //startActivity(intent)

                        completion.invoke(true,FaceModel(faceImage,identifiedPerson!!.face,identifiedPerson!!.name,identifiedSimilarity,faceBox.liveness))
                    }
                }
            }
        }

    }

    private fun enableCoolOfTime() {
        isFaceDetectionCoolOfTime = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(15000)
            isFaceDetectionCoolOfTime = false
        }

    }

    fun saveUserImage(bitmap: Bitmap){
        try {
            val uri = Helper.saveBitmapToFile(this,bitmap, System.currentTimeMillis().toString()+"testfile.png")
            var bitmap: Bitmap = Utils.getCorrectlyOrientedImage(this, Helper.saveBitmapToFile(this,bitmap, System.currentTimeMillis().toString()+"testfile.png"))

            val faceDetectionParam = FaceDetectionParam()
            faceDetectionParam.check_liveness = true
            faceDetectionParam.check_liveness_level = 1// SettingsActivity.getLivenessLevel(this)
            var faceBoxes: List<FaceBox>? = FaceSDK.faceDetection(bitmap, faceDetectionParam)

            if(faceBoxes.isNullOrEmpty()) {
               // Toast.makeText(this, getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show()
            } else if (faceBoxes.size > 1) {
                Toast.makeText(this, getString(R.string.multiple_face_detected), Toast.LENGTH_SHORT).show()
            } else {
                val faceImage = Utils.cropFace(bitmap, faceBoxes[0])
                val templates = FaceSDK.templateExtraction(bitmap, faceBoxes[0])

                dbManager.insertPerson("Hafiz" + Random.nextInt(10000, 20000), faceImage, templates)
                //personAdapter.notifyDataSetChanged()
                //Toast.makeText(this, getString(R.string.person_enrolled), Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception) {
            //handle exception
            e.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            // Handle permission denied
        }
    }


    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            // Set the language for the TTS engine
//            val result = tts!!.setLanguage(Locale.US) // Change Locale.US to your desired language
//
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "The Language not supported!")
//            } else {
//                //btnSpeak!!.isEnabled = true
//            }
//        }
    }

//    private fun speakOut(text: String) {
//        //val text = etSpeak!!.text.toString()
//        if(tts?.isSpeaking==true)
//            return
//        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
//
//    }

    private fun checkCommands(text: String) {
        Log.e(TAG, "checkCommands: $text", )
        if(Commands.quitRegex.matches(text)){
            finishAffinity()
        }else if(Commands.homeBotRegex.matches(text)){
            onBackPressed()
        } /*else if (Commands.chatBotSummariseRegex.commandContain(text)) {
           summarize()
        }*/
        else if (Commands.captureScreenCommand.commandContain(text)) {
            pauseObjDetector = true
           captureAndSummarize = true
        }
    /*    when(text.toLowerCase()){
            Commands.VoiceCommands.EXIT.rawValue ->{
                finish()
            }
            Commands.VoiceCommands.CLOSE.rawValue ->{
                finish()
            }
        }*/
    }

    private fun summarize() {
        DiaryLogger.INSTANCE?.summarize {
            if (it != null) {
                TextToSpeechManager.speakOut(it)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            if (!pauseObjDetector) {
                // Logging scene to text file.
                DiaryLogger.INSTANCE.logScene(rotatedBitmap)
                detector.detect(rotatedBitmap)
            }

            if (captureAndSummarize) {
                captureAndSummarize = false
                DiaryLogger.INSTANCE.captureAndSummarize(rotatedBitmap) {
                    Log.e(TAG, "bindCameraUseCases: $it", )
                    if (it != null) {
//                        TextToSpeechManager.stopSpeak()
                        TextToSpeechManager.speakOut(it)
                    }
                    pauseObjDetector = false
                }
            }

        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.CAMERA] == true) {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
        cameraExecutor.shutdown()
//        if (tts != null) {
//            tts!!.stop()
//            tts!!.shutdown()
//        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
        // Check for speech recognition permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        } else {
            startListening()
        }
    }

    override fun onPause() {
        super.onPause()

// ... later, when you want to stop:
        speechRecognizerWrapper?.stopListening()

// ... and when you're done with the SpeechRecognizer entirely (e.g., in onDestroy):
        speechRecognizerWrapper?.destroy()
    }



    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        Log.d(
            TAG,
            "onDetect() called with: boundingBoxes = $boundingBoxes, inferenceTime = $inferenceTime"
        )
        runOnUiThread {
            binding.overlay.apply {
//                setResults(boundingBoxes)
//                invalidate()

//                for(items in boundingBoxes){
//                    speakOut("There is a "+ items.clsName)
//                }
                boundingBoxes.firstOrNull()?.let { speakObject(it) }
            }
        }
    }

    private fun speakObject(item: BoundingBox) {
        if (item.cx < 0.33) {
            TextToSpeechManager.speakOut("There is a "+ item.clsName + " on your left side")
        } else if (item.cx > 0.66) {
            TextToSpeechManager.speakOut("There is a "+ item.clsName + " on your right side")
        } else {
            TextToSpeechManager.speakOut("There is a "+ item.clsName + " in front of you")
        }
    }



 /*   private fun startListening() {


// In your Activity or other class:
        speechRecognizerWrapper = SpeechRecognizerWrapper(this) // 'this' is your context

        speechRecognizerWrapper?.startListening(recognitionListener)

    }*/
    fun startListening(){
        com.example.visionmate.speech_listener.SpeechRecognizer().startListening(this@MainActivity, object:
            org.vosk.android.RecognitionListener{
            override fun onPartialResult(hypothesis: String?) {

            }

            override fun onResult(hypothesis: String?) {
                var speechModel = Gson().fromJson(hypothesis, SpeechModel::class.java)
                speechModel?.let { checkCommands(speechModel.text) }
            }

            override fun onFinalResult(hypothesis: String?) {

            }

            override fun onError(exception: java.lang.Exception?) {

            }

            override fun onTimeout() {

            }

        })
    }



    // Example usage:
    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("Speech", "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d("Speech", "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Log.d("Speech", "RMS changed: $rmsdB") // Use for visual feedback
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d("Speech", "Buffer received")
        }

        override fun onEndOfSpeech() {
            Log.d("Speech", "End of speech")
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                //SpeechRecognizer.ERROR_RECOGNITION_FAILURE -> "Recognition failure"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error: $error"
            }
            Log.e("Speech", "ERROR: $errorMessage")
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null) {
                Log.d("Speech", "Results: $matches")
                // Process the results (matches) here

                checkCommands(matches.get(0))
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialMatches =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (partialMatches != null) {
                Log.d("Speech", "Partial Results: $partialMatches")
                // Update UI with partial results
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d("Speech", "Event: $eventType")
        }
    }
}


