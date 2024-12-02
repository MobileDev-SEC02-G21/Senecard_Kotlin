package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ViewModelSignInForgotPassword : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _navigateToActivitySignIn = MutableLiveData<Boolean>()
    val navigateToActivitySignIn: LiveData<Boolean>
        get() = _navigateToActivitySignIn

    private val _navigateToActivitySignInVerification = MutableLiveData<Boolean>()
    val navigateToActivitySignInVerification: LiveData<Boolean>
        get() = _navigateToActivitySignInVerification

    private val _showToastMessage = MutableLiveData<String?>()
    val showToastMessage: LiveData<String?>
        get() = _showToastMessage

    fun backImageViewClicked() {
        _navigateToActivitySignIn.value = true
    }

    fun sendPasswordReset(email: String) {
        if (email.isEmpty()) {
            _showToastMessage.value = "Please enter your email"
            return
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _showToastMessage.value = "Email sent successfully"
                    //_navigateToActivitySignInVerification.value = true
                } else {
                    _showToastMessage.value = "Failed to send email"
                }
            }
    }

    fun onToastShown() {
        _showToastMessage.value = null
    }

    fun onNavigated() {
        _navigateToActivitySignIn.value = false
        _navigateToActivitySignInVerification.value = false
    }
}