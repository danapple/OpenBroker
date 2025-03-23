package com.danapple.openbroker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class OpenBrokerApplication

fun main(args: Array<String>) {
    runApplication<OpenBrokerApplication>(*args)
}
