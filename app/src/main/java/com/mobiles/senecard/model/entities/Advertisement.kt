package com.mobiles.senecard.model.entities

data class Advertisement (
    val id: String? = null,
    val store: Store? = null,
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val available: Boolean? = null
)