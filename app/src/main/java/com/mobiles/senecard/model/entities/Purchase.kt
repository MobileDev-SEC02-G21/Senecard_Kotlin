package com.mobiles.senecard.model.entities

data class Purchase(
    val purchaseId: String = "",
    val userId: String = "",
    val storeId: String = "",
    val purchaseDescription: String = "",
    val date: String = "",
    val eligible: Boolean = true,
    val rating: Int = 0
)