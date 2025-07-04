package com.auctionapp.com.auctionapp.expriment.concurrency

import com.auctionapp.domain.service.AuctionService
import com.auctionapp.expriment.concurrency.strategy.*
import com.auctionapp.infrastructure.persistence.AuctionRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class ConcurrencyControlStrategyRegistry(
    private val auctionService: AuctionService,
    private val auctionRepository: AuctionRepository,
) {
    private val strategies = mutableMapOf<String, ConcurrencyControlStrategy>()
    private var currentStrategy: String = "optimistic" // 기본값

    @PostConstruct
    fun init() {
        strategies["optimistic"] = OptimisticLockingStrategy(auctionService)
        strategies["pessimistic"] = PessimisticLockingStrategy(auctionService, auctionRepository)
        strategies["synchronized"] = SynchronizedStrategy(auctionService)
        strategies["tryLock"] = TryLockStrategy(auctionService)
        strategies["semaphore"] = SemaphoreStrategy(auctionService)
    }

    fun getCurrentStrategy(): ConcurrencyControlStrategy = strategies[currentStrategy]!!

    fun setCurrentStrategy(strategyName: String) {
        require(strategies.containsKey(strategyName)) { "Unknown strategy: $strategyName" }
        currentStrategy = strategyName
    }

    fun getCurrentStrategyName(): String = currentStrategy
}
