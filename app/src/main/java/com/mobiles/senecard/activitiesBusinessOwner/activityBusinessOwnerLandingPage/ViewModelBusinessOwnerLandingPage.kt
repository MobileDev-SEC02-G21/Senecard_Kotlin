package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.entities.User
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.AdvertisementResult
import com.mobiles.senecard.model.cache.CacheRepositoryAdvertisement
import com.mobiles.senecard.model.cache.CacheRepositoryPurchase
import com.mobiles.senecard.model.cache.CacheRepositoryStore
import com.mobiles.senecard.model.cache.CacheRepositoryUser
import com.mobiles.senecard.model.cache.PurchaseResult
import com.mobiles.senecard.model.cache.StoreResult
import com.mobiles.senecard.model.cache.UserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class  ViewModelBusinessOwnerLandingPage : ViewModel() {

    private var fetchJob: Job? = null

    // Reference the singleton instance of RepositoryAuthentication
    private val repositoryAuthentication = RepositoryAuthentication.instance

    // Repositories needed to gather data
    private val cacheRepositoryUser = CacheRepositoryUser.instance
    private val cacheRepositoryStore = CacheRepositoryStore.instance
    private val cacheRepositoryAdvertisement = CacheRepositoryAdvertisement.instance
    private val cacheRepositoryPurchase = CacheRepositoryPurchase.instance

    // LiveData to hold the user information
    private val _isUser = MutableLiveData<User?>()
    val isUser: LiveData<User?> get() = _isUser

    // Observable variables from front
    val todayCustomers = MutableLiveData<Int>()
    val averageRating = MutableLiveData<Double>()
    val advertisementsCount = MutableLiveData<Int>()

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> get() = _infoMessage

    // LiveData for navigation destination
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    fun getInformation() {
        _uiState.value = UiState.LOADING // Show loading popup

        // Cancel any existing fetch operation
        fetchJob?.cancel()

        // Start a new fetch operation
        fetchJob = viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (isOnline) {
                handleOnlineCase()
            } else {
                handleOfflineCase()
            }
        }
    }

    // Handles the case when the app is online
    private suspend fun handleOnlineCase() {
        val authenticatedUser = repositoryAuthentication.getCurrentUser()

        if (authenticatedUser != null && !authenticatedUser.email.isNullOrBlank()) {
            val email = authenticatedUser.email
            handleUserFetch(email, isOnline = true)
        } else {
            showErrorPopup("Unable to fetch user data.")
        }
    }

    // Handles the case when the app is offline
    private suspend fun handleOfflineCase() {
        val authenticatedUser = repositoryAuthentication.getCurrentUser()

        if (authenticatedUser != null && !authenticatedUser.email.isNullOrBlank()) {
            val email = authenticatedUser.email
            handleUserFetch(email, isOnline = false)
        } else {
            showErrorPopup("Offline view unavailable.")
        }
    }

    // Handles user data fetch for both online and offline cases
    private suspend fun handleUserFetch(email: String, isOnline: Boolean) {
        val userResult = if (isOnline) {
            cacheRepositoryUser.getUserByEmail(email) // Simulate network-first strategy
        } else {
            cacheRepositoryUser.getUserByEmail(email)
        }

        when (userResult) {
            is UserResult.Success -> {
                _isUser.value = userResult.user
                val businessOwnerId = userResult.user.id
                handleStoreFetch(businessOwnerId!!, isOnline)
            }
            is UserResult.Failure -> {
                showErrorPopup("Failed to load user details.")
            }
        }
    }

    // Handles store data fetch for both online and offline cases
    private suspend fun handleStoreFetch(businessOwnerId: String, isOnline: Boolean) {
        val storeResult = if (isOnline) {
            cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)
        } else {
            cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)
        }

        when (storeResult) {
            is StoreResult.Success -> {
                val store = storeResult.stores.first()
                averageRating.value = store.rating ?: 0.0
                handleAdvertisementsFetch(store.id!!, isOnline)
            }
            is StoreResult.Failure -> {
                showErrorPopup("Failed to fetch store data.")
            }
        }
    }

    // Handles advertisements data fetch for both online and offline cases
    private suspend fun handleAdvertisementsFetch(storeId: String, isOnline: Boolean) {
        val adResult = if (isOnline) {
            cacheRepositoryAdvertisement.getAdvertisementsByStoreId(storeId)
        } else {
            cacheRepositoryAdvertisement.getAdvertisementsByStoreId(storeId)
        }

        when (adResult) {
            is AdvertisementResult.Success -> {
                advertisementsCount.value = adResult.advertisements.size
                handlePurchasesFetch(storeId, isOnline)
            }
            is AdvertisementResult.Failure -> {
                showErrorPopup("Failed to fetch advertisements.")
            }
        }
    }

    // Handles purchases data fetch for both online and offline cases
    private suspend fun handlePurchasesFetch(storeId: String, isOnline: Boolean) {
        val purchaseResult = if (isOnline) {
            cacheRepositoryPurchase.getPurchasesByStoreId(storeId)
        } else {
            cacheRepositoryPurchase.getPurchasesByStoreId(storeId)
        }

        when (purchaseResult) {
            is PurchaseResult.Success -> {
                val today = LocalDate.now().toString()
                val todayCount = purchaseResult.purchases.count { it.date == today }
                todayCustomers.value = todayCount
                showInfoPopup("You are viewing ${if (isOnline) "an online" else "an offline"} version.")
            }
            is PurchaseResult.Failure -> {
                showErrorPopup("Failed to fetch purchases.")
            }
        }
    }

    private fun showErrorPopup(message: String) {
        _uiState.value = UiState.ERROR
        _errorMessage.value = message
    }

    private fun showInfoPopup(message: String) {
        _infoMessage.value = message
        _uiState.value = UiState.INFORMATION
    }

    // Dont allow the get Information to continue execution
    fun cancelFetchJob() {
        fetchJob?.cancel()
        fetchJob = null
    }

    // Navigation methods
    fun onAdvertisementsClicked() {
        _navigateTo.value = NavigationDestination.ADVERTISEMENTS
    }

    fun onQrScannerClicked() {
        viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (isOnline) {
                _navigateTo.value = NavigationDestination.QR_SCANNER
            } else {
                showInfoPopup("This functionality requires an internet connection.")
            }
        }
    }

    // Function to reset the navigation event after it's handled
    fun onNavigationHandled() {
        _navigateTo.value = null
    }

    // Log out logic
    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            _navigateTo.value = NavigationDestination.INITIAL
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun onInformationAcknowledged() {
        _infoMessage.value = null
        _uiState.value = UiState.SUCCESS
    }
}
