package com.mobiles.senecard.model.entities

data class RoyaltyCard(
    var id: String? = null,
    var uniandesMemberId: String? = null,
    var storeId: String? = null,
    var points: Int = 0,
    var maxPoints: Int = 0,
    var isCurrent: Boolean = true
) {
    // Constructor vacío para Firestore
    constructor() : this(null, null, null, 0, 0, true)
}