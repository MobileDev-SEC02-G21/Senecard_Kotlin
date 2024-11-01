package com.mobiles.senecard.LoyaltyCardsActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryAuthentication
import com.mobiles.senecard.model.RepositoryLoyaltyCard
import com.mobiles.senecard.model.entities.LoyaltyCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelLoyaltyCards : ViewModel() {

    private val repositoryAuthentication = RepositoryAuthentication.instance


    private val TAG = "LoyaltyCardsViewModel"
    private val repository = RepositoryLoyaltyCard.instance

    private val _loyaltyCards = MutableLiveData<List<LoyaltyCard>>()
    val loyaltyCards: LiveData<List<LoyaltyCard>> get() = _loyaltyCards

    // Método para simular la creación o actualización de una tarjeta de lealtad
    fun simulateLoyaltyCardCreation(businessOwnerId: String, uniandesMemberId: String, storeId: String, maxPoints: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Agrega log aquí
                Log.d(TAG, "Simulando creación de tarjeta: businessOwnerId=$businessOwnerId, uniandesMemberId=$uniandesMemberId, storeId=$storeId, maxPoints=$maxPoints")

                // Verificar si ya existe una LoyaltyCard entre el businessOwner y uniandesMember
                val existingCard = repository.getLoyaltyCardByUserAndStore(uniandesMemberId, storeId)

                // Agrega log para verificar si se encontró una tarjeta existente
                if (existingCard != null) {
                    Log.d(TAG, "Tarjeta existente encontrada: ${existingCard.id} con puntos: ${existingCard.points}")

                    // Si la tarjeta ya existe, verificar los puntos
                    if (existingCard.points < maxPoints) {
                        // Incrementar los puntos si no se ha alcanzado el máximo
                        existingCard.points += 1
                        repository.updateLoyaltyCard(existingCard)
                        Log.d(TAG, "Puntos incrementados. Ahora tienes ${existingCard.points} puntos.")
                    } else {
                        // Si alcanzó el máximo de puntos, marcar la tarjeta como inactiva
                        existingCard.isCurrent = false
                        repository.updateLoyaltyCard(existingCard)
                        Log.d(TAG, "Has alcanzado el máximo de puntos (${existingCard.maxPoints}). La tarjeta está inactiva.")
                    }
                } else {
                    // Si no existe, crear una nueva LoyaltyCard
                    val message = repository.addOrUpdateLoyaltyCard(
                        uniandesMemberId = uniandesMemberId,
                        storeId = storeId,
                        maxPoints = maxPoints
                    )
                    Log.d(TAG, message)  // Log del mensaje retornado por la creación de la tarjeta
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando o actualizando LoyaltyCard: ${e.message}")
            }
        }
    }


    // Obtiene tarjetas de lealtad para un usuario específico
    fun getLoyaltyCardsForUser(): LiveData<List<LoyaltyCard>> {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repositoryAuthentication.getCurrentUser()

            try {
                user?.id?.let { userId ->
                    val cards = repository.getLoyaltyCardsByUniandesMemberId(userId)

                    // Ordenar primero por tarjetas incompletas cercanas al max, luego por menos puntos, y las completas al final
                    val sortedCards = cards.sortedWith(
                        compareBy<LoyaltyCard> {
                            if (it.points == it.maxPoints) Float.MAX_VALUE else -it.points.toFloat() / it.maxPoints.toFloat()
                        }.thenByDescending { it.points }
                    )

                    _loyaltyCards.postValue(sortedCards)
                    Log.d(TAG, "Tarjetas de lealtad ordenadas obtenidas para el usuario: ${sortedCards.size}")
                } ?: Log.e(TAG, "ID de usuario es nulo")

            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo las tarjetas de lealtad: ${e.message}")
            }
        }
        return loyaltyCards
    }


}
