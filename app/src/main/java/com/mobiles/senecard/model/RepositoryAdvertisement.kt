package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.tasks.await

class RepositoryAdvertisement private constructor() {

    private val firebase = FirebaseClient.instance
    private val repositoryStore = RepositoryStore.instance

    companion object {
        val instance: RepositoryAdvertisement by lazy { RepositoryAdvertisement() }
    }

    suspend fun getAllAdvertisement(): List<Advertisement> {
        val advertisementsList = mutableListOf<Advertisement>()
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
        return advertisementsList
    }
}