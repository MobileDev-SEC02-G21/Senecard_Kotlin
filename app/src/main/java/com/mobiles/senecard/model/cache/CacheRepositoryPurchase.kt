package com.mobiles.senecard.model.cache

import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.Purchase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class PurchaseResult {
    data class Success(val purchases: List<Purchase>, val isFromCache: Boolean) : PurchaseResult()
    data class Failure(val error: String) : PurchaseResult()
}

class CacheRepositoryPurchase private constructor() {

    private val firebase = FirebaseClient.instance
    private val purchaseCache = CacheManager<String, Purchase>(expiryDuration = 60, maxSize = 100)

    companion object {
        val instance: CacheRepositoryPurchase by lazy { CacheRepositoryPurchase() }
    }

    // Add Purchase
    suspend fun addPurchase(loyaltyCardId: String, date: String, isEligible: Boolean, rating: Double): Boolean {
        return try {
            val purchase = hashMapOf(
                "loyaltyCardId" to loyaltyCardId,
                "date" to date,
                "isEligible" to isEligible,
                "rating" to rating
            )

            val documentReference = firebase.firestore.collection("purchases").add(purchase).await()

            // Cache the added purchase
            purchaseCache.put(documentReference.id, Purchase(
                id = documentReference.id,
                loyaltyCardId = loyaltyCardId,
                date = date,
                isEligible = isEligible,
                rating = rating
            ))

            true
        } catch (e: Exception) {
            false
        }
    }

    // Get Purchases By Store ID
    suspend fun getPurchasesByStoreId(storeId: String): PurchaseResult {
        val purchasesList = mutableListOf<Purchase>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("purchases")
                    .whereEqualTo("storeId", storeId)
                    .get(Source.SERVER)
                    .await()

                querySnapshot.documents.forEach { documentSnapshot ->
                    documentSnapshot.toObject<Purchase>()?.copy(id = documentSnapshot.id)?.let { purchase ->
                        purchasesList.add(purchase)
                        purchaseCache.put(purchase.id!!, purchase) // Cache the purchase
                    }
                }
                return PurchaseResult.Success(purchasesList, isFromCache = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to cache
        purchaseCache.getAll()?.let {
            purchasesList.addAll(it.filter { purchase -> purchase.loyaltyCardId == storeId })
        }

        return if (purchasesList.isNotEmpty()) {
            PurchaseResult.Success(purchasesList, isFromCache = true)
        } else {
            PurchaseResult.Failure("Failed to fetch purchases for Store ID: $storeId from both network and cache.")
        }
    }

    // Get Purchases By Loyalty Card ID
    suspend fun getPurchasesByLoyaltyCardId(loyaltyCardId: String): PurchaseResult {
        val purchasesList = mutableListOf<Purchase>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("purchases")
                    .whereEqualTo("loyaltyCardId", loyaltyCardId)
                    .get(Source.SERVER)
                    .await()

                querySnapshot.documents.forEach { documentSnapshot ->
                    documentSnapshot.toObject<Purchase>()?.copy(id = documentSnapshot.id)?.let { purchase ->
                        purchasesList.add(purchase)
                        purchaseCache.put(purchase.id!!, purchase) // Cache the purchase
                    }
                }
                return PurchaseResult.Success(purchasesList, isFromCache = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to cache
        purchaseCache.getAll()?.let {
            purchasesList.addAll(it.filter { purchase -> purchase.loyaltyCardId == loyaltyCardId })
        }

        return if (purchasesList.isNotEmpty()) {
            PurchaseResult.Success(purchasesList, isFromCache = true)
        } else {
            PurchaseResult.Failure("Failed to fetch purchases for Loyalty Card ID: $loyaltyCardId from both network and cache.")
        }
    }
}
