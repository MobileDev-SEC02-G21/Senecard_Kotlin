package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.Purchase
import kotlinx.coroutines.tasks.await

class RepositoryPurchase private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryPurchase by lazy { RepositoryPurchase() }
    }

    suspend fun addPurchase(loyaltyCardId: String, date: String, isEligible: Boolean, rating: Double): Boolean {
        try {
            val purchase = hashMapOf(
                "loyaltyCardId" to loyaltyCardId,
                "date" to date,
                "isEligible" to isEligible,
                "rating" to rating
            )

            firebase.firestore.collection("purchases").add(purchase).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getPurchaseById(purchaseId: String): Purchase? {
        try {
            val documentSnapshot = firebase.firestore.collection("purchases").document(purchaseId).get().await()
            return documentSnapshot.toObject<Purchase>()?.copy(id = documentSnapshot.id)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getPurchasesByStoreId(storeId: String): List<Purchase> {
        val purchasesList = mutableListOf<Purchase>()
        try {
            // TO DO
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
        return purchasesList
    }

    // New method to get all purchases related to a specific loyalty card ID
    suspend fun getPurchasesByLoyaltyCardId(loyaltyCardId: String): List<Purchase> {
        return try {
            val querySnapshot = firebase.firestore.collection("purchases")
                .whereEqualTo("loyaltyCardId", loyaltyCardId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject<Purchase>()?.copy(id = document.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
