package com.auctionapp

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestRedisConfig {
    @Bean
    fun redissonClient(
        @Value("\${spring.redis.port:16379}") redisPort: Int,
    ): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://localhost:$redisPort")
        return Redisson.create(config)
    }
}
