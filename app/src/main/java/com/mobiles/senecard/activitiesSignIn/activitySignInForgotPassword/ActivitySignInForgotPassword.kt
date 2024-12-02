package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivitySignInForgotPasswordBinding

class ActivitySignInForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivitySignInForgotPasswordBinding
    private val viewModelSignInForgotPassword: ViewModelSignInForgotPassword by viewModels()

    // LruCache para almacenar el tiempo del último intento
    private val cache: LruCache<String, Long> = LruCache(1)  // Usamos "1" para un solo valor por ahora.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setElements()
        setObservers()
    }

    private fun setElements() {
        // Asignar la funcionalidad del botón de retroceso para regresar a la actividad anterior
        binding.backImageView.setOnClickListener {
            // Llamamos al método para gestionar el retroceso
            onBackPressed()
        }

        binding.sendCodeButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()

            if (isInternetAvailable()) {
                if (canSendEmail()) {
                    viewModelSignInForgotPassword.sendPasswordReset(email)
                    updateLastRequestTime()
                } else {
                    val timeRemaining = getTimeRemaining()
                    // Mostrar el tiempo restante en un Toast
                    Toast.makeText(
                        this,
                        "You can request a verification email again in $timeRemaining.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setObservers() {
        viewModelSignInForgotPassword.showToastMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModelSignInForgotPassword.onToastShown()
            }
        }
    }

    private fun canSendEmail(): Boolean {
        val lastRequestTime = cache.get("last_email_request_time") ?: 0
        val currentTime = System.currentTimeMillis()

        // Verifica si han pasado al menos 30 segundos (30,000 ms)
        return (currentTime - lastRequestTime) >= 30 * 1000
    }

    private fun updateLastRequestTime() {
        val currentTime = System.currentTimeMillis()
        cache.put("last_email_request_time", currentTime)
    }

    private fun getTimeRemaining(): String {
        val lastRequestTime = cache.get("last_email_request_time") ?: 0
        val currentTime = System.currentTimeMillis()

        val timeRemainingMillis = (30 * 1000) - (currentTime - lastRequestTime)

        if (timeRemainingMillis <= 0) return "0s"

        val secondsRemaining = timeRemainingMillis / 1000
        return "$secondsRemaining seconds"
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    // Manejo del botón de retroceso
    override fun onBackPressed() {
        super.onBackPressed() // Llama al comportamiento estándar del botón de retroceso.
        // Si deseas hacer algo más específico con el ViewModel al retroceder, puedes agregar lógica aquí.
        viewModelSignInForgotPassword.onNavigated() // Asegúrate de manejar la navegación en tu ViewModel si es necesario.
    }
}
