package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.entities.User
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.AdvertisementResult
import com.mobiles.senecard.model.cache.CacheRepositoryAdvertisement
import com.mobiles.senecard.model.cache.CacheRepositoryLoyaltyCard
import com.mobiles.senecard.model.cache.CacheRepositoryPurchase
import com.mobiles.senecard.model.cache.CacheRepositoryStore
import com.mobiles.senecard.model.cache.CacheRepositoryUser
import com.mobiles.senecard.model.cache.LoyaltyCardResult
import com.mobiles.senecard.model.cache.PurchaseResult
import com.mobiles.senecard.model.cache.StoreResult
import com.mobiles.senecard.model.cache.UserResult
import com.mobiles.senecard.model.entities.Purchase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class  ViewModelBusinessOwnerLandingPage : ViewModel() {

    private var fetchJob: Job? = null

    // Reference the singleton instance of RepositoryAuthentication
    private val repositoryAuthentication = RepositoryAuthentication.instance

    // Repositories needed to gather data
    private val cacheRepositoryUser = CacheRepositoryUser.instance
    private val cacheRepositoryStore = CacheRepositoryStore.instance
    private val cacheRepositoryAdvertisement = CacheRepositoryAdvertisement.instance
    private val cacheRepositoryPurchase = CacheRepositoryPurchase.instance
    private val cacheRepositoryLoyaltyCard = CacheRepositoryLoyaltyCard.instance

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
        Log.d("ViewModel", "getInformation called")
        _uiState.value = UiState.LOADING // Show loading popup

        // Cancel any existing fetch operation
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

    // Handles the case when the app is online
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

    // Handles the case when the app is offline
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

    // Handles user data fetch for both online and offline cases
    private suspend fun handleUserFetch(email: String, isOnline: Boolean) {
        Log.d("ViewModel", "handleUserFetch called: email=$email, isOnline=$isOnline")
        val userResult = cacheRepositoryUser.getUserByEmail(email)

        when (userResult) {
            is UserResult.Success -> {
                Log.d("ViewModel", "User fetched successfully: ${userResult.user}")
                _isUser.value = userResult.user
                val businessOwnerId = userResult.user.id
                handleStoreFetch(businessOwnerId!!, isOnline)
            }
            is UserResult.Failure -> {
                Log.e("ViewModel", "Failed to fetch user details: ${userResult.error}")
                showErrorPopup("Failed to load user details.")
            }
        }
    }

    // Handles store data fetch for both online and offline cases
    private suspend fun handleStoreFetch(businessOwnerId: String, isOnline: Boolean) {
        Log.d("ViewModel", "handleStoreFetch called: businessOwnerId=$businessOwnerId, isOnline=$isOnline")
        val storeResult = cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)

        when (storeResult) {
            is StoreResult.Success -> {
                Log.d("ViewModel", "Store fetched successfully: ${storeResult.stores.first()}")
                val store = storeResult.stores.first()
                averageRating.value = store.rating ?: 0.0
                handleAdvertisementsFetch(store.id!!, isOnline)
            }
            is StoreResult.Failure -> {
                Log.e("ViewModel", "Failed to fetch store data")
                showErrorPopup("Failed to fetch store data.")
            }
        }
    }

    // Handles advertisements data fetch for both online and offline cases
    private suspend fun handleAdvertisementsFetch(storeId: String, isOnline: Boolean) {
        Log.d("ViewModel", "handleAdvertisementsFetch called: storeId=$storeId, isOnline=$isOnline")
        val adResult = cacheRepositoryAdvertisement.getAdvertisementsByStoreId(storeId)

        when (adResult) {
            is AdvertisementResult.Success -> {
                Log.d("ViewModel", "Advertisements fetched successfully: count=${adResult.advertisements.size}")
                advertisementsCount.value = adResult.advertisements.size
                handlePurchasesFetch(storeId, isOnline)
            }
            is AdvertisementResult.Failure -> {
                Log.e("ViewModel", "Failed to fetch advertisements")
                showErrorPopup("Failed to fetch advertisements.")
            }
        }
    }

    private suspend fun handlePurchasesFetch(storeId: String, isOnline: Boolean) {
        Log.d("ViewModel", "handlePurchasesFetch called: storeId=$storeId, isOnline=$isOnline")
        try {
            // Step 1: Get all loyalty cards for the store
            val loyaltyCardResult = cacheRepositoryLoyaltyCard.getLoyaltyCardsByStore(storeId)

            if (loyaltyCardResult is LoyaltyCardResult.Success) {
                val loyaltyCards = loyaltyCardResult.loyaltyCards
                Log.d("ViewModel", "Loyalty cards fetched successfully: count=${loyaltyCards.size}")

                if (loyaltyCards.isEmpty()) {
                    Log.d("ViewModel", "No loyalty cards found for the store.")
                    todayCustomers.value = 0
                    return
                }

                // Step 2: Fetch purchases for each loyalty card
                val allPurchases = mutableListOf<Purchase>()
                for (loyaltyCard in loyaltyCards) {
                    val purchaseResult = cacheRepositoryPurchase.getPurchasesByLoyaltyCardId(loyaltyCard.id!!)
                    if (purchaseResult is PurchaseResult.Success) {
                        Log.d("ViewModel", "Purchases fetched for loyaltyCardId=${loyaltyCard.id}")
                        allPurchases.addAll(purchaseResult.purchases)
                    }
                }

                // Step 3: Filter purchases for today's date
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val todayPurchases = allPurchases.filter { it.date == today }
                Log.d("ViewModel", "Today's purchases count: ${todayPurchases.size}")

                // Update the today customers count
                todayCustomers.value = todayPurchases.size

                // Show information popup
                showInfoPopup("You are viewing ${if (isOnline) "an online" else "an offline"} version.")
            } else {
                Log.e("ViewModel", "Failed to fetch loyalty cards for the store")
                // Update the today customers count
                todayCustomers.value = 0

                // Show information popup
                showInfoPopup("You are viewing ${if (isOnline) "an online" else "an offline"} version.")
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error while fetching purchases: ${e.message}", e)
            showErrorPopup("Error: An unexpected error occurred while fetching purchases.")
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
