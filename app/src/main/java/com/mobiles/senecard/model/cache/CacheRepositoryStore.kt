package com.mobiles.senecard.model.cache

import android.net.Uri
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.UUID

sealed class StoreResult {
    data class Success(val stores: List<Store>, val isFromCache: Boolean) : StoreResult()
    data class Failure(val error: String) : StoreResult()
}

class CacheRepositoryStore private constructor() {

    private val firebase = FirebaseClient.instance
    private val storeCache = CacheManager<String, Store>(expiryDuration = 60, maxSize = 1)

    companion object {
        val instance: CacheRepositoryStore by lazy { CacheRepositoryStore() }
    }

    suspend fun getStoreByBusinessOwnerId(businessOwnerId: String): StoreResult {
        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("stores")
                    .whereEqualTo("businessOwnerId", businessOwnerId)
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val store = documentSnapshot.toObject<Store>()?.copy(id = documentSnapshot.id)

                    store?.let {
                        storeCache.put(it.id!!, it) // Cache the fetched store
                    }

                    return store?.let { StoreResult.Success(listOf(it), isFromCache = false) }
                        ?: StoreResult.Failure("No store found for Business Owner ID: $businessOwnerId")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to cache if network fetch fails
        storeCache.getAll()?.let { cachedStores ->
            val cachedStore = cachedStores.find { it.businessOwnerId == businessOwnerId }
            if (cachedStore != null) {
                return StoreResult.Success(listOf(cachedStore), isFromCache = true)
            }
        }

        return StoreResult.Failure("Failed to fetch store for Business Owner ID: $businessOwnerId from both network and cache.")
    }

    suspend fun updateStore(store: Store): StoreResult {
        try {
            val storeId = store.id ?: return StoreResult.Failure("Store ID cannot be null.")

            // Prepare the fields to update
            val storeMap = mapOf(
                "name" to store.name,
                "address" to store.address,
                "category" to store.category,
                "image" to store.image,
                "schedule" to store.schedule,
                "rating" to store.rating
            )

            // Ensure the network is available
            if (!NetworkUtils.isInternetAvailable()) {
                return StoreResult.Failure("No internet connection. Update requires online access.")
            }

            // Update Firestore
            firebase.firestore.collection("stores")
                .document(storeId)
                .update(storeMap)
                .await()

            // Update cache only if Firestore update succeeds
            storeCache.put(storeId, store)
            return StoreResult.Success(listOf(store), isFromCache = false)
        } catch (e: Exception) {
            e.printStackTrace()
            return StoreResult.Failure("Failed to update store: ${e.message}")
        }
    }


    suspend fun uploadImage(imageUri: Uri): String {
        try {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = firebase.storage.child("stores_images/$imageName")

            val uploadTask = imageRef.putFile(imageUri).await()
            return uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}