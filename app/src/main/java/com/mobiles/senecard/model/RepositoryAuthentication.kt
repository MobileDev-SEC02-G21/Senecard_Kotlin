package com.mobiles.senecard.model

import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.tasks.await

class RepositoryAuthentication private constructor() {

    private val firebase = FirebaseClient.instance
    private val repositoryUser = RepositoryUser.instance
    private var currentUser: User? = null

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

    suspend fun getCurrentUser(): User? {
        if (currentUser == null) {
            val firebaseUser = firebase.auth.currentUser
            if (firebaseUser != null) {
                currentUser = repositoryUser.getUserByEmail(firebaseUser.email!!)
            }
        }
        return currentUser
    }

    fun logOut() {
        currentUser = null
        firebase.auth.signOut()
    }
}