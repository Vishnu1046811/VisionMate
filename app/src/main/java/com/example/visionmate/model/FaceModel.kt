package com.example.visionmate.model

import android.graphics.Bitmap

data class FaceModel(val identified_face: Bitmap,val enrolled_face:Bitmap, val identified_name: String,
                     val identifiedSimilarity:Float,val liveness:Float)
