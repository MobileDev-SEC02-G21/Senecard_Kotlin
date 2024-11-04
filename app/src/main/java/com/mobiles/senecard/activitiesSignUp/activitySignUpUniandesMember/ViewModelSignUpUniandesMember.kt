package com.mobiles.senecard.activitiesSignUp.activitySignUpUniandesMember

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.NetworkUtils
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelSignUpUniandesMember: ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryUser = RepositoryUser.instance

    private val _navigateToActivitySignUp = MutableLiveData<Boolean>()
    val navigateToActivitySignUp: LiveData<Boolean>
        get() = _navigateToActivitySignUp

    private val _navigateToActivityHomeUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMember

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun backImageViewClicked() {
        _navigateToActivitySignUp.value = true
    }

    fun registerButtonClicked(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            val nameRegex = "^(?! )[A-Za-z]+( [A-Za-z]+)*(?<! )$".toRegex()

            when {
                name.isEmpty() -> _message.value = "name_empty"
                !nameRegex.matches(name) -> _message.value = "name_invalid"
                email.isEmpty() -> _message.value = "email_empty"
                phone.isEmpty() -> _message.value = "phone_empty"
                password.isEmpty() -> _message.value = "password_empty"
                confirmPassword.isEmpty() -> _message.value = "confirm_password_empty"
                email.contains(" ") || phone.contains(" ") || password.contains(" ") || confirmPassword.contains(" ") -> _message.value = "no_spaces_allowed"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> _message.value = "email_invalid"
                password.length < 7 -> _message.value = "password_short"
                password != confirmPassword -> _message.value = "passwords_not_equals"
                !NetworkUtils.isInternetAvailable() -> {
                    _message.value = "no_internet_connection"
                }
                repositoryUser.existsUserByEmail(email) == true -> _message.value = "user_exists"
                else -> {
                    if (repositoryAuthentication.createUser(email = email, password = password)) {
                        if (repositoryUser.addUser(name = name, email = email, phone = phone, role = "uniandesMember")) {
                            _navigateToActivityHomeUniandesMember.value = true
                        } else {
                            _message.value = "error_firebase_firestore"
                        }
                    } else {
                        _message.value = "error_firebase_auth"
                    }
                }
            }
        }
    }

    fun onNavigated() {
        _navigateToActivitySignUp.value = false
        _navigateToActivityHomeUniandesMember.value = false
    }
}