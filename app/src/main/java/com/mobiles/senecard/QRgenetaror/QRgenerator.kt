package com.mobiles.senecard

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.Exception

class QRgenerator : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_generator)

        val ivCodigoQR: ImageView = findViewById(R.id.ivCodigoQR)
        val etDatos: EditText = findViewById(R.id.etDatos)
        val btnGenerar: Button = findViewById(R.id.btnGenerar)

        btnGenerar.setOnClickListener {
            try {
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                    etDatos.text.toString(),
                    BarcodeFormat.QR_CODE,
                    750,
                    750
                )
                ivCodigoQR.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        println(etDatos)
    }

}