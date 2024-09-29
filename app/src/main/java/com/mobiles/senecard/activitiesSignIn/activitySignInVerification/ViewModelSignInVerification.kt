package com.mobiles.senecard.activitiesSignIn.activitySignInVerification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelSignInVerification: ViewModel() {

    private val _navigateToActivitySignInForgotPassword = MutableLiveData<Boolean>()
    val navigateToActivitySignInForgotPassword: LiveData<Boolean>
        get() = _navigateToActivitySignInForgotPassword

    private val _navigateToActivitySignInChangePassword = MutableLiveData<Boolean>()
    val navigateToActivitySignInChangePassword: LiveData<Boolean>
        get() = _navigateToActivitySignInChangePassword

    fun backImageViewClicked() {
        _navigateToActivitySignInForgotPassword.value = true
    }

    fun verifyButtonClicked() {
        _navigateToActivitySignInChangePassword.value = true
    }

    fun onNavigated() {
        _navigateToActivitySignInForgotPassword.value = false
        _navigateToActivitySignInChangePassword.value = false
    }
}