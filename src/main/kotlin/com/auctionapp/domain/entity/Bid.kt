package com.auctionapp.domain.entity

import com.auctionapp.domain.exception.InvalidBidAmountException
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

    companion object {
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