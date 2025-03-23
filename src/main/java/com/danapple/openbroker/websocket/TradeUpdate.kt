package com.danapple.openbroker.websocket

import java.math.BigDecimal

data class TradeUpdate(
    val clientOrderId: String, // Unique identifier for the order from the client
    val quantity: Int,         // Quantity of the trade (positive or negative)
    val price: BigDecimal      // Price at which the trade was executed
)
