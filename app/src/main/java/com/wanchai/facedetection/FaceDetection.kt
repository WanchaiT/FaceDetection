package com.wanchai.facedetection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.nio.ByteBuffer
import java.util.*


class FaceDetection( textView: TextView, imvProfile: ImageView, mContext: Context) : ImageAnalysis.Analyzer {
    var s = textView
    var imv = imvProfile
    var context = mContext
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
//            val buffer = image.planes[0].buffer
//            val data = buffer.toByteArray()
//            val pixels = data.map { it.toInt() and 0xFF }
//            val luma = pixels.average()
//            onLuminosityUpdated?.invoke(luma)

//            val imageByteArray: ByteArray = Base64.decode(data, Base64.NO_WRAP)

        val mediaImage = image.image
        val imageRotation = degreesToFirebaseRotation(270)


        // add time delay
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (mediaImage != null) {
                    val imageFirebase = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)

                    val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build()

                    val detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts)

                    val result = detector.detectInImage(imageFirebase)
                        .addOnSuccessListener { faces ->
                            onSuccessListener(faces, imageFirebase)
                        }
                        .addOnFailureListener { e ->
                            onFailureListener(e)
                        }
                    Log.d("face:", "result:$result")
                }
                image.close()
            }
        }, 1000)
    }

    private fun onSuccessListener(faces: List<FirebaseVisionFace>, imageFirebase: FirebaseVisionImage) {
        for (face in faces) {
            s.text = "${face.smilingProbability} " + "${face.rightEyeOpenProbability} " + "${face.leftEyeOpenProbability} "
            Glide.with(context)
                    .asBitmap()
                    .load(imageFirebase.bitmap)
                    .into(imv)
        }
        Log.d("face:", faces.toString())
    }

    private fun onFailureListener(e: java.lang.Exception) {
        Log.d("face:", e.toString())
        s.text = ""
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}