package com.shinetech.tollview.models

import java.sql.Timestamp

data class Toll(
    val id: String,
    val userId: String,
    val gateId: String,
    val timestamp: Timestamp
)
