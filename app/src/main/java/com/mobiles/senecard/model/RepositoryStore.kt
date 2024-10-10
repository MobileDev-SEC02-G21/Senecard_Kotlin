package com.mobiles.senecard.model

import android.net.Uri
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RepositoryStore private constructor() {
    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryStore by lazy { RepositoryStore() }
    }

    suspend fun getAllStores(): List<Store> {
        var storesList = mutableListOf<Store>()
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

        storesList = storesList.sortedBy { store ->
            if (isStoreClosed(store)) 1 else 0
        }.toMutableList()

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

            val uploadTask = imageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

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
                    id = document.id,
                    name = document.getString("name")!!,
                    address = document.getString("address")!!,
                    image = document.getString("image")!!,
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

    fun isStoreClosed(store: Store) : Boolean {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)?.lowercase()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val schedule = store.schedule

        if (schedule != null && currentDayOfWeek != null && schedule.containsKey(currentDayOfWeek)) {
            val hours = schedule[currentDayOfWeek] ?: return true

            val openingHour = hours[0]
            val closingHour = hours[1]

            return currentHour < openingHour || currentHour > closingHour
        }

        return true
    }
}

