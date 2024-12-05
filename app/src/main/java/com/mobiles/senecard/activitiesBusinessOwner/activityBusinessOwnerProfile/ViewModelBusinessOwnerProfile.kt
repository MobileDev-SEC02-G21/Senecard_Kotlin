package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Store
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerProfile : ViewModel() {

    // Repositories
    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryStore = RepositoryStore.instance

    // LiveData for user and store information
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _store = MutableLiveData<Store?>()
    val store: LiveData<Store?> get() = _store

    // LiveData for navigation destination
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    // Function to fetch user and store data concurrently
    fun fetchProfileData() {
        viewModelScope.launch {
            try {
                // Concurrently fetch user and store data
                val userDeferred = async { repositoryAuthentication.getCurrentUser() }
                val storeDeferred = async {
                    val user = repositoryAuthentication.getCurrentUser()
                    user?.id?.let { repositoryStore.getStoreByBusinessOwnerId(it) }
                }

                // Wait for both operations to complete
                val (user, store) = listOf(userDeferred, storeDeferred).awaitAll()

                // Post results to LiveData
                _user.postValue(user as? User)
                _store.postValue(store as? Store)
            } catch (e: Exception) {
                // Handle any errors gracefully (e.g., logging or setting nulls)
                _user.postValue(null)
                _store.postValue(null)
            }
        }
    }

    // Function called when the Edit button is clicked
    fun onEditButtonClicked() {
        _navigateTo.value = NavigationDestination.EDIT_PROFILE
    }

    // Function called when the Back button is clicked
    fun onBackButtonClicked() {
        _navigateTo.value = NavigationDestination.LANDING_PAGE
    }

    // Function to reset the navigation event after it's handled
    fun onNavigationHandled() {
        _navigateTo.value = null
    }
}
