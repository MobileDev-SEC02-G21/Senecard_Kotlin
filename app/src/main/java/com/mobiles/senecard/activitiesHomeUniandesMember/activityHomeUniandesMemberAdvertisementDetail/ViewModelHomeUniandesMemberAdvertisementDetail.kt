package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.launch

class ViewModelHomeUniandesMemberAdvertisementDetail : ViewModel() {

    private val repositoryAdvertisement = RepositoryAdvertisement.instance
    private val repositoryStore = RepositoryStore.instance

    private val _navigateToActivityBack = MutableLiveData<Boolean>()
    val navigateToActivityBack: LiveData<Boolean>
        get() = _navigateToActivityBack

    private val _advertisement = MutableLiveData<Advertisement?>()
    val advertisement: LiveData<Advertisement?>
        get() = _advertisement

    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String>
        get() = _storeName

    fun getAdvertisementById(advertisementId: String) {
        viewModelScope.launch {
            val advertisement = repositoryAdvertisement.getAdvertisementById(advertisementId)
            _advertisement.value = advertisement

            val storeId = advertisement?.storeId
            if (storeId != null) {
                val store = repositoryStore.getStoreById(storeId)
                _storeName.value = store?.name ?: "Unknown Store"
            } else {
                _storeName.value = "Unknown Store"
            }
        }
    }

    fun backImageViewClicked() {
        _navigateToActivityBack.value = true
    }
}