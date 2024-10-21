package com.mobiles.senecard.LoyaltyCardsActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryRoyaltyCard
import com.mobiles.senecard.model.entities.RoyaltyCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoyaltyCardsViewModel : ViewModel() {

    private val TAG = "LoyaltyCardsViewModel"
    private val repository = RepositoryRoyaltyCard.instance

    private val _royaltyCards = MutableLiveData<List<RoyaltyCard>>()
    val royaltyCards: LiveData<List<RoyaltyCard>> get() = _royaltyCards

    // Método para simular la creación o actualización de una tarjeta de lealtad
    fun simulateRoyaltyCardCreation(businessOwnerId: String, uniandesMemberId: String, storeId: String, maxPoints: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Verificar si ya existe una RoyaltyCard entre el businessOwner y uniandesMember
                val existingCard = repository.getRoyaltyCardByUserAndStore(uniandesMemberId, storeId)

                if (existingCard != null) {
                    // Si la tarjeta ya existe, verificar los puntos
                    if (existingCard.points < maxPoints) {
                        // Incrementar los puntos si no se ha alcanzado el máximo
                        existingCard.points += 1
                        repository.updateRoyaltyCard(existingCard)
                        Log.d(TAG, "Puntos incrementados. Ahora tienes ${existingCard.points} puntos.")
                    } else {
                        // Si alcanzó el máximo de puntos, marcar la tarjeta como inactiva
                        existingCard.isCurrent = false
                        repository.updateRoyaltyCard(existingCard)
                        Log.d(TAG, "Has alcanzado el máximo de puntos (${existingCard.maxPoints}). La tarjeta está inactiva.")
                    }
                } else {
                    // Si no existe, crear una nueva RoyaltyCard
                    val message = repository.addOrUpdateRoyaltyCard(
                        uniandesMemberId = uniandesMemberId,
                        storeId = storeId,
                        maxPoints = maxPoints
                    )
                    Log.d(TAG, message)  // Log del mensaje retornado por la creación de la tarjeta
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando o actualizando RoyaltyCard: ${e.message}")
            }
        }
    }

    // Obtiene tarjetas de lealtad para un usuario específico
    fun getLoyaltyCardsForUser(uniandesMemberId: String): LiveData<List<RoyaltyCard>> {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cards = repository.getRoyaltyCardsByUser(uniandesMemberId)
                _royaltyCards.postValue(cards)
                Log.d(TAG, "Tarjetas de lealtad obtenidas para el usuario: ${cards.size}") // Log de cantidad
                cards.forEach { card ->
                    Log.d(TAG, "Tarjeta: ID=${card.id}, Puntos=${card.points}, MaxPuntos=${card.maxPoints}") // Log detallado
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo las tarjetas de lealtad: ${e.message}")
            }
        }
        return royaltyCards
    }
}
