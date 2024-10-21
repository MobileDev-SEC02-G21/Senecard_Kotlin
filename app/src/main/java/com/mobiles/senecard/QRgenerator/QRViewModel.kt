import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class QRViewModel : ViewModel() {

    private val repositoryUser = RepositoryUser.instance

    private val _qrCodeBitmap = MutableLiveData<Bitmap>()
    val qrCodeBitmap: LiveData<Bitmap> get() = _qrCodeBitmap

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // LiveData para almacenar el tiempo de generación
    private val _generationTime = MutableLiveData<Long>()
    val generationTime: LiveData<Long> get() = _generationTime

    // Método para generar un QR Code
    fun generateQRCode(data: String) {
        if (data.isEmpty()) {
            _error.value = "Los datos no pueden estar vacíos"
            return
        }

        try {
            // Obtener el tiempo de inicio
            val startTime = System.currentTimeMillis()

            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 750, 750)

            // Calcular el tiempo de generación
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime // Duración en milisegundos
            _generationTime.value = duration // Guardar duración en LiveData

            _qrCodeBitmap.value = bitmap
            _error.value = null // Restablece el estado de error si se genera correctamente
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Error al generar el código QR"
        }
    }

    // Método para generar un QR Code usando el correo electrónico del usuario autenticado
    fun generateQRCodeWithEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val email = user.email // Obtén el correo electrónico del usuario autenticado
            if (email != null) {
                viewModelScope.launch {
                    // Busca al usuario en Firestore usando su correo electrónico
                    val firestoreUser = repositoryUser.getUserByEmail(email)
                    if (firestoreUser != null && firestoreUser.id != null) {
                        // Genera el QR con el ID del usuario encontrado en Firestore
                        generateQRCode(firestoreUser.id!!)
                    } else {
                        _error.value = "Usuario no encontrado en Firestore"
                    }
                }
            } else {
                _error.value = "Correo electrónico no disponible"
            }
        } else {
            _error.value = "Usuario no autenticado"
        }
    }
}
