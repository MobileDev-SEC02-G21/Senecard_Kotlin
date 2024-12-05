package com.mobiles.senecard.model.cache

import android.util.Log
import com.google.firebase.firestore.toObject
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.FirebaseClient
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.tasks.await

sealed class UserResult {
    data class Success(val user: User, val isFromCache: Boolean) : UserResult()
    data class Failure(val error: String) : UserResult()
}

class CacheRepositoryUser private constructor() {

    private val firebase = FirebaseClient.instance

    // CacheManager instance for caching User entities
    private val userCache = CacheManager<String, User>(expiryDuration = 60, maxSize = 1)

    companion object {
        val instance: CacheRepositoryUser by lazy { CacheRepositoryUser() }
    }

    suspend fun addUser(name: String, email: String, phone: String, role: String): UserResult {
        return try {
            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "role" to role
            )

            val documentReference = firebase.firestore.collection("users").add(user).await()

            // Construct and cache the newly created user
            val newUser = User(
                id = documentReference.id,
                name = name,
                email = email,
                phone = phone,
                role = role
            )

            try {
                userCache.put(documentReference.id, newUser)
            } catch (e: Exception) {
                Log.e("RepositoryUser", "Failed to cache user: ${e.message}")
                return UserResult.Success(newUser, isFromCache = false) // Cache update failure
            }

            UserResult.Success(newUser, isFromCache = false)
        } catch (e: Exception) {
            Log.e("RepositoryUser", "Error adding user to Firebase: ${e.message}")
            UserResult.Failure("Error adding user to Firebase: ${e.message}")
        }
    }

    suspend fun getUserById(userId: String): UserResult {
        return try {
            Log.d("RepositoryUser", "Fetching user from Firestore with ID: $userId")
            val documentSnapshot = firebase.firestore.collection("users").document(userId).get().await()

            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)

                // Cache the fetched user
                user?.let {
                    try {
                        userCache.put(userId, it)
                    } catch (e: Exception) {
                        Log.e("RepositoryUser", "Failed to cache user: ${e.message}")
                        return UserResult.Success(it, isFromCache = false) // Cache update failure
                    }
                }

                UserResult.Success(user!!, isFromCache = false)
            } else {
                Log.e("RepositoryUser", "No document found for User ID: $userId")
                fetchFromCache(userId)
            }
        } catch (e: Exception) {
            Log.e("RepositoryUser", "Error fetching user from Firebase: ${e.message}")
            fetchFromCache(userId)
        }
    }

    suspend fun getUserByEmail(email: String): UserResult {
        return try {
            Log.d("RepositoryUser", "Fetching user from Firestore by email: $email")
            val querySnapshot = firebase.firestore.collection("users").whereEqualTo("email", email).get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                val documentSnapshot = querySnapshot.documents[0]
                val user = documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)

                // Cache the fetched user
                user?.let {
                    try {
                        userCache.put(it.id ?: "", it)
                    } catch (e: Exception) {
                        Log.e("RepositoryUser", "Failed to cache user: ${e.message}")
                        return UserResult.Success(it, isFromCache = false) // Cache update failure
                    }
                }

                UserResult.Success(user!!, isFromCache = false)
            } else {
                Log.e("RepositoryUser", "No user found with email: $email")
                fetchFromCacheByEmail(email)
            }
        } catch (e: Exception) {
            Log.e("RepositoryUser", "Error fetching user by email: ${e.message}")
            fetchFromCacheByEmail(email)
        }
    }

    private fun fetchFromCache(userId: String): UserResult {
        val cachedUser = userCache.get(userId)
        return if (cachedUser != null) {
            Log.d("RepositoryUser", "User found in cache: $cachedUser")
            UserResult.Success(cachedUser, isFromCache = true)
        } else {
            Log.e("RepositoryUser", "No user found in cache for ID: $userId")
            UserResult.Failure("No user found in cache")
        }
    }

    private fun fetchFromCacheByEmail(email: String): UserResult {
        val cachedUser = userCache.get(email)
        return if (cachedUser != null && cachedUser.email == email) {
            Log.d("RepositoryUser", "User found in cache by email: $cachedUser")
            UserResult.Success(cachedUser, isFromCache = true)
        } else {
            Log.e("RepositoryUser", "No user found in cache for email: $email")
            UserResult.Failure("No user found in cache")
        }
    }

    suspend fun updateUser(user: User): UserResult {
        try {
            val userId = user.id ?: return UserResult.Failure("User ID cannot be null.")

            // Prepare the fields to update
            val userMap = mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role
            )

            // Ensure the network is available
            if (!NetworkUtils.isInternetAvailable()) {
                return UserResult.Failure("No internet connection. Update requires online access.")
            }

            // Update Firestore
            firebase.firestore.collection("users")
                .document(userId)
                .update(userMap)
                .await()

            // Update cache only if Firestore update succeeds
            userCache.put(userId, user)
            return UserResult.Success(user, isFromCache = false)
        } catch (e: Exception) {
            e.printStackTrace()
            return UserResult.Failure("Failed to update user: ${e.message}")
        }
    }


    suspend fun existsUserByEmail(email: String): UserResult {
        return try {
            Log.d("RepositoryUser", "Checking Firestore for existence of user with email: $email")
            val querySnapshot = firebase.firestore.collection("users").whereEqualTo("email", email).get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                val documentSnapshot = querySnapshot.documents[0]
                val user = documentSnapshot.toObject<User>()?.copy(id = documentSnapshot.id)

                UserResult.Success(user!!, isFromCache = false)
            } else {
                UserResult.Failure("No user found with email: $email")
            }
        } catch (e: Exception) {
            Log.e("RepositoryUser", "Error checking existence of user by email: ${e.message}")
            UserResult.Failure("Error checking user existence: ${e.message}")
        }
    }
}
