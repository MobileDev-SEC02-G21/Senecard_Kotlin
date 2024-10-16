package com.mobiles.senecard.model

import com.google.firebase.firestore.toObject
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.tasks.await

class RepositoryUser private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryUser by lazy { RepositoryUser() }
    }

    suspend fun addUser(name: String, email: String, phone: String, role: String): Boolean {
        try {
            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "role" to role
            )

            firebase.firestore.collection("users").add(user).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getUserById(userId: String): User? {
        try {
            val documentSnapshot = firebase.firestore.collection("users").document(userId).get().await()
            return documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        try {
            val querySnapshot = firebase.firestore.collection("users").whereEqualTo("email", email).get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                val documentSnapshot = querySnapshot.documents[0]
                return documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun existsUserByEmail(email: String): Boolean? {
        try {
            val querySnapshot = firebase.firestore.collection("users").whereEqualTo("email", email).get().await()
            return querySnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            return null
        }
    }
}
