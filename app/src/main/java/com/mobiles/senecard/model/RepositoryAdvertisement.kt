package com.mobiles.senecard.model

import android.net.Uri
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RepositoryAdvertisement private constructor() {

    private val firebase = FirebaseClient.instance
    private val repositoryStore = RepositoryStore.instance
    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance

    companion object {
        val instance: RepositoryAdvertisement by lazy { RepositoryAdvertisement() }
    }

    suspend fun addAdvertisement(storeId: String, title: String, description: String, image: Uri, startDate: String, endDate: String, available: Boolean): Boolean {
        try {
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

            firebase.firestore.collection("advertisements").add(advertisement).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getAllAdvertisements(): List<Advertisement> {
        val advertisementsList = mutableListOf<Advertisement>()
        try {
            val querySnapshot = firebase.firestore.collection("advertisements").get().await()

            for (documentSnapshot in querySnapshot.documents) {
                val advertisement = documentSnapshot.toObject<Advertisement>()?.copy(id = documentSnapshot.id)
                if (advertisement != null) {
                    advertisementsList.add(advertisement)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val advertisementsWithStatus = advertisementsList.map { advertisement ->
            val isClosed = isAdvertisementStoreClosed(advertisement)
            Pair(advertisement, isClosed)
        }

        val sortedAdvertisements = advertisementsWithStatus
            .sortedBy { pair -> if (pair.second) 1 else 0 }
            .map { it.first }

        return sortedAdvertisements
    }

    suspend fun getRecommendedAdvertisementsByUniandesMemberId(uniandesMemberId: String): List<Advertisement> {
        // Get the list of all advertisements and member loyalty cards
        val advertisementsList = getAllAdvertisements()
        val loyaltyCardList = repositoryLoyaltyCard.getLoyaltyCardsByUniandesMemberId(uniandesMemberId)
        val storesList = repositoryStore.getAllStores()

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

        // Filter out ads that belong to non-purchased stores and are in frequent categories
        val recommendedAdvertisements = advertisementsList
            .filter { advertisement ->
                val store = storesList.find { it.id == advertisement.storeId }
                store != null &&
                        advertisement.id != null &&
                        store.id !in storeIdsWithPurchases &&
                        store.category != null &&
                        !isAdvertisementStoreClosed(advertisement)
            }
            .sortedWith(compareByDescending { advertisement ->
                val store = storesList.find { it.id == advertisement.storeId }
                frequentCategories.indexOf(store?.category).takeIf { it >= 0 } ?: Int.MAX_VALUE
            })

        return recommendedAdvertisements.take(2)
    }

    suspend fun getAdvertisementById(id: String): Advertisement? {
        try {
            val documentSnapshot = firebase.firestore.collection("advertisements").document(id).get().await()
            val advertisement = documentSnapshot.toObject<Advertisement>()?.copy(id = documentSnapshot.id)

            // Register a click for this advertisement analytics
            RepositoryAdvertisementAnalytics.instance.registerAdvertisementClick(id)

            return advertisement
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getAdvertisementsByStoreId(storeId: String): List<Advertisement> {
        val advertisementsList = mutableListOf<Advertisement>()
        try {
            val querySnapshot = firebase.firestore.collection("advertisements").whereEqualTo("storeId", storeId).get().await()

            for (documentSnapshot in querySnapshot.documents) {
                val advertisement = documentSnapshot.toObject<Advertisement>()?.copy(id = documentSnapshot.id)
                if (advertisement != null) {
                    advertisementsList.add(advertisement)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val advertisementsWithStatus = advertisementsList.map { advertisement ->
            val isClosed = isAdvertisementStoreClosed(advertisement)
            Pair(advertisement, isClosed)
        }

        val sortedAdvertisements = advertisementsWithStatus
            .sortedBy { pair -> if (pair.second) 1 else 0 }
            .map { it.first }

        return sortedAdvertisements
    }

    suspend fun isAdvertisementStoreClosed(advertisement: Advertisement): Boolean {

        val store = repositoryStore.getStoreById(advertisement.storeId!!)

        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)?.lowercase()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val schedule = store?.schedule

        if (schedule != null && currentDayOfWeek != null && schedule.containsKey(currentDayOfWeek)) {
            val hours = schedule[currentDayOfWeek] ?: return true

            val openingHour = hours[0]
            val closingHour = hours[1]

            return currentHour < openingHour || currentHour > closingHour
        }

        return true
    }
}