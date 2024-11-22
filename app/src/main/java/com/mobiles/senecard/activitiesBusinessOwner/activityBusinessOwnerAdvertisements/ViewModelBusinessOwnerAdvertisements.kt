package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerAdvertisements

import android.util.Log
import androidx.lifecycle.*
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.*
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelBusinessOwnerAdvertisements : ViewModel() {

    private var fetchJob: Job? = null

    // Repositories
    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val cacheRepositoryUser = CacheRepositoryUser.instance
    private val cacheRepositoryStore = CacheRepositoryStore.instance
    private val cacheRepositoryAdvertisement = CacheRepositoryAdvertisement.instance

    // LiveData for advertisements
    private val _advertisements = MutableLiveData<List<Advertisement>>()
    val advertisements: LiveData<List<Advertisement>> get() = _advertisements

    // UI state LiveData
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    // Error and information messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> get() = _infoMessage

    // Navigation destination LiveData
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    // Online status LiveData
    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> get() = _isOnline

    fun getAdvertisements() {
        Log.d("ViewModel", "getAdvertisements called")
        _uiState.value = UiState.LOADING

        // Cancel any existing fetch operations
        fetchJob?.cancel()

        // Start a new fetch operation
        fetchJob = viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }
            Log.d("ViewModel", "Network status: isOnline = $isOnline")

            if (isOnline) {
                handleOnlineCase()
            } else {
                handleOfflineCase()
            }
        }
    }

    private suspend fun handleOnlineCase() {
        Log.d("ViewModel", "handleOnlineCase called")
        val authenticatedUser = repositoryAuthentication.getCurrentUser()

        if (authenticatedUser != null && !authenticatedUser.email.isNullOrBlank()) {
            Log.d("ViewModel", "Authenticated user found: ${authenticatedUser.email}")
            handleUserFetch(authenticatedUser.email!!, isOnline = true)
        } else {
            Log.e("ViewModel", "Failed to authenticate user")
            showErrorPopup("Unable to fetch user data.")
        }
    }

    private suspend fun handleOfflineCase() {
        Log.d("ViewModel", "handleOfflineCase called")
        val authenticatedUser = repositoryAuthentication.getCurrentUser()

        if (authenticatedUser != null && !authenticatedUser.email.isNullOrBlank()) {
            Log.d("ViewModel", "Authenticated user found offline: ${authenticatedUser.email}")
            handleUserFetch(authenticatedUser.email!!, isOnline = false)
        } else {
            Log.e("ViewModel", "Offline view unavailable: User not authenticated")
            showErrorPopup("Offline view unavailable.")
        }
    }

    private suspend fun handleUserFetch(email: String, isOnline: Boolean) {
        Log.d("ViewModel", "handleUserFetch called: email=$email, isOnline=$isOnline")
        val userResult = cacheRepositoryUser.getUserByEmail(email)

        when (userResult) {
            is UserResult.Success -> {
                Log.d("ViewModel", "User fetched successfully: ${userResult.user}")
                val businessOwnerId = userResult.user.id
                handleStoreFetch(businessOwnerId!!, isOnline)
            }
            is UserResult.Failure -> {
                Log.e("ViewModel", "Failed to fetch user details: ${userResult.error}")
                showErrorPopup("Failed to load user details.")
            }
        }
    }

    private suspend fun handleStoreFetch(businessOwnerId: String, isOnline: Boolean) {
        Log.d("ViewModel", "handleStoreFetch called: businessOwnerId=$businessOwnerId, isOnline=$isOnline")
        val storeResult = cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)

        when (storeResult) {
            is StoreResult.Success -> {
                Log.d("ViewModel", "Store fetched successfully: ${storeResult.stores.first()}")
                val store = storeResult.stores.first()
                handleAdvertisementsFetch(store.id!!, isOnline)
            }
            is StoreResult.Failure -> {
                Log.e("ViewModel", "Failed to fetch store data")
                showErrorPopup("Failed to fetch store data.")
            }
        }
    }

    private suspend fun handleAdvertisementsFetch(storeId: String, isOnline: Boolean) {
        val adResult = cacheRepositoryAdvertisement.getAdvertisementsByStoreId(storeId)

        when (adResult) {
            is AdvertisementResult.Success -> {
                _advertisements.postValue(adResult.advertisements)
                _infoMessage.postValue("You are viewing ${if (isOnline) "an online" else "an offline"} version.")
                _uiState.postValue(UiState.INFORMATION)
            }
            is AdvertisementResult.Failure -> {
                _errorMessage.postValue("Failed to fetch advertisements.")
                _uiState.postValue(UiState.ERROR)
            }
        }
    }


    fun onAddAdvertisementClicked() {
        viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (isOnline) {
                _navigateTo.value = NavigationDestination.ADVERTISEMENT_CREATE
            } else {
                showInfoPopup("This functionality requires an internet connection.")
            }
        }
    }

    fun deleteAdvertisement(advertisement: Advertisement) {
        viewModelScope.launch {
            // Check online status
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (!isOnline) {
                // Update infoMessage and set UI state
                _infoMessage.value = "You can't delete advertisements while offline. Please connect to the internet and try again."
                _uiState.value = UiState.INFORMATION
                return@launch
            }

            // Proceed with deletion if online
            _uiState.value = UiState.LOADING

            val advertisementResult = withContext(Dispatchers.IO) {
                cacheRepositoryAdvertisement.deleteAdvertisement(advertisement.id!!)
            }

            when (advertisementResult) {
                is AdvertisementResult.Success -> {
                    // Update the list of advertisements
                    _advertisements.value = _advertisements.value?.filter { it.id != advertisement.id }
                    _uiState.value = UiState.SUCCESS
                    _infoMessage.value = "${advertisement.title} deleted successfully."
                    _uiState.value = UiState.INFORMATION
                }
                is AdvertisementResult.Failure -> {
                    // Show failure information message
                    _infoMessage.value = advertisementResult.error
                    _uiState.value = UiState.INFORMATION
                }
            }
        }
    }


    fun checkOnlineStatus() {
        viewModelScope.launch {
            try {
                val onlineStatus = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }
                _isOnline.postValue(onlineStatus)
                Log.d("ViewModel", "Online status: $onlineStatus")
            } catch (e: Exception) {
                Log.e("ViewModel", "Error checking online status", e)
                _isOnline.postValue(false)
            }
        }
    }

    private fun showErrorPopup(message: String) {
        _uiState.value = UiState.ERROR
        _errorMessage.value = message
    }

    private fun showInfoPopup(message: String) {
        _uiState.value = UiState.INFORMATION
        _infoMessage.value = message
    }

    fun onInformationAcknowledged() {
        _infoMessage.value = null
        _uiState.value = UiState.SUCCESS
    }

    fun onNavigationHandled() {
        _navigateTo.value = null
    }

    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            _navigateTo.value = NavigationDestination.INITIAL
        }
    }

    fun cancelFetchJob() {
        fetchJob?.cancel()
        fetchJob = null
    }
}
