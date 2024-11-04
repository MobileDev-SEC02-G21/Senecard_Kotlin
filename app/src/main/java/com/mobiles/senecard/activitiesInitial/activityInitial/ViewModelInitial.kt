package com.mobiles.senecard.activitiesInitial.activityInitial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import kotlinx.coroutines.launch

class ViewModelInitial : ViewModel() {

    private val _navigateToActivitySignIn = MutableLiveData<Boolean>()
    val navigateToActivitySignIn: LiveData<Boolean>
        get() = _navigateToActivitySignIn

    private val _navigateToActivitySignUp = MutableLiveData<Boolean>()
    val navigateToActivitySignUp: LiveData<Boolean>
        get() = _navigateToActivitySignUp

    fun onSignInClicked() {
        _navigateToActivitySignIn.value = true
    }

    fun onSignUpClicked() {
        _navigateToActivitySignUp.value = true
    }

    fun onNavigated() {
        _navigateToActivitySignIn.value = false
        _navigateToActivitySignUp.value = false
    }
}