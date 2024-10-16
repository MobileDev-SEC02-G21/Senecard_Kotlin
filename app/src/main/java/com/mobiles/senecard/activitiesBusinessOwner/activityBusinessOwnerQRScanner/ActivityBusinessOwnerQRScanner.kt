package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.mobiles.senecard.R
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRFailure.ActivityBusinessOwnerQRFailure
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess.ActivityBusinessOwnerQRSuccess
import com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage.ActivityBusinessOwnerLandingPage
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.RepositoryStore
import kotlinx.coroutines.launch
import java.time.LocalDate

class ActivityBusinessOwnerQRScanner : AppCompatActivity() {

    private val cameraPermissionCode = 101
    private val repositoryPurchase = RepositoryPurchase.instance // Assuming the repository is already set up
    private val repositoryStore = RepositoryStore.instance // Assuming store repository if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_owner_qr_scanner)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        }

        // Back button functionality
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            // Navigate back to the landing page
            val intent = Intent(this, ActivityBusinessOwnerLandingPage::class.java)
            startActivity(intent)
            finish() // Close current activity
        }

        // Test Success Button
        val testSuccessButton = findViewById<Button>(R.id.testSuccessButton)
        testSuccessButton.setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerQRSuccess::class.java)
            startActivity(intent)

            // Simulate saving a purchase in the database for testing
            lifecycleScope.launch {
                val storeId = "QuQWLPcClpc7b3AMm33CX"
                val rating = 5.0
                val uniandesMemberId = "CVlThQw5R3MklwATAJlV"

                val success = repositoryPurchase.addPurchase(
                    storeId = storeId,
                    rating = rating,
                    uniandesMemberId = uniandesMemberId,
                    eligible = true,
                    date= LocalDate.now().toString()
                )

                if (success) {
                    // Notify that the purchase was saved successfully (optional)
                }
            }
        }

        // Test Failure Button
        val testFailureButton = findViewById<Button>(R.id.testFailureButton)
        testFailureButton.setOnClickListener {
            val intent = Intent(this, ActivityBusinessOwnerQRFailure::class.java)
            startActivity(intent)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this), QRCodeAnalyzer { result ->
                    // Handle QR code result here
                })
            }

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            preview.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider)
        }, ContextCompat.getMainExecutor(this))
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
