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
    private var currentClientId: String? = null

    fun loadCustomerData(userId: String) {
        _uiState.value = UiState.LOADING
        currentClientId = userId

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
            fetchStoreDetails(businessOwnerId)
        } else {
            showErrorPopup("Failed to load customer details.")
        }
    }


    private suspend fun fetchStoreDetails(businessOwnerId: String) {
        val storeResult = cacheRepositoryStore.getStoreByBusinessOwnerId(businessOwnerId)

        if (storeResult is StoreResult.Success && storeResult.stores.isNotEmpty()) {
            if (storeResult.isFromCache) {
                showErrorPopup("This functionality is not available without an internet connection.")
                return
            }

            val store = storeResult.stores.first()
            currentStoreId = store.id
            fetchLoyaltyCards(currentClientId!!, store.id!!)
        } else {
            showErrorPopup("Failed to fetch store details.")
        }
    }

    private suspend fun fetchLoyaltyCards(userId: String, storeId: String) {
        Log.d("ViewModel", "fetchLoyaltyCards called: userId=$userId, storeId=$storeId")

        // Fetch the current loyalty card
        val loyaltyCardResult = cacheRepositoryLoyaltyCard.getCurrentLoyaltyCard(userId, storeId)

        when (loyaltyCardResult) {
            is LoyaltyCardResult.Success -> {
                if (loyaltyCardResult.isFromCache) {
                    Log.e("ViewModel", "Data fetched from cache, which is not allowed for this functionality.")
                    showErrorPopup("This functionality is not available without an internet connection.")
                    return
                }

                val currentCard = loyaltyCardResult.loyaltyCards.firstOrNull()

                if (currentCard != null) {
                    Log.d("ViewModel", "Current loyalty card found: id=${currentCard.id}")
                    currentLoyaltyCardId = currentCard.id
                    _currentStamps.value = currentCard.points
                    _maxStamps.value = currentCard.maxPoints

                    // Fetch all loyalty cards for counting redeemed ones
                    val allCardsResult = cacheRepositoryLoyaltyCard.getLoyaltyCardsByUserAndStore(userId, storeId)
                    when (allCardsResult) {
                        is LoyaltyCardResult.Success -> {
                            if (allCardsResult.isFromCache) {
                                Log.w("ViewModel", "Loyalty cards fetched from cache, which is not allowed.")
                                showErrorPopup("This functionality requires an active internet connection.")
                                return
                            }

                            val allCards = allCardsResult.loyaltyCards
                            _loyaltyCards.value = allCards.size - 1 // Subtract 1 for the active card
                            Log.d("ViewModel", "Total loyalty cards redeemed: ${allCards.size}")
                        }

                        is LoyaltyCardResult.Failure -> {
                            _loyaltyCards.value = 0
                            Log.w("ViewModel", "Failed to fetch all loyalty cards for counting redeemed ones: ${allCardsResult.error}")
                        }
                    }


                    updateButtonStates()
                    _uiState.value = UiState.SUCCESS
                } else {
                    Log.w("ViewModel", "No current loyalty card found. Creating a new one.")
                    createNewLoyaltyCard(userId, storeId)
                    _uiState.value = UiState.SUCCESS
                }
            }

            is LoyaltyCardResult.Failure -> {
                Log.w("ViewModel", "Failed to fetch current loyalty card: ${loyaltyCardResult.error}")

                // Differentiating between no data and failure
                if (loyaltyCardResult.error.contains("No active loyalty card found")) {
                    Log.d("ViewModel", "No loyalty card found in the database or cache. Creating a new one.")
                } else {
                    Log.d("ViewModel", "Unexpected failure while fetching loyalty card: ${loyaltyCardResult.error}")
                }

                createNewLoyaltyCard(userId, storeId)

                // Fetch all loyalty cards for counting redeemed ones
                val allCardsResult = cacheRepositoryLoyaltyCard.getLoyaltyCardsByUserAndStore(userId, storeId)
                when (allCardsResult) {
                    is LoyaltyCardResult.Success -> {
                        if (allCardsResult.isFromCache) {
                            Log.w("ViewModel", "Loyalty cards fetched from cache, which is not allowed.")
                            showErrorPopup("This functionality requires an active internet connection.")
                            return
                        }

                        val allCards = allCardsResult.loyaltyCards
                        _loyaltyCards.value = allCards.size - 1 // Subtract 1 for the active card
                        Log.d("ViewModel", "Total loyalty cards redeemed: ${allCards.size}")
                    }

                    is LoyaltyCardResult.Failure -> {
                        _loyaltyCards.value = 0
                        Log.w("ViewModel", "Failed to fetch all loyalty cards for counting redeemed ones: ${allCardsResult.error}")
                    }
                }

                _uiState.value = UiState.SUCCESS
            }
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
                    showInfoPopup("Stamps can only be added while Online")
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
                    showInfoPopup("Redemption requires an active internet connection.")
                    _uiState.value = UiState.SUCCESS // Reset UI state
                    return@launch
                }

                val loyaltyCardId = currentLoyaltyCardId ?: throw Exception("No active loyalty card found.")
                val storeId = currentStoreId ?: throw Exception("Store details are missing.")
                val userId = currentUserId ?: throw Exception("User details are missing.")

                // Mark the current loyalty card as redeemed
                if (cacheRepositoryLoyaltyCard.updateLoyaltyCardIsCurrent(loyaltyCardId, false)) {
                    // Update button states and notify the user
                    fetchLoyaltyCards(currentClientId!!, storeId) // Refresh data for UI consistency
                    updateButtonStates()
                    showInfoPopup("Loyalty card redeemed successfully!")
                } else {
                    throw Exception("Failed to mark loyalty card as redeemed.")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error redeeming loyalty card: ${e.message}", e)
                showErrorPopup("Error redeeming loyalty card: ${e.message}")
            } finally {
                _uiState.value = UiState.SUCCESS // Ensure UI state is reset
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
