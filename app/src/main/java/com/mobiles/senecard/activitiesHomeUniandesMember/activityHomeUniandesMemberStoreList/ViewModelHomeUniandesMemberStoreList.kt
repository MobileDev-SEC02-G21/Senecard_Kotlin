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

    private val _filteredStoreList = MutableLiveData<List<Store>>()
    val filteredStoreList: LiveData<List<Store>>
        get() = _filteredStoreList

    private var currentSearchQuery: String = ""
    private var currentCategory: String = "All"

    fun backImageViewClicked() {
        _navigateToActivityHomeUniandesMember.value = true
    }

    fun getAllStores() {
        viewModelScope.launch {
            val stores = repositoryStore.getAllStores()
            _storeList.value = stores
        }
    }

    fun filterStoresByName(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterStoresByCategory(category: String) {
        currentCategory = if (category == "Other") "Other" else category
        applyFilters()
    }

    fun applyFilters() {
        val validCategories = listOf("Bakeries", "Bar", "Coffee", "Electronic", "Pizzeria", "Restaurant", "Stationery")

        val allStores = _storeList.value ?: return

        val filteredStores = allStores.filter { store ->
            val matchesCategory = when (currentCategory) {
                "All" -> true
                "Other" -> store.category !in validCategories
                else -> store.category.equals(currentCategory, ignoreCase = true)
            }

            val matchesQuery = store.name?.contains(currentSearchQuery, ignoreCase = true) ?: false
            matchesCategory && matchesQuery
        }

        _filteredStoreList.value = filteredStores
    }

    fun onClickedItemStore(store: Store) {
        _navigateToActivityHomeUniandesMemberStoreDetail.value = store
    }

    fun onNavigated() {
        _navigateToActivityHomeUniandesMember.value = false
        _navigateToActivityHomeUniandesMemberStoreDetail.value = null
    }
}