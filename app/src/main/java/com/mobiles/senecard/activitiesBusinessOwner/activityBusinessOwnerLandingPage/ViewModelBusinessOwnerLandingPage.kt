package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerLandingPage : ViewModel() {

    // LiveData for the Store object
    private val _store = MutableLiveData<Store?>()
    val store: LiveData<Store?> get() = _store

    private val repositoryAuthentication = RepositoryAuthentication.instance

    private val _isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedOut: LiveData<Boolean>
        get() = _isLoggedOut

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

    fun fetchBusinessOwnerData(businessOwnerId: String) {
        viewModelScope.launch {
            try {
                // Fetch the store by business owner ID
                val storeData = repositoryStore.getStoreByBusinessOwnerId(businessOwnerId)
                if (storeData != null) {
                    // Update the LiveData store object
                    _store.value = storeData

                    _storeName.value = storeData.name!!
                    _rating.value = storeData.rating!!.toFloat()

                    val purchases = storeData.id?.let { repositoryPurchase.getPurchasesByStoreId(it) }
                    _transactionCount.value = purchases!!.size

                    val advertisements = repositoryAdvertisement.getAdvertisementsByStoreId(storeData.id)
                    _advertisementCount.value = advertisements.size
                } else {
                    _errorMessage.value = "Store not found."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching business owner data: ${e.message}"
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            if (repositoryAuthentication.getCurrentUser() == null) {
                _isLoggedOut.value = true
            }
        }
    }

    fun onNavigated() {
        _errorMessage.value = null
    }
}
