package com.mobiles.senecard.activitiesSignIn.activitySignIn

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelSignIn : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryUser = RepositoryUser.instance

    private val _navigateToActivityInitial = MutableLiveData<Boolean>()
    val navigateToActivityInitial: LiveData<Boolean>
        get() = _navigateToActivityInitial

    private val _navigateToActivitySignInForgotPassword = MutableLiveData<Boolean>()
    val navigateToActivitySignInForgotPassword: LiveData<Boolean>
        get() = _navigateToActivitySignInForgotPassword

    private val _navigateToActivityHomeUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMember

    private val _navigateToActivitySignUp = MutableLiveData<Boolean>()
    val navigateToActivitySignUp: LiveData<Boolean>
        get() = _navigateToActivitySignUp

    private val _navigateToActivityBusinessOwner = MutableLiveData<Boolean>()
    val navigateToActivityBusinessOwner: LiveData<Boolean>
        get() = _navigateToActivityBusinessOwner

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun backImageViewClicked() {
        _navigateToActivityInitial.value = true
    }

    fun forgotPasswordTextViewClicked() {
        _navigateToActivitySignInForgotPassword.value = true
    }

    fun enterButtonClicked(email: String, password: String, context: Context) {
        viewModelScope.launch {
            when {
                email.isEmpty() -> {
                    _message.value = "email_empty"
                }
                password.isEmpty() -> {
                    _message.value = "password_empty"
                }
                email.contains(" ") || password.contains(" ") -> {
                    _message.value = "no_spaces_allowed"
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    _message.value = "email_invalid"
                }
                !com.mobiles.senecard.activitiesInitial.activitySplash.ViewModelSplash.NetworkUtils.isInternetAvailable(context) -> {
                    _message.value = "no_internet_connection"
                }
                else -> {
                    if (repositoryAuthentication.authenticateUser(email, password)) {
                        val user = repositoryUser.getUserByEmail(email)
                        if (user != null) {
                            if (user.role == "businessOwner") {
                                _navigateToActivityBusinessOwner.value = true
                            } else {
                                _navigateToActivityHomeUniandesMember.value = true
                            }
                        } else {
                            _message.value = "error_firebase_auth"
                        }
                    } else {
                        _message.value = "error_firebase_auth"
                    }
                }
            }
        }
    }

    fun signUpTextViewClicked() {
        _navigateToActivitySignUp.value = true
    }

    fun onNavigated() {
        _navigateToActivityInitial.value = false
        _navigateToActivitySignInForgotPassword.value = false
        _navigateToActivityHomeUniandesMember.value = false
        _navigateToActivitySignUp.value = false
    }
}