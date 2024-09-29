package com.mobiles.senecard.activitiesSignUp.activitySignUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelSignUp: ViewModel() {

    private val _navigateToActivityInitial = MutableLiveData<Boolean>()
    val navigateToActivityInitial: LiveData<Boolean>
        get() = _navigateToActivityInitial

    private val _navigateToActivitySignUpUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivitySignUpUniandesMember: LiveData<Boolean>
        get() = _navigateToActivitySignUpUniandesMember

    private val _navigateToActivitySignUpStoreOwner1 = MutableLiveData<Boolean>()
    val navigateToActivitySignUpStoreOwner1: LiveData<Boolean>
        get() = _navigateToActivitySignUpStoreOwner1

    private val _navigateToActivitySignIn = MutableLiveData<Boolean>()
    val navigateToActivitySignIn: LiveData<Boolean>
        get() = _navigateToActivitySignIn

    fun backImageViewClicked() {
        _navigateToActivityInitial.value = true
    }

    fun uniandesCommunityButtonClicked() {
        _navigateToActivitySignUpUniandesMember.value = true
    }

    fun storeOwnerButtonClicked() {
        _navigateToActivitySignUpStoreOwner1.value = true
    }

    fun signInTextViewClicked() {
        _navigateToActivitySignIn.value = true
    }

    fun onNavigated() {
        _navigateToActivityInitial.value = false
        _navigateToActivitySignUpUniandesMember.value = false
        _navigateToActivitySignUpStoreOwner1.value = false
        _navigateToActivitySignIn.value = false
    }
}