package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidBidException
import com.auctionapp.domain.vo.Money
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Bid(
    @Embedded
    val amount: Money,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @ManyToOne
    val user: User,
    @ManyToOne
    val auction: Auction,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun isValidBid(auction: Auction): Boolean {
        val highestBid = auction.getHighestBid()

        if (auction.status != AuctionStatus.ACTIVE) {//진행중이 아닌 거에는 입찰 불가
            return false
        }

        if (user.id == auction.user.id) {// 자신의 경매에는 입찰 불가
            return false
        }

        return when {
            highestBid == null -> amount.isGreaterThanOrEqual(auction.initialPrice)
            else -> amount.isGreaterThan(highestBid.amount.add(auction.minimumBidUnit))
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
        fun create(amount: Money, user: User, auction: Auction): Bid {
            val bid = Bid(amount, LocalDateTime.now(), user, auction)

            if (!bid.isValidBid(auction)) {
                throw InvalidBidException()
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