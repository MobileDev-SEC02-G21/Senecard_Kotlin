package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.R
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.CacheRepositoryLoyaltyCard
import com.mobiles.senecard.model.cache.CacheRepositoryPurchase
import com.mobiles.senecard.model.cache.CacheRepositoryStore
import com.mobiles.senecard.model.cache.CacheRepositoryUser
import com.mobiles.senecard.model.cache.LoyaltyCardResult
import com.mobiles.senecard.model.cache.StoreResult
import com.mobiles.senecard.model.cache.UserResult
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewModelBusinessOwnerQRSuccess : ViewModel() {

    private var fetchJob: Job? = null

    // Reference the singleton instance of RepositoryAuthentication
    private val repositoryAuthentication = RepositoryAuthentication.instance

    // Repositories needed to gather data
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

    private val _navigationDestination = MutableLiveData<NavigationDestination?>()
    val navigationDestination: LiveData<NavigationDestination?> = _navigationDestination

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> = _infoMessage

    private var currentUserId: String? = null // To store the current user ID
    private var currentStoreId: String? = null // To store the current store ID
    private var currentLoyaltyCardId: String? = null // To store the current loyalty card ID

    fun loadCustomerData(userId: String) {
        _uiState.value = UiState.LOADING // Show loading popup

        // Cancel any existing fetch operation
        fetchJob?.cancel()

        // Start a new fetch operation
        fetchJob = viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (isOnline) {
                handleOnlineCustomerFetch(userId)
            } else {
                // Show error popup since the activity requires an online connection
                _uiState.value = UiState.ERROR
                _errorMessage.value = "This activity requires an active internet connection."
            }
        }
    }

    private suspend fun handleOnlineCustomerFetch(userId: String) {
        try {
            // Step 1: Get the current logged-in user
            val authenticatedUser = repositoryAuthentication.getCurrentUser()
            if (authenticatedUser == null || authenticatedUser.email.isNullOrBlank()) {
                showErrorPopup("Unable to fetch authenticated user.")
                return
            }

            // Save the current user ID
            currentUserId = authenticatedUser.id

            // Step 2: Fetch the user details from the cache repository
            val userResult = cacheRepositoryUser.getUserById(userId)
            if (userResult is UserResult.Success) {
                _customerName.value = userResult.user.name ?: "Unknown"
            } else {
                showErrorPopup("Failed to fetch user details.")
                return
            }

            // Step 3: Fetch the store data for the current logged-in user
            val storeResult = cacheRepositoryStore.getStoreByBusinessOwnerId(authenticatedUser.id!!)
            if (storeResult is StoreResult.Success && storeResult.stores.isNotEmpty()) {
                val store = storeResult.stores.first()

                // Save the current store ID
                currentStoreId = store.id

                handleLoyaltyCardFetch(userId, store.id!!)
            } else {
                showErrorPopup("Failed to fetch store details.")
            }
        } catch (e: Exception) {
            Log.e("ViewModelBusinessOwnerQRSuccess", "Error during fetch: ${e.message}")
            showErrorPopup("An unexpected error occurred while loading customer data.")
        }
    }

    private suspend fun handleLoyaltyCardFetch(userId: String, storeId: String) {
        try {
            // Fetch loyalty cards for the user and store
            val loyaltyCardResult = cacheRepositoryLoyaltyCard.getLoyaltyCardsByUserAndStore(userId, storeId)

            if (loyaltyCardResult is LoyaltyCardResult.Success) {
                val loyaltyCards = loyaltyCardResult.loyaltyCards

                // Find the first "current" loyalty card
                val currentCard = loyaltyCards.find { it.isCurrent }

                if (currentCard != null) {
                    // Update LiveData with the current loyalty card's details
                    _currentStamps.value = currentCard.points
                    _maxStamps.value = currentCard.maxPoints

                    // Save the current loyalty card ID
                    currentLoyaltyCardId = currentCard.id
                } else {
                    // No current card exists; create a new one
                    createNewLoyaltyCard(userId, storeId)
                }

                // Update LiveData with the total count of non-current cards (redeemed loyalty cards)
                _loyaltyCards.value = loyaltyCards.count { !it.isCurrent }

                updateButtonStates()
                _uiState.value = UiState.SUCCESS // Mark fetch as successful
            } else {
                showErrorPopup("Failed to fetch loyalty card details.")
            }
        } catch (e: Exception) {
            Log.e("ViewModelBusinessOwnerQRSuccess", "Error fetching loyalty cards: ${e.message}")
            showErrorPopup("An unexpected error occurred while fetching loyalty card data.")
        }
    }

    private suspend fun createNewLoyaltyCard(userId: String, storeId: String) {
        try {
            val newLoyaltyCard = LoyaltyCard(
                storeId = storeId,
                uniandesMemberId = userId,
                maxPoints = 10, // Default maximum points
                points = 0, // Starting points
                isCurrent = true
            )
            val success = cacheRepositoryLoyaltyCard.addLoyaltyCard(newLoyaltyCard)

            if (success) {
                // Update LiveData with the newly created loyalty card's details
                _currentStamps.value = newLoyaltyCard.points
                _maxStamps.value = newLoyaltyCard.maxPoints
            } else {
                showErrorPopup("Failed to create a new loyalty card.")
            }
        } catch (e: Exception) {
            Log.e("ViewModelBusinessOwnerQRSuccess", "Error creating new loyalty card: ${e.message}")
            showErrorPopup("An unexpected error occurred while creating a loyalty card.")
        }
    }

    fun addStamp() {
        _uiState.value = UiState.LOADING // Show loading popup

        viewModelScope.launch {
            val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

            if (!isOnline) {
                // Show error if offline
                _uiState.value = UiState.ERROR
                _errorMessage.value = "Stamps can only be added while online."
                return@launch
            }

            try {
                // Ensure necessary data is available
                val loyaltyCardId = currentLoyaltyCardId
                if (loyaltyCardId == null) {
                    throw Exception("No active loyalty card available to update.")
                }

                val current = _currentStamps.value ?: 0
                val max = _maxStamps.value ?: 0

                if (current < max) {
                    // Add a purchase using the repository
                    val purchaseAdded = cacheRepositoryPurchase.addPurchase(
                        loyaltyCardId = loyaltyCardId,
                        date = getCurrentDate(),
                        isEligible = true,
                        rating = 0.0 // Default rating for stamp
                    )

                    if (purchaseAdded) {
                        // Increment the loyalty card points
                        val updatedPoints = current + 1
                        val loyaltyCardUpdated = cacheRepositoryLoyaltyCard.updateLoyaltyCardPoints(
                            loyaltyCardId = loyaltyCardId,
                            newPoints = updatedPoints
                        )

                        if (loyaltyCardUpdated) {
                            _currentStamps.value = updatedPoints // Update UI with new points
                            updateButtonStates()
                            _uiState.value = UiState.SUCCESS

                            // Show information popup confirming stamp creation
                            _infoMessage.value = "Stamp successfully added!"
                        } else {
                            throw Exception("Failed to update loyalty card points.")
                        }
                    } else {
                        throw Exception("Failed to add purchase record.")
                    }
                } else {
                    _infoMessage.value = "The maximum number of stamps has already been reached."
                    _uiState.value = UiState.SUCCESS
                }
            } catch (e: Exception) {
                Log.e("ViewModelBusinessOwnerQRSuccess", "Error adding stamp: ${e.message}")
                _errorMessage.value = e.message ?: "Failed to add stamp. Please try again."
                _uiState.value = UiState.ERROR
            }
        }
    }

    fun redeemLoyaltyCard() {
        if (_isRedeemable.value == true) {
            _uiState.value = UiState.LOADING // Show loading popup

            viewModelScope.launch {
                val isOnline = withContext(Dispatchers.IO) { NetworkUtils.isInternetAvailable() }

                if (!isOnline) {
                    // Show error if offline
                    _uiState.value = UiState.ERROR
                    _errorMessage.value = "Loyalty card redemption is only available while online."
                    return@launch
                }

                try {
                    // Ensure necessary data is available
                    val loyaltyCardId = currentLoyaltyCardId
                    val storeId = currentStoreId
                    val userId = currentUserId

                    if (loyaltyCardId == null || storeId == null || userId == null) {
                        throw Exception("Missing required information to redeem the loyalty card.")
                    }

                    // Step 1: Update the current loyalty card to set `isCurrent` to false
                    val loyaltyCardUpdated = cacheRepositoryLoyaltyCard.updateLoyaltyCardIsCurrent(
                        loyaltyCardId = loyaltyCardId,
                        isCurrent = false
                    )

                    if (loyaltyCardUpdated) {
                        // Step 2: Create a new loyalty card to set as the current one
                        val newLoyaltyCard = LoyaltyCard(
                            storeId = storeId,
                            uniandesMemberId = userId,
                            maxPoints = 10, // Default max points
                            points = 0, // Reset points
                            isCurrent = true
                        )
                        val newCardAdded = cacheRepositoryLoyaltyCard.addLoyaltyCard(newLoyaltyCard)

                        if (newCardAdded) {
                            // Update LiveData to reflect changes
                            _loyaltyCards.value = (_loyaltyCards.value ?: 0) + 1 // Increment redeemed cards
                            _currentStamps.value = 0 // Reset stamps
                            _maxStamps.value = newLoyaltyCard.maxPoints // Update max stamps for the new card
                            currentLoyaltyCardId = newLoyaltyCard.id // Update current loyalty card ID

                            updateButtonStates()
                            _uiState.value = UiState.SUCCESS

                            // Show information popup confirming redemption
                            _infoMessage.value = "Loyalty card successfully redeemed!"
                        } else {
                            throw Exception("Failed to create a new loyalty card.")
                        }
                    } else {
                        throw Exception("Failed to update the current loyalty card.")
                    }
                } catch (e: Exception) {
                    Log.e("ViewModelBusinessOwnerQRSuccess", "Error redeeming loyalty card: ${e.message}")
                    _errorMessage.value = e.message ?: "Failed to redeem loyalty card. Please try again."
                    _uiState.value = UiState.ERROR
                }
            }
        }
    }

    private fun updateButtonStates() {
        val stamps = _currentStamps.value ?: 0
        val max = _maxStamps.value ?: 0
        _isRedeemable.value = stamps >= max
    }

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    fun navigateTo(destination: NavigationDestination) {
        _navigationDestination.value = destination
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun onInformationAcknowledged() {
        _infoMessage.value = null
        _uiState.value = UiState.SUCCESS
    }

    private fun showInfoPopup(message: String) {
        _infoMessage.value = message
        _uiState.value = UiState.INFORMATION
    }

    private fun showErrorPopup(message: String) {
        _uiState.value = UiState.ERROR
        _errorMessage.value = message
    }

}
