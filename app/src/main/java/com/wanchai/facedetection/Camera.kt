package com.wanchai.facedetection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Size
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class Camera(var context: Context, var activity: Activity) {

    @SuppressLint("RestrictedApi")
    public fun startCamera(viewFinder: PreviewView, textViewLuma: TextView, imvProfile: ImageView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                    }

            val imageAnalyzer = FaceDetection( textViewLuma, imvProfile, context)

            val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(360, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageAnalyzer)

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        activity as LifecycleOwner, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
            }


        }, ContextCompat.getMainExecutor(context))
    }
}