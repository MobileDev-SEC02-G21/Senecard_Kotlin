package com.mobiles.senecard.model

import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class RepositoryAdvertisementAnalytics private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryAdvertisementAnalytics by lazy { RepositoryAdvertisementAnalytics() }
    }

    // Method to register an advertisement click
    suspend fun registerAdvertisementClick(advertisementId: String) {
        try {
            // Check if an analytics document for this ad already exists
            val docRef = firebase.firestore.collection("advertisementClicksAnalytics").document(advertisementId)
            val docSnapshot = docRef.get().await()

            if (docSnapshot.exists()) {
                // Increment the click count
                docRef.update("clicks", FieldValue.increment(1)).await()
            } else {
                // Create a new document with the initial click count
                val analyticsData = hashMapOf(
                    "advertisementId" to advertisementId,
                    "clicks" to 1
                )
                docRef.set(analyticsData).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Method to get the click count for a specific advertisement
    suspend fun getAdvertisementClickCount(advertisementId: String): Int {
        return try {
            val docSnapshot = firebase.firestore.collection("advertisementClicksAnalytics")
                .document(advertisementId)
                .get()
                .await()

            docSnapshot.getLong("clicks")?.toInt() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
