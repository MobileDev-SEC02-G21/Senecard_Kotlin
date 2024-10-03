package com.mobiles.senecard.model

import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.tasks.await

class RepositoryUser private constructor() {
    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryUser by lazy { RepositoryUser() }
    }

    suspend fun addUser(
        name: String,
        email: String,
        phone: String,
        qrCode: String,
        role: String
    ): Boolean {
        try {
            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "qr_code" to qrCode,
                "role" to role
            )

            // Let Firestore generate the ID
            firebase.firestore.collection("users").add(user).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }


    suspend fun getUserById(userId: String): User? {
        try {
            val documentSnapshot = firebase.firestore.collection("users").document(userId).get().await()
            return documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getUserByQRCode(qrCode: String): User? {
        try {
            val querySnapshot = firebase.firestore
                .collection("users")
                .whereEqualTo("qr_code", qrCode)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents[0]
                return User(
                    userId = document.id,
                    email = document.getString("email")!!,
                    name = document.getString("name")!!,
                    phone = document.getString("phone")!!,
                    qrCode = document.getString("qr_code")!!,
                    role = document.getString("role")!!
                )
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }


    suspend fun getUser(email: String): User? {
        try {
            val querySnapshot = firebase.firestore.collection("users").whereEqualTo("email", email).get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents[0]
                val user = User(
                    userId = document.id,
                    email = document.getString("email")!!,
                    name = document.getString("name")!!,
                    phone = document.getString("phone")!!,
                    role = document.getString("role")!!
                )
                return user
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun updateUser(userId: String, updatedFields: Map<String, Any>): Boolean {
        return try {
            firebase.firestore.collection("users").document(userId).update(updatedFields).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun existsUser(email: String): Boolean? {
        try {
            val querySnapshot = firebase.firestore.collection("users").whereEqualTo("email", email).get().await()
            return querySnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            return null
        }
    }
}
