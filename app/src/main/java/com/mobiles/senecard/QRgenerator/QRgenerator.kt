package com.mobiles.senecard.QRgenerator

import QRViewModel
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mobiles.senecard.R

class QRgenerator : AppCompatActivity() {

    private lateinit var ivCodigoQR: ImageView
    private val qrViewModel: QRViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generator)

        ivCodigoQR = findViewById(R.id.ivCodigoQR)

        // Observa los cambios en el LiveData del ViewModel para el código QR
        qrViewModel.qrCodeBitmap.observe(this, Observer { bitmap ->
            ivCodigoQR.setImageBitmap(bitmap)
        })

        // Observa los cambios en el LiveData para manejar errores
        qrViewModel.error.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                showCustomToast(it) // Muestra el mensaje de error con un Toast personalizado
            }
        })

        // Observa el tiempo de generación del código QR
        qrViewModel.generationTime.observe(this, Observer { time ->
            time?.let {
                showCustomToast("Tiempo de generación: $it ms") // Muestra el tiempo de generación con un Toast personalizado
            }
        })

        // Genera el código QR usando el correo electrónico del usuario autenticado
        qrViewModel.generateQRCodeWithEmail()

        // Configurar el botón de "volver"
        val backButton = findViewById<ImageButton>(R.id.options_image_view2)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    // Método para mostrar un Toast personalizado
    private fun showCustomToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()

        // Usar un Handler para mostrar el Toast por más tiempo
        val handler = Handler()
        handler.postDelayed({
            toast.cancel() // Cancelar el Toast después de un tiempo específico
        }, 5000) // Tiempo en milisegundos (5000 ms = 5 segundos)
    }
}

