package com.auctionapp.com.auctionapp.application.service

import com.auctionapp.com.auctionapp.infrastructure.persistence.AuctionRepository
import com.auctionapp.com.auctionapp.infrastructure.persistence.BidRepository
import com.auctionapp.com.auctionapp.infrastructure.persistence.ProductRepository
import com.auctionapp.com.auctionapp.infrastructure.persistence.UserRepository
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.service.AuctionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuctionAppService(
    private val auctionService: AuctionService,
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun registerAuction(
        userId: Long,
        productId: Long,
        initialPrice: Long,
        minimumBidUnit: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ) {
    }

    @Transactional(readOnly = true)
    fun getAuctionList(): List<Auction> {
        return mutableListOf()
    }

    @Transactional(readOnly = true)
    fun getAuctionDetail(id: Long) {
    }

    @Transactional
    fun cancelAuction(
        userId: Long,
        id: Long,
    ) {
    }

    @Transactional
    fun placeBid(
        userId: Long,
        id: Long,
        amount: Long,
    ): Long {
        return 1L
    }

    @Transactional(readOnly = true)
    fun getBidListOfAuction(id: Long): List<Bid> {
        return mutableListOf()
    }

    @Transactional(readOnly = true)
    fun getBidListOfUser(id: Long): List<Bid> {
        return mutableListOf()
    }
}
