package com.auctionapp.com.auctionapp.application.controller

import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auctions")
class AuctionController(
    private val strategyRegistry: ConcurrencyControlStrategyRegistry
) {
    @GetMapping("/admin/strategy")
    fun getCurrentStrategy(): String {
        return strategyRegistry.getCurrentStrategyName()
    }

    @PostMapping("/admin/strategy")
    fun changeStrategy(@RequestParam strategy: String): String {
        strategyRegistry.setCurrentStrategy(strategy)
        return strategyRegistry.getCurrentStrategyName()
    }
}
