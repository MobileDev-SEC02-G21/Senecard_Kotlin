package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryLoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryLoyaltyCard by lazy { RepositoryLoyaltyCard() }
    }

    suspend fun addLoyaltyCard(loyaltyCardObject: LoyaltyCard): Boolean {
        try {
            val loyaltyCard = hashMapOf(
                "storeId" to loyaltyCardObject.storeId,
                "uniandesMemberId" to loyaltyCardObject.uniandesMemberId,
                "maxPoints" to loyaltyCardObject.maxPoints,
                "points" to loyaltyCardObject.points,
                "isCurrent" to true
            )

            firebase.firestore.collection("loyaltyCards").add(loyaltyCard).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getLoyaltyCardsByStoreId(storeId: String): List<LoyaltyCard> {
        val loyaltyCardList = mutableListOf<LoyaltyCard>()
        try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .get()
                .await()

            for (documentSnapshot in querySnapshot.documents) {
                val loyaltyCard = documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
                if (loyaltyCard != null) {
                    loyaltyCardList.add(loyaltyCard)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return loyaltyCardList
    }

    suspend fun getLoyaltyCardsByUniandesMemberId(uniandesMemberId: String): List<LoyaltyCard> {
        val loyaltyCardList = mutableListOf<LoyaltyCard>()
        try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            for (documentSnapshot in querySnapshot.documents) {
                val loyaltyCard = documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
                if (loyaltyCard != null) {
                    loyaltyCardList.add(loyaltyCard)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return loyaltyCardList
    }

    suspend fun getLoyaltyCardsByStoreIdAndUniandesMemberId(storeId: String, uniandesMemberId: String): List<LoyaltyCard> {
        val loyaltyCardList = mutableListOf<LoyaltyCard>()
        try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            for (documentSnapshot in querySnapshot.documents) {
                val loyaltyCard = documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
                if (loyaltyCard != null) {
                    loyaltyCardList.add(loyaltyCard)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return loyaltyCardList
    }

    suspend fun getLoyaltyCardById(loyaltyCardId: String): LoyaltyCard? {
        try {
            val documentSnapshot = firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCardId)
                .get()
                .await()

            return documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getCurrentLoyaltyCardByStoreIdAndUniandesMemberId(storeId: String, uniandesMemberId: String): LoyaltyCard? {
        try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .whereEqualTo("isCurrent", true)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val documentSnapshot = querySnapshot.documents[0]
                return documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    // Update an existing loyalty card
    suspend fun updateLoyaltyCard(loyaltyCard: LoyaltyCard): Boolean {
        return try {
            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCard.id!!)
                .set(loyaltyCard)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
