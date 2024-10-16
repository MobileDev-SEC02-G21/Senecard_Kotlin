package com.mobiles.senecard.model.entities

data class Purchase(
    val id: String? = null,
    val loyaltyCardId: String? = null,
    val date: String? = null,
    val isEligible: Boolean? = null,
    val rating: Double? = null
)