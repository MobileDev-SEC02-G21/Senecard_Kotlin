package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mobiles.senecard.model.RepositoryAuthentication

class ViewModelSignInForgotPassword(private val context: Context) : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repositoryAuth = RepositoryAuthentication.instance

    private val _navigateToActivitySignIn = MutableLiveData<Boolean>()
    val navigateToActivitySignIn: LiveData<Boolean>
        get() = _navigateToActivitySignIn

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

        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        if (!email.matches(emailPattern.toRegex())) {
            _showToastMessage.value = "Invalid email format"
            return
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _showToastMessage.value = "Email sent successfully"
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    _showToastMessage.value = "Failed to send email: $errorMessage"
                }
            }
    }

    fun onToastShown() {
        _showToastMessage.value = null
    }

    fun onNavigated() {
        _navigateToActivitySignIn.value = false
    }
}
