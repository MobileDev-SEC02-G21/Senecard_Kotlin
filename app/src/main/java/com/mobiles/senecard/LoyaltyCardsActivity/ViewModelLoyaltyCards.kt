package com.mobiles.senecard

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryLoyaltyCard
import com.mobiles.senecard.model.entities.LoyaltyCard
import com.mobiles.senecard.utils.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewModelLoyaltyCards : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance
    private val repositoryLoyaltyCard = RepositoryLoyaltyCard.instance
    private val TAG = "LoyaltyCardsViewModel"

    private val _loyaltyCards = MutableLiveData<List<LoyaltyCard>>()
    val loyaltyCards: LiveData<List<LoyaltyCard>> get() = _loyaltyCards

    // Obtiene el ID del usuario desde Firebase o caché
    suspend fun getCurrentUserId(context: Context): String? {
        val firebaseUserId = withContext(Dispatchers.IO) {
            repositoryAuthentication.getCurrentUser()?.id
        }
        return firebaseUserId ?: UserSessionManager.getUserId(context)
    }

    // Obtiene las tarjetas de lealtad para el usuario actual
    fun fetchLoyaltyCardsForUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cards = repositoryLoyaltyCard.getLoyaltyCardsByUniandesMemberId(userId)
                val sortedCards = cards.sortedWith(
                    compareBy<LoyaltyCard> {
                        if (it.points == it.maxPoints) Float.MAX_VALUE else -it.points.toFloat() / it.maxPoints.toFloat()
                    }.thenByDescending { it.points }
                )
                _loyaltyCards.postValue(sortedCards)
                Log.d(TAG, "Tarjetas de lealtad obtenidas para el usuario: ${sortedCards.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo las tarjetas de lealtad: ${e.message}")
            }
        }
    }
}
