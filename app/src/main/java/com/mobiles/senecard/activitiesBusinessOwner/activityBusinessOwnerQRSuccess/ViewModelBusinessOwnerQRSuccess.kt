package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.*
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelBusinessOwnerQRSuccess : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val cacheRepositoryUser = CacheRepositoryUser.instance
    private val cacheRepositoryStore = CacheRepositoryStore.instance
    private val cacheRepositoryPurchase = CacheRepositoryPurchase.instance
    private val cacheRepositoryLoyaltyCard = CacheRepositoryLoyaltyCard.instance

    private val _customerName = MutableLiveData<String>()
    val customerName: LiveData<String> = _customerName

    private val _loyaltyCards = MutableLiveData<Int>()
    val loyaltyCards: LiveData<Int> = _loyaltyCards

    private val _currentStamps = MutableLiveData<Int>()
    val currentStamps: LiveData<Int> = _currentStamps

    private val _maxStamps = MutableLiveData<Int>()
    val maxStamps: LiveData<Int> = _maxStamps

    private val _isRedeemable = MutableLiveData<Boolean>()
    val isRedeemable: LiveData<Boolean> = _isRedeemable

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    // LiveData for navigation destination
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> = _infoMessage

    private var currentUserId: String? = null
    private var currentStoreId: String? = null
    private var currentLoyaltyCardId: String? = null

    fun loadCustomerData(userId: String) {
        _uiState.value = UiState.LOADING

        viewModelScope.launch {
            try {
                val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

                // Check if the application is offline
                if (!isOnline) {
                    showErrorPopup("This functionality requires an internet connection.")
                    _uiState.value = UiState.ERROR
                    return@launch
                }

                // Proceed to fetch authenticated user only if online
                fetchAuthenticatedUser(userId)
            } catch (e: Exception) {
                showErrorPopup("An unexpected error occurred: ${e.message}")
                _uiState.value = UiState.ERROR
            }
        }
    }


    private suspend fun fetchAuthenticatedUser(userId: String) {
        val authenticatedUser = repositoryAuthentication.getCurrentUser()
        if (authenticatedUser == null || authenticatedUser.email.isNullOrBlank()) {
            showErrorPopup("Failed to authenticate the user.")
            return
        }
        currentUserId = authenticatedUser.id

        fetchUserDetails(userId, authenticatedUser.id!!)
    }

    private suspend fun fetchUserDetails(userId: String, businessOwnerId: String) {
        val userResult = cacheRepositoryUser.getUserById(userId)

        if (userResult is UserResult.Success) {
            if (userResult.isFromCache) {
                showErrorPopup("This functionality is not available without an internet connection.")
                return
            }

            _customerName.value = userResult.user.name ?: "Unknown"
            fetchStoreDetails(businessOwnerId, userId)
        } else {
            showErrorPopup("Failed to load customer details.")
        }
    }


    private suspend fun fetchStoreDetails(businessOwnerId: String, userId: String) {
        val storeResult = cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)

        if (storeResult is StoreResult.Success && storeResult.stores.isNotEmpty()) {
            if (storeResult.isFromCache) {
                showErrorPopup("This functionality is not available without an internet connection.")
                return
            }

            val store = storeResult.stores.first()
            currentStoreId = store.id
            fetchLoyaltyCards(userId, store.id!!)
        } else {
            showErrorPopup("Failed to fetch store details.")
        }
    }


    private suspend fun fetchLoyaltyCards(userId: String, storeId: String) {
        Log.d("ViewModel", "fetchLoyaltyCards called: userId=$userId, storeId=$storeId")
        val loyaltyCardResult = cacheRepositoryLoyaltyCard.getLoyaltyCardsByUserAndStore(userId, storeId)

        if (loyaltyCardResult is LoyaltyCardResult.Success) {
            if (loyaltyCardResult.isFromCache) {
                Log.e("ViewModel", "Data fetched from cache, which is not allowed for this functionality.")
                showErrorPopup("This functionality is not available without an internet connection.")
                return
            }

            val loyaltyCards = loyaltyCardResult.loyaltyCards
            Log.d("ViewModel", "Loyalty cards fetched: count=${loyaltyCards.size}")

            // Check for the current loyalty card
            val currentCard = loyaltyCards.find { it.isCurrent }

            if (currentCard != null) {
                Log.d("ViewModel", "Current loyalty card found: id=${currentCard.id}")
                currentLoyaltyCardId = currentCard.id
                _currentStamps.value = currentCard.points
                _maxStamps.value = currentCard.maxPoints
            } else {
                Log.d("ViewModel", "No current loyalty card found. Creating a new one.")
                createNewLoyaltyCard(userId, storeId)
            }

            _loyaltyCards.value = loyaltyCards.count { !it.isCurrent }
            updateButtonStates()
            _uiState.value = UiState.SUCCESS
        } else {
            // Handle non-success case: create a new loyalty card
            Log.w("ViewModel", "No loyalty cards found or failed to fetch. Creating a new loyalty card.")
            createNewLoyaltyCard(userId, storeId)
            _uiState.value = UiState.SUCCESS
        }
    }



    private suspend fun createNewLoyaltyCard(userId: String, storeId: String) {
        val newLoyaltyCard = LoyaltyCard(
            storeId = storeId,
            uniandesMemberId = userId,
            maxPoints = 10,
            points = 0,
            isCurrent = true
        )
        if (cacheRepositoryLoyaltyCard.addLoyaltyCard(newLoyaltyCard)) {
            currentLoyaltyCardId = newLoyaltyCard.id
            _currentStamps.value = newLoyaltyCard.points
            _maxStamps.value = newLoyaltyCard.maxPoints
        } else {
            showErrorPopup("Failed to create a new loyalty card.")
        }
    }

    fun addStamp() {
        if (_uiState.value == UiState.LOADING) return
        _uiState.value = UiState.LOADING

        viewModelScope.launch {
            try {
                val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }
                if (!isOnline) {
                    showErrorPopup("Stamps can only be added while online.")
                    return@launch
                }

                val loyaltyCardId = currentLoyaltyCardId ?: throw Exception("No active loyalty card found.")
                val current = _currentStamps.value ?: 0
                val max = _maxStamps.value ?: 0

                if (current >= max) {
                    showInfoPopup("Maximum stamps reached.")
                    return@launch
                }

                if (cacheRepositoryPurchase.addPurchase(loyaltyCardId, getCurrentDate(), true, 0.0)) {
                    val updatedPoints = current + 1
                    if (cacheRepositoryLoyaltyCard.updateLoyaltyCardPoints(loyaltyCardId, updatedPoints)) {
                        _currentStamps.value = updatedPoints
                        updateButtonStates()
                        showInfoPopup("Stamp added successfully!")
                    } else {
                        throw Exception("Failed to update loyalty card points.")
                    }
                } else {
                    throw Exception("Failed to record purchase.")
                }
            } catch (e: Exception) {
                showErrorPopup("Error adding stamp: ${e.message}")
            }
        }
    }

    fun redeemLoyaltyCard() {
        if (_isRedeemable.value == false || _uiState.value == UiState.LOADING) return
        _uiState.value = UiState.LOADING

        viewModelScope.launch {
            try {
                val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }
                if (!isOnline) {
                    showErrorPopup("Redemption requires an active internet connection.")
                    return@launch
                }

                val loyaltyCardId = currentLoyaltyCardId ?: throw Exception("No active loyalty card found.")
                val storeId = currentStoreId ?: throw Exception("Store details are missing.")
                val userId = currentUserId ?: throw Exception("User details are missing.")

                if (cacheRepositoryLoyaltyCard.updateLoyaltyCardIsCurrent(loyaltyCardId, false)) {
                    createNewLoyaltyCard(userId, storeId)
                    _loyaltyCards.value = (_loyaltyCards.value ?: 0) + 1
                    showInfoPopup("Loyalty card redeemed successfully!")
                } else {
                    throw Exception("Failed to mark loyalty card as redeemed.")
                }
            } catch (e: Exception) {
                showErrorPopup("Error redeeming loyalty card: ${e.message}")
            }
        }
    }

    private fun updateButtonStates() {
        _isRedeemable.value = (_currentStamps.value ?: 0) >= (_maxStamps.value ?: 0)
    }

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    private fun showInfoPopup(message: String) {
        _infoMessage.value = message
        _uiState.value = UiState.INFORMATION
    }

    private fun showErrorPopup(message: String) {
        _errorMessage.value = message
        _uiState.value = UiState.ERROR
    }

    fun onInformationAcknowledged() {
        _infoMessage.value = null
        _uiState.value = UiState.SUCCESS
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
}
