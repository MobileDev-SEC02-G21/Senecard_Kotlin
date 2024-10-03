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

    suspend fun addStore(
        userId: String,
        address: String,
        category: String,
        image: Uri,
        name: String,
        schedule: Map<String, List<Int>>
    ): Boolean {
        try {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = firebase.storage.child("stores_images/$imageName")

            val uploadTask = imageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            val store = hashMapOf(
                "address" to address,
                "category" to category,
                "image" to downloadUrl.toString(),
                "name" to name,
                "schedule" to schedule,
                "userId" to userId
            )

            // Let Firestore generate the ID
            firebase.firestore.collection("stores").add(store).await()
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

    // Fetch store by userId (new method)
    suspend fun getStoreByUserId(userId: String): Store? {
        try {
            // Query Firestore to get the store associated with the userId
            val querySnapshot = firebase.firestore.collection("stores")
                .whereEqualTo("businessOwnerId", userId).get().await()

            // If we found the store, return it
            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents[0]
                return Store(
                    storeId = document.id,
                    name = document.getString("name")!!,
                    address = document.getString("address")!!,
                    imageUrl = document.getString("image")!!,
                    rating = document.getDouble("rating") ?: 0.0,
                    category = document.getString("category")!!,
                    businessOwnerId = userId
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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

