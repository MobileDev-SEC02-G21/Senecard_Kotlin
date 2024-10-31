package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryLoyaltyCard
import com.mobiles.senecard.model.entities.Store
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.launch
import java.time.LocalDate
import android.util.Log

class ViewModelBusinessOwnerLandingPage : ViewModel() {

    // LiveData for the Store object
    private val _store = MutableLiveData<Store?>()
    val store: LiveData<Store?> get() = _store

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryStore = RepositoryStore.instance
    private val repositoryPurchase = RepositoryPurchase.instance
    private val repositoryAdvertisement = RepositoryAdvertisement.instance
    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance

    // Logout state
    private val _isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedOut: LiveData<Boolean> get() = _isLoggedOut

    // Store name
    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String> get() = _storeName

    // Number of today's transactions (purchases)
    private val _transactionCount = MutableLiveData<Int>()
    val transactionCount: LiveData<Int> get() = _transactionCount

    // Number of advertisements
    private val _advertisementCount = MutableLiveData<Int>()
    val advertisementCount: LiveData<Int> get() = _advertisementCount

    // Store rating
    private val _rating = MutableLiveData<Float>()
    val rating: LiveData<Float> get() = _rating

    private val _userLiveData = MutableLiveData<User?>()
    val userLiveData: LiveData<User?> get() = _userLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String>
        get() = _errorLiveData

    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Get current user information using Firebase Authentication
    fun getCurrentUserInformation() {
        viewModelScope.launch {
            val user = repositoryAuthentication.getCurrentUser()
            if (user != null) {
                // Now we can set the user info and continue based on that
                _userLiveData.value = user
            } else {
                // Handle case where no user is logged in
                _errorLiveData.value = "No user logged in"
            }
        }
    }

    // Function to fetch business owner data
    fun fetchBusinessOwnerData(businessOwnerId: String) {
        viewModelScope.launch {
            try {
                Log.d("fetchBusinessOwnerData", "Fetching store for business owner ID: $businessOwnerId")

                // Fetch the store by business owner ID
                val storeData = repositoryStore.getStoreByBusinessOwnerId(businessOwnerId)
                if (storeData != null) {
                    Log.d("fetchBusinessOwnerData", "Store fetched successfully: ${storeData.name}")

                    _store.value = storeData
                    _storeName.value = storeData.name ?: "Unknown Store"

                    if (storeData.rating != null) {
                        _rating.value = storeData.rating!!.toFloat()
                        Log.d("fetchBusinessOwnerData", "Store rating: ${storeData.rating}")
                    } else {
                        Log.w("fetchBusinessOwnerData", "Store rating is null")
                        _rating.value = 0.0f // Default value if rating is null
                    }

                    // Fetch all loyalty cards related to the store
                    Log.d("fetchBusinessOwnerData", "Fetching loyalty cards for store ID: ${storeData.id}")
                    val loyaltyCards = repositoryLoyaltyCard.getLoyaltyCardsByStoreId(storeData.id!!)

                    // Initialize the count for today's purchases
                    var todayPurchaseCount = 0
                    val today = LocalDate.now().toString()

                    // For each loyalty card, retrieve the associated purchases and filter today's purchases
                    loyaltyCards.forEach { loyaltyCard ->
                        Log.d("fetchBusinessOwnerData", "Fetching purchases for loyalty card ID: ${loyaltyCard.id}")
                        val purchases = repositoryPurchase.getPurchasesByLoyaltyCardId(loyaltyCard.id!!)
                        todayPurchaseCount += purchases.count { it.date == today }
                    }
                    _transactionCount.value = todayPurchaseCount
                    Log.d("fetchBusinessOwnerData", "Today's purchase count: $todayPurchaseCount")

                    // Fetch advertisement count
                    Log.d("fetchBusinessOwnerData", "Fetching advertisements for store ID: ${storeData.id}")
                    val advertisements = repositoryAdvertisement.getAdvertisementsByStoreId(storeData.id)
                    _advertisementCount.value = advertisements.size
                    Log.d("fetchBusinessOwnerData", "Advertisement count: ${advertisements.size}")

                } else {
                    _errorMessage.value = "Store not found."
                    Log.e("fetchBusinessOwnerData", "Store not found for business owner ID: $businessOwnerId")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching business owner data: ${e.message}"
                Log.e("fetchBusinessOwnerData", "Exception: ${e.message}", e)
            }
        }
    }
    // Function to log out the user
    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            if (repositoryAuthentication.getCurrentUser() == null) {
                _isLoggedOut.value = true
            }
        }
    }

    // Clear error message after navigation
    fun onNavigated() {
        _errorMessage.value = null
    }
}
