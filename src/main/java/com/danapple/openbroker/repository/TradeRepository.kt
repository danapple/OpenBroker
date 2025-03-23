package com.danapple.openbroker.repository

import com.danapple.openbroker.model.Trade
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class TradeRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun save(trade: Trade) {
        val sql = """
            INSERT INTO trades (order_id, symbol, price, quantity, trade_time)
            VALUES (:orderId, :symbol, :price, :quantity, :tradeTime)
        """
        val params = mapOf(
            "orderId" to trade.orderId,
            "symbol" to trade.symbol,
            "price" to trade.price,
            "quantity" to trade.quantity,
            "tradeTime" to trade.tradeTime
        )
        jdbcTemplate.update(sql, params)
    }

    fun getTradeById(tradeId: Long): Trade? {
        val sql = "SELECT * FROM trades WHERE trade_id = :tradeId"
        val params = mapOf("tradeId" to tradeId)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            Trade(
                tradeId = rs.getLong("trade_id"),
                orderId = rs.getLong("order_id"),
                symbol = rs.getString("symbol"),
                price = rs.getBigDecimal("price"),
                quantity = rs.getInt("quantity"),
                tradeTime = rs.getTimestamp("trade_time").toInstant()
            )
        }.firstOrNull()
    }

    fun getTradesByOrderId(orderId: Long): List<Trade> {
        val sql = "SELECT * FROM trades WHERE order_id = :orderId"
        val params = mapOf("orderId" to orderId)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            Trade(
                tradeId = rs.getLong("trade_id"),
                orderId = rs.getLong("order_id"),
                symbol = rs.getString("symbol"),
                price = rs.getBigDecimal("price"),
                quantity = rs.getInt("quantity"),
                tradeTime = rs.getTimestamp("trade_time").toInstant()
            )
        }
    }
}
