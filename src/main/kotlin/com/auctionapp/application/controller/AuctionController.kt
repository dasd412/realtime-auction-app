package com.auctionapp.com.auctionapp.application.controller

import com.auctionapp.application.dto.request.PlaceBidRequest
import com.auctionapp.application.dto.request.RegisterAuctionRequest
import com.auctionapp.application.dto.response.*
import com.auctionapp.application.service.AuctionAppService
import com.auctionapp.application.service.AuctionSortType
import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import com.auctionapp.domain.entity.AuctionStatus
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/auctions")
@SuppressWarnings("unused")
class AuctionController(
    private val auctionAppService: AuctionAppService,
    private val strategyRegistry: ConcurrencyControlStrategyRegistry,
) {
    @PostMapping
    fun registerAuction(
        @Valid @RequestBody request: RegisterAuctionRequest,
    ): ResponseEntity<AuctionRegisterResponse> {
        val auctionId =
            auctionAppService.registerAuction(
                productId = request.productId,
                initialPrice = request.initialPrice,
                minimumBidUnit = request.minimumBidUnit,
                startTime = request.startTime,
                endTime = request.endTime,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionRegisterResponse(auctionId))
    }

    @GetMapping
    fun getAuctionList(
        @RequestParam(defaultValue = "ONGOING") status: AuctionStatus,
        @RequestParam(defaultValue = "NONE") sortType: AuctionSortType,
        @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<AuctionListResponse> {
        return ResponseEntity.ok(auctionAppService.getAuctionList(status, sortType, pageNumber).toListResponse())
    }

    // 경매 상세 조회
    @GetMapping("/{auctionId}")
    fun getAuctionDetail(
        @PathVariable auctionId: Long,
    ): ResponseEntity<AuctionDetailResponse> {
        return ResponseEntity.ok(auctionAppService.getAuctionDetail(auctionId).toResponse())
    }

    @DeleteMapping("/{auctionId}")
    fun cancelAuction(
        @PathVariable auctionId: Long,
    ): ResponseEntity<AuctionCancelResponse> {
        auctionAppService.cancelAuction(auctionId)
        return ResponseEntity.ok(AuctionCancelResponse(auctionId))
    }

    // 입찰 진행
    @PostMapping("/{auctionId}/bids")
    fun placeBid(
        @PathVariable auctionId: Long,
        @Valid @RequestBody request: PlaceBidRequest,
    ): ResponseEntity<PlaceBidResponse> {
        val bidId = auctionAppService.placeBid(auctionId, request.amount)
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaceBidResponse(bidId))
    }

    // 입찰 진행 (Redis Lock 사용)
    @PostMapping("/{auctionId}/bids/distributed")
    fun placeBidWithRedisLock(
        @PathVariable auctionId: Long,
        @Valid @RequestBody request: PlaceBidRequest,
    ): ResponseEntity<PlaceBidResponse> {
        val bidId = auctionAppService.placeBidWithRedisLock(auctionId, request.amount)
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaceBidResponse(bidId))
    }

    // 특정 경매의 입찰 내역 조회
    @GetMapping("/{auctionId}/bids")
    fun getAuctionBids(
        @PathVariable auctionId: Long,
        @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<BidListResponse> {
        return ResponseEntity.ok(auctionAppService.getBidsOfAuction(auctionId, pageNumber).toListResponse())
    }

    // 내가 생성한 경매 목록 조회
    @GetMapping("/my-auctions")
    fun getMyAuctions(
        @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<AuctionListResponse> {
        return ResponseEntity.ok(auctionAppService.getAuctionsOfAuctionOwner(pageNumber).toListResponse())
    }

    // 내가 입찰한 경매 목록 조회
    @GetMapping("/my-bids/auctions")
    fun getMyBiddingAuctions(
        @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<AuctionListResponse> {
        return ResponseEntity.ok(auctionAppService.getAuctionsOfBidder(pageNumber).toListResponse())
    }

    // 내 입찰 내역 조회
    @GetMapping("/my-bids")
    fun getMyBids(
        @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<BidListResponse> {
        return ResponseEntity.ok(auctionAppService.getBidsOfUser(pageNumber).toListResponse())
    }

    @GetMapping("/admin/strategy")
    fun getCurrentStrategy(): ResponseEntity<StrategyResponse> {
        return ResponseEntity.ok(StrategyResponse(strategyRegistry.getCurrentStrategyName()))
    }

    @PostMapping("/admin/strategy")
    fun changeStrategy(
        @RequestParam strategy: String,
    ): ResponseEntity<StrategyResponse> {
        strategyRegistry.setCurrentStrategy(strategy)
        return ResponseEntity.ok(StrategyResponse(strategyRegistry.getCurrentStrategyName()))
    }
}
