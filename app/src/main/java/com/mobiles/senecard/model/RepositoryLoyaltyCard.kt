package com.mobiles.senecard.model

import com.google.firebase.firestore.ktx.toObject
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryLoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryLoyaltyCard by lazy { RepositoryLoyaltyCard() }
    }

    suspend fun addLoyaltyCard(loyaltyCard: LoyaltyCard): Boolean {
        return try {
            firebase.firestore.collection("loyaltyCards").add(loyaltyCard).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get all loyalty cards by storeId and uniandesMemberId
    suspend fun getLoyaltyCardsByStoreIdAndUniandesMemberId(storeId: String, uniandesMemberId: String): List<LoyaltyCard> {
        return try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject<LoyaltyCard>()?.copy(id = it.id) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return empty list on error
        }
    }

    // Get all loyalty cards by storeId
    suspend fun getLoyaltyCardsByStoreId(storeId: String): List<LoyaltyCard> {
        return try {
            val loyaltyCardsSnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .get()
                .await()

            loyaltyCardsSnapshot.documents.mapNotNull { document ->
                document.toObject(LoyaltyCard::class.java)?.apply {
                    id = document.id // Set the document ID
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Get all loyalty cards by uniandesMemberId
    suspend fun getLoyaltyCardsByUniandesMemberId(uniandesMemberId: String): List<LoyaltyCard> {
        return try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject<LoyaltyCard>()?.copy(id = it.id) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Function to get loyalty card by its ID
    suspend fun getLoyaltyCardById(loyaltyCardId: String): LoyaltyCard? {
        return try {
            val documentSnapshot = firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCardId).get().await()
            documentSnapshot.toObject<LoyaltyCard>()?.copy(id = documentSnapshot.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get current active loyalty card by storeId and uniandesMemberId
    suspend fun getCurrentLoyaltyCardsByStoreIdAndUniandesMemberId(storeId: String, uniandesMemberId: String): LoyaltyCard? {
        return try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .whereEqualTo("isCurrent", true) // Fetch only active/current loyalty card
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
