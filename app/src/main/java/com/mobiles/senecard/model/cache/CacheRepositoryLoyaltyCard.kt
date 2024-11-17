package com.mobiles.senecard.model.cache

import android.util.Log
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.tasks.await

sealed class LoyaltyCardResult {
    data class Success(val loyaltyCards: List<LoyaltyCard>, val isFromCache: Boolean) : LoyaltyCardResult()
    data class Failure(val error: String) : LoyaltyCardResult()
}

class CacheRepositoryLoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance
    private val loyaltyCardCache = CacheManager<String, LoyaltyCard>(expiryDuration = 60, maxSize = 50)

    companion object {
        val instance: CacheRepositoryLoyaltyCard by lazy { CacheRepositoryLoyaltyCard() }
    }

    // Fetch all loyalty cards for a specific user and store
    suspend fun getLoyaltyCardsByUserAndStore(uniandesMemberId: String, storeId: String): LoyaltyCardResult {
        val loyaltyCards = mutableListOf<LoyaltyCard>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                // Fetch loyalty cards from Firestore
                val querySnapshot = firebase.firestore.collection("loyaltyCards")
                    .whereEqualTo("uniandesMemberId", uniandesMemberId)
                    .whereEqualTo("storeId", storeId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val loyaltyCard = document.toObject<LoyaltyCard>()?.apply {
                        id = document.id
                    }
                    if (loyaltyCard != null) {
                        loyaltyCardCache.put(loyaltyCard.id!!, loyaltyCard)
                        loyaltyCards.add(loyaltyCard)
                    }
                }

                // If no loyalty cards found, create the first one
                if (loyaltyCards.isEmpty()) {
                    val newLoyaltyCard = LoyaltyCard(
                        storeId = storeId,
                        uniandesMemberId = uniandesMemberId,
                        maxPoints = 10, // Default maximum points
                        points = 0, // Starting points
                        isCurrent = true
                    )
                    val success = addLoyaltyCard(newLoyaltyCard)

                    if (success) {
                        loyaltyCards.add(newLoyaltyCard)
                        return LoyaltyCardResult.Success(loyaltyCards, isFromCache = false)
                    } else {
                        return LoyaltyCardResult.Failure("Failed to create a new loyalty card for the user and store.")
                    }
                }

                return LoyaltyCardResult.Success(loyaltyCards, isFromCache = false)
            } catch (e: Exception) {
                Log.e("CacheRepositoryLoyaltyCard", "Error fetching LoyaltyCards: ${e.message}")
            }
        }

        // Fallback to cache if no cards were fetched
        if (loyaltyCards.isEmpty()) {
            loyaltyCardCache.getAll()?.filter {
                it.uniandesMemberId == uniandesMemberId && it.storeId == storeId
            }?.let { cachedCards ->
                loyaltyCards.addAll(cachedCards)
            }

            return if (loyaltyCards.isNotEmpty()) {
                LoyaltyCardResult.Success(loyaltyCards, isFromCache = true)
            } else {
                LoyaltyCardResult.Failure("No loyalty cards found for the specified user and store.")
            }
        }

        return LoyaltyCardResult.Failure("Failed to fetch loyalty cards from both network and cache.")
    }


    // Add a new loyalty card
    suspend fun addLoyaltyCard(loyaltyCardObject: LoyaltyCard): Boolean {
        return try {
            val loyaltyCardData = hashMapOf(
                "storeId" to loyaltyCardObject.storeId,
                "uniandesMemberId" to loyaltyCardObject.uniandesMemberId,
                "maxPoints" to loyaltyCardObject.maxPoints,
                "points" to loyaltyCardObject.points,
                "isCurrent" to true
            )

            val documentReference = firebase.firestore.collection("loyaltyCards")
                .add(loyaltyCardData)
                .await()

            loyaltyCardObject.id = documentReference.id
            loyaltyCardCache.put(loyaltyCardObject.id!!, loyaltyCardObject)

            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error adding LoyaltyCard: ${e.message}")
            false
        }
    }

    // Update an existing loyalty card
    suspend fun updateLoyaltyCard(loyaltyCard: LoyaltyCard): Boolean {
        return try {
            val loyaltyCardData = hashMapOf(
                "storeId" to loyaltyCard.storeId,
                "uniandesMemberId" to loyaltyCard.uniandesMemberId,
                "maxPoints" to loyaltyCard.maxPoints,
                "points" to loyaltyCard.points,
                "isCurrent" to loyaltyCard.isCurrent
            )

            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCard.id!!)
                .set(loyaltyCardData)
                .await()

            loyaltyCardCache.put(loyaltyCard.id!!, loyaltyCard)

            Log.d("CacheRepositoryLoyaltyCard", "LoyaltyCard updated successfully.")
            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error updating LoyaltyCard: ${e.message}")
            false
        }
    }

    // Fetch loyalty cards by user ID
    suspend fun getLoyaltyCardsByUserId(uniandesMemberId: String): LoyaltyCardResult {
        val loyaltyCards = mutableListOf<LoyaltyCard>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("loyaltyCards")
                    .whereEqualTo("uniandesMemberId", uniandesMemberId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val loyaltyCard = document.toObject<LoyaltyCard>()?.apply {
                        id = document.id
                    }
                    if (loyaltyCard != null) {
                        loyaltyCardCache.put(loyaltyCard.id!!, loyaltyCard)
                        loyaltyCards.add(loyaltyCard)
                    }
                }

                return LoyaltyCardResult.Success(loyaltyCards, isFromCache = false)
            } catch (e: Exception) {
                Log.e("CacheRepositoryLoyaltyCard", "Error fetching LoyaltyCards: ${e.message}")
            }
        }

        // Fallback to cache if no cards were fetched
        if (loyaltyCards.isEmpty()) {
            loyaltyCardCache.getAll()?.filter {
                it.uniandesMemberId == uniandesMemberId
            }?.let { cachedCards ->
                loyaltyCards.addAll(cachedCards)
            }

            return if (loyaltyCards.isNotEmpty()) {
                LoyaltyCardResult.Success(loyaltyCards, isFromCache = true)
            } else {
                LoyaltyCardResult.Failure("No loyalty cards found for the specified user.")
            }
        }

        return LoyaltyCardResult.Failure("Failed to fetch loyalty cards from both network and cache.")
    }

    suspend fun updateLoyaltyCardPoints(loyaltyCardId: String, newPoints: Int): Boolean {
        return try {
            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCardId)
                .update("points", newPoints)
                .await()

            // Update the cached version
            loyaltyCardCache[loyaltyCardId]?.let {
                it.points = newPoints
                loyaltyCardCache.put(loyaltyCardId, it)
            }

            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error updating loyalty card points: ${e.message}")
            false
        }
    }
    suspend fun getCurrentLoyaltyCard(uniandesMemberId: String, storeId: String): LoyaltyCardResult {
        return try {
            if (NetworkUtils.isInternetAvailable()) {
                // Fetch the current active loyalty card from Firestore
                val querySnapshot = firebase.firestore.collection("loyaltyCards")
                    .whereEqualTo("uniandesMemberId", uniandesMemberId)
                    .whereEqualTo("storeId", storeId)
                    .whereEqualTo("isCurrent", true) // Filter for the active card
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val documentSnapshot = querySnapshot.documents.first()
                    val loyaltyCard = documentSnapshot.toObject<LoyaltyCard>()?.apply {
                        id = documentSnapshot.id
                    }

                    loyaltyCard?.let {
                        loyaltyCardCache.put(it.id!!, it) // Cache the active card
                        return LoyaltyCardResult.Success(listOf(it), isFromCache = false)
                    }
                }

                // No active card found
                LoyaltyCardResult.Failure("No active loyalty card found for the user and store.")
            } else {
                // Fallback to cache
                val cachedCard = loyaltyCardCache.getAll()?.find {
                    it.uniandesMemberId == uniandesMemberId &&
                            it.storeId == storeId &&
                            it.isCurrent
                }

                cachedCard?.let {
                    return LoyaltyCardResult.Success(listOf(it), isFromCache = true)
                }

                LoyaltyCardResult.Failure("No active loyalty card found in cache.")
            }
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error fetching current loyalty card: ${e.message}")
            LoyaltyCardResult.Failure("An error occurred while fetching the current loyalty card.")
        }
    }
    suspend fun updateLoyaltyCardIsCurrent(loyaltyCardId: String, isCurrent: Boolean): Boolean {
        return try {
            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCardId)
                .update("isCurrent", isCurrent)
                .await()

            // Update the cached version
            loyaltyCardCache[loyaltyCardId]?.let {
                it.isCurrent = isCurrent
                loyaltyCardCache.put(loyaltyCardId, it)
            }

            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error updating loyalty card's current status: ${e.message}")
            false
        }
    }

}
