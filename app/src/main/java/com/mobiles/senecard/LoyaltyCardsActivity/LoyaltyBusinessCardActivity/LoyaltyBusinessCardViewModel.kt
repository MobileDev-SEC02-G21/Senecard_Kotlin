package com.mobiles.senecard.LoyaltyCardsActivity.LoyaltyBusinessCardActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoyaltyBusinessCardViewModel : ViewModel() {

    // LiveData para manejar el estado del botón de regreso
    private val _backButtonClicked = MutableLiveData<Boolean>()
    val backButtonClicked: LiveData<Boolean> get() = _backButtonClicked

    fun onBackButtonClicked() {
        _backButtonClicked.value = true
    }
}
