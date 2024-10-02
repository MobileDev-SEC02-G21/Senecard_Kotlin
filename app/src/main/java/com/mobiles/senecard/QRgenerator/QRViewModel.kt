package com.mobiles.senecard.QRgenerator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRViewModel : ViewModel() {

    // LiveData para el código QR generado (Bitmap)
    private val _qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> get() = _qrCodeBitmap

    // Método para generar el código QR
    fun generateQRCode(data: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                data,
                BarcodeFormat.QR_CODE,
                750,
                750
            )
            // Asignar el código QR generado al LiveData
            _qrCodeBitmap.value = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
