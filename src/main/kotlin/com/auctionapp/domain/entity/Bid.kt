package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidBidAmountException
import com.auctionapp.domain.exception.InvalidBidException
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Bid(
    val amount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @ManyToOne
    val user: User,
    @ManyToOne
    val auction: Auction,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        if (amount <= 0) {
            throw InvalidBidAmountException()
        }
    }

    fun isValidBid(auction: Auction): Boolean {
        val highestBid = auction.getHighestBid()

        return when {
            auction.status != AuctionStatus.ACTIVE -> false //진행중이 아닌 거에는 입찰 불가
            user.id == auction.user.id -> false // 자신의 경매에는 입찰 불가
            highestBid == null -> amount >= auction.initialPrice
            else -> amount > highestBid.amount + auction.minimumBidUnit
        }
    }

    fun isHigherThan(other: Bid): Boolean {
        return this.amount > other.amount
    }

    fun isPlacedAfter(dateTime: LocalDateTime): Boolean {
        return this.createdAt.isAfter(dateTime)
    }

    companion object {
        /*
            입찰 생성 팩토리 메서드
            isValidBid() 검증을 생성자에 넣으면 Auction 객체에 의존하게 되어 순환 참조 문제가 발생할 수 있습니다.
         */
        fun create(amount: Long, user: User, auction: Auction): Bid {
            val bid = Bid(amount, LocalDateTime.now(), user, auction)

            if (!bid.isValidBid(auction)) {
                throw InvalidBidException()
            }

            return bid
        }

        fun fixture(
            amount: Long = 1000L,
            createdAt: LocalDateTime,
            user: User,
            auction: Auction,
        ): Bid {
            return Bid(
                amount = amount,
                createdAt = createdAt,
                user = user,
                auction = auction,
            )
        }
    }
}