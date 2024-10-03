package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ViewModelHomeUniandesMemberAdvertisementList : ViewModel() {

    private val repositoryAdvertisement = RepositoryAdvertisement.instance

    private val _navigateToActivityHomeUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMember

    private val _advertisementList = MutableLiveData<List<Advertisement>>()
    val advertisementList: LiveData<List<Advertisement>>
        get() = _advertisementList

    fun getAllAdvertisements() {
        viewModelScope.launch {
            val advertisements = repositoryAdvertisement.getAllAdvertisement()

            _advertisementList.value = advertisements.sortedBy { advertisement ->
                if (isAdvertisementStoreClosed(advertisement)) 1 else 0
            }
        }
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

    fun backImageViewClicked() {
        _navigateToActivityHomeUniandesMember.value = true
    }

    fun onNavigated() {
        _navigateToActivityHomeUniandesMember.value = false
    }
}