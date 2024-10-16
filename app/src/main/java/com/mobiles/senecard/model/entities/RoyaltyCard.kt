package com.mobiles.senecard.model.entities

data class RoyaltyCard(
    val id: String? = null,
    val userId: String, // ID del usuario que posee la tarjeta
    val storeId: String, // ID de la tienda asociada
    var points: Int, // Puntos acumulados
    val maxPoints: Int // Máximo de puntos
)
