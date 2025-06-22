package com.auctionapp.domain.vo

import com.auctionapp.com.auctionapp.domain.exception.InvalidAmountException
import jakarta.persistence.Embeddable

@Embeddable
data class Money(val amount: Long) : Comparable<Money> {
    init {
        if (amount < 0) {
            throw InvalidAmountException()
        }
    }

    fun add(other: Money): Money = Money(this.amount + other.amount)

    fun subtract(other: Money): Money = Money(this.amount - other.amount)

    fun isGreaterThan(other: Money): Boolean = this.amount > other.amount

    fun isGreaterThanOrEqual(other: Money): Boolean = this.amount >= other.amount

    override fun compareTo(other: Money): Int {
        return if (this.amount > other.amount) {
            1
        } else if (this.amount < other.amount) {
            -1
        } else {
            0
        }
    }
}
