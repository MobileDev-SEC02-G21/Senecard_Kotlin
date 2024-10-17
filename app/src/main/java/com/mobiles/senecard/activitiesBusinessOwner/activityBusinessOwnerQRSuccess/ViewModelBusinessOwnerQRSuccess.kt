import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryPurchase
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.launch
import java.time.LocalDate

class ViewModelBusinessOwnerQRSuccess : ViewModel() {

    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance
    private val repositoryPurchase = RepositoryPurchase.instance

    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> get() = _purchaseSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Function to handle the creation of a purchase and updating the loyalty card
    fun makeStamp(businessOwnerId: String, storeId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Retrieve the loyalty card for this user and store
                var loyaltyCard = repositoryLoyaltyCard.getLoyaltyCardByStoreIdAndUniandesMemberId(storeId, userId)

                // If the loyalty card does not exist, create a new one
                if (loyaltyCard == null) {
                    loyaltyCard = createNewLoyaltyCard(storeId, userId)
                    if (loyaltyCard == null) {
                        _errorMessage.value = "Failed to create a new loyalty card."
                        return@launch
                    }
                }

                // Create a new purchase by passing individual parameters
                val purchaseSuccessful = repositoryPurchase.addPurchase(
                    loyaltyCardId = loyaltyCard.id!!,
                    date = LocalDate.now().toString(),
                    isEligible = true,
                    rating = 5.0 // Placeholder rating
                )

                if (purchaseSuccessful) {
                    _purchaseSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to register purchase"
                }

            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
            }
        }
    }

    // Function to create a new loyalty card if one does not exist
    private suspend fun createNewLoyaltyCard(storeId: String, uniandesMemberId: String): LoyaltyCard? {
        return try {
            val newLoyaltyCard = LoyaltyCard(
                storeId = storeId,
                uniandesMemberId = uniandesMemberId,
                maxPoints = 20, // Set a default value for max points
                points = 0,     // Start with 0 points
                isCurrent = true
            )
            val isSuccess = repositoryLoyaltyCard.addLoyaltyCard(newLoyaltyCard)
            if (isSuccess) {
                repositoryLoyaltyCard.getLoyaltyCardByStoreIdAndUniandesMemberId(storeId, uniandesMemberId)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
