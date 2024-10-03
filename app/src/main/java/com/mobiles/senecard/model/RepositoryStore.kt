package com.mobiles.senecard.model

import android.net.Uri
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.UploadTask
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RepositoryStore private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryStore by lazy { RepositoryStore() }
    }

    suspend fun getAllStores(): List<Store> {
        val storesList = mutableListOf<Store>()
        try {
            val querySnapshot = firebase.firestore.collection("stores").get().await()

            for (document in querySnapshot.documents) {
                val store = document.toObject<Store>()?.copy(id = document.id)
                store.let {
                    if (it != null) {
                        storesList.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return storesList
    }

    suspend fun getStore(storeId: String): Store? {
        return try {
            val document = firebase.firestore.collection("stores").document(storeId).get().await()
            document.toObject<Store>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun addStore(
        businessOwnerId: String,
        name: String,
        address: String,
        category: String,
        image: Uri,
        schedule: Map<String, List<Int>>
    ): Boolean {
        try {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = firebase.storage.child("stores_images/$imageName")

            val uploadTask: UploadTask = imageRef.putFile(image)
            val uploadTaskSnapshot = uploadTask.await()

            val downloadUrl = uploadTaskSnapshot.storage.downloadUrl.await()

            val store = hashMapOf(
                "businessOwnerId" to businessOwnerId,
                "address" to address,
                "category" to category,
                "image" to downloadUrl.toString(),
                "name" to name,
                "schedule" to schedule,
                "rating" to null
            )

            firebase.firestore.collection("stores").add(store).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}