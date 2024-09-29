package com.mobiles.senecard.activitiesSignIn.activitySignInChangePassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelSignInChangePassword: ViewModel() {

    private val _navigateToActivitySignInVerification = MutableLiveData<Boolean>()
    val navigateToActivitySignInVerification: LiveData<Boolean>
        get() = _navigateToActivitySignInVerification

    fun backImageViewClicked() {
        _navigateToActivitySignInVerification.value = true
    }

    fun onNavigated() {
        _navigateToActivitySignInVerification.value = false
    }
}