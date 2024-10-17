package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryLoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryLoyaltyCard by lazy { RepositoryLoyaltyCard() }
    }

    suspend fun addLoyaltyCard(): Boolean {
        return true
    }

    suspend fun getLoyaltyCardByStoreIdAndUniandesMemberId(storeId: String, uniandesMemberId: String): LoyaltyCard? {
        return try {
            val querySnapshot = firebase.firestore.collection("royaltyCards")
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

    suspend fun getLoyaltyCardsByUniandesMemberId(uniandesMemberId: String): List<LoyaltyCard> {
        val loyaltyCardsList = mutableListOf<LoyaltyCard>()
        try {
            val querySnapshot = firebase.firestore.collection("royaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            for (documentSnapshot in querySnapshot.documents) {
                val loyaltyCard = documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
                if (loyaltyCard != null) {
                    loyaltyCardsList.add(loyaltyCard)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return loyaltyCardsList
    }
}
