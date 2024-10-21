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
    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance

    companion object {
        val instance: RepositoryStore by lazy { RepositoryStore() }
    }

    suspend fun addStore(businessOwnerId: String, name: String, address: String, category: String, image: Uri, schedule: Map<String, List<Int>>): Boolean {
        try {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = firebase.storage.child("stores_images/$imageName")

            val uploadTask = imageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            val store = hashMapOf(
                "businessOwnerId" to businessOwnerId,
                "name" to name,
                "address" to address,
                "category" to category,
                "image" to downloadUrl.toString(),
                "schedule" to schedule,
                "rating" to null
            )

            firebase.firestore.collection("stores").add(store).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getAllStores(): List<Store> {
        var storesList = mutableListOf<Store>()
        try {
            val querySnapshot = firebase.firestore.collection("stores")
                .get()
                .await()

            for (documentSnapshot in querySnapshot.documents) {
                val store = documentSnapshot.toObject<Store>()?.copy(id = documentSnapshot.id)
                if (store != null) {
                    storesList.add(store)
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

    suspend fun getRecommendedStoresByUniandesMemberId(uniandesMemberId: String): List<Store> {
        // Get the list of all stores and member loyalty cards
        val storesList = getAllStores()
        val loyaltyCardList = repositoryLoyaltyCard.getLoyaltyCardsByUniandesMemberId(uniandesMemberId)

        // Get the store IDs where the member has made purchases
        val storeIdsWithPurchases = loyaltyCardList.mapNotNull { it.storeId }

        // Identify the most frequent categories in member purchases
        val frequentCategories = storesList
            .filter { it.id in storeIdsWithPurchases && it.category != null }
            .groupingBy { it.category }
            .eachCount()
            .toList()
            .sortedByDescending { (_, count) -> count }
            .map { it.first }

        // Filter stores that are not on the shopping list, that are not closed, that belong to frequent categories and have good rating
        val recommendedStores = storesList
            .filter { store ->
                store.id != null &&
                        store.id !in storeIdsWithPurchases &&
                        !isStoreClosed(store) &&
                        store.category != null
            }
            .sortedWith(compareByDescending<Store> { it.rating ?: 0.0 }
                .thenComparing { store -> frequentCategories.indexOf(store.category).takeIf { it >= 0 } ?: Int.MAX_VALUE })

        return recommendedStores.take(2)
    }

    suspend fun getStoreById(storeId: String): Store? {
        try {
            val documentSnapshot = firebase.firestore.collection("stores")
                .document(storeId)
                .get()
                .await()

            return documentSnapshot.toObject<Store>()?.copy(id = documentSnapshot.id)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getStoreByBusinessOwnerId(businessOwnerId: String): Store? {
        try {
            val querySnapshot = firebase.firestore.collection("stores")
                .whereEqualTo("businessOwnerId", businessOwnerId)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val documentSnapshot = querySnapshot.documents[0]
                return documentSnapshot.toObject<Store>()?.copy(id = documentSnapshot.id)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
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

