package com.mobiles.senecard.model

import android.net.Uri
import com.google.firebase.storage.UploadTask
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

            val uploadTask: UploadTask = imageRef.putFile(image)
            val uploadTaskSnapshot = uploadTask.await()

            val downloadUrl = uploadTaskSnapshot.storage.downloadUrl.await()

            val store = hashMapOf(
                "address" to address,
                "category" to category,
                "image" to downloadUrl.toString(),
                "name" to name,
                "schedule" to schedule,
                "userId" to userId
            )

            firebase.firestore.collection("stores").add(store).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}