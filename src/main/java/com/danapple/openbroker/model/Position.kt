package com.danapple.openbroker.model

import java.math.BigDecimal

data class Position(
    val positionId: Long,
    val userId: Long,
    val symbol: String,
    val quantity: Int, // Changed to Int
    val avgPrice: BigDecimal // New field to track the average price of the position
)
