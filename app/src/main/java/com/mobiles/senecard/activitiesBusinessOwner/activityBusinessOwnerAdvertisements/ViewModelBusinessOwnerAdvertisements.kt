package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements

import androidx.lifecycle.*
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.AdvertisementResult
import com.mobiles.senecard.model.cache.CacheRepositoryAdvertisement
import com.mobiles.senecard.model.cache.CacheRepositoryStore
import com.mobiles.senecard.model.cache.CacheRepositoryUser
import com.mobiles.senecard.model.cache.StoreResult
import com.mobiles.senecard.model.cache.UserResult
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelBusinessOwnerAdvertisements : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val cacheRepositoryUser = CacheRepositoryUser.instance
    private val cacheRepositoryStore = CacheRepositoryStore.instance
    private val cacheRepositoryAdvertisement = CacheRepositoryAdvertisement.instance

    // LiveData for advertisements
    private val _advertisements = MutableLiveData<List<Advertisement>>()
    val advertisements: LiveData<List<Advertisement>> get() = _advertisements

    // LiveData for UI state
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    // LiveData for error and information messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> get() = _infoMessage

    // LiveData for navigation destination
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    // Fetch advertisements
    fun getAdvertisements() {
        _uiState.value = UiState.LOADING // Show loading popup

        viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (isOnline) {
                handleOnlineCase()
            } else {
                handleOfflineCase()
            }
        }
    }

    private suspend fun handleOnlineCase() {
        val authenticatedUser = repositoryAuthentication.getCurrentUser()

        if (authenticatedUser != null && !authenticatedUser.email.isNullOrBlank()) {
            val email = authenticatedUser.email
            fetchUser(email, isOnline = true)
        } else {
            showError("Unable to fetch user data.")
        }
    }

    private suspend fun handleOfflineCase() {
        val authenticatedUser = repositoryAuthentication.getCurrentUser()

        if (authenticatedUser != null && !authenticatedUser.email.isNullOrBlank()) {
            val email = authenticatedUser.email
            fetchUser(email, isOnline = false)
        } else {
            showError("Offline view unavailable.")
        }
    }

    private suspend fun fetchUser(email: String, isOnline: Boolean) {
        val userResult = if (isOnline) {
            cacheRepositoryUser.getUserByEmail(email)
        } else {
            cacheRepositoryUser.getUserByEmail(email)
        }

        when (userResult) {
            is UserResult.Success -> {
                val user = userResult.user
                fetchStore(user.id!!, isOnline)
            }
            is UserResult.Failure -> {
                showError("Failed to load user details.")
            }
        }
    }

    private suspend fun fetchStore(businessOwnerId: String, isOnline: Boolean) {
        val storeResult = if (isOnline) {
            cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)
        } else {
            cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)
        }

        when (storeResult) {
            is StoreResult.Success -> {
                val store = storeResult.stores.first()
                fetchAdvertisements(store.id!!, isOnline)
            }
            is StoreResult.Failure -> {
                showError("Failed to fetch store data.")
            }
        }
    }

    private suspend fun fetchAdvertisements(storeId: String, isOnline: Boolean) {
        val adResult = if (isOnline) {
            cacheRepositoryAdvertisement.getAdvertisementsByStoreId(storeId)
        } else {
            cacheRepositoryAdvertisement.getAdvertisementsByStoreId(storeId)
        }

        when (adResult) {
            is AdvertisementResult.Success -> {
                _advertisements.value = adResult.advertisements
                _uiState.value = UiState.SUCCESS // Dismiss loading popup
            }
            is AdvertisementResult.Failure -> {
                showError("Failed to fetch advertisements.")
            }
        }
    }

    // Helper functions to manage UI state and messages
    private fun showError(message: String) {
        _uiState.value = UiState.ERROR
        _errorMessage.value = message
    }

    private fun showInfo(message: String) {
        _infoMessage.value = message
        _uiState.value = UiState.INFORMATION
    }

    fun onAddAdvertisementClicked() {
        viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (isOnline) {
                _navigateTo.value = NavigationDestination.ADVERTISEMENT_CREATE
            } else {
                showInfo("This functionality requires an internet connection.")
            }
        }
    }

    // Reset navigation event after it's handled
    fun onNavigationHandled() {
        _navigateTo.value = null
    }

    fun deleteAdvertisement(advertisement: Advertisement) {
        viewModelScope.launch {
            // Show loading popup while deleting
            _uiState.value = UiState.LOADING

            val advertisementResult = withContext(Dispatchers.IO) {
                cacheRepositoryAdvertisement.deleteAdvertisement(advertisement.id!!)
            }

            when (advertisementResult) {
                is AdvertisementResult.Success -> {
                    // Successfully deleted the advertisement
                    _advertisements.value = _advertisements.value?.filter { it.id != advertisement.id }
                    _uiState.value = UiState.SUCCESS // Hide loading popup
                    showInfo("${advertisement.title} deleted successfully.")
                }
                is AdvertisementResult.Failure -> {
                    // Deletion failed
                    _uiState.value = UiState.INFORMATION // Hide loading popup
                    showInfo(advertisementResult.error)
                }
            }
        }
    }


    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            _navigateTo.value = NavigationDestination.INITIAL
        }
    }

    fun onInformationAcknowledged() {
        _infoMessage.value = null
        _uiState.value = UiState.SUCCESS
    }

}
