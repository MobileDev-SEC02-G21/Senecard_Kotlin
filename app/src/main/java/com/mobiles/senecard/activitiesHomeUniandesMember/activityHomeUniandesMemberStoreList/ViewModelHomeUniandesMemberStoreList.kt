package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberStoreList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Store
import kotlinx.coroutines.launch

class ViewModelHomeUniandesMemberStoreList : ViewModel() {

    private val repositoryStore = RepositoryStore.instance

    private val _navigateToActivityHomeUniandesMember = MutableLiveData<Boolean>()
    val navigateToActivityHomeUniandesMember: LiveData<Boolean>
        get() = _navigateToActivityHomeUniandesMember

    private val _navigateToActivityHomeUniandesMemberStoreDetail = MutableLiveData<Store?>()
    val navigateToActivityHomeUniandesMemberStoreDetail: LiveData<Store?>
        get() = _navigateToActivityHomeUniandesMemberStoreDetail

    private val _storeList = MutableLiveData<List<Store>>()
    val storeList: LiveData<List<Store>>
        get() = _storeList

    fun backImageViewClicked() {
        _navigateToActivityHomeUniandesMember.value = true
    }

    fun getAllStores() {
        viewModelScope.launch {
            _storeList.value = repositoryStore.getAllStores()
        }
    }

    fun onClickedItemStore(store: Store) {
        _navigateToActivityHomeUniandesMemberStoreDetail.value = store
    }

    fun onNavigated() {
        _navigateToActivityHomeUniandesMember.value = false
        _navigateToActivityHomeUniandesMemberStoreDetail.value = null
    }
}