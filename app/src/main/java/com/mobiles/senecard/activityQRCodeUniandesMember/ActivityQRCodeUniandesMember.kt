package com.mobiles.senecard.activityQRCodeUniandesMember

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mobiles.senecard.R
import com.mobiles.senecard.activityQRCodeUniandesMember.ViewModelQRCodeUniandesMember
import com.mobiles.senecard.activityQRCodeUniandesMember.ViewModelQRCodeUniandesMemberFactory
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
        // Configura el SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            refreshQRCode()
        }

        // Intenta cargar el QR desde el caché o lo genera
        lifecycleScope.launch {
            val currentUserId = viewModelQRCodeUniandesMember.getCurrentUserId()

            if (currentUserId != null) {
                val isLoadedFromCache = viewModelQRCodeUniandesMember.loadQRCodeFromCache()

                if (!isLoadedFromCache) {
                    viewModelQRCodeUniandesMember.generateQRCode(currentUserId)
                }
            } else {
                Toast.makeText(
                    this@ActivityQRCodeUniandesMember,
                    "No se pudo obtener el ID del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Botón de retroceso
        binding.optionsImageView2.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun refreshQRCode() {
        lifecycleScope.launch {
            val currentUserId = viewModelQRCodeUniandesMember.getCurrentUserId()

            if (currentUserId != null) {
                // Fuerza la regeneración del QR ignorando el caché
                viewModelQRCodeUniandesMember.generateQRCode(currentUserId)
            } else {
                Toast.makeText(
                    this@ActivityQRCodeUniandesMember,
                    "No se pudo obtener el ID del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Detiene la animación del SwipeRefresh
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setObservers() {
        viewModelQRCodeUniandesMember.qrCodeBitmap.observe(this) { bitmap ->
            binding.ivCodigoQR.setImageBitmap(bitmap)
        }

        viewModelQRCodeUniandesMember.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
