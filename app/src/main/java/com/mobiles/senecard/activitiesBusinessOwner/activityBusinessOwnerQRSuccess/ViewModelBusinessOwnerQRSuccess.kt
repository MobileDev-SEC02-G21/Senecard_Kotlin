package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRSuccess

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryLoyaltyCard
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.launch
import java.time.LocalDate

class ViewModelBusinessOwnerQRSuccess : ViewModel() {

    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance
    private val repositoryPurchase = RepositoryPurchase.instance

    private val _loyaltyCardInfo = MutableLiveData<LoyaltyCardInfo>()
    val loyaltyCardInfo: LiveData<LoyaltyCardInfo> get() = _loyaltyCardInfo

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _navigateToRedeemLoyalty = MutableLiveData<Boolean>()
    val navigateToRedeemLoyalty: LiveData<Boolean> get() = _navigateToRedeemLoyalty

    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> get() = _purchaseSuccess

    fun getLoyaltyCardAndPurchases(storeId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Retrieve the active loyalty card (current) for the given store and user
                val activeLoyaltyCard = repositoryLoyaltyCard.getCurrentLoyaltyCardByStoreIdAndUniandesMemberId(storeId, userId)

                if (activeLoyaltyCard != null) {
                    // Get the purchases associated with the active loyalty card
                    val purchases = repositoryPurchase.getPurchasesByLoyaltyCardId(activeLoyaltyCard.id!!)

                    // Calculate redeemed loyalty cards count
                    val allLoyaltyCards = repositoryLoyaltyCard.getLoyaltyCardsByStoreIdAndUniandesMemberId(storeId, userId)
                    val redeemedLoyaltyCards = allLoyaltyCards.count { it.isCurrent == false }

                    // Update the UI with loyalty card info
                    _loyaltyCardInfo.value = LoyaltyCardInfo(
                        loyaltyCardsRedeemed = redeemedLoyaltyCards,
                        currentPoints = activeLoyaltyCard.points ?: 0,
                        maxPoints = activeLoyaltyCard.maxPoints ?: 20
                    )

                    // Check if the loyalty card points are enough to redeem, navigate to redeem loyalty
                    if (activeLoyaltyCard.points!! >= activeLoyaltyCard.maxPoints!!) {
                        _navigateToRedeemLoyalty.value = true
                    }

                } else {
                    // No active loyalty card found, create a new one
                    val newLoyaltyCard = LoyaltyCard(
                        storeId = storeId,
                        uniandesMemberId = userId,
                        maxPoints = 20,
                        points = 0,
                        isCurrent = true
                    )
                    val success = repositoryLoyaltyCard.addLoyaltyCard(newLoyaltyCard)

                    if (success) {
                        _errorMessage.value = "Created new loyalty card. Please try again."
                        // Optionally, reload the newly created loyalty card
                        getLoyaltyCardAndPurchases(storeId, userId)
                    } else {
                        _errorMessage.value = "Failed to create a new loyalty card."
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            }
        }
    }

    fun makeStamp(storeId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Retrieve the current active loyalty card
                val loyaltyCard = repositoryLoyaltyCard.getCurrentLoyaltyCardByStoreIdAndUniandesMemberId(storeId, userId)

                if (loyaltyCard != null) {
                    // Create a new purchase for the current loyalty card
                    val purchaseSuccessful = repositoryPurchase.addPurchase(
                        loyaltyCardId = loyaltyCard.id!!,
                        date = LocalDate.now().toString(),
                        isEligible = true,
                        rating = 5.0
                    )

                    if (purchaseSuccessful) {
                        // Update loyalty card points
                        loyaltyCard.points = loyaltyCard.points!! + 1
                        repositoryLoyaltyCard.updateLoyaltyCard(loyaltyCard)

                        // Check if the loyalty card has reached max points
                        if (loyaltyCard.points!! >= loyaltyCard.maxPoints!!) {
                            _navigateToRedeemLoyalty.value = true
                        }
                        _purchaseSuccess.value = true
                    } else {
                        _errorMessage.value = "Failed to register purchase"
                    }
                } else {
                    _errorMessage.value = "No active loyalty card found"
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
