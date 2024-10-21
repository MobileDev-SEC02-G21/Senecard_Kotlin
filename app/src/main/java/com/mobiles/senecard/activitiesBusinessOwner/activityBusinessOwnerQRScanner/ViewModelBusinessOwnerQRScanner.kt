package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerQRScanner : ViewModel() {

    private val repositoryUser = RepositoryUser.instance

    private val _navigateToSuccess = MutableLiveData<Boolean>()
    val navigateToSuccess: LiveData<Boolean> get() = _navigateToSuccess

    private val _navigateToFailure = MutableLiveData<Boolean>()
    val navigateToFailure: LiveData<Boolean> get() = _navigateToFailure

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData to hold the userId once a user is found
    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    // Function to process the QR code
    fun processQRCode(qrCode: String) {
        viewModelScope.launch {
            Log.d("QRScannerViewModel", "Processing QR Code: $qrCode")

            try {
                // Fetch the user by QR code
                Log.d("QRScannerViewModel", "Attempting to fetch user with ID: $qrCode")
                val user = repositoryUser.getUserById(qrCode)

                if (user != null) {
                    Log.d("QRScannerViewModel", "User found: ${user.name}")
                    _userId.value = qrCode // Save the user ID
                    _navigateToSuccess.value = true
                } else {
                    Log.e("QRScannerViewModel", "No user found for QR Code: $qrCode")
                    _errorMessage.value = "User not found"
                    _navigateToFailure.value = true
                }
            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Error while processing QR code: ${e.message}")
                _errorMessage.value = "Error processing QR Code"
                _navigateToFailure.value = true
            }
        }
    }

    // This function resets the navigation flag after the navigation is triggered
    fun onNavigated() {
        _navigateToSuccess.value = false
        _navigateToFailure.value = false
    }
}
