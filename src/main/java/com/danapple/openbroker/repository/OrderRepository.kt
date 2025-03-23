package com.danapple.openbroker.repository

import com.danapple.openbroker.model.Order
import com.danapple.openbroker.model.OrderStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
open class OrderRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    // Save order and return generated orderId
    fun saveOrder(order: Order): Long {
        val sql = """
            INSERT INTO orders (clientOrderId, userId, symbol, quantity, filledQuantity, price, orderStatus, createdAt) 
            VALUES (:clientOrderId, :userId, :symbol, :quantity, :filledQuantity, :price, :orderStatus, :createdAt)
        """
        val params = mapOf(
            "clientOrderId" to order.clientOrderId,
            "userId" to order.userId,
            "symbol" to order.symbol,
            "quantity" to order.quantity,
            "filledQuantity" to order.filledQuantity,
            "price" to order.price,
            "orderStatus" to order.orderStatus.name,
            "createdAt" to order.createdAt
        )

        // Execute insert query
        jdbcTemplate.update(sql, params)

        // Get the generated orderId (assuming MySQL with LAST_INSERT_ID)
        val generatedOrderId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", emptyMap<String, Any>(), Long::class.java)
        return generatedOrderId ?: throw IllegalStateException("Failed to retrieve generated orderId.")
    }

    // Update the order based on orderId
    fun updateOrder(order: Order) {
        val sql = """
            UPDATE orders 
            SET filledQuantity = :filledQuantity, orderStatus = :orderStatus 
            WHERE orderId = :orderId
        """
        val params = mapOf(
            "orderId" to order.orderId,
            "filledQuantity" to order.filledQuantity,
            "orderStatus" to order.orderStatus.name
        )

        // Execute update query
        jdbcTemplate.update(sql, params)
    }

    // Get order by orderId
    fun getOrderById(orderId: Long): Order? {
        val sql = "SELECT * FROM orders WHERE orderId = :orderId"
        val params = mapOf("orderId" to orderId)

        // Query and map the result set to Order
        return jdbcTemplate.query(sql, params) { rs, _ ->
            Order(
                orderId = rs.getLong("orderId"),
                clientOrderId = rs.getString("clientOrderId"),
                userId = rs.getLong("userId"),
                symbol = rs.getString("symbol"),
                quantity = rs.getInt("quantity"),
                filledQuantity = rs.getInt("filledQuantity"),
                price = rs.getBigDecimal("price"),
                orderStatus = OrderStatus.valueOf(rs.getString("orderStatus")),
                createdAt = rs.getTimestamp("createdAt").toInstant()
            )
        }.firstOrNull()
    }

    // Get order by clientOrderId
    fun getOrderByClientOrderId(clientOrderId: String): Order? {
        val sql = "SELECT * FROM orders WHERE clientOrderId = :clientOrderId"
        val params = mapOf("clientOrderId" to clientOrderId)

        // Query and map the result set to Order
        return jdbcTemplate.query(sql, params) { rs, _ ->
            Order(
                orderId = rs.getLong("orderId"),
                clientOrderId = rs.getString("clientOrderId"),
                userId = rs.getLong("userId"),
                symbol = rs.getString("symbol"),
                quantity = rs.getInt("quantity"),
                filledQuantity = rs.getInt("filledQuantity"),
                price = rs.getBigDecimal("price"),
                orderStatus = OrderStatus.valueOf(rs.getString("orderStatus")),
                createdAt = rs.getTimestamp("createdAt").toInstant()
            )
        }.firstOrNull()
    }

    // Get all orders by userId
    fun getOrdersByUserId(userId: Long): List<Order> {
        val sql = "SELECT * FROM orders WHERE userId = :userId"
        val params = mapOf("userId" to userId)

        // Query and map the result set to a list of Order objects
        return jdbcTemplate.query(sql, params) { rs, _ ->
            Order(
                orderId = rs.getLong("orderId"),
                clientOrderId = rs.getString("clientOrderId"),
                userId = rs.getLong("userId"),
                symbol = rs.getString("symbol"),
                quantity = rs.getInt("quantity"),
                filledQuantity = rs.getInt("filledQuantity"),
                price = rs.getBigDecimal("price"),
                orderStatus = OrderStatus.valueOf(rs.getString("orderStatus")),
                createdAt = rs.getTimestamp("createdAt").toInstant()
            )
        }
    }
}
