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
                val loyaltyCards = repositoryLoyaltyCard.getLoyaltyCardsByStoreIdAndUniandesMemberId(storeId, userId)
                val activeLoyaltyCard = loyaltyCards.firstOrNull { it.isCurrent == true }

                if (activeLoyaltyCard != null) {
                    val purchases = repositoryPurchase.getPurchasesByLoyaltyCardId(activeLoyaltyCard.id!!)
                    val redeemedLoyaltyCards = loyaltyCards.count { it.isCurrent == false }

                    val loyaltyCardInfo = LoyaltyCardInfo(
                        loyaltyCardsRedeemed = redeemedLoyaltyCards,
                        currentPoints = activeLoyaltyCard.points ?: 0,
                        maxPoints = activeLoyaltyCard.maxPoints ?: 20
                    )
                    _loyaltyCardInfo.value = loyaltyCardInfo

                    if (activeLoyaltyCard.points!! >= activeLoyaltyCard.maxPoints!!) {
                        _navigateToRedeemLoyalty.value = true
                    }
                } else {
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
                val loyaltyCard = repositoryLoyaltyCard.getLoyaltyCardByStoreIdAndUniandesMemberId(storeId, userId)

                if (loyaltyCard != null) {
                    val purchaseSuccessful = repositoryPurchase.addPurchase(
                        loyaltyCardId = loyaltyCard.id!!,
                        date = LocalDate.now().toString(),
                        isEligible = true,
                        rating = 5.0
                    )

                    if (purchaseSuccessful) {
                        loyaltyCard.points = loyaltyCard.points!! + 1
                        repositoryLoyaltyCard.updateLoyaltyCard(loyaltyCard)

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

data class LoyaltyCardInfo(
    val loyaltyCardsRedeemed: Int,
    val currentPoints: Int,
    val maxPoints: Int
)
