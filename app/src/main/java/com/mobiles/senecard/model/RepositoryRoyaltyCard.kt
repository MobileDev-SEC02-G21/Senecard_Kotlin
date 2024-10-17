package com.mobiles.senecard.model

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import com.mobiles.senecard.model.entities.RoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryRoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryRoyaltyCard by lazy { RepositoryRoyaltyCard() }
    }

    // Añadir o actualizar la RoyaltyCard
    suspend fun addOrUpdateRoyaltyCard(uniandesMemberId: String, storeId: String, maxPoints: Int): String {
        return try {
            // Busca todas las RoyaltyCards asociadas al usuario y la tienda
            val royaltyCards = getRoyaltyCardsByUserAndStore(uniandesMemberId, storeId)

            // Si no hay tarjetas, crear una nueva
            if (royaltyCards.isEmpty()) {
                val newRoyaltyCard = RoyaltyCard(
                    uniandesMemberId = uniandesMemberId,
                    storeId = storeId,
                    points = 1,  // Comienza con 1 punto
                    maxPoints = maxPoints,
                    isCurrent = true // Esta es la nueva tarjeta actual
                )

                // Añadir la nueva tarjeta y obtener el ID del documento creado
                val documentRef = firebase.firestore.collection("royaltyCards").add(newRoyaltyCard).await()
                newRoyaltyCard.id = documentRef.id // Asignar el ID generado

                Log.d("RepositoryRoyaltyCard", "Se ha creado una nueva tarjeta con 1 punto. ID: ${newRoyaltyCard.id}")
                "Se ha creado una nueva tarjeta de fidelidad. Tienes 1 punto."
            } else {
                // Si hay tarjetas, verificar el estado
                var currentCard: RoyaltyCard? = null
                var allInactive = true

                for (card in royaltyCards) {
                    if (card.isCurrent) {
                        currentCard = card
                        allInactive = false
                        break // Si encontramos una tarjeta actual, salimos del bucle
                    }
                    // Si encontramos una tarjeta que no está llena
                    if (card.points < card.maxPoints) {
                        allInactive = false // No todas están inactivas
                    }
                }

                // Si hay una tarjeta actual, incrementamos sus puntos
                if (currentCard != null) {
                    Log.d("RepositoryRoyaltyCard", "Tarjeta actual encontrada con ${currentCard.points} puntos.")

                    if (currentCard.points < currentCard.maxPoints) {
                        currentCard.points += 1
                        Log.d("RepositoryRoyaltyCard", "Puntos incrementados: ${currentCard.points}")

                        // Actualiza la tarjeta en Firestore
                        firebase.firestore.collection("royaltyCards").document(currentCard.id!!).set(currentCard).await()
                        return "Puntos incrementados. Ahora tienes ${currentCard.points} puntos."
                    } else {
                        // Si se alcanza el máximo de puntos, inactivamos la tarjeta
                        currentCard.isCurrent = false
                        firebase.firestore.collection("royaltyCards").document(currentCard.id!!).set(currentCard).await()
                        Log.d("RepositoryRoyaltyCard", "Tarjeta inactivada: ${currentCard.id}")
                    }
                }

                // Si todas las tarjetas están inactivas o llenas, creamos una nueva
                if (allInactive) {
                    val newRoyaltyCard = RoyaltyCard(
                        uniandesMemberId = uniandesMemberId,
                        storeId = storeId,
                        points = 1,  // Comienza con 1 punto
                        maxPoints = maxPoints,
                        isCurrent = true // Esta es la nueva tarjeta actual
                    )

                    // Añadir la nueva tarjeta y obtener el ID del documento creado
                    val documentRef = firebase.firestore.collection("royaltyCards").add(newRoyaltyCard).await()
                    newRoyaltyCard.id = documentRef.id // Asignar el ID generado

                    Log.d("RepositoryRoyaltyCard", "Se ha creado una nueva tarjeta con 1 punto. ID: ${newRoyaltyCard.id}")
                    return "Se ha creado una nueva tarjeta de fidelidad. Tienes 1 punto."
                }

                // Si no se ha creado ninguna tarjeta nueva, devuelve un mensaje informativo
                "No se realizaron cambios en las tarjetas."
            }
        } catch (e: Exception) {
            Log.e("RepositoryRoyaltyCard", "Error al procesar la tarjeta de fidelidad: ${e.message}")
            "Error al procesar la tarjeta de fidelidad."
        }
    }

    // Obtener todas las RoyaltyCards por usuario y tienda
    suspend fun getRoyaltyCardsByUserAndStore(uniandesMemberId: String, storeId: String): List<RoyaltyCard> {
        val cards = mutableListOf<RoyaltyCard>()
        try {
            val querySnapshot = firebase.firestore.collection("royaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .whereEqualTo("storeId", storeId)
                .get()
                .await()

            // Verificar si se encontró alguna tarjeta
            for (document in querySnapshot.documents) {
                val royaltyCard = document.toObject(RoyaltyCard::class.java)
                royaltyCard?.id = document.id // Asignar el ID del documento
                if (royaltyCard != null) {
                    cards.add(royaltyCard)
                }
            }
        } catch (e: Exception) {
            Log.e("RepositoryRoyaltyCard", "Error fetching RoyaltyCards: ${e.message}")
        }
        return cards
    }

    // Método para obtener una RoyaltyCard específica (sólo la última actual)
    suspend fun getRoyaltyCardByUserAndStore(uniandesMemberId: String, storeId: String): RoyaltyCard? {
        val cards = getRoyaltyCardsByUserAndStore(uniandesMemberId, storeId)
        return cards.firstOrNull { it.isCurrent }  // Retorna la tarjeta actual si existe
    }

    // Desactiva todas las tarjetas actuales de un usuario para una tienda específica
    private suspend fun deactivateAllCurrentCards(uniandesMemberId: String, storeId: String) {
        try {
            val querySnapshot = firebase.firestore.collection("royaltyCards")
                .whereEqualTo("uniandesMemberId", uniandesMemberId)
                .whereEqualTo("storeId", storeId)
                .whereEqualTo("isCurrent", true)  // Solo desactivar las actuales
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val card = document.toObject<RoyaltyCard>()
                if (card != null && card.isCurrent) {
                    card.isCurrent = false
                    firebase.firestore.collection("royaltyCards").document(document.id).set(card).await()
                    Log.d("RepositoryRoyaltyCard", "Tarjeta inactivada: ${document.id}")
                }
            }
        } catch (e: Exception) {
            Log.e("RepositoryRoyaltyCard", "Error al desactivar tarjetas: ${e.message}")
        }
    }

    // Actualizar una RoyaltyCard
    suspend fun updateRoyaltyCard(royaltyCard: RoyaltyCard) {
        try {
            Log.d("RepositoryRoyaltyCard", "Actualizando la tarjeta con puntos: ${royaltyCard.points}")
            firebase.firestore.collection("royaltyCards")
                .document(royaltyCard.id!!)  // Asegúrate de que el ID no sea nulo
                .set(royaltyCard)
                .await()
            Log.d("RepositoryRoyaltyCard", "RoyaltyCard actualizada correctamente.")
        } catch (e: Exception) {
            Log.e("RepositoryRoyaltyCard", "Error actualizando RoyaltyCard: ${e.message}")
        }
    }
}
