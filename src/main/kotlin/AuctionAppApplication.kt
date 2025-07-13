package com.auctionapp

import org.redisson.spring.starter.RedissonAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        RedissonAutoConfiguration::class,
        RedisAutoConfiguration::class,
        RedisRepositoriesAutoConfiguration::class,
    ],
)
class AuctionAppApplication

fun main(args: Array<String>) {
    runApplication<AuctionAppApplication>(*args)
}
