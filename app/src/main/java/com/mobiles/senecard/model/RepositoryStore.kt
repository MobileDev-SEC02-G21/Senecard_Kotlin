package com.mobiles.senecard.model

import android.net.Uri
import com.google.firebase.storage.UploadTask
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RepositoryStore private constructor() {
    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryStore by lazy { RepositoryStore() }
    }

    suspend fun addStore(storeId: String, address: String, category: String, image: Uri, name: String, rating: Double, schedule: Map<String, List<Int>>, businessOwnerId: String): Boolean {
        try {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = firebase.storage.child("stores_images/$imageName")
            val uploadTaskSnapshot = imageRef.putFile(image).await()
            val downloadUrl = uploadTaskSnapshot.storage.downloadUrl.await()

            val store = hashMapOf(
                "address" to address,
                "category" to category,
                "image" to downloadUrl.toString(),
                "name" to name,
                "rating" to rating,
                "schedule" to schedule,
                "businessOwnerId" to businessOwnerId
            )

            firebase.firestore.collection("stores").document(storeId).set(store).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getStoreById(storeId: String): Store? {
        try {
            val documentSnapshot = firebase.firestore.collection("stores").document(storeId).get().await()
            return documentSnapshot.toObject(Store::class.java)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun updateStore(storeId: String, updatedFields: Map<String, Any>): Boolean {
        return try {
            firebase.firestore.collection("stores").document(storeId).update(updatedFields).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

