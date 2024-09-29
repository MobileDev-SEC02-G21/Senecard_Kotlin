package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelSignInForgotPassword: ViewModel() {

    private val _navigateToActivitySignIn = MutableLiveData<Boolean>()
    val navigateToActivitySignIn: LiveData<Boolean>
        get() = _navigateToActivitySignIn

    private val _navigateToActivitySignInVerification = MutableLiveData<Boolean>()
    val navigateToActivitySignInVerification: LiveData<Boolean>
        get() = _navigateToActivitySignInVerification

    fun backImageViewClicked() {
        _navigateToActivitySignIn.value = true
    }

    fun sendCodeButtonClicked() {
        _navigateToActivitySignInVerification.value = true
    }

    fun onNavigated() {
        _navigateToActivitySignIn.value = false
        _navigateToActivitySignInVerification.value = false
    }
}