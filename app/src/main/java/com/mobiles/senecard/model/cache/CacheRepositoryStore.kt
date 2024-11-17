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

}