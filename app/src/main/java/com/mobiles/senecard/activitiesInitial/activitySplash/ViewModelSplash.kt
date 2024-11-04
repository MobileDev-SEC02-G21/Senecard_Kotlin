package com.mobiles.senecard.activitiesInitial.activitySplash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModelSplash : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _sessionValidationComplete = MutableLiveData<Boolean>()
    val sessionValidationComplete: LiveData<Boolean>
        get() = _sessionValidationComplete

    private val _isLoggedRole = MutableLiveData<String?>()
    val isLoggedRole: LiveData<String?>
        get() = _isLoggedRole

    init {
        viewModelScope.launch  {
            val user = repositoryAuthentication.getCurrentUser()
            _isLoggedRole.value = user?.role
            _isLoading.value = false
            _sessionValidationComplete.value = true
        }
    }
}
