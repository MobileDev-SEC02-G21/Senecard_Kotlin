package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.mobiles.senecard.databinding.ActivitySignInForgotPasswordBinding
import kotlinx.coroutines.tasks.await
import java.util.Locale

class ActivitySignInForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivitySignInForgotPasswordBinding
    private val viewModelSignInForgotPassword: ViewModelSignInForgotPassword by viewModels {
        ViewModelSignInForgotPasswordFactory(applicationContext)
    }

    private val cache: LruCache<String, Long> = LruCache(2) // Para controlar el tiempo entre solicitudes
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setElements()
        setObservers()
    }

    private fun setElements() {
        binding.backImageView.setOnClickListener {
            onBackPressed()
        }

        binding.sendCodeButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()

            if (isInternetAvailable()) {
                if (canSendEmail()) {
                    checkLocationPermission()
                } else {
                    val timeRemaining = getTimeRemaining()
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

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            lifecycleScope.launchWhenStarted {
                val address = getAddress()
                if (address != "Permission not granted") {
                    viewModelSignInForgotPassword.sendPasswordReset(binding.emailEditText.text.toString())
                    updateLastRequestTime()
                    logButtonClickInFirestore(address)
                } else {
                    Toast.makeText(this@ActivitySignInForgotPassword, "Location permission is required.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun logButtonClickInFirestore(address: String) {
        val data = hashMapOf(
            "address" to address,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            firestore.collection("passwordChangeClicks").add(data).await()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to log button click: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getAddress(): String {
        val location: Location? = fusedLocationClient.lastLocation.await()
        return if (location != null) {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: MutableList<Address>? = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            if (addresses?.isNotEmpty() == true) {
                val address = addresses?.get(0)
                "${address?.thoroughfare}, ${address?.locality}, ${address?.countryName}"
            } else {
                "Address not found"
            }
        } else {
            "Location not available"
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lifecycleScope.launchWhenStarted {
                    val address = getAddress()
                    if (address != "Permission not granted") {
                        viewModelSignInForgotPassword.sendPasswordReset(binding.emailEditText.text.toString())
                        updateLastRequestTime()
                        logButtonClickInFirestore(address)
                    }
                }
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModelSignInForgotPassword.onNavigated()
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
    }
}
