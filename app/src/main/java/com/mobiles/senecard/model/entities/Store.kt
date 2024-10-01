package com.mobiles.senecard.model.entities

data class Store (
    val id: String,
    val businessOwnerId: String,
    val name: String,
    val address: String,
    val category: String,
    val image: String,
    val schedule: Map<String, List<Int>>,
    val rating: Number?
)