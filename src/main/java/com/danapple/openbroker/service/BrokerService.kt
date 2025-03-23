package com.danapple.openbroker.service

import com.danapple.openbroker.model.OrderStatus
import com.danapple.openbroker.model.Order
import com.danapple.openbroker.model.Position
import com.danapple.openbroker.model.Trade
import com.danapple.openbroker.repository.OrderRepository
import com.danapple.openbroker.repository.PositionRepository
import com.danapple.openbroker.repository.TradeRepository
import com.danapple.openbroker.websocket.TradeUpdate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.UUID

@Service
class BrokerService(
    private val orderRepository: OrderRepository,
    private val positionRepository: PositionRepository,
    private val tradeRepository: TradeRepository,
    private val restTemplate: RestTemplate
) {
    private val stockExchangeUrl = "https://api.openexchange.com/orders"

    fun placeOrder(order: Order): String {
        // Generate a unique clientOrderId (e.g., using UUID or a combination of userId and timestamp)
        val generatedClientOrderId = generateClientOrderId(order.userId)

        // Override the clientOrderId in the order
        val orderWithClientOrderId = order.copy(clientOrderId = generatedClientOrderId)

        // Check if there's an open position for the given symbol
        val position = positionRepository.findByUserIdAndSymbol(order.userId, order.symbol)

        // Check if the order flips the position
        if (position != null && willFlipPosition(orderWithClientOrderId, position)) {
            return "Order rejected: Attempt to flip the position (buy/sell mismatch)."
        }

        // Save the order in the broker's database
        val orderId = orderRepository.saveOrder(orderWithClientOrderId)

        // Send order to OpenExchange
        val response: ResponseEntity<String> = restTemplate.exchange(
            stockExchangeUrl,
            HttpMethod.POST,
            HttpEntity(orderWithClientOrderId),
            String::class.java
        )

        return if (response.statusCode.is2xxSuccessful) {
            "Order $orderId placed successfully with the stock exchange."
        } else {
            "Order $orderId failed to place."
        }
    }

    fun cancelOrder(orderId: Long): String {
        val order = orderRepository.getOrderById(orderId) ?: return "Order $orderId does not exist."

        if (order.orderStatus == OrderStatus.FILLED) {
            return "Order $orderId has already been executed and cannot be canceled."
        }

        // Send cancel request to OpenExchange
        val exchangeCancelUrl = "https://api.openexchange.com/orders/$orderId/cancel"
        val response: ResponseEntity<String> = restTemplate.exchange(
            exchangeCancelUrl,
            HttpMethod.DELETE,
            null,
            String::class.java
        )

        return if (response.statusCode.is2xxSuccessful) {
            // If OpenExchange acknowledges cancellation, update the local order status to CANCELED
            orderRepository.updateOrder(order.copy(orderStatus = OrderStatus.CANCELLED))
            "Order $orderId successfully canceled on both broker and stock exchange."
        } else {
            "Failed to send cancel request for order $orderId to the stock exchange."
        }
    }

    private fun willFlipPosition(order: Order, position: Position): Boolean {
        // If position exists, check if the order will flip the position
        return when {
            order.quantity > 0 && position.quantity < 0 -> true  // Buy order with a short position
            order.quantity < 0 && position.quantity > 0 -> true  // Sell order with a long position
            else -> false
        }
    }

    private fun generateClientOrderId(userId: Long): String {
        // Generate a unique clientOrderId (could be a UUID or a combination of userId and timestamp)
        val uniqueId = UUID.randomUUID().toString()
        return "USER$userId-$uniqueId-${Instant.now().toEpochMilli()}"
    }

    fun handleTradeUpdate(tradeUpdate: TradeUpdate) {
        // Find the order associated with the trade update
        val order = orderRepository.getOrderByClientOrderId(tradeUpdate.clientOrderId)
            ?: throw IllegalArgumentException("Order not found for clientOrderId: ${tradeUpdate.clientOrderId}")

        val newFilledQuantity = order.filledQuantity + tradeUpdate.quantity

        // Update the order status based on the filled quantity
        val newStatus = when {
            newFilledQuantity < order.quantity -> OrderStatus.PARTIALLY_FILLED
            else -> OrderStatus.FILLED
        }

        // Update the order in the database
        val updatedOrder = order.copy(filledQuantity = newFilledQuantity, orderStatus = newStatus)
        orderRepository.updateOrder(updatedOrder) // Pass the updated Order object

        // Update the position in the database
        positionRepository.update(order.userId, order.symbol, tradeUpdate.quantity, tradeUpdate.price)

        // Create and save the trade
        val trade = Trade(
            tradeId = generateTradeId(), // Ensure you generate a unique trade ID
            orderId = order.orderId,     // Now referring to orderId
            symbol = order.symbol,
            price = tradeUpdate.price,
            quantity = tradeUpdate.quantity,
            tradeTime = Instant.now()
        )
        tradeRepository.save(trade)

        // Log the processed trade
        println("Processed trade: $trade, Updated order: ${order.orderId}, New filled qty: $newFilledQuantity, Status: $newStatus")
    }

    // Generate a unique trade ID (could be a simple approach or use a database sequence)
    private fun generateTradeId(): Long {
        // This method could be more sophisticated based on your requirements.
        // A simple approach could be to use a timestamp or a sequence in the database.
        return System.currentTimeMillis()
    }
}
