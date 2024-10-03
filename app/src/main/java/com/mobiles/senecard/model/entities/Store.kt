package com.mobiles.senecard.model.entities

data class Store (
    val id: String? = null,
    val businessOwnerId: String? = null,
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
    val image: String? = null,
    val schedule: Map<String, List<Int>>? = null,
    val rating: Double? = null
)