package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.Purchase
import kotlinx.coroutines.tasks.await

class RepositoryPurchase private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryPurchase by lazy { RepositoryPurchase() }
    }

    suspend fun addPurchase(storeId: String, uniandesMemberId: String, date: String, eligible: Boolean, rating: Double): Boolean {
        try {
            val purchase = hashMapOf(
                "storeId" to storeId,
                "uniandesMemberId" to uniandesMemberId,
                "date" to date,
                "eligible" to eligible,
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
            val querySnapshot = firebase.firestore.collection("purchases").whereEqualTo("storeId", storeId).get().await()

            for (documentSnapshot in querySnapshot.documents) {
                val purchase = documentSnapshot.toObject<Purchase>()?.copy(id = documentSnapshot.id)
                if (purchase != null) {
                    purchasesList.add(purchase)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
        return purchasesList
    }
}
