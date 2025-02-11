package com.example.visionmate

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
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
import com.example.visionmate.databinding.ActivityMainBinding
import com.example.visionmate.diary_logger.DiaryLogger
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Detector.DetectorListener, TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: Detector

    private var tts: TextToSpeech? = null
    private lateinit var textView: TextView

    private lateinit var cameraExecutor: ExecutorService
    var speechRecognizerWrapper :SpeechRecognizerWrapper?= null

    private var dailyLogger: DiaryLogger? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textView = binding.speechText

        // Initialize TextToSpeech object
        tts = TextToSpeech(this, this)

        detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        detector.setup()

        dailyLogger = DiaryLogger(this)

        if (allPermissionsGranted()) {
            //startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        //initListener()

        binding.btnSummarize.setOnClickListener {
            dailyLogger?.summarize{
                if (it != null) {
                    tts?.stop()
                    speakOut(it)
                }
            }
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
        if (status == TextToSpeech.SUCCESS) {
            // Set the language for the TTS engine
            val result = tts!!.setLanguage(Locale.US) // Change Locale.US to your desired language

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            } else {
                //btnSpeak!!.isEnabled = true
            }
        }
    }

    private fun speakOut(text: String) {
        //val text = etSpeak!!.text.toString()
        if(tts?.isSpeaking==true)
            return
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")

    }

    private fun checkCommands(text: String) {
        if(Commands.quitRegex.matches(text)){
            finishAffinity()
        }else if(Commands.homeBotRegex.matches(text)){
            onBackPressed()
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
            // Logging scene to text
            dailyLogger?.logScene(rotatedBitmap)
            detector.detect(rotatedBitmap)
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
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
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

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
                for(items in boundingBoxes){
                    speakOut("There is a"+ items.clsName +" in 5 meters")
                }
            }
        }
    }



    private fun startListening() {


// In your Activity or other class:
        speechRecognizerWrapper = SpeechRecognizerWrapper(this) // 'this' is your context

        speechRecognizerWrapper?.startListening(recognitionListener)

    }

       /* SpeechRecognizer(this).startListening(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Called when the user is ready to speak
                Log.e("SpeechRecognition", "")
            }

            override fun onBeginningOfSpeech() {
                // Called when the user starts to speak
                Log.e("SpeechRecognition", "")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Called when the energy of the speech changes
                Log.e("SpeechRecognition", "")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Called when a buffer of audio is received
                Log.e("SpeechRecognition", "")
            }

            override fun onEndOfSpeech() {
                // Called when the user stops speaking
                Log.e("SpeechRecognition", "")
            }

            override fun onError(error: Int) {
                // Called when an error occurs
                Log.e("SpeechRecognition", "Error: $error")
                if(error == 7)
               // speakOut("REPEAT")
                startListening()
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null && data.isNotEmpty()) {
                    val spokenText = data[0]
                    textView.text = textView.text.toString() + "\n $spokenText"
                    startListening()

                    checkCommands(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Called when partial results are available
                Log.e("SpeechRecognition", "")
                //speakOut("REPEAT")
                startListening()
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Called when a platform-specific event occurs
            }
        })

        //speechRecognizer.startListening(speechRecognizerIntent)
    }*/




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


