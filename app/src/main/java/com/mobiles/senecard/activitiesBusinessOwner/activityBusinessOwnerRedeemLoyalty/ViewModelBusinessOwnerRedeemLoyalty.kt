package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerRedeemLoyalty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryLoyaltyCard
import com.mobiles.senecard.model.RepositoryUser
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerRedeemLoyalty : ViewModel() {

    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance
    private val repositoryUser = RepositoryUser.instance

    private val _redeemSuccess = MutableLiveData<Boolean>()
    val redeemSuccess: LiveData<Boolean> get() = _redeemSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loyaltyCardInfo = MutableLiveData<LoyaltyCardInfo>()
    val loyaltyCardInfo: LiveData<LoyaltyCardInfo> get() = _loyaltyCardInfo

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    // Function to retrieve the user name and loyalty card details
    fun getUserAndLoyaltyCardDetails(storeId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Fetch user details
                val user = repositoryUser.getUserById(userId)
                _userName.value = user?.name ?: "Unknown User"

                // Fetch all loyalty cards for this user and store
                val loyaltyCards = repositoryLoyaltyCard.getLoyaltyCardsByStoreIdAndUniandesMemberId(storeId, userId)

                // Find the current active loyalty card
                val activeLoyaltyCard = loyaltyCards.firstOrNull { it.isCurrent }
                val redeemedLoyaltyCards = loyaltyCards.count { !it.isCurrent && it.points >= it.maxPoints }

                _loyaltyCardInfo.value = if (activeLoyaltyCard != null) {
                    LoyaltyCardInfo(
                        loyaltyCardsRedeemed = redeemedLoyaltyCards,
                        currentPoints = activeLoyaltyCard.points,
                        maxPoints = activeLoyaltyCard.maxPoints
                    )
                } else {
                    LoyaltyCardInfo(
                        loyaltyCardsRedeemed = redeemedLoyaltyCards,
                        currentPoints = 0,
                        maxPoints = 20
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            }
        }
    }

    // Function to redeem the current loyalty card, deactivate it, and create a new one
    fun redeemLoyaltyCard(storeId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Retrieve all loyalty cards for this user and store
                val loyaltyCards = repositoryLoyaltyCard.getLoyaltyCardsByStoreIdAndUniandesMemberId(storeId, userId)

                // Find the current active loyalty card
                val activeLoyaltyCard = loyaltyCards.firstOrNull { it.isCurrent }

                if (activeLoyaltyCard != null && activeLoyaltyCard.points >= activeLoyaltyCard.maxPoints) {
                    // Deactivate the current loyalty card
                    activeLoyaltyCard.isCurrent = false
                    val deactivateSuccess = repositoryLoyaltyCard.updateLoyaltyCard(activeLoyaltyCard)

                    if (deactivateSuccess) {
                        // Create a new loyalty card
                        val newLoyaltyCard = LoyaltyCard(
                            storeId = storeId,
                            uniandesMemberId = userId,
                            maxPoints = 20,  // Set a default value for max points
                            points = 0,      // Start with 0 points
                            isCurrent = true
                        )

                        val createSuccess = repositoryLoyaltyCard.addLoyaltyCard(newLoyaltyCard)

                        if (createSuccess) {
                            _redeemSuccess.value = true // Successfully redeemed and created a new card
                        } else {
                            _errorMessage.value = "Failed to create a new loyalty card."
                        }
                    } else {
                        _errorMessage.value = "Failed to deactivate the current loyalty card."
                    }
                } else {
                    _errorMessage.value = "The current loyalty card does not have enough points."
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            }
        }
    }
}

// Data class to represent loyalty card information in the UI
data class LoyaltyCardInfo(
    val loyaltyCardsRedeemed: Int,
    val currentPoints: Int,
    val maxPoints: Int
)
