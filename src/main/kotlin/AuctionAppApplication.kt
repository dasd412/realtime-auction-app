package com.auctionapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuctionAppApplication

fun main(args: Array<String>) {
    runApplication<AuctionAppApplication>(*args)
}
