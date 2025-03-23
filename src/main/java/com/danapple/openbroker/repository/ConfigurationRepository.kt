package com.danapple.openbroker.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class ConfigurationRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun getReconnectDelay(): Int? {
        val sql = "SELECT value FROM configuration WHERE key = :key"
        val params = mapOf("key" to "reconnect_delay")
        return jdbcTemplate.queryForObject(sql, params, Int::class.java)
    }

    fun getBrokerId(): String? {
        val sql = "SELECT value FROM configuration WHERE key = :key"
        val params = mapOf("key" to "broker_id")
        return jdbcTemplate.queryForObject(sql, params, String::class.java)
    }
}