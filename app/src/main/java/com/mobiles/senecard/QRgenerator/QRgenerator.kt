package com.mobiles.senecard.QRgenerator
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.mobiles.senecard.R

class QRgenerator : AppCompatActivity() {

    private lateinit var ivCodigoQR: ImageView
    private lateinit var etDatos: EditText
    private lateinit var btnGenerar: Button

    // Inicializa el ViewModel
    private val qrViewModel: QRViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generator)

        ivCodigoQR = findViewById(R.id.ivCodigoQR)
        etDatos = findViewById(R.id.etDatos)
        btnGenerar = findViewById(R.id.btnGenerar)

        // Observa los cambios en el LiveData del ViewModel
        qrViewModel.qrCodeBitmap.observe(this, Observer { bitmap ->
            // Actualiza el ImageView con el código QR generado
            ivCodigoQR.setImageBitmap(bitmap)
        })

        // Al hacer clic en el botón, llama al método para generar el QR en el ViewModel
        btnGenerar.setOnClickListener {
            val data = etDatos.text.toString()
            qrViewModel.generateQRCode(data)
        }
    }
}
