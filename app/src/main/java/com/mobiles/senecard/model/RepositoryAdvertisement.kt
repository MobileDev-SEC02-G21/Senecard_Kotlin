package com.mobiles.senecard.model

import com.google.firebase.firestore.FirebaseFirestore
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.tasks.await

class RepositoryAdvertisement private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryAdvertisement by lazy { RepositoryAdvertisement() }
    }

    suspend fun addAdvertisement(
        available: Boolean,
        description: String,
        endDate: String,
        imageUrl: String,
        startDate: String,
        storeId: String,
        title: String
    ): Boolean {
        try {
            val advertisement = hashMapOf(
                "available" to available,
                "description" to description,
                "endDate" to endDate,
                "image" to imageUrl,
                "startDate" to startDate,
                "storeId" to storeId,
                "title" to title
            )

            // Let Firestore generate the ID
            firebase.firestore.collection("advertisements").add(advertisement).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }


    suspend fun getAdvertisementById(id: String): Advertisement? {
        try {
            val document = firebase.firestore.collection("advertisements").document(id).get().await()
            return if (document.exists()) {
                document.toObject(Advertisement::class.java)
            } else null
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun updateAdvertisement(id: String, updatedFields: Map<String, Any>): Boolean {
        try {
            firebase.firestore.collection("advertisements").document(id).update(updatedFields).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}