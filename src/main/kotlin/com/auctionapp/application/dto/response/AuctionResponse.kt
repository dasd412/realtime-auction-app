package com.auctionapp.application.dto.response

import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.domain.entity.Bid
import com.auctionapp.infrastructure.persistence.AuctionDetail
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class AuctionRegisterResponse(
    val auctionId: Long,
)

data class AuctionCancelResponse(
    val auctionId: Long,
)

data class BidResponse(
    val bidId: Long,
)

data class AuctionDetailResponse(
    val auctionId: Long,
    val productId: Long,
    val productName: String,
    val productImageUrl: String,
    val sellerId: Long,
    val sellerName: String,
    val initialPrice: Long,
    val minimumBidUnit: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: AuctionStatus,
    val currentHighestBid: Long?,
    val bidCount: Long,
)

fun AuctionDetail.toResponse(): AuctionDetailResponse {
    val auction = this.getAuction()
    return AuctionDetailResponse(
        auctionId = auction.id!!,
        productId = auction.product.id!!,
        productName = auction.product.name,
        productImageUrl = auction.product.imageUrl,
        sellerId = auction.user.id!!,
        sellerName = auction.user.name,
        initialPrice = auction.initialPrice.amount,
        minimumBidUnit = auction.minimumBidUnit.amount,
        startTime = auction.startTime,
        endTime = auction.endTime,
        status = auction.status,
        currentHighestBid = this.getHighestBidAmount(),
        bidCount = this.getBidCount(),
    )
}

data class AuctionSimpleResponse(
    val auctionId: Long,
    val productName: String,
    val productImageUrl: String,
    val currentHighestBid: Long?,
    val endTime: LocalDateTime,
    val status: AuctionStatus,
)

fun Auction.toSimpleResponse(highestBid: Long? = null): AuctionSimpleResponse {
    return AuctionSimpleResponse(
        auctionId = this.id!!,
        productName = this.product.name,
        productImageUrl = this.product.imageUrl,
        currentHighestBid = highestBid,
        endTime = this.endTime,
        status = this.status,
    )
}

data class AuctionListResponse(
    val auctions: List<AuctionSimpleResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val hasNext: Boolean,
)

fun Page<Auction>.toListResponse(): AuctionListResponse {
    return AuctionListResponse(
        auctions = this.content.map { it.toSimpleResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        currentPage = this.number,
        hasNext = this.hasNext(),
    )
}

data class PlaceBidResponse(
    val bidId: Long,
)

data class BidDetailResponse(
    val bidId: Long,
    val amount: Long,
    val bidderName: String,
    val bidTime: LocalDateTime,
)

fun Bid.toDetailResponse(): BidDetailResponse {
    return BidDetailResponse(
        bidId = this.id!!,
        amount = this.amount.amount,
        bidderName = this.user.name,
        bidTime = this.createdAt,
    )
}

data class BidListResponse(
    val bids: List<BidDetailResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val hasNext: Boolean,
)

fun Page<Bid>.toListResponse(): BidListResponse {
    return BidListResponse(
        bids = this.content.map { it.toDetailResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        currentPage = this.number,
        hasNext = this.hasNext(),
    )
}

data class StrategyResponse(
    val strategy: String,
)
