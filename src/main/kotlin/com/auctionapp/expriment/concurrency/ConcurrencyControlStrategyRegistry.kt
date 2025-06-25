package com.auctionapp.com.auctionapp.expriment.concurrency

import com.auctionapp.domain.service.AuctionService
import com.auctionapp.expriment.concurrency.strategy.ConcurrencyControlStrategy
import com.auctionapp.expriment.concurrency.strategy.OptimisticLockingStrategy
import com.auctionapp.expriment.concurrency.strategy.PessimisticLockingStrategy
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class ConcurrencyControlStrategyRegistry(
    private val auctionService: AuctionService,
) {
    private val strategies = mutableMapOf<String, ConcurrencyControlStrategy>()
    private var currentStrategy: String = "optimistic" // 기본값

    @PostConstruct
    fun init() {
        strategies["optimistic"] = OptimisticLockingStrategy(auctionService)
        strategies["pessimistic"] = PessimisticLockingStrategy(auctionService)
    }

    fun getCurrentStrategy(): ConcurrencyControlStrategy = strategies[currentStrategy]!!

    // todo 런타임에 집어 넣을 수 있도록 컨트롤러에서 지정해야 함.
    fun setCurrentStrategy(strategyName: String) {
        require(strategies.containsKey(strategyName)) { "Unknown strategy: $strategyName" }
        currentStrategy = strategyName
    }

    fun getCurrentStrategyName(): String = currentStrategy
}
