package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.launch

class ViewModelHomeUniandesMemberAdvertisementList : ViewModel() {

    private val repositoryAdvertisement = RepositoryAdvertisement.instance

    private val _navigateToActivityHomeUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMember

    private val _navigateToActivityHomeUniandesMemberAdvertisementDetail = MutableLiveData<Advertisement?>()
    val navigateToActivityHomeUniandesMemberAdvertisementDetail: LiveData<Advertisement?>
        get() = _navigateToActivityHomeUniandesMemberAdvertisementDetail

    private val _advertisementList = MutableLiveData<List<Advertisement>>()
    val advertisementList: LiveData<List<Advertisement>>
        get() = _advertisementList

    private val _filteredAdvertisementList = MutableLiveData<List<Advertisement>>()
    val filteredAdvertisementList: LiveData<List<Advertisement>>
        get() = _filteredAdvertisementList

    fun backImageViewClicked() {
        _navigateToActivityHomeUniandesMember.value = true
    }

    fun getAllAdvertisements() {
        viewModelScope.launch {
            _advertisementList.value = repositoryAdvertisement.getAllAdvertisements()
        }
    }

    fun filterAdvertisementsByTitle(query: String) {
        val allAdvertisements = _advertisementList.value ?: return
        if (query.isEmpty()) {
            _filteredAdvertisementList.value = allAdvertisements
        } else {
            _filteredAdvertisementList.value = allAdvertisements.filter { advertisement ->
                advertisement.title!!.contains(query, ignoreCase = true)
            }
        }
    }

    fun onClickedItemAdvertisement(advertisement: Advertisement) {
        _navigateToActivityHomeUniandesMemberAdvertisementDetail.value = advertisement
    }

    fun onNavigated() {
        _navigateToActivityHomeUniandesMember.value = false
        _navigateToActivityHomeUniandesMemberAdvertisementDetail.value = null
    }
}