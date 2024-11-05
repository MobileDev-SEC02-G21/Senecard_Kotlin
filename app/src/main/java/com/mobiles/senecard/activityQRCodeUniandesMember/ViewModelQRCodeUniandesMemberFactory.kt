package com.mobiles.senecard.activityQRCodeUniandesMember

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelQRCodeUniandesMemberFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelQRCodeUniandesMember::class.java)) {
            return ViewModelQRCodeUniandesMember(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
