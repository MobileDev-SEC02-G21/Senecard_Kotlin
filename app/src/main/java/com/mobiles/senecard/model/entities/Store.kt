package com.mobiles.senecard.model.entities

data class Store (
    val id: String,
    val address: String,
    val category: String,
    val image: String,
    val name: String,
    val schedule: Map<String, List<Int>>,
    val userId: String
)