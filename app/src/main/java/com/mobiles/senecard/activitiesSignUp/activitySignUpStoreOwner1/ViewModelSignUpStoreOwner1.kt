package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner1

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.activitiesSignUp.SignUpUser
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelSignUpStoreOwner1: ViewModel() {

    private val repositoryUser = RepositoryUser.instance
    private val signUpUser = SignUpUser.instance

    private val _navigateToActivitySignUp = MutableLiveData<Boolean>()
    val navigateToActivitySignUp: LiveData<Boolean>
        get() = _navigateToActivitySignUp

    private val _navigateToActivitySignUpStoreOwner2 = MutableLiveData<Boolean>()
    val navigateToActivitySignUpStoreOwner2: LiveData<Boolean>
        get() = _navigateToActivitySignUpStoreOwner2

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun backImageViewClicked() {
        _navigateToActivitySignUp.value = true
    }

    fun nextButtonClicked(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            val nameRegex = "^(?! )[A-Za-z]+( [A-Za-z]+)*(?<! )$".toRegex()

            if (name.isEmpty()) { _message.value = "name_empty" }
            else if (!nameRegex.matches(name)) { _message.value = "name_invalid" }
            else if (email.isEmpty()) { _message.value = "email_empty" }
            else if (phone.isEmpty()) { _message.value = "phone_empty" }
            else if (password.isEmpty()) { _message.value = "password_empty" }
            else if (confirmPassword.isEmpty()) { _message.value = "confirm_password_empty" }
            else if (email.contains(" ") || phone.contains(" ") || password.contains(" ") || confirmPassword.contains(" ")) { _message.value = "no_spaces_allowed" }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { _message.value = "email_invalid" }
            else if (password.length < 6) { _message.value = "password_short" }
            else if (password != confirmPassword) { _message.value = "passwords_not_equals" }
            else if (repositoryUser.existsUserByEmail(email) == true) { _message.value = "user_exists" }
            else {
                signUpUser.name = name
                signUpUser.email = email
                signUpUser.phone = phone
                signUpUser.password = password
                _navigateToActivitySignUpStoreOwner2.value = true
            }
        }
    }

    fun onNavigated() {
        _navigateToActivitySignUp.value = false
        _navigateToActivitySignUpStoreOwner2.value = false
    }
}