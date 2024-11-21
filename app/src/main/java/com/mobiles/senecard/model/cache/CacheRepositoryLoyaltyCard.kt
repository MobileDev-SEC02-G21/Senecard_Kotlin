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

    suspend fun getLoyaltyCardsByUserAndStore(uniandesMemberId: String, storeId: String): LoyaltyCardResult {
        Log.d("CacheRepositoryLoyaltyCard", "Fetching loyalty cards for user: $uniandesMemberId, store: $storeId")
        val loyaltyCards = mutableListOf<LoyaltyCard>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("loyaltyCards")
                    .whereEqualTo("uniandesMemberId", uniandesMemberId)
                    .whereEqualTo("storeId", storeId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val loyaltyCard = document.toObject<LoyaltyCard>()?.apply { id = document.id }
                    if (loyaltyCard != null) {
                        loyaltyCardCache.put(loyaltyCard.id!!, loyaltyCard)
                        loyaltyCards.add(loyaltyCard)
                    }
                }

                Log.d("CacheRepositoryLoyaltyCard", "Fetched ${loyaltyCards.size} loyalty cards from Firestore.")
                if (loyaltyCards.isNotEmpty()) {
                    return LoyaltyCardResult.Success(loyaltyCards, isFromCache = false)
                }
            } catch (e: Exception) {
                Log.e("CacheRepositoryLoyaltyCard", "Error fetching loyalty cards from Firestore: ${e.message}")
            }
        }

        Log.d("CacheRepositoryLoyaltyCard", "Falling back to cache.")
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

    suspend fun getLoyaltyCardsByStore(storeId: String): LoyaltyCardResult {
        Log.d("CacheRepositoryLoyaltyCard", "Fetching all loyalty cards for store: $storeId")
        val loyaltyCards = mutableListOf<LoyaltyCard>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("loyaltyCards")
                    .whereEqualTo("storeId", storeId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val loyaltyCard = document.toObject<LoyaltyCard>()?.apply { id = document.id }
                    if (loyaltyCard != null) {
                        loyaltyCardCache.put(loyaltyCard.id!!, loyaltyCard)
                        loyaltyCards.add(loyaltyCard)
                    }
                }

                Log.d("CacheRepositoryLoyaltyCard", "Fetched ${loyaltyCards.size} loyalty cards from Firestore.")
                if (loyaltyCards.isNotEmpty()) {
                    return LoyaltyCardResult.Success(loyaltyCards, isFromCache = false)
                }
            } catch (e: Exception) {
                Log.e("CacheRepositoryLoyaltyCard", "Error fetching loyalty cards from Firestore: ${e.message}")
            }
        }

        Log.d("CacheRepositoryLoyaltyCard", "Falling back to cache.")
        loyaltyCardCache.getAll()?.filter { it.storeId == storeId }?.let { cachedCards ->
            loyaltyCards.addAll(cachedCards)
        }

        return if (loyaltyCards.isNotEmpty()) {
            LoyaltyCardResult.Success(loyaltyCards, isFromCache = true)
        } else {
            LoyaltyCardResult.Failure("No loyalty cards found for the specified store.")
        }
    }


    suspend fun getCurrentLoyaltyCard(uniandesMemberId: String, storeId: String): LoyaltyCardResult {
        Log.d("CacheRepositoryLoyaltyCard", "Fetching current loyalty card for user: $uniandesMemberId, store: $storeId")
        return try {
            if (NetworkUtils.isInternetAvailable()) {
                val querySnapshot = firebase.firestore.collection("loyaltyCards")
                    .whereEqualTo("uniandesMemberId", uniandesMemberId)
                    .whereEqualTo("storeId", storeId)
                    .whereEqualTo("isCurrent", true)
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val documentSnapshot = querySnapshot.documents.first()
                    val loyaltyCard = documentSnapshot.toObject<LoyaltyCard>()?.apply { id = documentSnapshot.id }

                    loyaltyCard?.let {
                        loyaltyCardCache.put(it.id!!, it)
                        return LoyaltyCardResult.Success(listOf(it), isFromCache = false)
                    }
                }
            }

            val cachedCard = loyaltyCardCache.getAll()?.find {
                it.uniandesMemberId == uniandesMemberId &&
                        it.storeId == storeId &&
                        it.isCurrent
            }

            cachedCard?.let {
                Log.w("CacheRepositoryLoyaltyCard", "Using outdated cached loyalty card: $it")
                return LoyaltyCardResult.Success(listOf(it), isFromCache = true)
            }

            LoyaltyCardResult.Failure("No active loyalty card found.")
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error fetching current loyalty card: ${e.message}")
            LoyaltyCardResult.Failure("Error fetching the current loyalty card.")
        }
    }


    suspend fun addLoyaltyCard(loyaltyCardObject: LoyaltyCard): Boolean {
        Log.d("CacheRepositoryLoyaltyCard", "Adding loyalty card: $loyaltyCardObject")
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

            Log.d("CacheRepositoryLoyaltyCard", "Loyalty card added successfully with ID: ${loyaltyCardObject.id}")
            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error adding loyalty card: ${e.message}")
            false
        }
    }

    suspend fun updateLoyaltyCard(loyaltyCard: LoyaltyCard): Boolean {
        Log.d("CacheRepositoryLoyaltyCard", "Updating loyalty card: ${loyaltyCard.id}")
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

            Log.d("CacheRepositoryLoyaltyCard", "Loyalty card updated successfully.")
            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error updating loyalty card: ${e.message}")
            false
        }
    }

    suspend fun updateLoyaltyCardPoints(loyaltyCardId: String, newPoints: Int): Boolean {
        Log.d("CacheRepositoryLoyaltyCard", "Updating points for loyalty card: $loyaltyCardId to $newPoints")
        return try {
            loyaltyCardCache[loyaltyCardId]?.let { card ->
                if (newPoints > card.maxPoints) {
                    Log.e("CacheRepositoryLoyaltyCard", "Error: New points exceed the maximum allowed.")
                    return false
                }
            }

            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCardId)
                .update("points", newPoints)
                .await()

            loyaltyCardCache[loyaltyCardId]?.let {
                it.points = newPoints
                loyaltyCardCache.put(loyaltyCardId, it)
            }

            Log.d("CacheRepositoryLoyaltyCard", "Points updated successfully for loyalty card: $loyaltyCardId")
            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error updating loyalty card points: ${e.message}")
            false
        }
    }


    suspend fun updateLoyaltyCardIsCurrent(loyaltyCardId: String, isCurrent: Boolean): Boolean {
        Log.d("CacheRepositoryLoyaltyCard", "Updating current status for loyalty card: $loyaltyCardId to $isCurrent")
        return try {
            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCardId)
                .update("isCurrent", isCurrent)
                .await()

            loyaltyCardCache[loyaltyCardId]?.let {
                it.isCurrent = isCurrent
                loyaltyCardCache.put(loyaltyCardId, it)
            }

            Log.d("CacheRepositoryLoyaltyCard", "Current status updated successfully for loyalty card: $loyaltyCardId")
            true
        } catch (e: Exception) {
            Log.e("CacheRepositoryLoyaltyCard", "Error updating loyalty card's current status: ${e.message}")
            false
        }
    }
}
