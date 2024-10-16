package com.mobiles.senecard.model.entities

data class Purchase(
    val id: String? = null,
    val storeId: String? = null,
    val uniandesMemberId: String? = null,
    val date: String? = null,
    val eligible: Boolean? = null,
    val rating: Double? = null
)