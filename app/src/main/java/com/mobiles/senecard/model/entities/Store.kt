package com.mobiles.senecard.model.entities

data class Store(
    val storeId: String = "",         // Unique ID for the store
    val name: String = "",            // Store name
    val address: String = "",         // Store address
    val category: String = "",        // Store category
    val imageUrl: String = "",        // Store image URL
    val rating: Double = 0.0,         // Store rating
    val businessOwnerId: String = "", // Foreign key to the user ID (business owner)
    val schedule: Map<String, Any> = emptyMap()  // Store schedule
)
