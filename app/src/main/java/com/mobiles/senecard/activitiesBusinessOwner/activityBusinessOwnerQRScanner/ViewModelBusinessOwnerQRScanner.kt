package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch
import java.time.LocalDate

class ViewModelBusinessOwnerQRScanner : ViewModel() {

    private val repositoryPurchase = RepositoryPurchase.instance
    private val repositoryUser = RepositoryUser.instance

    // LiveData for navigation
    private val _navigateToSuccess = MutableLiveData<Boolean>()
    val navigateToSuccess: LiveData<Boolean> get() = _navigateToSuccess

    private val _navigateToFailure = MutableLiveData<Boolean>()
    val navigateToFailure: LiveData<Boolean> get() = _navigateToFailure

    // LiveData for QR code result
    private val _qrCodeResult = MutableLiveData<String>()
    val qrCodeResult: LiveData<String> get() = _qrCodeResult

    // Function to process the scanned QR code
    fun processQRCode(qrCode: String) {
        _qrCodeResult.value = qrCode

        // Launch a coroutine to verify the user by the QR code
        viewModelScope.launch {
            val user = repositoryUser.getUserById(qrCode)
            if (user != null) {
                // Simulate adding a purchase or further actions
                val purchaseSuccessful = repositoryPurchase.addPurchase(
                    loyaltyCardId = "CVIThQw5R3MklwATaJlv",
                    date= LocalDate.now().toString(),
                    isEligible = true, // Eligible for a loyalty program
                    rating = 5.0, // A rating placeholder
                )
                if (purchaseSuccessful) {
                    _navigateToSuccess.value = true
                } else {
                    _navigateToFailure.value = true
                }
            } else {
                _navigateToFailure.value = true
            }
        }
    }

    // Function to reset navigation after it's been handled
    fun onNavigated() {
        _navigateToSuccess.value = false
        _navigateToFailure.value = false
    }
}
