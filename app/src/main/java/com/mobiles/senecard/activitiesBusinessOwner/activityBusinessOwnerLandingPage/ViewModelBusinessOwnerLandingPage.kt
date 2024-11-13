package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerLandingPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.entities.User
import com.mobiles.senecard.model.RepositoryAuthentication
import kotlinx.coroutines.launch

class  ViewModelBusinessOwnerLandingPage : ViewModel() {

    // Reference the singleton instance of RepositoryAuthentication
    private val repositoryAuthentication = RepositoryAuthentication.instance

    // LiveData to hold the user information
    private val _isUser = MutableLiveData<User?>()
    val isUser: LiveData<User?> get() = _isUser

    // Observable variables from front
    val todayCustomers = MutableLiveData<Int>()
    val averageRating = MutableLiveData<Float>()
    val advertisementsCount = MutableLiveData<Int>()

    // LiveData for navigation destination
    private val _navigateTo = MutableLiveData<NavigationDestination?>()
    val navigateTo: LiveData<NavigationDestination?> get() = _navigateTo

    // Function to retrieve information like user and recommended items
    fun getInformation() {
        viewModelScope.launch {
            _isUser.value = repositoryAuthentication.getCurrentUser()
            _isUser.value?.id?.let { userId ->
                getStoresRecommended(userId)
                getAdvertisementRecommended(userId)
            }
        }
    }

    // Navigation methods
    fun onAdvertisementsClicked() {
        _navigateTo.value = NavigationDestination.ADVERTISEMENTS
    }

    fun onQrScannerClicked() {
        _navigateTo.value = NavigationDestination.QR_SCANNER
    }

    // Function to reset the navigation event after it's handled
    fun onNavigationHandled() {
        _navigateTo.value = null
    }

    // Log out logic
    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            _navigateTo.value = NavigationDestination.INITIAL
        }
    }

    // Stub functions for recommended stores and advertisements
    private fun getStoresRecommended(userId: String) {
        // Retrieve recommended stores based on the user ID
    }

    private fun getAdvertisementRecommended(userId: String) {
        // Retrieve recommended advertisements based on the user ID
    }
}
