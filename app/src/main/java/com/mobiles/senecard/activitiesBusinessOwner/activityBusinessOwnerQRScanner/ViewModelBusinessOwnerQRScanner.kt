package com.mobiles.senecard.activitiesBusinessOwner.activityBusinessOwnerQRScanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiles.senecard.model.RepositoryLoyaltyCard
import com.mobiles.senecard.model.RepositoryUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelBusinessOwnerQRScanner : ViewModel() {

    private val repositoryUser = RepositoryUser.instance
    private val repository = RepositoryLoyaltyCard.instance

    private val TAG = "LoyaltyCardsViewModel"

    private val _navigateToSuccess = MutableLiveData<Boolean>()
    val navigateToSuccess: LiveData<Boolean> get() = _navigateToSuccess

    private val _navigateToFailure = MutableLiveData<Boolean>()
    val navigateToFailure: LiveData<Boolean> get() = _navigateToFailure

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData to hold the userId once a user is found
    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    // Function to process the QR code
    fun processQRCode(qrCode: String) {
        viewModelScope.launch {
            Log.d("QRScannerViewModel", "Processing QR Code: $qrCode")

            try {
                // Fetch the user by QR code
                Log.d("QRScannerViewModel", "Attempting to fetch user with ID: $qrCode")
                val user = repositoryUser.getUserById(qrCode)

                if (user != null) {
                    Log.d("QRScannerViewModel", "User found: ${user.name}")
                    _userId.value = qrCode // Save the user ID
                    _navigateToSuccess.value = true
                } else {
                    Log.e("QRScannerViewModel", "No user found for QR Code: $qrCode")
                    _errorMessage.value = "User not found"
                    _navigateToFailure.value = true
                }
            } catch (e: Exception) {
                Log.e("QRScannerViewModel", "Error while processing QR code: ${e.message}")
                _errorMessage.value = "Error processing QR Code"
                _navigateToFailure.value = true
            }
        }
    }

    // This function resets the navigation flag after the navigation is triggered
    fun onNavigated() {
        _navigateToSuccess.value = false
        _navigateToFailure.value = false
    }
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
}
