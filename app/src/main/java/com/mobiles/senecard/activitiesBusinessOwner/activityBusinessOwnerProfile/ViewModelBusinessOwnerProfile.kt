package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.cache.CacheRepositoryUser
import com.mobiles.senecard.model.cache.CacheRepositoryStore
import com.mobiles.senecard.model.cache.StoreResult
import com.mobiles.senecard.model.cache.UserResult
import com.mobiles.senecard.model.entities.Store
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerProfile : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val cacheRepositoryUser = CacheRepositoryUser.instance
    private val repositoryStore = CacheRepositoryStore.instance

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _store = MutableLiveData<Store?>()
    val store: LiveData<Store?> get() = _store

    private val _isOffline = MutableLiveData<Boolean>()
    val isOffline: LiveData<Boolean> get() = _isOffline

    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    fun fetchProfileData() {
        viewModelScope.launch {
            try {
                // Fetch user ID from the authentication repository
                val currentUser = repositoryAuthentication.getCurrentUser()
                val userId = currentUser?.id

                if (userId != null) {
                    // Fetch user data from CacheRepositoryUser
                    val userDeferred = async { cacheRepositoryUser.getUserById(userId) }
                    val storeDeferred = async { repositoryStore.getStoreByBusinessOwnerId(userId) }

                    val (userResult, storeResult) = listOf(userDeferred, storeDeferred).awaitAll()

                    // Handle user data
                    when (userResult) {
                        is UserResult.Success -> {
                            _user.postValue(userResult.user)
                            _isOffline.postValue(userResult.isFromCache)
                        }
                        is UserResult.Failure -> {
                            _user.postValue(null)
                            _isOffline.postValue(true)
                        }
                    }

                    // Handle store data
                    when (storeResult) {
                        is StoreResult.Success -> _store.postValue(storeResult.stores.firstOrNull())
                        is StoreResult.Failure -> _store.postValue(null)
                    }
                } else {
                    _user.postValue(null)
                    _store.postValue(null)
                    _isOffline.postValue(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _user.postValue(null)
                _store.postValue(null)
                _isOffline.postValue(true)
            }
        }
    }

    fun onEditButtonClicked() {
        if (_isOffline.value == true) {
            _navigateTo.postValue(NavigationDestination.OFFLINE_WARNING)
        } else {
            _navigateTo.postValue(NavigationDestination.EDIT_PROFILE)
        }
    }

    fun onBackButtonClicked() {
        _navigateTo.postValue(NavigationDestination.LANDING_PAGE)
    }

    fun onNavigationHandled() {
        _navigateTo.value = null
    }
}
