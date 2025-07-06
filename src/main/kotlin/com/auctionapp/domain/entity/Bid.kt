package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidBidException
import com.auctionapp.domain.vo.Money
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Bid(
    @Embedded
    val amount: Money,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    val auction: Auction,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun validateBid(auction: Auction) {
        val highestBid = auction.getHighestBid()

        if (auction.status != AuctionStatus.ACTIVE) {
            throw InvalidBidException("경매가 진행 중이 아닙니다. 현재 상태: ${auction.status}")
        }

        if (user.id == auction.user.id) {
            throw InvalidBidException("자신의 경매에는 입찰할 수 없습니다.")
        }

        when {
            highestBid == null -> {
                if (!amount.isGreaterThanOrEqual(auction.initialPrice)) {
                    throw InvalidBidException("입찰가(${amount.amount})가 시작가(${auction.initialPrice.amount})보다 낮습니다.")
                }
            }
            else -> {
                val minimumAmount = highestBid.amount.add(auction.minimumBidUnit)
                if (!amount.isGreaterThan(highestBid.amount)) {
                    throw InvalidBidException("입찰가(${amount.amount})가 현재 최고가(${highestBid.amount.amount})보다 낮습니다.")
                }
                if (!amount.isGreaterThanOrEqual(minimumAmount)) {
                    throw InvalidBidException("입찰가(${amount.amount})가 최소 입찰 단위(${auction.minimumBidUnit.amount})를 충족하지 않습니다. 최소 입찰액: ${minimumAmount.amount}")
                }
            }
        }
    }

    fun isHigherThan(other: Bid): Boolean {
        return this.amount.isGreaterThan(other.amount)
    }

    fun isPlacedAfter(other: Bid): Boolean {
        return this.createdAt.isAfter(other.createdAt)
    }

    companion object {
        /*
            입찰 생성 팩토리 메서드
            isValidBid() 검증을 생성자에 넣으면 Auction 객체에 의존하게 되어 순환 참조 문제가 발생할 수 있습니다.
         */
        fun create(
            amount: Money,
            user: User,
            auction: Auction,
        ): Bid {
            val bid = Bid(amount, LocalDateTime.now(), user, auction)

            try {
                bid.validateBid(auction)
            } catch (e: InvalidBidException) {
                throw e // 이미 메시지가 포함된 예외를 그대로 전달
            }

            return bid
        }

        fun fixture(
            amount: Money = Money(1000L),
            createdAt: LocalDateTime,
            user: User,
            auction: Auction,
            id: Long? = null,
        ): Bid {
            return Bid(
                amount = amount,
                createdAt = createdAt,
                user = user,
                auction = auction,
                id = id,
            )
        }
    }
}
