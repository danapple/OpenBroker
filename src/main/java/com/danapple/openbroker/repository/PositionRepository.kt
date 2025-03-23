package com.danapple.openbroker.repository

import com.danapple.openbroker.model.Position
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
open class PositionRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun findByUserIdAndSymbol(userId: Long, symbol: String): Position? {
        val sql = "SELECT * FROM positions WHERE user_id = :userId AND symbol = :symbol"
        val params = mapOf("userId" to userId, "symbol" to symbol)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            Position(
                positionId = rs.getLong("position_id"),
                userId = rs.getLong("user_id"),
                symbol = rs.getString("symbol"),
                quantity = rs.getInt("quantity"), // Changed to Int
                avgPrice = rs.getBigDecimal("avg_price")
            )
        }.firstOrNull()
    }

    fun getAllPositions(userId: Long): List<Position> {
        val sql = "SELECT * FROM positions WHERE user_id = :userId"
        val params = mapOf("userId" to userId)

        return jdbcTemplate.query(sql, params) { rs, _ ->
            Position(
                positionId = rs.getLong("position_id"),
                userId = rs.getLong("user_id"),
                symbol = rs.getString("symbol"),
                quantity = rs.getInt("quantity"), // Changed to Int
                avgPrice = rs.getBigDecimal("avg_price")
            )
        }
    }

    fun save(userId: Long, symbol: String, quantity: Int, avgPrice: BigDecimal) { // Changed to Int
        val sql = """
            INSERT INTO positions (user_id, symbol, quantity, avg_price)
            VALUES (:userId, :symbol, :quantity, :avgPrice)
        """.trimIndent()

        val params = mapOf(
            "userId" to userId,
            "symbol" to symbol,
            "quantity" to quantity,  // Changed to Int
            "avgPrice" to avgPrice
        )

        jdbcTemplate.update(sql, params)
    }

    fun update(userId: Long, symbol: String, quantity: Int, avgPrice: BigDecimal) { // Changed to Int
        val sql = """
            UPDATE positions 
            SET quantity = :quantity, avg_price = :avgPrice 
            WHERE user_id = :userId AND symbol = :symbol
        """.trimIndent()

        val params = mapOf(
            "userId" to userId,
            "symbol" to symbol,
            "quantity" to quantity,  // Changed to Int
            "avgPrice" to avgPrice
        )

        jdbcTemplate.update(sql, params)
    }
}
