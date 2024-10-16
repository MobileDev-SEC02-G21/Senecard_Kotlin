package com.mobiles.senecard.activityQRCodeUniandesMember

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mobiles.senecard.model.RepositoryAuthentication
import kotlinx.coroutines.launch

class ViewModelQRCodeUniandesMember : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance

    private val _qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> get() = _qrCodeBitmap

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun generateQRCodeWithEmail() {
        viewModelScope.launch {
            val user = repositoryAuthentication.getCurrentUser()

            if (user != null) {
                generateQRCode(user.id!!)
            } else {
                _error.value = "Usuario no autenticado"
            }
        }
    }

    private fun generateQRCode(data: String) {
        if (data.isEmpty()) {
            _error.value = "Los datos no pueden estar vacíos"
            return
        }

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)
            _qrCodeBitmap.value = bitmap
            _error.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Error al generar el código QR"
        }
    }
}

