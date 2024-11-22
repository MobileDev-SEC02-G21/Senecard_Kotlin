
package com.mobiles.senecard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelLoyaltyCardDetail : ViewModel() {

    private val _backButtonClicked = MutableLiveData<Boolean>()
    val backButtonClicked: LiveData<Boolean>
        get() = _backButtonClicked

    // Método para manejar el clic en el botón de atrás
    fun onBackButtonClicked() {
        _backButtonClicked.value = true
    }
}
