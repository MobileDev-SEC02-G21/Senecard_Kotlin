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

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _infoMessage = MutableLiveData<String?>()
    val infoMessage: LiveData<String?> get() = _infoMessage

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

    // Functions to set or clear informational and error messages
    fun showInformation(message: String) {
        _infoMessage.value = message
        _uiState.value = UiState.INFORMATION
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun onErrorCancel() {
        TODO("Not yet implemented")
    }

    fun onInformationAcknowledged() {
        TODO("Not yet implemented")
    }
}
