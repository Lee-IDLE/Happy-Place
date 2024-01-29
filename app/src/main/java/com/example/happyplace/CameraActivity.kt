package com.example.happyplace

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.happyplace.databinding.ActivityCameraBinding
import java.io.File
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarCamera)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            binding.toolbarCamera.title = "사진 촬영"
        }

        binding.toolbarCamera.setNavigationOnClickListener{
            finish()
        }

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .build()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.btnTakePhoto.setOnClickListener{
            takePhoto()
        }
    }

    private fun startCamera(){
        // ProcessCameraProvider 인스턴스 생성
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    // 이거 UI 쪽에서 카메라 VIEW 만든 다음에 VISIBLE 처리로 필요할 때만 딱 하도록 해야겠다
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("startCamera Exception", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture?: return

        val photoFile = File(outputDirectory, newImageFileName())

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        try{
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object: ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_LONG).show()

                        val returnIntent = Intent()
                        returnIntent.putExtra("path", savedUri.path)
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    }

                    override fun onError(e: ImageCaptureException) {
                        Log.d("CameraX-Debug", "Photo capture failed: ${e.message}", e)
                        e.printStackTrace()
                    }
                }
            )
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun newImageFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.bmp"
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()

        // 카메라 실행 객체 삭제
        if(cameraExecutor.isShutdown == false)
            cameraExecutor.shutdown()
    }

}