package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditProfile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.NetworkUtils
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

    private val _isOffline = MutableLiveData<Boolean>()
    val isOffline: LiveData<Boolean> get() = _isOffline

    fun fetchProfileData() {
        viewModelScope.launch {
            try {
                val isOnline = NetworkUtils.isInternetAvailable()
                _isOffline.postValue(!isOnline)

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
            if (!NetworkUtils.isInternetAvailable()) {
                _saveStatus.postValue(false)
                return@launch
            }

            try {
                val currentUser = _user.value
                val updatedUser = User(
                    id = currentUser?.id ?: user.id,
                    name = user.name,
                    email = currentUser?.email, // Preserve existing email
                    phone = user.phone,
                    role = currentUser?.role ?: user.role
                )

                val currentStore = _store.value
                val updatedStore = Store(
                    id = currentStore?.id ?: store.id,
                    businessOwnerId = currentStore?.businessOwnerId,
                    name = store.name,
                    address = store.address,
                    category = currentStore?.category,
                    image = currentStore?.image,
                    schedule = currentStore?.schedule,
                    rating = currentStore?.rating
                )

                if (imageUri != null) {
                    val imageUrl = repositoryStore.uploadImage(imageUri)
                    updatedStore.image = imageUrl
                }

                val userUpdateDeferred = async { repositoryUser.updateUser(updatedUser) }
                val storeUpdateDeferred = async { repositoryStore.updateStore(updatedStore) }

                val (userUpdateResult, storeUpdateResult) = listOf(userUpdateDeferred, storeUpdateDeferred).awaitAll()

                val success = userUpdateResult == true && storeUpdateResult == true
                _saveStatus.postValue(success)
            } catch (e: Exception) {
                e.printStackTrace()
                _saveStatus.postValue(false)
            }
        }
    }

}
