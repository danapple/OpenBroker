package com.danapple.openbroker.websocket

import com.danapple.openbroker.repository.ConfigurationRepository
import com.danapple.openbroker.service.BrokerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.concurrent.TimeUnit

@Service
class ExchangeWebSocketClient(
    private val brokerService: BrokerService,
    private val configurationRepository: ConfigurationRepository
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(ExchangeWebSocketClient::class.java)
    private val objectMapper = jacksonObjectMapper()
    private var session: WebSocketSession? = null

    private lateinit var brokerId: String
    private var exchangeUrl: String? = null // To store the URL for reconnection
    private var reconnectDelay: Int = 5 // Default reconnect delay is 5 seconds

    fun connect(exchangeUrl: String) {
        this.exchangeUrl = exchangeUrl // Store the URL for reconnection
        reconnectDelay = configurationRepository.getReconnectDelay() ?: 5 // Retrieve reconnect delay from DB
        val client = StandardWebSocketClient()
        client.doHandshake(this, exchangeUrl)
        logger.info("Connecting to exchange WebSocket at $exchangeUrl")
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        this.session = session
        logger.info("Connected to exchange WebSocket")

        // Retrieve brokerId from DB
        brokerId = configurationRepository.getBrokerId() ?: throw IllegalStateException("Broker ID not found in configuration table")

        // Subscribe with brokerId once connection is established
        subscribeToBroker(brokerId)
    }

    fun subscribeToBroker(brokerId: String) {
        val subscriptionMessage = """{ "action": "subscribe", "brokerId": "$brokerId" }"""
        session?.sendMessage(TextMessage(subscriptionMessage))
        logger.info("Subscribed to WebSocket with brokerId: $brokerId")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val payload = message.payload.toString()
        logger.info("Received message from exchange: $payload")

        try {
            val tradeUpdate: TradeUpdate = objectMapper.readValue(payload)
            processTradeUpdate(tradeUpdate)
        } catch (e: Exception) {
            logger.error("Error processing trade message: ${e.message}", e)
        }
    }

    private fun processTradeUpdate(tradeUpdate: TradeUpdate) {
        // Pass the TradeUpdate object to BrokerService for handling
        brokerService.handleTradeUpdate(tradeUpdate)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("WebSocket transport error: ${exception.message}", exception)
        reconnect()
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.info("Exchange WebSocket closed: $closeStatus")
        reconnect()
    }

    private fun reconnect() {
        logger.info("Attempting to reconnect to WebSocket...")
        try {
            // Wait for a brief moment before reconnecting to avoid tight looping
            TimeUnit.SECONDS.sleep(reconnectDelay.toLong())

            // Use the stored exchangeUrl for reconnection
            if (exchangeUrl != null) {
                connect(exchangeUrl!!)  // Try to reconnect with the previously stored URL
            } else {
                logger.error("No exchange URL available for reconnection.")
            }
        } catch (e: InterruptedException) {
            logger.error("Reconnect attempt interrupted", e)
        }
    }

    override fun supportsPartialMessages(): Boolean = false
}
