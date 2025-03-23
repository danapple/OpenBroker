package com.danapple.openbroker.controller

import com.danapple.openbroker.model.Order
import com.danapple.openbroker.model.Position
import com.danapple.openbroker.repository.OrderRepository
import com.danapple.openbroker.repository.PositionRepository
import com.danapple.openbroker.service.BrokerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/broker")
open class BrokerController(
    private val brokerService: BrokerService,
    private val orderRepository: OrderRepository,
    private val positionRepository: PositionRepository
) {

    @PostMapping("/orders")
    fun placeOrder(@RequestBody order: Order): ResponseEntity<String> {
        val response = brokerService.placeOrder(order)
        return ResponseEntity.ok(response)
    }

    @RestController
    @RequestMapping("/api")
    class BrokerController(private val orderRepository: OrderRepository, private val positionRepository: PositionRepository) {

        @GetMapping("/orders/{userId}")
        fun getOrders(@PathVariable userId: Long): List<Order> {
            return orderRepository.getOrdersByUserId(userId)
        }

        @GetMapping("/positions/{userId}")
        fun getPositions(@PathVariable userId: Long): List<Position> {
            return positionRepository.getAllPositions(userId)
        }
    }


    @DeleteMapping("/orders/{orderId}")
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<String> {
        val response = brokerService.cancelOrder(orderId)
        return ResponseEntity.ok(response)
    }
}
