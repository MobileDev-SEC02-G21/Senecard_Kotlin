package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mobiles.senecard.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ViewModelBusinessOwnerQRScanner : ViewModel() {

    private val _navigationDestination = MutableLiveData<NavigationDestination?>()
    val navigationDestination: LiveData<NavigationDestination?> = _navigationDestination

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> = _infoMessage

    private val _scannedUserId = MutableLiveData<String?>()
    val scannedUserId: LiveData<String?> = _scannedUserId

    private var isHandlingQRCode = false
    private lateinit var cameraExecutor: ExecutorService

    fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("QRScanner", "Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(previewView.context))
    }

    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        handleQRCode(barcode.rawValue)
                        break // Process only the first QR code
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QRScanner", "QR code processing failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun handleQRCode(qrCodeValue: String?) {
        if (isHandlingQRCode) return // Prevent redundant execution

        isHandlingQRCode = true

        qrCodeValue?.let { userId ->
            if (!NetworkUtils.isInternetAvailable()) {
                _uiState.value = UiState.INFORMATION
                _infoMessage.value = "QR Scanning is only available when online."
                isHandlingQRCode = false
                return
            }

            _scannedUserId.value = userId
            _navigationDestination.value = NavigationDestination.QR_SUCCESS
            isHandlingQRCode = false
        } ?: run {
            _uiState.value = UiState.INFORMATION
            _infoMessage.value = "Invalid QR code. Please try again."
            isHandlingQRCode = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

    fun onInformationAcknowledged() {
        _infoMessage.value = null
    }

    fun navigateBack() {
        _navigationDestination.value = NavigationDestination.LANDING_PAGE
    }
}
