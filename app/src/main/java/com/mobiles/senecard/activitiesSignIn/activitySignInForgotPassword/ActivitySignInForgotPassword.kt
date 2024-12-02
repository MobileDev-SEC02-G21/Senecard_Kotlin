package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.mobiles.senecard.R
import com.mobiles.senecard.databinding.ActivitySignInForgotPasswordBinding
import com.mobiles.senecard.activitiesSignIn.activitySignIn.ActivitySignIn
import com.mobiles.senecard.activitiesSignIn.activitySignInVerification.ActivitySignInVerification

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
        binding.backImageView.setOnClickListener {
            viewModelSignInForgotPassword.backImageViewClicked()
        }
        binding.sendCodeButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()

            if (isInternetAvailable()) {
                if (canSendEmail()) {
                    viewModelSignInForgotPassword.sendPasswordReset(email)
                    updateLastRequestTime()
                } else {
                    Toast.makeText(
                        this,
                        "You can only request a verification email every 10 minutes.",
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

        // Verifica si han pasado al menos 10 minutos (600,000 ms)
        return (currentTime - lastRequestTime) >= 10 * 60 * 1000
    }

    private fun updateLastRequestTime() {
        val currentTime = System.currentTimeMillis()
        cache.put("last_email_request_time", currentTime)
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
}