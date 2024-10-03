package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.RepositoryAdvertisement
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerLandingPage : ViewModel() {

    // Constant user ID for the business owner (Delete with integration)
    private val businessOwnerId = "1Lp1RRd1uo11fgfIsFMU"

    private val repositoryStore = RepositoryStore.instance
    private val repositoryPurchase = RepositoryPurchase.instance
    private val repositoryAdvertisement = RepositoryAdvertisement.instance

    // Store data
    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String>
        get() = _storeName

    // Number of transactions (purchases)
    private val _transactionCount = MutableLiveData<Int>()
    val transactionCount: LiveData<Int>
        get() = _transactionCount

    // Number of advertisements
    private val _advertisementCount = MutableLiveData<Int>()
    val advertisementCount: LiveData<Int>
        get() = _advertisementCount

    // Store rating
    private val _rating = MutableLiveData<Float>()
    val rating: LiveData<Float>
        get() = _rating

    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    // Fetch the store data, advertisements, and transactions based on the business owner ID
    fun fetchBusinessOwnerData() {
        viewModelScope.launch {
            try {
                // Fetch store by the user ID
                val store = repositoryStore.getStoreByUserId(businessOwnerId)
                if (store != null) {
                    _storeName.value = store.name
                    _rating.value = store.rating.toFloat()

                    // Fetch the number of transactions (purchases) by the store ID
                    val purchases = repositoryPurchase.getPurchasesByStore(store.storeId)
                    _transactionCount.value = purchases.size

                    // Fetch the number of advertisements by the store ID
                    val advertisements = repositoryAdvertisement.getAdvertisementsByStoreId(store.storeId)
                    _advertisementCount.value = advertisements.size
                } else {
                    _errorMessage.value = "Store not found."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching business owner data: ${e.message}"
            }
        }
    }

    // Handle any clean-up or navigation
    fun onNavigated() {
        _errorMessage.value = null
    }
}
