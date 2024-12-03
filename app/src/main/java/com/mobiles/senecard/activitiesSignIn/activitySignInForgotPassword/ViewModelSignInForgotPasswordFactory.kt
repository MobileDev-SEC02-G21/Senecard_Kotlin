package com.mobiles.senecard.activitiesSignIn.activitySignInForgotPassword

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelSignInForgotPasswordFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelSignInForgotPassword::class.java)) {
            return ViewModelSignInForgotPassword(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
