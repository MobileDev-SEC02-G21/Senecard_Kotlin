package com.mobiles.senecard.activitiesSignUp.activitySignUpStoreOwner2

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiles.senecard.activitiesSignUp.SignUpStore

class ViewModelSignUpStoreOwner2: ViewModel() {

    private val signUpStore = SignUpStore.instance

    private val _navigateToActivitySignUpStoreOwner1 = MutableLiveData<Boolean>()
    val navigateToActivitySignUpStoreOwner1: LiveData<Boolean>
        get() = _navigateToActivitySignUpStoreOwner1

    private val _navigateToActivitySignUpStoreOwner3 = MutableLiveData<Boolean>()
    val navigateToActivitySignUpStoreOwner3: LiveData<Boolean>
        get() = _navigateToActivitySignUpStoreOwner3

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    fun nextButtonClicked(storeName: String, storeAddress: String, storeCategory: String, storeImage: Uri?) {
        val nameRegex = "^(?! )[A-Za-z]+( [A-Za-z]+)*(?<! )$".toRegex()
        val addressRegex = "^(?! )[A-Za-z0-9\\s.,'#-]*\\S[A-Za-z0-9\\s.,'#-]*(?<! )$".toRegex()

        if (storeName.isEmpty()) { _message.value = "store_name_empty" }
        else if (!nameRegex.matches(storeName)) { _message.value = "store_name_invalid" }
        else if (storeAddress.isEmpty()) { _message.value = "store_address_empty" }
        else if (!addressRegex.matches(storeAddress)) { _message.value = "store_address_invalid" }
        else if (storeImage == null) { _message.value = "store_image_empty" }
        else {
            signUpStore.name = storeName
            signUpStore.address = storeAddress
            signUpStore.category = storeCategory
            signUpStore.image = storeImage
            _navigateToActivitySignUpStoreOwner3.value = true
        }
    }

    fun backImageViewClicked() {
        _navigateToActivitySignUpStoreOwner1.value = true
    }

    fun onNavigated() {
        _navigateToActivitySignUpStoreOwner1.value = false
        _navigateToActivitySignUpStoreOwner3.value = false
    }
}