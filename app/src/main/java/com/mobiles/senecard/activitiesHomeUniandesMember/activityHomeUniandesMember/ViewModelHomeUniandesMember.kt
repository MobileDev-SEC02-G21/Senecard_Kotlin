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
import java.util.Locale

class ViewModelHomeUniandesMember: ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryStore = RepositoryStore.instance
    private val repositoryAdvertisement = RepositoryAdvertisement.instance

    private val _isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedOut: LiveData<Boolean>
        get() = _isLoggedOut

    private val _navigateToActivityHomeUniandesMemberStoreList = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMemberStoreList: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMemberStoreList

    private val _navigateToActivityHomeUniandesMemberAdvertisementList = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMemberAdvertisementList: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMemberAdvertisementList

    private val _isUserLogged = MutableLiveData<User?>()
    val isUserLogged: LiveData<User?>
        get() = _isUserLogged

    private val _storeListRecommended = MutableLiveData<List<Store>>()
    val storeListRecommended: LiveData<List<Store>>
        get() = _storeListRecommended

    private val _advertisementListRecommended = MutableLiveData<List<Advertisement>>()
    val advertisementListRecommended: LiveData<List<Advertisement>>
        get() = _advertisementListRecommended

    fun validateSession() {
        viewModelScope.launch {
            _isUserLogged.value = repositoryAuthentication.getCurrentUser()
        }
    }

    fun getStoresRecommended() {
        viewModelScope.launch {
            val stores = repositoryStore.getAllStores().take(2)

            _storeListRecommended.value = stores.sortedBy { store ->
                if (isStoreClosed(store)) 1 else 0
            }
        }
    }

    fun getAdvertisementRecommended() {
        viewModelScope.launch {
            val advertisements = repositoryAdvertisement.getAllAdvertisement().take(2)

            _advertisementListRecommended.value = advertisements.sortedBy { advertisement ->
                if (isAdvertisementStoreClosed(advertisement)) 1 else 0
            }
        }
    }

    fun isStoreClosed(store: Store): Boolean {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)?.lowercase()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val schedule = store.schedule

        if (schedule != null && currentDayOfWeek != null && schedule.containsKey(currentDayOfWeek)) {
            val hours = schedule[currentDayOfWeek] ?: return true

            val openingHour = hours[0]
            val closingHour = hours[1]

            return currentHour < openingHour || currentHour > closingHour
        }

        return true
    }

    fun isAdvertisementStoreClosed(advertisement: Advertisement): Boolean {

        val store = advertisement.store

        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)?.lowercase()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val schedule = store?.schedule

        if (schedule != null && currentDayOfWeek != null && schedule.containsKey(currentDayOfWeek)) {
            val hours = schedule[currentDayOfWeek] ?: return true

            val openingHour = hours[0]
            val closingHour = hours[1]

            return currentHour < openingHour || currentHour > closingHour
        }

        return true
    }

    fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hourOfDay) {
            in 5..11 -> "¡Good Morning!"
            in 12..17 -> "¡Good Afternoon!"
            else -> "¡Good Night!"
        }
    }

    fun storesEditTextClicked() {
        _navigateToActivityHomeUniandesMemberStoreList.value = true
    }

    fun advertisementsEditTextClicked() {
        _navigateToActivityHomeUniandesMemberAdvertisementList.value = true
    }

    fun logOut() {
        viewModelScope.launch {
            repositoryAuthentication.logOut()
            if (repositoryAuthentication.getCurrentUser() == null) {
                _isLoggedOut.value = true
            }
        }
    }

    fun onNavigated() {
        _navigateToActivityHomeUniandesMemberStoreList.value = false
        _navigateToActivityHomeUniandesMemberAdvertisementList.value = false
    }

}