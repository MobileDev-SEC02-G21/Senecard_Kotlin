package com.mobiles.senecard.activityHome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mobiles.senecard.model.RepositoryAuthentication
import kotlinx.coroutines.launch

class ViewModelHome: ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private var user: FirebaseUser? = null

    private val _isLogged = MutableLiveData<Boolean>()
    val isLogged: LiveData<Boolean>
        get() = _isLogged

    private val _isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedOut: LiveData<Boolean>
        get() = _isLoggedOut

    fun validateSession() {
        viewModelScope.launch {
            user = repositoryAuthentication.getCurrentUser()
            _isLogged.value = user != null
        }
    }

    fun getUserEmail(): String? {
        return user?.email
    }

    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            if (repositoryAuthentication.getCurrentUser() == null) {
                _isLoggedOut.value = true
            }
        }
    }

}