package com.mobiles.senecard.activityQRCodeUniandesMember

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.utils.UserSessionManager
import java.io.ByteArrayOutputStream

class ViewModelQRCodeUniandesMember(private val context: Context) : ViewModel() {

    private val repositoryAuth = RepositoryAuthentication.instance
    private val _qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> get() = _qrCodeBitmap

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Obtiene el ID del usuario actual de Firebase o el caché si no hay conexión
    suspend fun getCurrentUserId(): String? {
        val firebaseUserId = repositoryAuth.getCurrentUser()?.id

        return if (firebaseUserId != null) {
            UserSessionManager.saveUserId(context, firebaseUserId) // Guarda el ID en caché para uso offline
            firebaseUserId
        } else {
            UserSessionManager.getUserId(context) // Carga el ID desde el caché si no hay conexión
        }
    }

    // Genera el QR con el ID del usuario proporcionado
    fun generateQRCode(userId: String) {
        viewModelScope.launch {
            try {
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 750, 750)
                _qrCodeBitmap.value = bitmap
                saveQRCodeToCache(bitmap, userId) // Guarda el QR generado en caché
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al generar el código QR"
            }
        }
    }

    private fun saveQRCodeToCache(bitmap: Bitmap, userId: String) {
        val prefs = context.getSharedPreferences("QRCodeCache", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        editor.putString("lastQRCode_$userId", encodedImage)
        editor.apply()
    }

    suspend fun loadQRCodeFromCache(): Boolean {
        val userId = UserSessionManager.getUserId(context)
        if (userId != null) {
            val prefs = context.getSharedPreferences("QRCodeCache", Context.MODE_PRIVATE)
            val encodedImage = prefs.getString("lastQRCode_$userId", null)

            if (encodedImage != null) {
                val decodedByte = Base64.decode(encodedImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                _qrCodeBitmap.value = bitmap
                return true
            }
        }
        return false
    }
}
