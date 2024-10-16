package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import ViewModelBusinessOwnerQRScanner
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess.ActivityBusinessOwnerQRSuccess
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ActivityBusinessOwnerQRScanner : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var businessOwnerId: String? = null
    private var storeId: String? = null

    private val viewModel: ViewModelBusinessOwnerQRScanner by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_scanner)

        // Retrieve the businessOwnerId and storeId passed from the previous activity
        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")

        // Back button functionality
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish() // Close current activity
        }

        // Start the camera preview and scanner
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Observe ViewModel changes
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigateToSuccess.observe(this, Observer { success ->
            if (success == true) {
                val intent = Intent(this, ActivityBusinessOwnerQRSuccess::class.java)
                intent.putExtra("businessOwnerId", businessOwnerId)
                intent.putExtra("storeId", storeId)
                startActivity(intent)
            }
        })

        viewModel.navigateToFailure.observe(this, Observer {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, QRCodeAnalyzer { result ->
                    handleQRCodeResult(result)
                })
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleQRCodeResult(result: String) {
        // Pass the QR code (user ID) to the ViewModel for processing
        viewModel.processQRCode(result, businessOwnerId!!, storeId!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class QRCodeAnalyzer(val onQRCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            val source = PlanarYUVLuminanceSource(bytes, imageProxy.width, imageProxy.height, 0, 0, imageProxy.width, imageProxy.height, false)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = MultiFormatReader().decode(binaryBitmap)
                onQRCodeScanned(result.text)
            } catch (e: NotFoundException) {
                // No QR code found
            } finally {
                imageProxy.close()
            }
        }
    }
}