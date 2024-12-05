package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.RepositoryUser
import com.mobiles.senecard.model.entities.Store
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerEditProfile : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryUser = RepositoryUser.instance
    private val repositoryStore = RepositoryStore.instance

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _store = MutableLiveData<Store?>()
    val store: LiveData<Store?> get() = _store

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> get() = _saveStatus

    fun fetchProfileData() {
        viewModelScope.launch {
            try {
                // Fetch user and store data concurrently
                val userDeferred = async { repositoryAuthentication.getCurrentUser() }
                val storeDeferred = async {
                    userDeferred.await()?.id?.let { repositoryStore.getStoreByBusinessOwnerId(it) }
                }

                // Wait for both operations to complete
                val (currentUser, currentStore) = listOf(userDeferred, storeDeferred).awaitAll()

                // Post results to LiveData
                _user.postValue(currentUser as? User)
                _store.postValue(currentStore as? Store)
            } catch (e: Exception) {
                e.printStackTrace()
                _user.postValue(null)
                _store.postValue(null)
            }
        }
    }

    fun saveProfileData(user: User, store: Store, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Starting saveProfileData process.")

                // Retrieve the full current user and store for missing fields
                val currentUser = _user.value
                val currentStore = _store.value

                // Preserve non-changing fields for user
                val updatedUser = User(
                    id = currentUser?.id ?: user.id, // Ensure the ID is set
                    name = user.name,
                    email = user.email,
                    phone = user.phone,
                    role = currentUser?.role // Preserve the role
                )

                // Preserve non-changing fields for store
                val updatedStore = Store(
                    id = currentStore?.id ?: store.id, // Ensure the ID is set
                    businessOwnerId = currentStore?.businessOwnerId, // Preserve the business owner ID
                    name = store.name,
                    address = store.address,
                    category = currentStore?.category, // Preserve the category
                    image = currentStore?.image, // Preserve the existing image if no new one is provided
                    schedule = currentStore?.schedule, // Preserve the schedule
                    rating = currentStore?.rating // Preserve the rating
                )

                // Concurrently handle user and store updates
                val userUpdateDeferred = async {
                    Log.d("ViewModel", "Updating user data: $updatedUser")
                    repositoryUser.updateUser(updatedUser).also {
                        Log.d("ViewModel", "User update result: $it")
                    }
                }

                val storeUpdateDeferred = async {
                    Log.d("ViewModel", "Updating store data: $updatedStore")
                    if (imageUri != null) {
                        Log.d("ViewModel", "Uploading new image: $imageUri")
                        val imageUrl = repositoryStore.uploadImage(imageUri)
                        updatedStore.image = imageUrl
                        Log.d("ViewModel", "Image uploaded. URL: $imageUrl")
                    }
                    repositoryStore.updateStore(updatedStore).also {
                        Log.d("ViewModel", "Store update result: $it")
                    }
                }

                // Wait for both operations to complete
                val (userUpdateResult, storeUpdateResult) = listOf(userUpdateDeferred, storeUpdateDeferred).awaitAll()

                // Post success status if both updates were successful
                val success = userUpdateResult == true && storeUpdateResult == true
                _saveStatus.postValue(success)

                if (success) {
                    Log.d("ViewModel", "Profile save successful.")
                } else {
                    Log.e("ViewModel", "Profile save failed.")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error during saveProfileData: ${e.message}", e)
                _saveStatus.postValue(false)
            }
        }
    }
}
