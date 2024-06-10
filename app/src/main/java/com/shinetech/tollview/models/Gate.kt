package com.shinetech.tollview.models

data class Gate(
    val id: String = "",
    val name: String = "",
    val sourceId: Int = 0,
    val type: String = "",
    val chargeType: String = "",
    val cost: Double = 0.0,
    val costWithoutTag: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val cardinality: String = ""
)
