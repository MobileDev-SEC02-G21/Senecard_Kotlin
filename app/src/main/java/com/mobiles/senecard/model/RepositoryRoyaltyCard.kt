package com.mobiles.senecard.model

import com.google.firebase.firestore.ktx.toObject
import com.mobiles.senecard.model.entities.RoyaltyCard
import kotlinx.coroutines.tasks.await

class RepositoryRoyaltyCard private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryRoyaltyCard by lazy { RepositoryRoyaltyCard() }
    }

    // Añadir o actualizar la RoyaltyCard
    suspend fun addOrUpdateRoyaltyCard(userId: String, storeId: String, maxPoints: Int): String {
        try {
            // Verifica si la tarjeta ya existe
            val royaltyCard = getRoyaltyCardByUserAndStore(userId, storeId)

            return if (royaltyCard != null) {
                // Si ya existe, actualiza los puntos
                if (royaltyCard.points < royaltyCard.maxPoints) {
                    royaltyCard.points += 1
                    firebase.firestore.collection("royaltyCards").document(royaltyCard.id!!).set(royaltyCard).await()
                    "Puntos incrementados. Ahora tienes ${royaltyCard.points} puntos."
                } else {
                    "Ya has alcanzado el máximo de puntos (${royaltyCard.maxPoints}). Canjea tu tarjeta."
                }
            } else {
                // Si no existe, crea una nueva
                val newRoyaltyCard = RoyaltyCard(
                    userId = userId,
                    storeId = storeId,
                    points = 1,  // Comienza con 1 punto
                    maxPoints = maxPoints
                )
                firebase.firestore.collection("royaltyCards").add(newRoyaltyCard).await()
                "Se ha creado una nueva tarjeta de fidelidad. Tienes 1 punto."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error al procesar la tarjeta de fidelidad."
        }
    }

    // Obtener la RoyaltyCard por usuario y tienda
    suspend fun getRoyaltyCardByUserAndStore(userId: String, storeId: String): RoyaltyCard? {
        return try {
            val querySnapshot = firebase.firestore.collection("royaltyCards")
                .whereEqualTo("userId", userId)
                .whereEqualTo("storeId", storeId)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].toObject<RoyaltyCard>()?.copy(id = querySnapshot.documents[0].id)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
