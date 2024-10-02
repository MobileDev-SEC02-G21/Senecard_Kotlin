package com.mobiles.senecard.QRgenerator

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import android.widget.ImageView
import com.mobiles.senecard.R

class QRgenerator : AppCompatActivity() {

    private lateinit var ivCodigoQR: ImageView

    // Inicializa el ViewModel
    private val qrViewModel: QRViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generator)

        ivCodigoQR = findViewById(R.id.ivCodigoQR)

        // Observa los cambios en el LiveData del ViewModel
        qrViewModel.qrCodeBitmap.observe(this, Observer { bitmap ->
            // Actualiza el ImageView con el código QR generado
            ivCodigoQR.setImageBitmap(bitmap)
        })

        // Genera el código QR dinámicamente al iniciar la actividad
        qrViewModel.generateFidelityCardQRCode()
    }
}
