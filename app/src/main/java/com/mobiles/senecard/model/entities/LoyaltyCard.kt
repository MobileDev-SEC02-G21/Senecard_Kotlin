package com.mobiles.senecard.model.entities

data class LoyaltyCard(
    val id: String? = null,
    val storeId: String? = null,
    val uniandesMemberId: String? = null,
    val maxPoints: Int? = null,
    val points: Int? = null,
    val isCurrent: Boolean? = null
)
