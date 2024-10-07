package com.mobiles.senecard.activitiesSignUp.activitySignUpUniandesMember

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelSignUpUniandesMember: ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryUser = RepositoryUser.instance

    private val _navigateToActivitySignUp = MutableLiveData<Boolean>()
    val navigateToActivitySignUp: LiveData<Boolean>
        get() = _navigateToActivitySignUp

    private val _navigateToActivityHome = MutableLiveData<Boolean>()
    val navigateToActivityHome: LiveData<Boolean>
        get() = _navigateToActivityHome

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun backImageViewClicked() {
        _navigateToActivitySignUp.value = true
    }

    fun registerButtonClicked(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (name.isEmpty()) { _message.value = "name_empty" }
            else if (email.isEmpty()) { _message.value = "email_empty" }
            else if (phone.isEmpty()) { _message.value = "phone_empty" }
            else if (password.isEmpty()) { _message.value = "password_empty" }
            else if (confirmPassword.isEmpty()) { _message.value = "confirm_password_empty" }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { _message.value = "email_invalid" }
            else if (password.length < 7) { _message.value = "password_short" }
            else if (password != confirmPassword) { _message.value = "passwords_not_equals" }
            else if (repositoryUser.existsUser(email) == true) { _message.value = "user_exists" }
            else {
                if (repositoryAuthentication.createUser(email = email, password = password)) {
                    if (repositoryUser.addUser(name = name, email = email, phone = phone, role = "uniandesMember", qrCode = "")) {
                        _navigateToActivityHome.value = true
                    } else {
                        _message.value = "error_firebase_firestore"
                    }
                } else {
                    _message.value = "error_firebase_auth"
                }
            }
        }
    }

    fun onNavigated() {
        _navigateToActivitySignUp.value = false
        _navigateToActivityHome.value = false
    }
}