package com.auctionapp.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Bid(
    val amount: Long,
    val createdAt: LocalDateTime,
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
            throw IllegalArgumentException("입찰 금액은 음수가 될 수 없습니다")
        }
    }

    companion object {
        fun fixture(
            amount: Long = 1000L,
            createdAt: LocalDateTime = LocalDateTime.now(),
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