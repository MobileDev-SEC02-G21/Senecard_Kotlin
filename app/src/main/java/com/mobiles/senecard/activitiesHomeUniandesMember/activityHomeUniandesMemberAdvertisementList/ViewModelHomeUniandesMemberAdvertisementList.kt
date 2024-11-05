package com.mobiles.senecard.activitiesHomeUniandesMember.activityHomeUniandesMemberAdvertisementList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAdvertisement
import com.mobiles.senecard.model.RepositoryStore
import com.mobiles.senecard.model.entities.Advertisement
import kotlinx.coroutines.launch

class ViewModelHomeUniandesMemberAdvertisementList : ViewModel() {

    private val repositoryStore = RepositoryStore.instance
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

    private var currentSearchQuery: String = ""
    private var currentCategory: String = "All"

    fun backImageViewClicked() {
        _navigateToActivityHomeUniandesMember.value = true
    }

    fun getAllAdvertisements() {
        viewModelScope.launch {
            val advertisements = repositoryAdvertisement.getAllAdvertisements()
            _advertisementList.value = advertisements
            _filteredAdvertisementList.value = advertisements
        }
    }

    fun filterAdvertisementsByTitle(query: String) {
        viewModelScope.launch {
            currentSearchQuery = query
            applyFilters()
        }
    }

    fun filterStoresByCategory(category: String) {
        viewModelScope.launch {
            currentCategory = if (category == "Other") "Other" else category
            applyFilters()
        }
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val validCategories = listOf("Bakeries", "Bar", "Coffee", "Electronic", "Pizzeria", "Restaurant", "Stationery")

            val allAdvertisements = _advertisementList.value ?: return@launch

            val filteredAdvertisements = allAdvertisements.filter { advertisement ->

                val store = advertisement.storeId?.let { repositoryStore.getStoreById(it) }

                val matchesCategory = when (currentCategory) {
                    "All" -> true
                    "Other" -> store?.category !in validCategories
                    else -> store?.category.equals(currentCategory, ignoreCase = true)
                }

                val matchesQuery = advertisement.title?.contains(currentSearchQuery, ignoreCase = true) ?: false
                matchesCategory && matchesQuery
            }

            _filteredAdvertisementList.value = filteredAdvertisements
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