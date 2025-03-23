package com.danapple.openbroker.model

import java.math.BigDecimal
import java.time.Instant

data class Order(
    val orderId: Long,           // Surrogate key in DB
    val clientOrderId: String,   // Unique order ID for tracking with the exchange
    val userId: Long,            // Orders are tracked per user
    val symbol: String,          // Stock ticker symbol
    val quantity: Int,           // Positive for buy, negative for sell
    val price: BigDecimal,       // Limit price of the order (changed to BigDecimal)
    val orderStatus: OrderStatus, // Uses enum for status tracking
    val filledQuantity: Int,     // Tracks how much of the order is filled
    val createdAt: Instant       // Timestamp for order creation
)
