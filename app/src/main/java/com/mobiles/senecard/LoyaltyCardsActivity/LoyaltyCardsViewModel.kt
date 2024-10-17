package com.mobiles.senecard.LoyaltyCardsActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryRoyaltyCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoyaltyCardsViewModel : ViewModel() {

    private val TAG = "LoyaltyCardsViewModel"

    // Método para simular la creación o actualización de una tarjeta de lealtad
    fun simulateRoyaltyCardCreation(businessOwnerId: String, uniandesMemberId: String, storeId: String, maxPoints: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repositoryRoyaltyCard = RepositoryRoyaltyCard.instance

                // Verificar si ya existe una RoyaltyCard entre el businessOwner y uniandesMember
                val existingCard = repositoryRoyaltyCard.getRoyaltyCardByUserAndStore(uniandesMemberId, storeId)

                if (existingCard != null) {
                    // Si la tarjeta ya existe, verificar los puntos
                    if (existingCard.points < maxPoints) {
                        // Incrementar los puntos si no se ha alcanzado el máximo
                        existingCard.points += 1
                        repositoryRoyaltyCard.updateRoyaltyCard(existingCard)
                        Log.d(TAG, "Puntos incrementados. Ahora tienes ${existingCard.points} puntos.")
                    } else {
                        // Si alcanzó el máximo de puntos, marcar la tarjeta como inactiva
                        existingCard.isCurrent = false
                        repositoryRoyaltyCard.updateRoyaltyCard(existingCard)
                        Log.d(TAG, "Has alcanzado el máximo de puntos (${existingCard.maxPoints}). La tarjeta está inactiva.")
                    }
                } else {
                    // Si no existe, crear una nueva RoyaltyCard
                    val message = repositoryRoyaltyCard.addOrUpdateRoyaltyCard(
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
}
