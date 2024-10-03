package com.mobiles.senecard.model

import com.google.firebase.firestore.FirebaseFirestore
import com.mobiles.senecard.model.entities.Purchase
import kotlinx.coroutines.tasks.await

class RepositoryPurchase private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryPurchase by lazy { RepositoryPurchase() }
    }

    suspend fun addPurchase(
        date: String,
        eligible: Boolean,
        purchase: String,
        rating: Int,
        storeId: String,
        uniandesMemberId: String
    ): Boolean {
        try {
            val purchaseData = hashMapOf(
                "date" to date,
                "eligible" to eligible,
                "purchase" to purchase,
                "rating" to rating,
                "storeId" to storeId,
                "uniandesMemberId" to uniandesMemberId
            )

            // Let Firestore generate the ID
            firebase.firestore.collection("purchases").add(purchaseData).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }


    suspend fun getPurchaseById(id: String): Purchase? {
        try {
            val document = firebase.firestore.collection("purchases").document(id).get().await()
            return if (document.exists()) {
                document.toObject(Purchase::class.java)
            } else null
        } catch (e: Exception) {
            return null
        }
    }


    // Fetch purchases by storeId (new method)
    suspend fun getPurchasesByStore(storeId: String): List<Purchase> {
        try {
            val querySnapshot = firebase.firestore.collection("purchases")
                .whereEqualTo("storeId", storeId).get().await()

            val purchases = mutableListOf<Purchase>()

            for (document in querySnapshot.documents) {
                val purchase = Purchase(
                    purchaseId = document.id,
                    date = document.getString("date")!!,
                    eligible = document.getBoolean("eligible") ?: false,
                    purchaseDescription = document.getString("purchase")!!,
                    rating = document.getDouble("rating")?.toInt() ?: 0,
                    storeId = storeId,
                    userId = document.getString("uniandesMemberId")!!
                )
                purchases.add(purchase)
            }

            return purchases
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun updatePurchase(id: String, updatedFields: Map<String, Any>): Boolean {
        try {
            firebase.firestore.collection("purchases").document(id).update(updatedFields).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
