package com.mobiles.senecard.activityQRCodeUniandesMember

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobiles.senecard.databinding.ActivityQrCodeUniandesMemberBinding
import kotlinx.coroutines.launch

class ActivityQRCodeUniandesMember : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeUniandesMemberBinding
    private val viewModelQRCodeUniandesMember: ViewModelQRCodeUniandesMember by viewModels {
        ViewModelQRCodeUniandesMemberFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeUniandesMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        // Intentamos siempre generar el QR usando el ID del usuario, ya sea de Firebase o caché
        lifecycleScope.launch {
            val currentUserId = viewModelQRCodeUniandesMember.getCurrentUserId()

            if (currentUserId != null) {
                viewModelQRCodeUniandesMember.generateQRCode(currentUserId)
            } else {
                // Si no se pudo obtener el ID del usuario (ni desde Firebase ni del caché)
                Toast.makeText(this@ActivityQRCodeUniandesMember, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
