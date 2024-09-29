package com.mobiles.senecard.model

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class RepositoryAuthentication private constructor() {

    private val firebase = FirebaseClient.instance

    companion object {
        val instance: RepositoryAuthentication by lazy { RepositoryAuthentication() }
    }

    suspend fun createUser(email: String, password: String): Boolean {
        try {
            firebase.auth.createUserWithEmailAndPassword(email, password).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun authenticateUser(email: String, password: String): Boolean {
        try {
            firebase.auth.signInWithEmailAndPassword(email, password).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebase.auth.currentUser
    }

    fun logOut() {
        firebase.auth.signOut()
    }
}