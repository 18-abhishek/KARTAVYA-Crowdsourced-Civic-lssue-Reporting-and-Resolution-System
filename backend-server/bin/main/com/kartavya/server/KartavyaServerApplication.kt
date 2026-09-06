package com.kartavya.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KartavyaServerApplication

fun main(args: Array<String>) {
    runApplication<KartavyaServerApplication>(*args)
}
