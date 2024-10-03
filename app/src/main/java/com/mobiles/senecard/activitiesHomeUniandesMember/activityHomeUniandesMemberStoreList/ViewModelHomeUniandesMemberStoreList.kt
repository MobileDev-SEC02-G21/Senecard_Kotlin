package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ViewModelHomeUniandesMemberStoreList : ViewModel() {

    private val repositoryStore = RepositoryStore.instance

    private val _navigateToActivityHomeUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMember

    private val _storeList = MutableLiveData<List<Store>>()
    val storeList: LiveData<List<Store>>
        get() = _storeList

    fun backImageViewClicked() {
        _navigateToActivityHomeUniandesMember.value = true
    }

    fun getAllStores() {
        viewModelScope.launch {
            val stores = repositoryStore.getAllStores()

            _storeList.value = stores.sortedBy { store ->
                if (isStoreClosed(store)) 1 else 0
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

    fun onNavigated() {
        _navigateToActivityHomeUniandesMember.value = false
    }
}