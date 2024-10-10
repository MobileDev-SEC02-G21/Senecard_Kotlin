package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.launch

class ViewModelHomeUniandesMemberAdvertisementDetail : ViewModel() {

    private val repositoryAdvertisement = RepositoryAdvertisement.instance

    private val _navigateToActivityBack = MutableLiveData<Boolean>()
    val navigateToActivityBack: LiveData<Boolean>
        get() = _navigateToActivityBack

    private val _advertisement = MutableLiveData<Advertisement>()
    val advertisement: LiveData<Advertisement>
        get() = _advertisement

    fun getAdvertisementById(advertisementId: String) {
        viewModelScope.launch {
            _advertisement.value = repositoryAdvertisement.getAdvertisementByIdJeff(advertisementId)
        }
    }

    fun backImageViewClicked() {
        _navigateToActivityBack.value = true
    }
}