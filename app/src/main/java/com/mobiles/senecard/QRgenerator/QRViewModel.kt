package com.mobiles.senecard.QRgenerator

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRViewModel : ViewModel() {

    // LiveData para el código QR
    private val _qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> get() = _qrCodeBitmap

    // LiveData para manejar errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Método para generar un QR Code
    fun generateQRCode(data: String) {
        if (data.isEmpty()) {
            _error.value = "Los datos no pueden estar vacíos"
            return
        }

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)
            _qrCodeBitmap.value = bitmap
            _error.value = null // Restablece el estado de error si se genera correctamente
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Error al generar el código QR"
        }
    }

    // Método para generar un QR Code dinámicamente con datos aleatorios
    fun generateFidelityCardQRCode() {
        val timestamp = System.currentTimeMillis()
        val message = "Se ha registrado su tarjeta de fidelidad en $timestamp"
        generateQRCode(message)
    }
}
