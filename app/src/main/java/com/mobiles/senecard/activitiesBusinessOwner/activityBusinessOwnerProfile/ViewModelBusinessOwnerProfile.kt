package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.entities.User
import com.mobiles.senecard.model.RepositoryAuthentication
import kotlinx.coroutines.launch

class  ViewModelBusinessOwnerProfile : ViewModel() {

    // Reference the singleton instance of RepositoryAuthentication
    private val repositoryAuthentication = RepositoryAuthentication.instance

    // LiveData to hold the user information
    private val _isUser = MutableLiveData<User?>()
    val isUser: LiveData<User?> get() = _isUser

    // LiveData for navigation destination
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    // Function to retrieve information like user and recommended items
    fun getInformation() {
        viewModelScope.launch {
            _isUser.value = repositoryAuthentication.getCurrentUser()
            _isUser.value?.id?.let { userId ->
            }
        }
    }

    // Navigation methods

    // Function to reset the navigation event after it's handled
    fun onNavigationHandled() {
        _navigateTo.value = null
    }

}
