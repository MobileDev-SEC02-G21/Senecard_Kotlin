package com.mobiles.senecard.model

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryLoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryLoyaltyCard by lazy { RepositoryLoyaltyCard() }
    }

    suspend fun addOrUpdateLoyaltyCard(uniandesMemberId: String, storeId: String, maxPoints: Int): String {
        return try {
            // Fetch all loyalty cards associated with the user and store
            val loyaltyCards = getLoyaltyCardsByUserAndStore(uniandesMemberId, storeId)

            // If no cards exist, create a new one
            if (loyaltyCards.isEmpty()) {
                val newLoyaltyCard = LoyaltyCard(
                    uniandesMemberId = uniandesMemberId,
                    storeId = storeId,
                    points = 1,  // Starts with 1 point
                    maxPoints = maxPoints,
                    isCurrent = true // This is the new current card
                )

                // Add the new card and get the document ID
                val documentRef = firebase.firestore.collection("loyaltyCards").add(newLoyaltyCard).await()
                newLoyaltyCard.id = documentRef.id // Assign the generated ID

                Log.d("RepositoryLoyaltyCard", "A new loyalty card with 1 point was created. ID: ${newLoyaltyCard.id}")
                return "A new loyalty card was created. You have 1 point."
            } else {
                // If cards exist, check their status
                var currentCard: LoyaltyCard? = null
                var allInactive = true

                for (card in loyaltyCards) {
                    if (card.isCurrent) {
                        currentCard = card
                        allInactive = false
                        break // If we find an active card, exit the loop
                    }
                    // Check if there's a card that's not full
                    if (card.points < card.maxPoints) {
                        allInactive = false // Not all cards are inactive
                    }
                }

                // If there is a current card, increment its points
                if (currentCard != null) {
                    Log.d("RepositoryLoyaltyCard", "Current card found with ${currentCard.points} points.")

                    if (currentCard.points < currentCard.maxPoints) {
                        currentCard.points += 1
                        Log.d("RepositoryLoyaltyCard", "Points incremented: ${currentCard.points}")

                        // Update the card in Firestore
                        firebase.firestore.collection("loyaltyCards").document(currentCard.id!!).set(currentCard).await()
                        return "Points incremented. You now have ${currentCard.points} points."
                    } else {
                        // If the maximum points are reached, deactivate the card
                        currentCard.isCurrent = false
                        firebase.firestore.collection("loyaltyCards").document(currentCard.id!!).set(currentCard).await()
                        Log.d("RepositoryLoyaltyCard", "Card deactivated: ${currentCard.id}")
                    }
                }

                // If all cards are inactive or full, deactivate all active cards and create a new one
                if (allInactive) {
                    // Deactivate all current cards before creating a new one
                    deactivateAllCurrentCards(uniandesMemberId, storeId)

                    // Create a new card
                    val newLoyaltyCard = LoyaltyCard(
                        uniandesMemberId = uniandesMemberId,
                        storeId = storeId,
                        points = 1,  // Starts with 1 point
                        maxPoints = maxPoints,
                        isCurrent = true // This is the new current card
                    )

                    // Add the new card and get the document ID
                    val documentRef = firebase.firestore.collection("loyaltyCards").add(newLoyaltyCard).await()
                    newLoyaltyCard.id = documentRef.id // Assign the generated ID

                    Log.d("RepositoryLoyaltyCard", "A new loyalty card with 1 point was created. ID: ${newLoyaltyCard.id}")
                    return "A new loyalty card was created. You have 1 point."
                }

                // If no new card was created, return an informational message
                return "No changes were made to the loyalty cards."
            }
        } catch (e: Exception) {
            Log.e("RepositoryLoyaltyCard", "Error processing the loyalty card: ${e.message}")
            return "Error processing the loyalty card."
        }
    }


    // Obtener todas las LoyaltyCards por usuario y tienda
    suspend fun getLoyaltyCardsByUserAndStore(uniandesMemberId: String, storeId: String): List<LoyaltyCard> {
        val cards = mutableListOf<LoyaltyCard>()
        try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .whereEqualTo("storeId", storeId)
                .get()
                .await()

            // Verificar si se encontró alguna tarjeta
            for (document in querySnapshot.documents) {
                val loyaltyCard = document.toObject(LoyaltyCard::class.java)
                loyaltyCard?.id = document.id // Asignar el ID del documento
                if (loyaltyCard != null) {
                    cards.add(loyaltyCard)
                }
            }
        } catch (e: Exception) {
            Log.e("RepositoryLoyaltyCard", "Error fetching LoyaltyCards: ${e.message}")
        }
        return cards
    }

    // Método para obtener una LoyaltyCard específica (sólo la última actual)
    suspend fun getLoyaltyCardByUserAndStore(uniandesMemberId: String, storeId: String): LoyaltyCard? {
        val cards = getLoyaltyCardsByUserAndStore(uniandesMemberId, storeId)
        return cards.firstOrNull { it.isCurrent }  // Retorna la tarjeta actual si existe
    }

    // Desactiva todas las tarjetas actuales de un usuario para una tienda específica
    private suspend fun deactivateAllCurrentCards(uniandesMemberId: String, storeId: String) {
        try {
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("isCurrent", true)  // Solo desactivar las actuales
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val card = document.toObject<LoyaltyCard>()
                if (card != null && card.isCurrent) {
                    card.isCurrent = false
                    firebase.firestore.collection("loyaltyCards").document(document.id).set(card).await()
                    Log.d("RepositoryLoyaltyCard", "Tarjeta inactivada: ${document.id}")
                }
            }
        } catch (e: Exception) {
            Log.e("RepositoryLoyaltyCard", "Error al desactivar tarjetas: ${e.message}")
        }
    }


    suspend fun getLoyaltyCardsByUser(uniandesMemberId: String): List<LoyaltyCard> {
        val loyaltyCardsList = mutableListOf<LoyaltyCard>()
        try {
            // Consulta Firebase usando uniandesMemberId en lugar de userId
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get()
                .await()

            // Procesa los documentos obtenidos
            for (document in querySnapshot.documents) {
                val loyaltyCard = document.toObject<LoyaltyCard>()?.copy(id = document.id)
                if (loyaltyCard != null) {
                    loyaltyCardsList.add(loyaltyCard)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Manejo de errores
        }
        return loyaltyCardsList
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
            // Force Firestore to fetch data from the server (bypassing cache)
            val querySnapshot = firebase.firestore.collection("loyaltyCards")
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .get(com.google.firebase.firestore.Source.SERVER)  // Fetch from server
                .await()

            Log.d("LoyaltyCardRepository", "Number of loyalty cards found: ${querySnapshot.documents.size}")

            for (documentSnapshot in querySnapshot.documents) {
                val loyaltyCard = documentSnapshot.toObject(LoyaltyCard::class.java)

                // Explicitly setting the `isCurrent` field from the raw data
                val rawIsCurrent = documentSnapshot.getBoolean("isCurrent") ?: true // Default to true if not present
                loyaltyCard?.isCurrent = rawIsCurrent

                // Manually setting the document ID as the LoyaltyCard ID
                loyaltyCard?.id = documentSnapshot.id

                loyaltyCard?.let {
                    Log.d("LoyaltyCardRepository", "LoyaltyCard ID: ${it.id}, isCurrent: ${it.isCurrent}, Points: ${it.points}, MaxPoints: ${it.maxPoints}")
                    loyaltyCardList.add(it)
                }
            }

            Log.d("LoyaltyCardRepository", "Total loyalty cards retrieved: ${loyaltyCardList.size}")

        } catch (e: Exception) {
            Log.e("LoyaltyCardRepository", "Error fetching loyalty cards: ${e.message}")
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
            // Create a map without the 'id' field to prevent it from being saved as a document field
            val loyaltyCardData = hashMapOf(
                "storeId" to loyaltyCard.storeId,
                "uniandesMemberId" to loyaltyCard.uniandesMemberId,
                "maxPoints" to loyaltyCard.maxPoints,
                "points" to loyaltyCard.points,
                "isCurrent" to loyaltyCard.isCurrent // Use the correct field name
            )

            // Use the 'id' to update the document, but do not store it inside the document
            firebase.firestore.collection("loyaltyCards")
                .document(loyaltyCard.id!!)  // Use the document ID for the reference
                .set(loyaltyCardData)        // Set the data without 'id'
                .await()

            Log.d("RepositoryLoyaltyCard", "LoyaltyCard updated successfully.")
            true
        } catch (e: Exception) {
            Log.e("RepositoryLoyaltyCard", "Error updating LoyaltyCard: ${e.message}")
            false
        }
    }



}

