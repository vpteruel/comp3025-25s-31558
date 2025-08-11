package com.example.final_assignment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.final_assignment.databinding.ActivityMediaPhotoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MediaPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPhotoBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var currentPhotoUri: Uri? = null
    private val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
    private val permissionsRequestCode = 123
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    companion object {
        private const val TAG = "MediaPhotoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMediaPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        if (hasPermissions())
            startCamera()
        else
            requestPermissions()

        binding.captureButton.setOnClickListener {
            capturePhoto()
        }

        binding.previewButton.setOnClickListener {
            showPreview()
        }

        binding.uploadButton.setOnClickListener {
            uploadImageToFirestore()
        }

        binding.backToCameraButton.setOnClickListener {
            binding.cameraPreview.visibility = View.VISIBLE
            binding.imagePreview.visibility = View.GONE

            startCamera()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mediaPhoto)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (exc: Exception) {
                Log.e(TAG, "Camera initialization failed", exc)
                Toast.makeText(
                    this@MediaPhotoActivity,
                    "Camera failed: ${exc.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(binding.cameraPreview.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun uploadImageToFirestore() {
        if (currentPhotoUri == null) {
            Toast.makeText(this, "No photo to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(this, "You must be logged in to upload photos", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = firebaseStorage.reference
        val imagesRef = storageRef.child("images/${user.uid}/${currentPhotoUri!!.lastPathSegment}")
        val uploadTask = imagesRef.putFile(currentPhotoUri!!)

        uploadTask.addOnSuccessListener {
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                val imageMap = hashMapOf(
                    "imageUrl" to imageUrl,
                    "userId" to user.uid,
                    "createdAt" to System.currentTimeMillis()
                )

                firebaseFirestore.collection("images")
                    .add(imageMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        val fileName = "IMG_${System.currentTimeMillis()}.jpg"

        val outputOptions: ImageCapture.OutputFileOptions

        outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValuesOf()
        ).build()

        // val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        // val photoFile = File(picturesDir, fileName)
        // outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    currentPhotoUri = output.savedUri
                    binding.previewButton.isEnabled = true

                    val message = "Photo saved: ${currentPhotoUri?.path ?: "Unknown path"}"
                    Toast.makeText(
                        this@MediaPhotoActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(
                        this@MediaPhotoActivity,
                        "Photo failed: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun showPreview() {
        currentPhotoUri?.let {
            binding.cameraPreview.visibility = View.GONE
            binding.imagePreview.visibility = View.VISIBLE

            Glide.with(this)
                .load(it)
                .into(binding.imagePreview)
        } ?: run {
            Toast.makeText(this, "No photo available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermissions() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            permissionsRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionsRequestCode && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera and storage permissions required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}