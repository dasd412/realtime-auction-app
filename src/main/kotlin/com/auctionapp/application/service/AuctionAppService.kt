package com.auctionapp.application.service

import com.auctionapp.application.constant.DEFAULT_AUCTION_PAGE_SIZE
import com.auctionapp.application.exception.NotFoundAuctionException
import com.auctionapp.application.exception.NotFoundProductException
import com.auctionapp.application.exception.NotFoundUserException
import com.auctionapp.com.auctionapp.expriment.concurrency.ConcurrencyControlStrategyRegistry
import com.auctionapp.domain.entity.Auction
import com.auctionapp.domain.entity.AuctionStatus
import com.auctionapp.domain.entity.Bid
import com.auctionapp.domain.service.AuctionService
import com.auctionapp.domain.vo.Money
import com.auctionapp.infrastructure.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

enum class AuctionSortType {
    NONE,
    TIME_ASC, // 시간순
    POPULARITY, // 인기순(입찰 수)
}

@Service
class AuctionAppService(
    private val auctionService: AuctionService,
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val strategyRegistry: ConcurrencyControlStrategyRegistry,
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
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()
        val product = productRepository.findByIdOrNull(productId) ?: throw NotFoundProductException()
        val auction =
            Auction(
                initialPrice = Money(initialPrice),
                minimumBidUnit = Money(minimumBidUnit),
                user = user,
                product = product,
                startTime = startTime,
                endTime = endTime,
            )

        auctionService.registerAuction(auction, user, product)
        auctionRepository.save(auction)
    }

    @Transactional(readOnly = true)
    fun getAuctionList(
        status: AuctionStatus,
        sortType: AuctionSortType,
        pageNumber: Int,
    ): Page<Auction> {
        return when (sortType) {
            AuctionSortType.NONE ->
                auctionRepository.findByStatus(
                    status,
                    PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
                )

            AuctionSortType.TIME_ASC ->
                auctionRepository.findByStatusOrderByStartTimeAsc(
                    status,
                    PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
                )

            AuctionSortType.POPULARITY ->
                auctionRepository.findByStatusOrderByBidsCountDesc(
                    status,
                    PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
                )
        }
    }

    @Transactional(readOnly = true)
    fun getAuctionDetail(auctionId: Long): AuctionDetail {
        return auctionRepository.findAuctionDetailById(auctionId) ?: throw NotFoundAuctionException()
    }

    @Transactional
    fun cancelAuction(
        userId: Long,
        auctionId: Long,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()
        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
        auctionService.cancelAuction(auction, user)
    }

    @Transactional
    fun placeBid(
        userId: Long,
        auctionId: Long,
        amount: Long,
    ): Long {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()
        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
        val money = Money(amount)

        val strategy = strategyRegistry.getCurrentStrategy()
        val bid = strategy.placeBid(auction, user, money)

        val savedBid = bidRepository.save(bid)

        return savedBid.id!!
    }

    @Transactional(readOnly = true)
    fun getBidsOfAuction(
        auctionId: Long,
        pageNumber: Int,
    ): Page<Bid> {
        val auction = auctionRepository.findByIdOrNull(auctionId) ?: throw NotFoundAuctionException()
        return bidRepository.findByAuctionOrderByCreatedAtDesc(
            auction,
            PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE),
        )
    }

    @Transactional(readOnly = true)
    fun getBidsOfUser(
        userId: Long,
        pageNumber: Int,
    ): Page<Bid> {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()
        return bidRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
    }

    @Transactional(readOnly = true)
    fun getAuctionsOfAuctionOwner(
        userId: Long,
        pageNumber: Int,
    ): Page<Auction> {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()
        return auctionRepository.findByUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
    }

    @Transactional(readOnly = true)
    fun getAuctionsOfBidder(
        userId: Long,
        pageNumber: Int,
    ): Page<Auction> {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundUserException()
        return auctionRepository.findByBidUser(user, PageRequest.of(pageNumber, DEFAULT_AUCTION_PAGE_SIZE))
    }
}
