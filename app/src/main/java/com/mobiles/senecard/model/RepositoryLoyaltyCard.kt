import com.google.firebase.firestore.ktx.toObject
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryLoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryLoyaltyCard by lazy { RepositoryLoyaltyCard() }
    }

    // Add loyalty card to Firestore
    suspend fun addLoyaltyCard(loyaltyCard: LoyaltyCard): Boolean {
        return try {
            firebase.firestore.collection("loyaltyCards").add(loyaltyCard).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get loyalty card by storeId and uniandesMemberId
    suspend fun getLoyaltyCardByStoreIdAndUniandesMemberId(storeId: String, uniandesMemberId: String): LoyaltyCard? {
        return try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].toObject<LoyaltyCard>()?.copy(id = querySnapshot.documents[0].id)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

