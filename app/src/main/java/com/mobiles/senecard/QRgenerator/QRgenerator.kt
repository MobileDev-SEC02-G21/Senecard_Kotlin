package com.mobiles.senecard.QRgenerator

import QRViewModel
import android.os.Bundle
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
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
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
}
