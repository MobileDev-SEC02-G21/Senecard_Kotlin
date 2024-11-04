package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner3

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.activitiesSignUp.SignUpStore
import com.mobiles.senecard.activitiesSignUp.SignUpUser
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.launch

class ViewModelSignUpStoreOwner3: ViewModel() {

    private val signUpUser = SignUpUser.instance
    private val signUpStore = SignUpStore.instance

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryUser = RepositoryUser.instance
    private val repositoryStore = RepositoryStore.instance

    private val _navigateToActivitySignUpStoreOwner2 = MutableLiveData<Boolean>()
    val navigateToActivitySignUpStoreOwner2: LiveData<Boolean>
        get() = _navigateToActivitySignUpStoreOwner2

    private val _navigateToActivityBusinessOwner = MutableLiveData<Boolean>()
    val navigateToActivityBusinessOwner: LiveData<Boolean>
        get() = _navigateToActivityBusinessOwner

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun backImageViewClicked() {
        _navigateToActivitySignUpStoreOwner2.value = true
    }

    fun registerButtonClicked(storeSchedule: Map<String, List<Int>>, context: Context) {
        viewModelScope.launch {
            var isScheduleValid = true

            for ((day, schedule) in storeSchedule) {
                if (schedule[1] < schedule[0]) {
                    isScheduleValid = false
                    _message.value = day + "_error_hours"
                    break
                }
            }

            if (!com.mobiles.senecard.activitiesInitial.activitySplash.ViewModelSplash.NetworkUtils.isInternetAvailable(context)) {
                _message.value = "no_internet_connection"
            } else {
                if (isScheduleValid) {
                    if (repositoryAuthentication.createUser(signUpUser.email!!, signUpUser.password!!)) {
                        if (repositoryUser.addUser(name = signUpUser.name!!, email = signUpUser.email!!, phone = signUpUser.phone!!, role = "businessOwner") ) {
                            val user = repositoryUser.getUserByEmail(signUpUser.email!!)
                            if (user != null) {
                                if (repositoryStore.addStore(businessOwnerId = user.id!!, name = signUpStore.name!!, category = signUpStore.category!!, address = signUpStore.address!!, image = signUpStore.image!!, schedule = storeSchedule)) {
                                    signUpUser.reset()
                                    signUpStore.reset()
                                    _navigateToActivityBusinessOwner.value = true
                                } else { _message.value = "error_firebase_firestore" }
                            }
                        } else { _message.value = "error_firebase_firestore" }
                    } else { _message.value = "error_firebase_auth" }
                }
            }
        }
    }

    fun onNavigated() {
        _navigateToActivitySignUpStoreOwner2.value = false
        _navigateToActivityBusinessOwner.value = false
    }
}