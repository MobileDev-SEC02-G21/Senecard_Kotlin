package com.mobiles.senecard.model.cache

import android.net.Uri
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.UUID

sealed class AdvertisementResult {
    data class Success(val advertisements: List<Advertisement>, val isFromCache: Boolean) : AdvertisementResult()
    data class Failure(val error: String) : AdvertisementResult()
}

class CacheRepositoryAdvertisement private constructor() {

    private val firebase = FirebaseClient.instance
    private val advertisementCache = CacheManager<String, Advertisement>(expiryDuration = 60, maxSize =100)

    companion object {
        val instance: CacheRepositoryAdvertisement by lazy { CacheRepositoryAdvertisement() }
    }

    // Add Advertisement
    suspend fun addAdvertisement(
        storeId: String,
        title: String,
        description: String,
        image: Uri,
        startDate: String,
        endDate: String,
        available: Boolean
    ): Boolean {
        return try {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = firebase.storage.child("advertisements_images/$imageName")

            val uploadTask = imageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            val advertisement = hashMapOf(
                "storeId" to storeId,
                "title" to title,
                "description" to description,
                "image" to downloadUrl.toString(),
                "startDate" to startDate,
                "endDate" to endDate,
                "available" to available
            )

            val documentReference = firebase.firestore.collection("advertisements").add(advertisement).await()

            // Cache the advertisement
            advertisementCache.put(documentReference.id, Advertisement(
                id = documentReference.id,
                storeId = storeId,
                title = title,
                description = description,
                image = downloadUrl.toString(),
                startDate = startDate,
                endDate = endDate,
                available = available
            ))

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAdvertisement(advertisementId: String): AdvertisementResult {
        return try {
            if (NetworkUtils.isInternetAvailable()) {
                // Attempt to delete from Firestore
                firebase.firestore.collection("advertisements").document(advertisementId).delete().await()
            } else {
                return AdvertisementResult.Failure("No internet connection. Unable to delete advertisement.")
            }

            // Remove from cache after successful deletion
            advertisementCache.invalidate(advertisementId)
            AdvertisementResult.Success(emptyList(), isFromCache = false)
        } catch (e: Exception) {
            e.printStackTrace()
            AdvertisementResult.Failure("Failed to delete advertisement with ID: $advertisementId. Error: ${e.message}")
        }
    }

    // Get Advertisement By ID
    suspend fun getAdvertisementById(id: String): AdvertisementResult {
        if (NetworkUtils.isInternetAvailable()) {
            try {
                val documentSnapshot = firebase.firestore.collection("advertisements").document(id).get(Source.SERVER).await()
                documentSnapshot.toObject<Advertisement>()?.copy(id = documentSnapshot.id)?.let { advertisement ->
                    advertisementCache.put(advertisement.id!!, advertisement) // Cache the advertisement
                    return AdvertisementResult.Success(listOf(advertisement), isFromCache = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to cache
        advertisementCache.get(id)?.let {
            return AdvertisementResult.Success(listOf(it), isFromCache = true)
        }

        return AdvertisementResult.Failure("Failed to fetch advertisement with ID: $id from both network and cache.")
    }

    // Get Advertisements By Store ID
    suspend fun getAdvertisementsByStoreId(storeId: String): AdvertisementResult {
        val advertisementsList = mutableListOf<Advertisement>()

        if (NetworkUtils.isInternetAvailable()) {
            try {
                val querySnapshot = firebase.firestore.collection("advertisements").whereEqualTo("storeId", storeId).get(Source.SERVER).await()
                querySnapshot.documents.forEach { documentSnapshot ->
                    documentSnapshot.toObject<Advertisement>()?.copy(id = documentSnapshot.id)?.let { advertisement ->
                        advertisementsList.add(advertisement)
                        advertisementCache.put(advertisement.id!!, advertisement) // Cache the advertisement
                    }
                }
                return AdvertisementResult.Success(advertisementsList, isFromCache = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to cache
        advertisementCache.getAll()?.let {
            advertisementsList.addAll(it.filter { ad -> ad.storeId == storeId })
        }

        return if (advertisementsList.isNotEmpty()) {
            AdvertisementResult.Success(advertisementsList, isFromCache = true)
        } else {
            AdvertisementResult.Failure("Failed to fetch advertisements for Store ID: $storeId from both network and cache.")
        }
    }
}
