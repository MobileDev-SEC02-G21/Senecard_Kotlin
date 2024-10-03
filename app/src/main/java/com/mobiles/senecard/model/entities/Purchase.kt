package com.mobiles.senecard.model.entities

data class Purchase(
    val purchaseId: String = "",        // Unique purchase ID
    val userId: String = "",            // Foreign key to the user ID
    val storeId: String = "",           // Foreign key to the store ID
    val purchaseDescription: String = "", // Description of the purchase (e.g., 'Burrito: 1 meat')
    val date: String = "",              // Date of the purchase
    val eligible: Boolean = true,       // Is the purchase eligible for loyalty rewards?
    val rating: Int = 0                 // User rating of the purchase
)