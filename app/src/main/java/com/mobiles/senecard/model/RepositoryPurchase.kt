package com.mobiles.senecard.model

import com.google.firebase.firestore.FirebaseFirestore
import com.mobiles.senecard.model.entities.Purchase
import kotlinx.coroutines.tasks.await

class RepositoryPurchase private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryPurchase by lazy { RepositoryPurchase() }
    }

    suspend fun addPurchase(purchase: Purchase): Boolean {
        try {
            firebase.firestore.collection("purchases").add(purchase).await()
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

    suspend fun updatePurchase(id: String, updatedFields: Map<String, Any>): Boolean {
        try {
            firebase.firestore.collection("purchases").document(id).update(updatedFields).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
