package com.danapple.openbroker.model

import java.math.BigDecimal
import java.time.Instant

data class Trade(
    val tradeId: Long,
    val orderId: Long,            // Changed from clientOrderId to orderId
    val symbol: String,
    val price: BigDecimal,
    val quantity: Int,           // ✅ Kept as Int for consistency
    val tradeTime: Instant
)
