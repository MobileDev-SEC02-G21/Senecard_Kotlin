package com.mobiles.senecard.activityQRCodeUniandesMember

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.databinding.ActivityQrCodeUniandesMemberBinding

class ActivityQRCodeUniandesMember : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeUniandesMemberBinding
    private val viewModelQRCodeUniandesMember: ViewModelQRCodeUniandesMember by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrCodeUniandesMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        viewModelQRCodeUniandesMember.generateQRCodeWithEmail()

        binding.optionsImageView2.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setObservers() {
        viewModelQRCodeUniandesMember.qrCodeBitmap.observe(this) { bitmap ->
            binding.ivCodigoQR.setImageBitmap(bitmap)
        }

        viewModelQRCodeUniandesMember.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
