package com.shinetech.tollview.models

import java.sql.Timestamp

data class Toll(
    val gateId: String = "",
    val timestamp: Timestamp? = null
)

