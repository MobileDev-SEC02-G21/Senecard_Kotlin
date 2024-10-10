package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.FirebaseFirestore
import com.mobiles.senecard.model.entities.Advertisement
import com.mobiles.senecard.model.entities.Advertisement2
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale

class RepositoryAdvertisement private constructor() {

    private val firebase = FirebaseClient.instance
    private val repositoryStore = RepositoryStore.instance

    companion object {
        val instance: RepositoryAdvertisement by lazy { RepositoryAdvertisement() }
    }

    suspend fun getAllAdvertisement(): List<Advertisement> {
        var advertisementsList = mutableListOf<Advertisement>()
        try {
            val querySnapshot = firebase.firestore.collection("advertisements").get().await()

            for (document in querySnapshot.documents) {
                val advertisement = document.toObject<Advertisement>()?.copy(id = document.id)

                advertisement?.let {
                    val storeId = document.getString("storeId")
                    storeId?.let { id ->
                        val store = repositoryStore.getStore(id)
                        advertisementsList.add(it.copy(store = store))
                    } ?: run {
                        advertisementsList.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        advertisementsList = advertisementsList.sortedBy { advertisement ->
            if (isAdvertisementStoreClosed(advertisement)) 1 else 0
        }.toMutableList()

        return advertisementsList
    }

    suspend fun addAdvertisement(
        available: Boolean,
        description: String,
        endDate: String,
        imageUrl: String,
        startDate: String,
        storeId: String,
        title: String
    ): Boolean {
        try {
            val advertisement = hashMapOf(
                "available" to available,
                "description" to description,
                "endDate" to endDate,
                "image" to imageUrl,
                "startDate" to startDate,
                "storeId" to storeId,
                "title" to title
            )

            // Let Firestore generate the ID
            firebase.firestore.collection("advertisements").add(advertisement).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getAdvertisementByIdJeff(advertisementId: String): Advertisement? {
        var advertisement: Advertisement? = null
        try {
            val documentSnapshot = firebase.firestore.collection("advertisements").document(advertisementId).get().await()

            documentSnapshot.toObject<Advertisement>()?.let { adv ->
                advertisement = adv.copy(id = documentSnapshot.id)

                val storeId = documentSnapshot.getString("storeId")

                storeId?.let { id ->
                    val store = repositoryStore.getStore(id)
                    advertisement = advertisement?.copy(store = store)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return advertisement
    }

    suspend fun getAdvertisementById(id: String): Advertisement? {
        try {
            val document = firebase.firestore.collection("advertisements").document(id).get().await()
            return if (document.exists()) {
                document.toObject(Advertisement::class.java)
            } else null
        } catch (e: Exception) {
            return null
        }
    }

    // Fetch advertisements by storeId (new method)
    suspend fun getAdvertisementsByStoreId(storeId: String): List<Advertisement2> {
        try {
            val querySnapshot = firebase.firestore.collection("advertisements")
                .whereEqualTo("storeId", storeId).get().await()

            val advertisements = mutableListOf<Advertisement2>()

            for (document in querySnapshot.documents) {
                val advertisement = Advertisement2(
                    id = document.id,
                    available = document.getBoolean("available") ?: false,
                    description = document.getString("description")!!,
                    endDate = document.getString("endDate")!!,
                    image = document.getString("image")!!,
                    startDate = document.getString("startDate")!!,
                    store = storeId,
                    title = document.getString("title")!!
                )
                advertisements.add(advertisement)
            }

            return advertisements
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun updateAdvertisement(id: String, updatedFields: Map<String, Any>): Boolean {
        try {
            firebase.firestore.collection("advertisements").document(id).update(updatedFields).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun isAdvertisementStoreClosed(advertisement: Advertisement): Boolean {

        val store = advertisement.store

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