package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
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
    private var isProcessingQR: Boolean = false // Flag to track if a QR code is being processed

    private val viewModel: ViewModelBusinessOwnerQRScanner by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_scanner)

        businessOwnerId = intent.getStringExtra("businessOwnerId")
        storeId = intent.getStringExtra("storeId")

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigateToSuccess.observe(this, Observer { success ->
            if (success == true) {
                // Navigate to the success page with the user ID and store ID
                val intent = Intent(this, ActivityBusinessOwnerQRSuccess::class.java)
                intent.putExtra("businessOwnerId", businessOwnerId)
                intent.putExtra("storeId", storeId)
                intent.putExtra("userId", viewModel.userId.value) // Pass the scanned user ID
                startActivity(intent)
                viewModel.onNavigated()
                isProcessingQR = false // Re-enable scanning after navigation
            }
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                isProcessingQR = false // Re-enable scanning after error
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
        if (!isProcessingQR) {
            Log.d("QRScannerActivity", "QR code result: $result")

            isProcessingQR = true // Set the flag to prevent further scans until processing is done
            lifecycleScope.launch {
                viewModel.processQRCode(result) // Only process the QR code, no loyalty card logic here
            }
        }
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
                Log.e("QRCodeAnalyzer", "No QR code found")
            } finally {
                imageProxy.close()
            }
        }
    }
}
