package com.mobiles.senecard.QRgenerator

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

class QRViewModel : ViewModel() {

    private val _qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> get() = _qrCodeBitmap

    // Método para generar un QR Code
    fun generateQRCode(data: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)
            _qrCodeBitmap.value = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Método para generar un QR Code dinámicamente con datos aleatorios
    fun generateFidelityCardQRCode() {

        val timestamp = System.currentTimeMillis()
        val message = "Se ha registrado su tarjeta de fidelidad en $timestamp"
        generateQRCode(message)
    }
}
