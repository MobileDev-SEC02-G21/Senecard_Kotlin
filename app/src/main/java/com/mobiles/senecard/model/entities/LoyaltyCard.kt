package com.mobiles.senecard.model.entities

data class LoyaltyCard(
    var id: String? = null,
    val storeId: String? = null,
    val uniandesMemberId: String? = null,
    val maxPoints: Int? = null,
    var points: Int? = null,
    var isCurrent: Boolean? = null
)
