package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerEditLoyalty

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerEditLoyalty : ViewModel() {
    // Reference the singleton instance of RepositoryAuthentication
    private val repositoryAuthentication = RepositoryAuthentication.instance

    // LiveData to hold the user information
    private val _isUser = MutableLiveData<User?>()
    val isUser: LiveData<User?> get() = _isUser


    // Function to retrieve information like user and recommended items
    fun getInformation() {
        viewModelScope.launch {
            _isUser.value = repositoryAuthentication.getCurrentUser()
            _isUser.value?.id?.let { userId ->
            }
        }
    }
    // Navigation Methods

}