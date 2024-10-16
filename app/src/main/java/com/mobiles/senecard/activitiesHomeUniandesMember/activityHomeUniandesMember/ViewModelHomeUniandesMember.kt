package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMember

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Advertisement
import com.mobiles.senecard.model.entities.Store
import com.mobiles.senecard.model.entities.User
import kotlinx.coroutines.launch
import java.util.Calendar

class ViewModelHomeUniandesMember: ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryStore = RepositoryStore.instance
    private val repositoryAdvertisement = RepositoryAdvertisement.instance

    private val _navigateToActivityHomeUniandesMemberStoreList = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMemberStoreList: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMemberStoreList

    private val _navigateToActivityHomeUniandesMemberAdvertisementList = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMemberAdvertisementList: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMemberAdvertisementList

    private val _navigateToActivityHomeUniandesMemberStoreDetail = MutableLiveData<Store?>()
    val navigateToActivityHomeUniandesMemberStoreDetail: LiveData<Store?>
        get() = _navigateToActivityHomeUniandesMemberStoreDetail

    private val _navigateToActivityHomeUniandesMemberAdvertisementDetail = MutableLiveData<Advertisement?>()
    val navigateToActivityHomeUniandesMemberAdvertisementDetail: LiveData<Advertisement?>
        get() = _navigateToActivityHomeUniandesMemberAdvertisementDetail

    private val _navigateToActivityQrCodeUniandesMemberImageView = MutableLiveData<Boolean>()
    val navigateToActivityQrCodeUniandesMemberImageView: LiveData<Boolean>
        get() = _navigateToActivityQrCodeUniandesMemberImageView

    private val _navigateToActivityQrCodeUniandesMemberButton = MutableLiveData<Boolean>()
    val navigateToActivityQrCodeUniandesMemberButton: LiveData<Boolean>
        get() = _navigateToActivityQrCodeUniandesMemberButton

    private val _navigateToActivityLoyaltyCardsUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityLoyaltyCardsUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityLoyaltyCardsUniandesMember

    private val _navigateToActivityInitial = MutableLiveData<Boolean>()
    val navigateToActivityInitial: LiveData<Boolean>
        get() = _navigateToActivityInitial

    private val _isUser= MutableLiveData<User?>()
    val isUser: LiveData<User?>
        get() = _isUser

    private val _storeListRecommended = MutableLiveData<List<Store>>()
    val storeListRecommended: LiveData<List<Store>>
        get() = _storeListRecommended

    private val _advertisementListRecommended = MutableLiveData<List<Advertisement>>()
    val advertisementListRecommended: LiveData<List<Advertisement>>
        get() = _advertisementListRecommended

    fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hourOfDay) {
            in 5..11 -> "¡Good Morning!"
            in 12..17 -> "¡Good Afternoon!"
            else -> "¡Good Night!"
        }
    }

    fun getUser() {
        viewModelScope.launch {
            _isUser.value = repositoryAuthentication.getCurrentUser()
        }
    }

    fun getStoresRecommended() {
        viewModelScope.launch {
            _storeListRecommended.value = repositoryStore.getAllStores().take(2)
        }
    }

    fun getAdvertisementRecommended() {
        viewModelScope.launch {
            _advertisementListRecommended.value = repositoryAdvertisement.getAllAdvertisements().take(2)
        }
    }

    fun storesEditTextClicked() {
        _navigateToActivityHomeUniandesMemberStoreList.value = true
    }

    fun advertisementsEditTextClicked() {
        _navigateToActivityHomeUniandesMemberAdvertisementList.value = true
    }

    fun storeItemClicked(store: Store) {
        _navigateToActivityHomeUniandesMemberStoreDetail.value = store
    }

    fun advertisementItemClicked(advertisement: Advertisement) {
        _navigateToActivityHomeUniandesMemberAdvertisementDetail.value = advertisement
    }

    fun qrCodeImageViewClicked() {
        _navigateToActivityQrCodeUniandesMemberImageView.value = true
    }

    fun qrCodeButtonClicked() {
        _navigateToActivityQrCodeUniandesMemberButton.value = true
    }

    fun loyaltyCardsButtonClicked() {
        _navigateToActivityLoyaltyCardsUniandesMember.value = true
    }

    fun logOutButtonClicked() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            if (repositoryAuthentication.getCurrentUser() == null) {
                _navigateToActivityInitial.value = true
            }
        }
    }

    fun onNavigated() {
        _navigateToActivityHomeUniandesMemberStoreList.value = false
        _navigateToActivityHomeUniandesMemberAdvertisementList.value = false
        _navigateToActivityHomeUniandesMemberStoreDetail.value = null
        _navigateToActivityHomeUniandesMemberAdvertisementDetail.value = null

        _navigateToActivityQrCodeUniandesMemberImageView.value = false
        _navigateToActivityQrCodeUniandesMemberButton.value = false
        _navigateToActivityLoyaltyCardsUniandesMember.value = false
        _navigateToActivityInitial.value = false
    }
}