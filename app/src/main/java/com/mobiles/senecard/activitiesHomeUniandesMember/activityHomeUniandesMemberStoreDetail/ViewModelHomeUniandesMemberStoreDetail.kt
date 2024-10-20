package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.launch

class ViewModelHomeUniandesMemberStoreDetail : ViewModel() {

    private val repositoryStore = RepositoryStore.instance

    private val _navigateToActivityBack = MutableLiveData<Boolean>()
    val navigateToActivityBack: LiveData<Boolean>
        get() = _navigateToActivityBack

    private val _store = MutableLiveData<Store?>()
    val store: LiveData<Store?>
        get() = _store

    fun getStoreById(storeId: String) {
        viewModelScope.launch {
            _store.value = repositoryStore.getStoreById(storeId)
        }
    }

    fun backImageViewClicked() {
        _navigateToActivityBack.value = true
    }
}