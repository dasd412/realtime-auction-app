package com.auctionapp.com.auctionapp.application.controller

import com.auctionapp.application.dto.request.PlaceBidRequest
import com.auctionapp.application.dto.request.RegisterAuctionRequest
import com.auctionapp.application.service.AuctionAppService
import com.auctionapp.application.service.AuctionSortType
import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import com.auctionapp.domain.entity.AuctionStatus
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auctions")
class AuctionController(
    private val auctionAppService: AuctionAppService,
    private val strategyRegistry: ConcurrencyControlStrategyRegistry,
) {

    @PostMapping
    fun registerAuction(@Valid @RequestBody request: RegisterAuctionRequest): ResponseEntity<Map<String, Long>> {
        auctionAppService.registerAuction(
            productId = request.productId,
            initialPrice = request.initialPrice,
            minimumBidUnit = request.minimumBidUnit,
            startTime = request.startTime,
            endTime = request.endTime
        )

        return ResponseEntity.ok(mapOf("message" to 1L))
    }

    @GetMapping
    fun getAuctionList(
        @RequestParam(defaultValue = "ONGOING") status: AuctionStatus,
        @RequestParam(defaultValue = "NONE") sortType: AuctionSortType,
        @RequestParam(defaultValue = "0") pageNumber: Int
    ) = auctionAppService.getAuctionList(status, sortType, pageNumber)

    // 경매 상세 조회
    @GetMapping("/{auctionId}")
    fun getAuctionDetail(@PathVariable auctionId: Long) = auctionAppService.getAuctionDetail(auctionId)

    @DeleteMapping("/{auctionId}")
    fun cancelAuction(@PathVariable auctionId: Long): ResponseEntity<Map<String, String>> {
        auctionAppService.cancelAuction(auctionId)
        return ResponseEntity.ok(mapOf("message" to "경매가 취소되었습니다."))
    }

    // 입찰 진행
    @PostMapping("/{auctionId}/bids")
    fun placeBid(
        @PathVariable auctionId: Long,
        @Valid @RequestBody request: PlaceBidRequest
    ): ResponseEntity<Map<String, Long>> {
        val bidId = auctionAppService.placeBid(auctionId, request.amount)
        return ResponseEntity.ok(mapOf("bidId" to bidId))
    }

    // 입찰 진행 (Redis Lock 사용)
    @PostMapping("/{auctionId}/bids/distributed")
    fun placeBidWithRedisLock(
        @PathVariable auctionId: Long,
        @Valid @RequestBody request: PlaceBidRequest
    ): ResponseEntity<Map<String, Long>> {
        val bidId = auctionAppService.placeBidWithRedisLock(auctionId, request.amount)
        return ResponseEntity.ok(mapOf("bidId" to bidId))
    }

    // 특정 경매의 입찰 내역 조회
    @GetMapping("/{auctionId}/bids")
    fun getAuctionBids(
        @PathVariable auctionId: Long,
        @RequestParam(defaultValue = "0") pageNumber: Int
    ) = auctionAppService.getBidsOfAuction(auctionId, pageNumber)

    // 내가 생성한 경매 목록 조회
    @GetMapping("/my-auctions")
    fun getMyAuctions(@RequestParam(defaultValue = "0") pageNumber: Int) =
        auctionAppService.getAuctionsOfAuctionOwner(pageNumber)

    // 내가 입찰한 경매 목록 조회
    @GetMapping("/my-bids/auctions")
    fun getMyBiddingAuctions(@RequestParam(defaultValue = "0") pageNumber: Int) =
        auctionAppService.getAuctionsOfBidder(pageNumber)

    // 내 입찰 내역 조회
    @GetMapping("/my-bids")
    fun getMyBids(@RequestParam(defaultValue = "0") pageNumber: Int) =
        auctionAppService.getBidsOfUser(pageNumber)

    @GetMapping("/admin/strategy")
    fun getCurrentStrategy(): String {
        return strategyRegistry.getCurrentStrategyName()
    }

    @PostMapping("/admin/strategy")
    fun changeStrategy(
        @RequestParam strategy: String,
    ): String {
        strategyRegistry.setCurrentStrategy(strategy)
        return strategyRegistry.getCurrentStrategyName()
    }
}
